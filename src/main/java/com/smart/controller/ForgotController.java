package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	Random random= new Random(1000);
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	@RequestMapping("/forgot")
 public String openEmailForm() {
	 return "normal/forgot_email_form";
	 }
	
	
	@PostMapping( "/send-otp")
 public String sendOTP(@RequestParam ("email") String email,HttpSession session) {
		System.out.println("EMAIL"+email);
		//generating 4 digits email
		
	int otp=	random.nextInt(9999);
	System.out.println("otp"+otp);
	
	//send email service
	String subject="OTP from Smart Contact Manager";
	String message=""
			+ "<div style='border:1px solid #2e2e2e; padding:20px'>"
			+ "<h1>"
			+ "OTP is : "
			+ "<b>"+otp
			+ "</b>"
			+ "</h1>"
			+ "</div>";
	String to=email;
	
     boolean flag=	this.emailService.sendEmail(subject, message, to);
  
     if(flag) {
    	 session.setAttribute("myotp", otp);
    	 session.setAttribute("email", email);
    	 return "normal/verify_otp";
     }else { 
    	 session.setAttribute("message" ,new Message("Check your email !!" , "danger"));
	 return "normal/forgot_email_form";
	 }
	}
	
	
	  //verify-otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam ("otp")int otp,HttpSession session) {
     int myotp = (int)session.getAttribute("myotp");
     	String email=(String)session.getAttribute("email");
	    if(myotp==otp) {
		//password change form
		User user=  this.userRepository.getUserbyUserName(email);
		  if(user==null) {
			  //send error message
			  session.setAttribute("message", new Message("No user exist with this email !!","danger"));
			  return "forgot_email_form";
		  }else {
			  //send change password form
		  }
		
		return "normal/password_change_form";
	         }
	else {
		session.setAttribute("message", new Message("You have entered wrong otp !!","danger"));
		 return "normal/verify_otp";
           	}
		
	}
	  
	//change password
	@PostMapping("/change-password")
	public String changePassowrd(@RequestParam("newpassword")String newpassword,HttpSession session) {
		String email =(String) session.getAttribute("email");
	User user=	this.userRepository.getUserbyUserName(email);
	user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/login?change=password changed successfully..";
		
	}
}
