package com.zerobase.fastlms.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginHistoryDto {

    Long number;
    String loginDate;
    String accessIp;
    String userAgentInfo;

}//end of class
