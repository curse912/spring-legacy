package com.kh.spring.security.model.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.kh.spring.member.model.vo.Member;

public class MemberValidator implements Validator{

	@Override
	public boolean supports(Class<?> clazz) {
		// 유효성검사를 수행할 클래스를 지정하는 메서드
		return Member.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		//유효성 검사 매서드
		Member member = (Member) target;
		
		//유효성사 로직
		if(member.getUserId() != null) {
			if(member.getUserId().length() <4 || member.getUserId().length() > 20) {
				errors.rejectValue("userID", "length","아이디는 4~20자 이내여야 함니다.");
			}
			if(!member.getUserId().matches("^[a-zA-Z0-9_]+$")) {
				errors.rejectValue("userID", "pattern","아이디는 영문, 숫자, _만 사용가능합니다.");
			}
		}
	}
	
	
}
