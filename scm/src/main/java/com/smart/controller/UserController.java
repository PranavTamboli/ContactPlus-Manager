package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
//	Method for adding common data to response
	@ModelAttribute
	public void addCommonDate(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("userName " +userName); 
		
				//	Get the user using username(email)
		User userByUserName = userRepository.getUserByUserName(userName);
		System.out.println("User : " +userByUserName);
		
		model.addAttribute("user", userByUserName);
	}
				//	Dashboard Home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "user Dashboard");
		return "normal/user_dashboard";
	}
	
				//	Open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

@PostMapping("/process-contact")
public String processContact(@ModelAttribute Contact contact, 
							@RequestParam("profileImage") MultipartFile file, 
							Principal principal, HttpSession session) {
	try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
//		It is a by directional contact ke pass user amd user ke pass contacts
		contact.setUser(user);
		
//		if(3>2) {
//			throw new Exception();
//		}
	//	processing and uploading file
		
			if (file.isEmpty()) {
//				If the file is empty then try our message
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}
			else {
//				get the file to the folder and update the name to contact
//				return the orinal file name when we upload(upload karne ke bad name ayega)
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}
		
		
		//pahale user get kiya fir user ke isme contact list add kar diya then user ko update kar diya	
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Data " +contact);
		System.out.println("Added to database");
		
//		Message Success 
		session.setAttribute("message", new Message("Your contact is added!!", "success"));
	}
	catch(Exception e){
		System.out.println("Error: " +e.getMessage());
		e.printStackTrace();
		
//		Message Error
		session.setAttribute("message", new Message("Something went wrong !! try again", "danger"));
		
	}
//	  session.removeAttribute("message");

	return "normal/add_contact_form";
}
			// Show Contacts Handler
			// per page =5[n]
			// current page = 0 [page]

@GetMapping("/show-contacts/{page}")
public String showContacts(@PathVariable("page") Integer page ,Model m, Principal principal) {
	m.addAttribute("Title", "Show contacts page");
//	Contact ki list ko bhejana hai
	
	String userName = principal.getName();
	User user = this.userRepository.getUserByUserName(userName);
	
	Pageable pageable = PageRequest.of(page, 5);
	
	Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
	
	m.addAttribute("contacts", contacts);
	m.addAttribute("currentpage", page);
	m.addAttribute("totalPages", contacts.getTotalPages());
	
	return "normal/show_contacts";
}

		// Showing perticular Contact Details
@RequestMapping("/{cId}/contact")
public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
	System.out.println("cId:" +cId);
	
	Optional<Contact> byId = this.contactRepository.findById(cId);
	Contact contact = byId.get();	
	
//	konsa user login hai check
	String userName = principal.getName();
//	Konsa user login hai ye pata chalega
	User user = this.userRepository.getUserByUserName(userName);
	
	if(user.getId() == contact.getUser().getId()) {
		model.addAttribute("contact", contact);
		model.addAttribute("title", contact.getName());
	}
	
	return "normal/contact_details";
	
}

//Delete Contact handler after clicking on delete button
@GetMapping("/delete/{cid}")
public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
	Optional<Contact> contactoptional = this.contactRepository.findById(cId);
	Contact contact = contactoptional.get();
//	Contact Deleted
	this.contactRepository.delete(contact);
	session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		
	return "redirect:/user/show-contacts/0";
}
		// Updating form handler
@PostMapping("/update-contact/{cid}")
public String updateForm(@PathVariable("cid") Integer cid, Model m) {
	
	Contact contact = this.contactRepository.findById(cid).get();
	m.addAttribute(contact);
	return "normal/update_form";
}

		//Updating contact Handler

@RequestMapping(value = "/process-update", method = RequestMethod.POST)
public String updateHandler(@ModelAttribute Contact contact, 
		@RequestParam ("profileImage") MultipartFile file,
		Model m, HttpSession session, Principal principal) {
	
	try {
//		Old Contact details
		Contact oldcontactdetail = this.contactRepository.findById(contact.getcId()).get();
//		Image....
		if(!file.isEmpty()) {
//			file work
//			file rewrite
//			Delete old photo
			File deleteFile = new ClassPathResource("static/img").getFile();
			File file1 = new File(deleteFile, oldcontactdetail.getImage());
			file1.delete();
			
			
//			Update new photo
			
			File file2 = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			contact.setImage(file.getOriginalFilename());
			System.out.println("image is uploaded");
			
		}
		else {
			contact.setImage(oldcontactdetail.getImage());
			
		}
//		Current user kon hai check it
		User user = this.userRepository.getUserByUserName(principal.getName());
//		Contact aa raha hai usme ye user set kar dijiye 
		contact.setUser(user);
		this.contactRepository.save(contact);
		session.setAttribute("message", new Message("Your contact is updated!!", "Success"));
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	System.out.println("Contact Name " +contact.getName());
	System.out.println("Contact Id " +contact.getcId());
	return "redirect:/user/"+contact.getcId()+"/contact";
}

								// Your Profile Handler
@GetMapping("/profile")
public String yourProfile() {
	return "normal/profile";
}

							// Open Setting handler
@GetMapping("/settings")
public String openSetting() {
	return "normal/settings";
}

					// Change Password Handler
@PostMapping("/change-password")
public String changePassword(@RequestParam("oldPassword") String oldPassword,
		@RequestParam("newPassword") String newPassword, 
		Principal principal, HttpSession session) {
	System.out.println("OldPassword: " +oldPassword);
	System.out.println("NewPassword: " +newPassword);
	
	String userName = principal.getName();
	User currentUser = this.userRepository.getUserByUserName(userName);
	System.out.println(currentUser.getPassword());
//	old wala password and abhi diya hua password same hoga tb
	if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())){
			//		Change the password
		currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(currentUser);
		session.setAttribute("message", new Message("Your Password is successfully changed!", "success"));
		
	}
	else {
		session.setAttribute("message", new Message("please enter correct old password!", "danger"));
		return "redirect:/user/settings";
	}
	
	return "redirect:/user/index";
}
}

