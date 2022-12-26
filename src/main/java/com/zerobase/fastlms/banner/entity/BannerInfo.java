package com.zerobase.fastlms.banner.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class BannerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bannerName;

    private String linkPathOnClick;
    private String browserOpenMethod;//(새 탭 열기 OR 새 창 열기)
    private int priorityNumber;
    boolean isPublished;

    private LocalDateTime createdDate;
    private LocalDateTime editDate;

    private String fileName;
    private String urlFileName;

}//End of class
