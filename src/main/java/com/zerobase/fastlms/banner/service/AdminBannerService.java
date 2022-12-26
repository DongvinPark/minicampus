package com.zerobase.fastlms.banner.service;

import com.zerobase.fastlms.banner.dto.BannerDto;
import com.zerobase.fastlms.banner.entity.BannerInfo;
import com.zerobase.fastlms.banner.model.BannerInput;
import com.zerobase.fastlms.banner.model.BannerParam;

import java.util.List;

public interface AdminBannerService {

    /**
     * 배너 등록
     */
    boolean add(BannerInput parameter);

    /**
     * 배너 1개 정보수정
     */
    boolean set(BannerInput parameter);

    /**
     * 모든 배너 목록 - 관리자 페이지 전용
     */
    List<BannerDto> list(BannerParam parameter, List<BannerInfo> bannerInfoList);

    /**
     * 배너 상세정보
     */
    BannerDto getById(long id);

    /**
     * 강좌 내용 삭제
     */
    boolean del(String idList);

    /**
     * 퍼블리싱 허가 된 배너 목록
     */
    List<BannerDto> getPublishedBannerDto();


}//end of interface
