package com.zerobase.fastlms.member.controller;


import com.zerobase.fastlms.admin.dto.MemberDto;
import com.zerobase.fastlms.course.dto.TakeCourseDto;
import com.zerobase.fastlms.course.model.ServiceResult;
import com.zerobase.fastlms.course.service.TakeCourseService;
import com.zerobase.fastlms.member.model.MemberInput;
import com.zerobase.fastlms.member.model.ResetPasswordInput;
import com.zerobase.fastlms.member.service.MemberService;
import com.zerobase.fastlms.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@Slf4j
public class MemberController {
    
    private final MemberService memberService;
    private final TakeCourseService takeCourseService;
    
    @RequestMapping("/member/login")
    public String login(HttpServletRequest request) {

        /*
         * IP 정보에 대한 기본 설정은 IPV6 형식을 따르고 있으므로, IPV4 형식을 따르도록 인텔리제이의 관련 설정을 바꿔야한다.
         * 또한 로드 밸런서, 프록시, 방화벽 등을 거치면 아이피 주소가 해당 다른 서버의 IP로 나오므로 이를 필터링하여
         * 접속 Client의 원래 IP를 획득할 수 있도록 해야 한다.
         * 단, X-Forwarded-For 옵션은 사용자의 위치정보와 관련 있는 민감한 개인정보를 노출시킬 수 있으므로 실 서비스에서는
         * 이를 주의하여 사용해야 한다.
         * */
        String userAgentInfo = request.getHeader("User-Agent");
        String ip = ipAddrFiltering(request.getHeader("X-Forwarded-For"), request);

        memberService.setUserAgentInfo(userAgentInfo);
        memberService.setUserIpAddr(ip);

        return "member/login";
    }



    
    @GetMapping("/member/find-password")
    public String findPassword() {
        
        return "member/find_password";
    }



    
    @PostMapping("/member/find-password")
    public String findPasswordSubmit(Model model, ResetPasswordInput parameter) {
        
        boolean result = memberService.sendResetPassword(parameter);
        model.addAttribute("result", result);
        
        return "member/find_password_result";
    }


    
    @GetMapping("/member/register")
    public String register() {
        
        return "member/register";
    }


    
    @PostMapping("/member/register")
    public String registerSubmit(Model model, HttpServletRequest request
            , MemberInput parameter) {
        
        boolean result = memberService.register(parameter);
        model.addAttribute("result", result);
        
        return "member/register_complete";
    }


    
    // http://www.naver.com/news/list.do?id=123&key=124&text=쿼리
    // https://
    // 프로토콜://도메인(IP)/news/list.do?쿼리스트링(파라미터)
    // https://www.naver.com/cafe/detail.do?id=1111
    // https://www.naver.com/cafe/detail.do?id=2222


    
    @GetMapping("/member/email-auth")
    public String emailAuth(Model model, HttpServletRequest request) {
        
        String uuid = request.getParameter("id");
        System.out.println(uuid);
        
        boolean result = memberService.emailAuth(uuid);
        model.addAttribute("result", result);
        
        return "member/email_auth";
    }



    
    @GetMapping("/member/info")
    public String memberInfo(Model model, Principal principal) {
        
        String userId = principal.getName();
        MemberDto detail = memberService.detail(userId);
        
        model.addAttribute("detail", detail);
        
        return "member/info";
    }



    
    @PostMapping("/member/info")
    public String memberInfoSubmit(Model model
            , MemberInput parameter
            , Principal principal) {
        
        String userId = principal.getName();
        parameter.setUserId(userId);
        
        ServiceResult result = memberService.updateMember(parameter);
        if (!result.isResult()) {
            model.addAttribute("message", result.getMessage());
            return "common/error";
        }
        return "redirect:/member/info";
    }



    
    @GetMapping("/member/password")
    public String memberPassword(Model model, Principal principal) {
        
        String userId = principal.getName();
        MemberDto detail = memberService.detail(userId);
        
        model.addAttribute("detail", detail);
        
        return "member/password";
    }



    
    @PostMapping("/member/password")
    public String memberPasswordSubmit(Model model
            , MemberInput parameter
            , Principal principal) {
        
        String userId = principal.getName();
        parameter.setUserId(userId);
        
        ServiceResult result = memberService.updateMemberPassword(parameter);
        if (!result.isResult()) {
            model.addAttribute("message", result.getMessage());
            return "common/error";
        }
        
        return "redirect:/member/info";
    }



    
    
    @GetMapping("/member/takecourse")
    public String memberTakeCourse(Model model, Principal principal) {
        
        String userId = principal.getName();
        List<TakeCourseDto> list = takeCourseService.myCourse(userId);
        
        model.addAttribute("list", list);
        
        return "member/takecourse";
    }



    
    
    @GetMapping("/member/withdraw")
    public String memberWithdraw(Model model) {
        
        return "member/withdraw";
    }



    
    @PostMapping("/member/withdraw")
    public String memberWithdrawSubmit(Model model
            , MemberInput parameter
            , Principal principal) {
        
        String userId = principal.getName();
        
        ServiceResult result = memberService.withdraw(userId, parameter.getPassword());
        if (!result.isResult()) {
            model.addAttribute("message", result.getMessage());
            return "common/error";
        }
        
        return "redirect:/member/logout";
    }






    //------------------- PRIVATE HELPER METHOD AREA -------------

    private String ipAddrFiltering(String ip, HttpServletRequest request){
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
            //log.info(">>>> Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
            //log.info(">>>> WL-Proxy-Client-IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            //log.info(">>>> HTTP_CLIENT_IP : " + ip);
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            //log.info(">>>> HTTP_X_FORWARDED_FOR : " + ip);
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }//func


}//End of class
