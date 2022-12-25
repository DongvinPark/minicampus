package com.zerobase.fastlms.member.service.impl;

import com.zerobase.fastlms.admin.dto.MemberDto;
import com.zerobase.fastlms.admin.mapper.MemberMapper;
import com.zerobase.fastlms.admin.model.MemberParam;
import com.zerobase.fastlms.components.MailComponents;
import com.zerobase.fastlms.course.model.ServiceResult;
import com.zerobase.fastlms.member.entity.LoginInfoEntity;
import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.entity.MemberCode;
import com.zerobase.fastlms.member.exception.MemberNotEmailAuthException;
import com.zerobase.fastlms.member.exception.MemberStopUserException;
import com.zerobase.fastlms.member.model.MemberInput;
import com.zerobase.fastlms.member.model.ResetPasswordInput;
import com.zerobase.fastlms.member.repository.LoginInfoRepository;
import com.zerobase.fastlms.member.repository.MemberRepository;
import com.zerobase.fastlms.member.service.MemberService;
import com.zerobase.fastlms.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    private final LoginInfoRepository loginInfoRepository;
    private final MailComponents mailComponents;
    private final MemberMapper memberMapper;


    private String userAgentInfo = "";
    private String userIpAddr = "";
    
    /**
     * 회원 가입
     */
    @Override
    public boolean register(MemberInput parameter) {
    
        Optional<Member> optionalMember = memberRepository.findById(parameter.getUserId());
        if (optionalMember.isPresent()) {
            //현재 userId에 해당하는 데이터 존재
            return false;
        }

        String encPassword = BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt());
        String uuid = UUID.randomUUID().toString();
        
        Member member = Member.builder()
                .userId(parameter.getUserId())
                .userName(parameter.getUserName())
                .phone(parameter.getPhone())
                .password(encPassword)
                .regDt(LocalDateTime.now())
                .emailAuthYn(false)
                .emailAuthKey(uuid)
                .userStatus(Member.MEMBER_STATUS_REQ)
                .build();
        memberRepository.save(member);
        
        String email = parameter.getUserId();
        String subject = "fastlms 사이트 가입을 축하드립니다. ";
        String text = "<p>fastlms 사이트 가입을 축하드립니다.<p><p>아래 링크를 클릭하셔서 가입을 완료 하세요.</p>"
                + "<div><a target='_blank' href='http://localhost:8080/member/email-auth?id=" + uuid + "'> 가입 완료 </a></div>";
        mailComponents.sendMail(email, subject, text);
        
        return true;
    }//func




    
    @Override
    public boolean emailAuth(String uuid) {
        
        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(uuid);
        if (!optionalMember.isPresent()) {
            return false;
        }
        
        Member member = optionalMember.get();
        
        if (member.isEmailAuthYn()) {
            return false;
        }
        
        member.setUserStatus(Member.MEMBER_STATUS_ING);
        member.setEmailAuthYn(true);
        member.setEmailAuthDt(LocalDateTime.now());
        memberRepository.save(member);
        
        return true;
    }//func




    
    @Override
    public boolean sendResetPassword(ResetPasswordInput parameter) {
    
        Optional<Member> optionalMember = memberRepository.findByUserIdAndUserName(parameter.getUserId(), parameter.getUserName());
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }
        
        Member member = optionalMember.get();
        
        String uuid = UUID.randomUUID().toString();
        
        member.setResetPasswordKey(uuid);
        member.setResetPasswordLimitDt(LocalDateTime.now().plusDays(1));
        memberRepository.save(member);
        
        String email = parameter.getUserId();
        String subject = "[fastlms] 비밀번호 초기화 메일 입니다. ";
        String text = "<p>fastlms 비밀번호 초기화 메일 입니다.<p>" +
                "<p>아래 링크를 클릭하셔서 비밀번호를 초기화 해주세요.</p>"+
                "<div><a target='_blank' href='http://localhost:8080/member/reset/password?id=" + uuid + "'> 비밀번호 초기화 링크 </a></div>";
        mailComponents.sendMail(email, subject, text);
    
        return false;
    }//func




    
    @Override
    public List<MemberDto> list(MemberParam parameter) {
        
        long totalCount = memberMapper.selectListCount(parameter);
        
        List<MemberDto> list = memberMapper.selectList(parameter);
        if (!CollectionUtils.isEmpty(list)) {
            int i = 0;
            for(MemberDto x : list) {
                x.setTotalCount(totalCount);
                x.setSeq(totalCount - parameter.getPageStart() - i);
                i++;
            }
        }
        return list;
        //return memberRepository.findAll();
    }//func




    
    @Override
    public MemberDto detail(String userId) {
        
        Optional<Member> optionalMember  = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return null;
        }
        
        Member member = optionalMember.get();
        
        return MemberDto.of(member);
    }//func




    
    @Override
    public boolean updateStatus(String userId, String userStatus) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        member.setUserStatus(userStatus);
        memberRepository.save(member);
        
        return true;
    }//func




    
    @Override
    public boolean updatePassword(String userId, String password) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        String encPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        member.setPassword(encPassword);
        memberRepository.save(member);
    
        return true;
        
    }//func




    
    @Override
    public ServiceResult updateMember(MemberInput parameter) {
        
        String userId = parameter.getUserId();
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        member.setPhone(parameter.getPhone());
        member.setZipcode(parameter.getZipcode());
        member.setAddr(parameter.getAddr());
        member.setAddrDetail(parameter.getAddrDetail());
        member.setUdtDt(LocalDateTime.now());
        memberRepository.save(member);
        
        return new ServiceResult();
    }//func




    
    @Override
    public ServiceResult updateMemberPassword(MemberInput parameter) {
    
        String userId = parameter.getUserId();
        
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        if (!PasswordUtils.equals(parameter.getPassword(), member.getPassword())) {
            return new ServiceResult(false, "비밀번호가 일치하지 않습니다.");
        }
        
        String encPassword = PasswordUtils.encPassword(parameter.getNewPassword());
        member.setPassword(encPassword);
        memberRepository.save(member);
        
        return new ServiceResult(true);
    }//func




    
    @Override
    public ServiceResult withdraw(String userId, String password) {
    
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (!optionalMember.isPresent()) {
            return new ServiceResult(false, "회원 정보가 존재하지 않습니다.");
        }
    
        Member member = optionalMember.get();
        
        if (!PasswordUtils.equals(password, member.getPassword())) {
            return new ServiceResult(false, "비밀번호가 일치하지 않습니다.");
        }
    
        member.setUserName("삭제회원");
        member.setPhone("");
        member.setPassword("");
        member.setRegDt(null);
        member.setUdtDt(null);
        member.setEmailAuthYn(false);
        member.setEmailAuthDt(null);
        member.setEmailAuthKey("");
        member.setResetPasswordKey("");
        member.setResetPasswordLimitDt(null);
        member.setUserStatus(MemberCode.MEMBER_STATUS_WITHDRAW);
        member.setZipcode("");
        member.setAddr("");
        member.setAddrDetail("");
        memberRepository.save(member);
        
        return new ServiceResult();
    }//func





    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> optionalMember = memberRepository.findById(username);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Member member = optionalMember.get();
        
        if (Member.MEMBER_STATUS_REQ.equals(member.getUserStatus())) {
            throw new MemberNotEmailAuthException("이메일 활성화 이후에 로그인을 해주세요.");
        }
        
        if (Member.MEMBER_STATUS_STOP.equals(member.getUserStatus())) {
            throw new MemberStopUserException("정지된 회원 입니다.");
        }
    
        if (Member.MEMBER_STATUS_WITHDRAW.equals(member.getUserStatus())) {
            throw new MemberStopUserException("탈퇴된 회원 입니다.");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        if (member.isAdminYn()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        /*
        * 매 로그인 호출 시 이 메서드를 호출한다. 여기까지 전부 통과했다면, 로그인이 허용된다는 뜻이다.
        * 로그인 시도 때마다 loadUserByUsername() 메서드를 호출하는데, 이 메서드에 유저 접속 관련 정보를 담은
        * 다른 파라미터를 전달할 경우, 오버라이드가 성립되지 않게 되면서 로그인 로직이 손상될 우려가 있다.
        * 따라서, 컨트롤러 단에서 login()메서드가 호출될 때마다 memberServiceImpl의 userAgentInfo,
        * userIpAddr 를 초기화 해주고, 로그인이 허용될 때마다
        * 컨트롤러단에서 넘겨 받은 정보를 활용하여 유저 접속 정보를 DB에 저장하는 방식으로 진행하기로 했다.
        * */

        LoginInfoEntity loginInfoEntity = LoginInfoEntity.builder()
                .loginId(username)
                .loginDate(LocalDateTime.now())
                .userIpAddr(userIpAddr)
                .userAgentInfo(userAgentInfo)
                .build();

        loginInfoRepository.save(loginInfoEntity);
        //log.info("로그인 일시 저장 완료!!");

        return new User(member.getUserId(), member.getPassword(), grantedAuthorities);
    }//func





    //--------------- PRIVATE HELPER METHODS ----------------
    @Override
    public String getUserAgentInfo() {
        return userAgentInfo;
    }

    @Override
    public void setUserAgentInfo(String userAgentInfo) {
        this.userAgentInfo = userAgentInfo;
    }

    @Override
    public String getUserIpAddr() {
        return userIpAddr;
    }

    @Override
    public void setUserIpAddr(String userIpAddr) {
        this.userIpAddr = userIpAddr;
    }
}//End of class















