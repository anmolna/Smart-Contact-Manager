package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.dao.myOrderRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class userController {
	@Autowired	
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private myOrderRepository myOrderRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		 String userName =principal.getName();
		    System.out.println("USERNAME "+ userName);
		 User user =   userRepository.getUserbyUserName(userName);
		 System.out.println("USER"+ user);
		 model.addAttribute("user",user);
		    
		    
	}
	
	//dashboard home
	@RequestMapping("/index")
    public String dashboard(Model model,Principal principal) {
    
		return "normal/user_dashboard";
    }
	
	
	
	//add  form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
		}
	//processing contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage")MultipartFile file,
			Principal principal,HttpSession session) {
		
		try {
			User user =	this.userRepository.getUserbyUserName(principal.getName());
			
		    contact.setUser(user);
		    //processing and uploading image file
		    if(file.isEmpty()) {
		    // if the file is empty then try our message
		    	System.out.println("File is Empty");
		    	contact.setImage("profilePhoto.png");
		    }else {
		    	//upload the file to folder and update to contact
		    	contact.setImage(file.getOriginalFilename());
		    File saveFile=	new ClassPathResource("static/img").getFile();
		    
		 Path path  =   Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		    Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
		    System.out.println("Image Uploaded");
		    }
			
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("DATA "+contact);
			session.setAttribute("message",new Message( "Your Contact is Added", "success"));
			
		} catch (Exception e) {
			System.out.println("ERROR"+ e.getMessage());
			e.printStackTrace();
			session.setAttribute("message",new Message( "Something Went Wrong !!", "danger"));
			
		}
	
		return "normal/add_contact_form";
	}
	//Show Contact Handler
	//per page 5
	@GetMapping("/show-contacts/{page}")
	public String showContact( @PathVariable("page")Integer page ,Model model,Principal principal) {
		model.addAttribute("title","Show User Contacts");
	String userName = principal.getName();
	User user = this.userRepository.getUserbyUserName(userName);
	Pageable pageable=PageRequest.of(page, 5);
	Page<Contact> contacts =this.contactRepository.findContactsByUser(user.getId(),pageable);
	model.addAttribute("contacts",contacts);
	model.addAttribute("currentPage",page);
	model.addAttribute("totalPages",contacts.getTotalPages());
		
	
		
		return "normal/show_contacts";
		
	}
	@GetMapping("{cId}/contact")
	public String showContactDetails( @PathVariable ("cId")Integer cId ,Model model,Principal principal)
	{	 System.out.println("cId :"+cId);
	      Optional<Contact> contactOptional =  this.contactRepository.findById(cId);
	      
	  Contact contact=  contactOptional.get();
	String userName =  principal.getName();
	User user=  this.userRepository.getUserbyUserName(userName);
	  
	if(user.getId()==contact.getUser().getId()) {
		  model.addAttribute("contact",contact);
		  model.addAttribute("title",contact.getName());
	}
	  
	
		return "normal/contact_detail";
	}
	
	
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cId,HttpSession session)
	{ 
  
       this.contactRepository.deleteByIdCustom(cId);
       
     
       session.setAttribute("message", new Message("Contact deleted Successfully", "success"));
       
       
		return "redirect:/user/show-contacts/0";
			
	}
	//update contact button
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId  ,Model model) {
		model.addAttribute("title","Update Contact");
	    Optional<Contact> contactOptional =  this.contactRepository.findById(cId);
	    Contact contact=contactOptional.get();
	    model.addAttribute("contact",contact); 
		return "normal/update_form" ;
				}
	// update details of contact  handler
	@PostMapping("/process-update")
	public String view(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model model,
			HttpSession session,Principal principal) {
		try {
		Contact oldContactDetailContact=   this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//delete old photo 
				File deleteFile=	new ClassPathResource("static/img").getFile();
				File file2=new File(deleteFile, oldContactDetailContact.getImage());
				file2.delete();
				
				
				//update new photo
				File saveFile=	new ClassPathResource("static/img").getFile();
			    
				 Path path  =   Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				    Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
				    contact.setImage(file.getOriginalFilename());
				
				
			}else {
				contact.setImage(oldContactDetailContact.getImage());
			}
			User user=this.userRepository.getUserbyUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message",new Message("Your Contact is Updated...", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			}
		return "redirect:/user/" +contact.getcId() +"/contact";
	}
	
	
	 //login user profile
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		
		
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session ){
		System.out.println("old password"+ oldPassword);
		System.out.println("old password"+ newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserbyUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change password
		    	currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
	          this.userRepository.save(currentUser);
	     
	    session.setAttribute("message", new Message("Your password is successfully changed.." ,"success"));
	
		}else {
			session.setAttribute("message" ,new Message("Entered old password is wrong !!" , "danger"));
		
			   return"redirect:/user/settings";
		}
		return"redirect:/user/index";
	}
	
	
	//creating order for payment
        @PostMapping("/create_order")
        @ResponseBody
     	public String createOrder(@RequestBody Map<String,Object>data,Principal principal)throws RazorpayException {	
		int  amt=Integer.parseInt(data.get("amount").toString());
		
	     var client=	new RazorpayClient("rzp_test_LhW8AA6SCDARv9", "dss3gTDkjSubRQjkVRfzVrCv");
	     JSONObject ob= new JSONObject();
	     ob.put("amount",amt*100);
	     ob.put("currency","INR");
	     ob.put("receipt","txn_235425");
	     
	     //creating new order
	     Order order = client.Orders.create(ob);
	     System.out.println("Order: "+order);
	     
	     //save in database
	     MyOrder myOrder=new MyOrder();
	     myOrder.setAmount(order.get("amount")+"");
	     myOrder.setOrderId(order.get("id"));
	     myOrder.setPaymentId(null);
	     myOrder.setStatus("created");
	     myOrder.setReceipt(order.get("receipt"));
	     myOrder.setUser(this.userRepository.getUserbyUserName(principal.getName()));
	     
	     this.myOrderRepository.save(myOrder);
	      return order.toString();
		
	}
	
        @PostMapping("/update_order")
        public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
        	
            MyOrder myOrder= 	this.myOrderRepository.findByOrderId(data.get("order_id").toString());
        	myOrder.setPaymentId(data.get("payment_id").toString());
        	myOrder.setStatus(data.get("status").toString());
        
            	this.myOrderRepository.save(myOrder);
        	
            	System.out.println("data"+data);
        	      	
        	      return ResponseEntity.ok(Map.of("msg","updated"));
        	
        	
        	
        }
        
	
	
	
	
	
}
