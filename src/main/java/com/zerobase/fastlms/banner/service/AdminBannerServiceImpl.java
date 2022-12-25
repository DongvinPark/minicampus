package com.zerobase.fastlms.banner.service;


import com.zerobase.fastlms.banner.dto.BannerDto;
import com.zerobase.fastlms.banner.entity.BannerInfo;
import com.zerobase.fastlms.banner.mapper.BannerMapper;
import com.zerobase.fastlms.banner.model.BannerInput;
import com.zerobase.fastlms.banner.model.BannerParam;
import com.zerobase.fastlms.banner.repository.BannerRepository;
import com.zerobase.fastlms.course.dto.CourseDto;
import com.zerobase.fastlms.course.model.CourseInput;
import com.zerobase.fastlms.course.model.CourseParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminBannerServiceImpl implements AdminBannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;


    @Override
    public boolean add(BannerInput parameter) {

        bannerRepository.save(
                BannerInfo.builder()
                        .bannerName(parameter.getBannerName())
                        .linkPathOnClick(parameter.getLinkPathOnClick())
                        .browserOpenMethod(parameter.getBrowserOpenMethod())
                        .priorityNumber(parameter.getPriorityNumber())
                        .isPublished(parameter.isPublished())
                        .createdDate(LocalDateTime.now())
                        .fileName(parameter.getFileName())
                        .urlFileName(parameter.getUrlFileName())
                        .build()
        );

        return true;
    }//func





    @Override
    public boolean set(BannerInput parameter) {
        return false;
    }//func





    @Override
    public List<BannerDto> list(BannerParam parameter) {

        List<BannerInfo> bannerEntityList = bannerMapper.selectList(parameter);

        List<BannerInfo> bannerInfoList = bannerRepository.findAll();
        long totalCount = bannerInfoList.size();

        System.out.println("배너 서비스 내 토탈카운트" + totalCount);

        List<BannerDto> bannerDtoList = new ArrayList<>();
        for(BannerInfo bannerEntity : bannerEntityList){
            bannerDtoList.add(
                    BannerDto.builder()
                            .id(bannerEntity.getId())
                            .bannerName(bannerEntity.getBannerName())
                            .linkPathOnClick(bannerEntity.getLinkPathOnClick())
                            //urlFileName을 타임리프에 넘겨줘야 한다.
                            .priorityNumber(bannerEntity.getPriorityNumber())
                            .isPublished(bannerEntity.isPublished())
                            .urlFileName(bannerEntity.getUrlFileName())
                            .createdDate(bannerEntity.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .build()
            );
        }

        /*System.out.println("배너디티오 리스트 내 모든 이미지 소스 파일 출력");
        for(BannerDto bannerDto : bannerDtoList){
            System.out.println("유알엘 파일패스 : " + bannerDto.getUrlFileName());
        }*/

        if (!CollectionUtils.isEmpty(bannerDtoList)) {
            int i=0;
            for (BannerDto x : bannerDtoList) {
                x.setTotalCount(bannerDtoList.size());
                x.setSeq(totalCount - parameter.getPageStart() - i);
                i++;
            }
        }

        return bannerDtoList;
    }





    @Override
    public BannerDto getById(int id) {
        return null;
    }//func





    @Override
    public boolean del(String idList) {
        return false;
    }//func





    @Override
    public List<BannerDto> frontList(BannerParam parameter) {
        return null;
    }//func





    @Override
    public BannerDto bannerDetailClickedByUser(long id) {
        return null;
    }//func

}//end of class
