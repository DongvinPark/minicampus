package com.zerobase.fastlms.banner.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BannerDto {

     Long id;

     String bannerName;
     String linkPathOnClick;
     String browserOpenMethod;
     int priorityNumber;
     boolean isPublished;

     String createdDate;


     //추가컬럼
     long totalCount;
     long seq;

     String fileName;
     String urlFileName;

}//end of class
