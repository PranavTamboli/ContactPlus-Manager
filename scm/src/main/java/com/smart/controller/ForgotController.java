package com.smart.controller;

import java.util.Random;

import javax.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
public class ForgotController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
//	Email Id Form Open Handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
		System.out.println("EMAIL: " +email);
//		Generating otp of 4 digit
		
		Random random = new Random();
		int otp = random.nextInt(999999);
		System.out.println("OTP: " +otp);
		
//		Write code for send otp to email....
		
		String subject ="OTP from SCM";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+ otp
				+ "</n>"
				+ "</h1>"
				+ "</div>";
		
		String to =email;		
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("MyOTP", otp);
			session.setAttribute("email", email);
			
			return "verify_otp";
			
		}
		else {
			session.setAttribute("message", "Check your email id!!");
			return "forgot_email_form";
		}
	}
	
//	Verify--otp
	@PostMapping("/verify_otp")
	public String VerifyOtp(@RequestParam("otp") int otp, HttpSession session) {
//		Purana wala otp (session is used to store data and data yad rakahane ke liye)
		int myOtp = (int) session.getAttribute("MyOTP");
		String email = (String) session.getAttribute("email");
		
		if(myOtp==otp) {
//			Password change form
			User user = this.userRepository.getUserByUserName(email);
			if(user==null) {
//				send error message
				session.setAttribute("message", "User doesn't exist with this email!!");
				return "forgot_email_form";
			
			}else {
//				Send change password form
				
			}
			
			return "password-change-form";
		}
		else {
			session.setAttribute("message", "You have entered wrong otp!!");
			return "verify_otp";
					
		}
	}
			//	Change Password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
//		Session se email fetch kar lo(Session email id hogi)
	    String email = (String) session.getAttribute("email");
	    User user = this.userRepository.getUserByUserName(email);
	    user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
	    this.userRepository.save(user);
 		return "redirect:/sign?change=password changed Successfully..";
	}
}
