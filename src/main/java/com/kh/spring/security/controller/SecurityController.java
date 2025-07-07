package com.kh.spring.security.controller;

//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.vo.Member;
import com.kh.spring.security.model.validator.MemberValidator;
import com.kh.spring.security.model.vo.MemberExt;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SecurityController {
	
	//의존성 주입방법 : field or 생성자
	private BCryptPasswordEncoder passwordEncoder;
	private MemberService mService;
	
	//생성자 방식의 의존성 주입(생성자가 현재 클래스에 1개라면 @Autowired 생략가능)
	public SecurityController(BCryptPasswordEncoder passwordEncoder, MemberService mService) {
		this.passwordEncoder = passwordEncoder;
		this.mService = mService;
	}
	
	// 에러페이지 포워딩용 url
	@GetMapping("/security/accessDenied")
	public String accessDenied(Model model) {
		model.addAttribute("errorMsg","접근불가!!!!!!!!!");
		return "common/errorPage";
	}
	
	//회원가입 페이지 이동
	@GetMapping("/security/insert")
	public String enroll(@ModelAttribute Member member) {
		// @ModelAttribute
		// - 커맨드 객체 바인딩시 사용
		// - model영역에 커맨드객체 저장
		return "member/memberEnrollForm";
	}
	
	/*
	 * InitBinder
	 *  - 현재 컨트롤러에서 Binding 작업을 수행할때 실행되는 객체
	 *  - @MeodelAttribute 에 대한 바인딩 설정을 수행
	 *  
	 *  처리순서
	 *   1) 클라이언트의 요청 파라미터를 커맨드 객체 필드에 바인딩
	 *   2) 바인딩 과정에서 WebdataBinder가 필요한 경우 타입변환이나, 유효성 검사를 수행
	 *   3) 유효성 검사 결과 BindingResult에 저장
	 * */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(new MemberValidator());
		
		// 타입변환
		// 문자열형태의 날짜값을 Date로 변환
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");	//생년월일
		dateFormat.setLenient(false);	//yyMMdd형식이 아닌경우 에러를 발생
		
		 binder.registerCustomEditor(Date.class, "birthday", new CustomDateEditor(dateFormat, true));
		//birthday = 991225와 같은 형식으로 데이터가 들어오는 경우 수행되는 커스텀에디터 등록
	}
	
	@PostMapping("/security/insert")
	public String register(@Validated @ModelAttribute Member member, 
							BindingResult bindingResult, 
							//BindingResult
							// - 유효성 검사 결과를 저장하는 객체
							// - forward시 자동으로 jsp에게 전달되며, form태그 내부에 에러내용을 바인딩 할 때 사용
							RedirectAttributes ra) {
		// 유효성 검사
		if(bindingResult.hasErrors()) {
			return "member/memberEnrollForm";
		}
		// 유효성 검사 통과시 비밀번호 정보는 암호화하여, 회원가입 진행
		String encryptedPassword = passwordEncoder.encode(member.getUserPwd());
		member.setUserPwd(encryptedPassword);
		
		mService.insertMember(member);
		
		// 회원가입 완료 후 로그인페이지로 리다이렉트
		return "redirect:/member/login";
	}
	
	/*
	 * Authentication
	 *  - Principal : 인증에 사용된 사용자 객체
	 *  - Credentials : 인증에 필요한 비밀번호에 대한 정보를 가진 객체(내부적으로 인증작업시 사용)
	 *  - Authorities : 인증된 사용자가 가진 권한을 저장하는 객체
	 * 
	 * */
	@GetMapping("/security/myPage")
	public String myPage(Authentication auth2, Principal principal2,Model model) {
		//인증된 사용자 정보 가져오기
		//1. SecurityContextHodler이용
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		MemberExt principal = (MemberExt)auth.getPrincipal();
		model.addAttribute("logUser",principal);
		
		// 2. ArgumentResolver를 이용한 자동 바인딩
		log.info("auth = {}", auth);
		log.info("principal = {}", principal);
		log.info("auth2 = {}", auth2);
		log.info("principal2 = {}", principal2);
		
		return"member/myPage";
	}
}
