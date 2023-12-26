package com.smart.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Service;



@Service
public class EmailService {
	
	
	
	public boolean sendEmail(String subject,String message,String to)
	{     boolean flag=false;
		//variable for g-mail host
		String host = "smtp.gmail.com";
		String from = "anmolnayyar13@gmail.com";
		
		//get the system properties
	    Properties properties=	System.getProperties();
    	System.out.println("Properties"+properties);
	
	
	//setting important information to properties object
	properties.put("mail.smtp.host",host);
	properties.put("mail.smtp.port","465");
	properties.put("mail.smtp.ssl.enable",true);
	properties.put("mail.smtp.auth",true);
	
	//step 1: get the session  object
     Session session=	Session.getInstance(properties, new Authenticator() {
	

			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("anmolnayyar13@gmail.com","mogecgjchxrwkdfx");
			}
	});
	
	session.setDebug(true);
	//step 2: compose the message
	MimeMessage mimeMessage=new  MimeMessage(session);
	try {
		//from email
		mimeMessage.setFrom(from);
		
		//adding recipient to message
		mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
	 
		//adding subject to message
		mimeMessage.setSubject(subject);
		
		//adding text to message
		mimeMessage.setText(message);
		
		//step 3: send the message using Transport class
		Transport.send(mimeMessage);
		System.out.println("Send Successfully");
		flag=true;
	} catch (Exception e) {
	e.printStackTrace();
	}	
		return flag;
	} 
	
	
	
	
	
	private static void sendAttachment(String message, String subject, String to, String from) {
		// //variable for gmail
		String host = "smtp.gmail.com";
		
		//get the system properties 
		Properties properties = System.getProperties();
		System.out.println(properties);
		 //setting important information to properties object
		  
		 //host set 
		 properties.put("mail.smtp.host",host);
		 properties.put("mail.smtp.port",465);
		 properties.put("mail.smtp.ssl.enable","true");
		 properties.put("mail.smtp.auth","true");
		  
		 //step:1 to get the session object
		 Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("smartcontactmanager.2021@gmail.com","scm#2021");
			}
				
		 }); 
		 session.setDebug(true);
		 
		 //step:2 compose the message [text, multimedia]
		 MimeMessage mimeMessage = new MimeMessage(session);
		 try 
		 {
			 //from mail
			 mimeMessage.setFrom(from);
			 
			 //adding recipient
			 mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			 
			 //adding subject to message
			 mimeMessage.setSubject(subject);
			 
			 //adding attachment to message 
			 String path = "C:\\Users\\Krishna\\Desktop\\GID-6.pdf";
			 
			 MimeMultipart part = new MimeMultipart();
			
			 MimeBodyPart fortext = new MimeBodyPart();
			 MimeBodyPart forfile = new MimeBodyPart();
			 
			 try 
			 {
				 fortext.setText(message);
				 
				 File file = new File(path);
				 forfile.attachFile(file);
				 part.addBodyPart(fortext);
				 part.addBodyPart(forfile);
			 }
			 catch (Exception e) 
			 {
				 e.printStackTrace();
			 }
			 
			 mimeMessage.setContent(part);
			 
			 //step:3 send the message using transport class
			 Transport.send(mimeMessage);
			 
			 System.out.println("send message successfully..");
		 }
		 catch (Exception e) 
		 {
			e.printStackTrace();
		}
	}
}

