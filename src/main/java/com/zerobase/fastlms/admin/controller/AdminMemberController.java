package com.zerobase.fastlms.admin.controller;


import com.zerobase.fastlms.admin.dto.LoginHistoryDto;
import com.zerobase.fastlms.admin.dto.MemberDto;
import com.zerobase.fastlms.admin.model.MemberParam;
import com.zerobase.fastlms.admin.model.MemberInput;
import com.zerobase.fastlms.course.controller.BaseController;
import com.zerobase.fastlms.member.repository.LoginInfoRepository;
import com.zerobase.fastlms.member.service.MemberService;
import com.zerobase.fastlms.member.entity.LoginInfoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class AdminMemberController extends BaseController {
    private final MemberService memberService;
    private final LoginInfoRepository loginInfoRepository;
    
    @GetMapping("/admin/member/list.do")
    public String list(Model model, MemberParam parameter) {

        log.info("어드민멤버 컨트롤러 내 list()메서드 호출!");

        parameter.init();
        List<MemberDto> members = memberService.list(parameter);

        //MemberDto를 순회하면서, 각 dto별로 가장 최근에 로그인한 일자를 채워 넣어준다.
        for(MemberDto memberDto : members){
            List<LoginInfoEntity> loginInfoEntityList = loginInfoRepository.findLoginInfoByLoginId(memberDto.getUserId());
            //log.info("loginInfoLIst 리스트 사이즈 측정! : " + loginInfoEntityList.size());
            if(loginInfoEntityList.size() > 0){
                LocalDateTime latestLoginDate = loginInfoEntityList.get(loginInfoEntityList.size()-1).getLoginDate();
                memberDto.setLatestLoginDate(
                        latestLoginDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
                log.info("마지막 로그인 날짜! : " + loginInfoEntityList.get(loginInfoEntityList.size()-1).getLoginDate());
            }
        }//for
        
        long totalCount = 0;
        if (members != null && members.size() > 0) {
            totalCount = members.get(0).getTotalCount();
        }
        String queryString = parameter.getQueryString();
        String pagerHtml = getPaperHtml(totalCount, parameter.getPageSize(), parameter.getPageIndex(), queryString);
        
        model.addAttribute("list", members);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pager", pagerHtml);
        
        return "admin/member/list";
    }//func




    
    @GetMapping("/admin/member/detail.do")
    public String detail(Model model, MemberParam parameter) {

        log.info("어드민 멤버 컨트롤러 내 detail()메서드 호출!!");

        parameter.init();
        
        MemberDto member = memberService.detail(parameter.getUserId());
        List<LoginInfoEntity> loginInfoEntityList = loginInfoRepository.findLoginInfoByLoginId(parameter.getUserId());

        //LoginInfo 엔티티를 dto 형태로 변환한다.
        LinkedList<LoginHistoryDto> historyDtoList = new LinkedList<>();
        Long i = 1L;
        for(LoginInfoEntity loginInfoEntity : loginInfoEntityList){
            //최신 로그인 목록부터 보여주기 위하여 LinkedList의 addFirst()로 더해 준다.
            historyDtoList.addFirst(
                    LoginHistoryDto.builder()
                            .number(i)
                            .loginDate(loginInfoEntity.getLoginDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                            .accessIp(loginInfoEntity.getUserIpAddr())
                            .userAgentInfo(loginInfoEntity.getUserAgentInfo())
                            .build()
            );
            i++;
        }//for

        model.addAttribute("member", member);
        model.addAttribute("loginHistory", historyDtoList);

        log.info("admin/member/detail 페이지 내 어트리뷰트 삽입 완료");

        return "admin/member/detail";
    }//func




    
    @PostMapping("/admin/member/status.do")
    public String status(Model model, MemberInput parameter) {

        boolean result = memberService.updateStatus(parameter.getUserId(), parameter.getUserStatus());
        
        return "redirect:/admin/member/detail.do?userId=" + parameter.getUserId();
    }//func




    
    
    @PostMapping("/admin/member/password.do")
    public String password(Model model, MemberInput parameter) {

        boolean result = memberService.updatePassword(parameter.getUserId(), parameter.getPassword());
        
        return "redirect:/admin/member/detail.do?userId=" + parameter.getUserId();
    }//func
    
    
    


}//end of class
