package com.zerobase.fastlms.member.repository;

import com.zerobase.fastlms.member.entity.LoginInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginInfoRepository extends JpaRepository<LoginInfoEntity, Long> {
        List<LoginInfoEntity> findLoginInfoByLoginId(String loginId);
}//end of class
