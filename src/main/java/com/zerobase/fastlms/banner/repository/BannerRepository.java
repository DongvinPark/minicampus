package com.zerobase.fastlms.banner.repository;


import com.zerobase.fastlms.banner.entity.BannerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerInfo, Long> {
    
}
