package com.haegreen.fishing.security;

import com.haegreen.fishing.constant.Role;
import com.haegreen.fishing.entitiy.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

// userDetails를 상속받는다.
public class CustomUserDetails implements UserDetails {

    // 스프링 시큐리티에서는 기본적으로 로그인 한 사람의 정보를
    // (String) id, password, role로 저장한다.

    // 그래서 "Custom"으로 멤버 정보까지 담을려고 하는 것
    private final Member member;
    public CustomUserDetails(Member member) {
        this.member = member;
    }
    // Member member = memberRepository.findByEmail(email)
    // CusteomtuserDetails(member)
    // 를 하는 순간 CustomUserDetails에 member 객체에 저장됨!

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = Collections.singleton(member.getRole());
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        return authorities; // 권한
    }


    public Member getMember() {
        return member;
    } // Member 자체 getter

    @Override
    public String getPassword() { return member.getPassword();}

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}