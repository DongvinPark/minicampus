package com.zerobase.fastlms.banner.controller;


import com.zerobase.fastlms.banner.dto.BannerDto;
import com.zerobase.fastlms.banner.model.BannerInput;
import com.zerobase.fastlms.banner.model.BannerParam;
import com.zerobase.fastlms.banner.service.AdminBannerService;
import com.zerobase.fastlms.course.dto.CourseDto;
import com.zerobase.fastlms.course.model.CourseInput;
import com.zerobase.fastlms.course.model.CourseParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@Slf4j
public class AdminBannerController extends BaseController {
    private final AdminBannerService adminBannerService;


    @GetMapping("/admin/banner/list.do")
    public String list(Model model, BannerParam parameter) {
        //log.info("어드민 배너 컨트롤러 내 list()메서드 호출!!");

        parameter.init();
        List<BannerDto> bannerDtoList = adminBannerService.list(parameter);

        log.info("배너디티오 리스트 사이즈 : " + bannerDtoList.size());

        long totalCount = bannerDtoList.size();

        String queryString = parameter.getQueryString();
        String pagerHtml = getPaperHtml(totalCount, parameter.getPageSize(), parameter.getPageIndex(), queryString);

        System.out.println("배너 파트 콜!!");
        System.out.println("코스 쿼리스트링" + queryString);
        System.out.println("페이저HTML : " + pagerHtml);
        System.out.println("토탈 카운트" + totalCount);
        System.out.println("페이지 사이즈" + parameter.getPageSize());
        System.out.println("페이지 인덱스" + parameter.getPageIndex());

        model.addAttribute("list", bannerDtoList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pager", pagerHtml);

        return "admin/banner/list";
    }//func





    //이 메서드는 배너를 실제로 추가하는 메서드가 아니라,
    //배너를 추가하기 위해 추가 페이지에 들어온 유저에게 추가 페이지를 리턴해 주기 위한 것이다.
    @GetMapping("/admin/banner/add.do")
    public String add(Model model, HttpServletRequest request
            , BannerInput parameter) {
        //오픈 방법 선택 동작은 타임리프 없이 html의 select 태그만으로 수행한다.

        BannerDto detail = new BannerDto();
        model.addAttribute("detail", detail);

        log.info("어드민 배너 컨트롤러 내 add()메서드 호출!!");

        return "admin/banner/add";
    }//func






    //배너를 실제로 리포지토리에 저장하는 것은 여기에서 담당한다.
    @PostMapping("/admin/banner/add.do")
    public String addSubmit(Model model, HttpServletRequest request
            , MultipartFile file
            , BannerInput parameter) {

        String saveFilename = "";
        String urlFilename = "";

        if (file != null) {
            String originalFilename = file.getOriginalFilename();

            String baseLocalPath = "/Users/bagdongbin/Documents/fastlms/files";
            String baseUrlPath = "/files";

            String[] arrFilename = getNewSaveFile(baseLocalPath, baseUrlPath, originalFilename);

            saveFilename = arrFilename[0];
            urlFilename = arrFilename[1];

            try {
                File newFile = new File(saveFilename);
                FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(newFile));
            } catch (IOException e) {
                log.info("############################ - 1");
                log.info(e.getMessage());
            }
        }

        parameter.setFileName(saveFilename);
        parameter.setUrlFileName(urlFilename);
        parameter.setBannerName(request.getParameter("bannerName"));
        parameter.setPublished(request.getParameterValues("isPublished")==null ? false : true);
        parameter.setBrowserOpenMethod(request.getParameter("openMethodSelection"));

        /*System.out.println("parameter 배너이름 = " + parameter.getBannerName());
        System.out.println("parameter 배너 링크 주소 = " + parameter.getLinkPathOnClick());
        System.out.println("parameter 배너 공개여부 = " + parameter.isPublished());
        System.out.println("오픈 동작 선택 결과" + request.getParameter("openMethodSelection"));*/

        boolean result = adminBannerService.add(parameter);

        return "redirect:/admin/banner/list.do";
    }





    //-------------- PRIVATE HELPER METHODS ---------------


    private String[] getNewSaveFile(String baseLocalPath, String baseUrlPath, String originalFilename) {

        LocalDate now = LocalDate.now();

        String[] dirs = {
                String.format("%s/%d/", baseLocalPath,now.getYear()),
                String.format("%s/%d/%02d/", baseLocalPath, now.getYear(),now.getMonthValue()),
                String.format("%s/%d/%02d/%02d/", baseLocalPath, now.getYear(), now.getMonthValue(), now.getDayOfMonth())};

        String urlDir = String.format("%s/%d/%02d/%02d/", baseUrlPath, now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        for(String dir : dirs) {
            File file = new File(dir);
            if (!file.isDirectory()) {
                file.mkdir();
            }
        }

        String fileExtension = "";
        if (originalFilename != null) {
            int dotPos = originalFilename.lastIndexOf(".");
            if (dotPos > -1) {
                fileExtension = originalFilename.substring(dotPos + 1);
            }
        }

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String newFilename = String.format("%s%s", dirs[2], uuid);
        String newUrlFilename = String.format("%s%s", urlDir, uuid);
        if (fileExtension.length() > 0) {
            newFilename += "." + fileExtension;
            newUrlFilename += "." + fileExtension;
        }

        return new String[]{newFilename, newUrlFilename};
    }



}//End of class
