package com.zerobase.fastlms.banner.model;

import lombok.Data;

@Data
public class BannerInput {

     Long id;

     String bannerName;
     String imgPath;
     String linkPathOnClick;
     String browserOpenMethod;
     int priorityNumber;
    boolean isPublished;

     String createdDate;


    //추가컬럼
    long totalCount;
    long seq;

    //파일 처리용
     String fileName;
     String urlFileName;

}//end of class
