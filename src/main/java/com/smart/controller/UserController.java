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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String userName=principal.getName();
		System.out.println("username--->"+userName);
		User  user=userRepository.getUserByUserName(userName);
		System.out.println("user---->"+user);
		model.addAttribute("user", user);
		
	}
	
	
	
	@RequestMapping("/index")
	public String dashBord(Model model, Principal principal) {
		model.addAttribute("title", "User Dashbord ");
		String userName=principal.getName();
		System.out.println("username--->"+userName);
		User  user=userRepository.getUserByUserName(userName);
		System.out.println("user---->"+user);
		model.addAttribute("user", user);
		return "normal/user_dashbord";
	}
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	/// handle process-contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Principal principal, HttpSession session) {
		try {

		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		//processing and uploadin file
		if(file.isEmpty()) {
			System.out.println("File is Empty");
			contact.setImage("contact.jpg");
		}
		else {
		
			contact.setImage(file.getOriginalFilename());
		
			File savFile=new ClassPathResource("static/img").getFile();
		
			Path path=Paths.get(savFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is upload");
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		this.userRepository.save(user);
		//success message......!!!
		session.setAttribute("message", new Message("Your contact is added!! add more..", "success") );
		
		}
		catch (Exception e) {
			System.out.println("error-->"+e.getMessage());
			// TODO: handle exception
			session.setAttribute("message", new Message("Some went wrong !! try again", "danger") );
		}
		return "normal/add_contact_form";
		
	}
	
	//show contacts handler
	//per page=5[n]
	//current page = [page]
	@GetMapping("/show-contacts/{page}")
	public String shoContacts(@PathVariable ("page") Integer page,Model model, Principal principle) {
		model.addAttribute("title", "Show Contacts");
		//we have pass contact list
//		String userName=principle.getName();
//		User user=this.userRepository.getUserByUserName(userName);
//		user.getContacts();
//		2nd way
//		String userName=principle.getName();
//		User user=this.userRepository.getUserByUserName(userName);
//		List<Contact> contacts=this.contactRepository.findContactsByUser(user.getId());
//		model.addAttribute("contacts", contacts);
		
		//paggination 
		String userName=principle.getName();
		User user=this.userRepository.getUserByUserName(userName);
	    
		//pagination
		//current page
		//cotact Per page- 5
		Pageable pageable=PageRequest.of(page,5);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("currentPage", page);
		model.addAttribute("contacts", contacts);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
		}
	
	//showing particular contact details
	@RequestMapping("/contact/{cId}")
	public String showCOntactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		Optional<Contact>   contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		model.addAttribute("title", "contact Details");
		//
		String userName=principal.getName();
		User user =this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
		}
		
		
		return "normal/show_contact_detail";
	}
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cid, Model model,HttpSession session,Principal principal) {
		
		System.out.println("Contact delete ");
		
	
		Optional<Contact> conOptional=this.contactRepository.findById(cid);
		Contact contact=conOptional.get();
//		contact.setUser(null);
		//remove img
		User user =this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
//		this.contactRepository.delete(contact);
		System.out.println("delete done!!");
		session.setAttribute("message", new Message("Contact Delete Successfully", "success"));
		
		return "redirect:/users/show-contacts/0";
	}
	
	//Update Userhandeler
	@PostMapping("/update-contact/{cid}")
	public String updateUser(@PathVariable("cid") Integer cid, Model model) {
		
		model.addAttribute("title", "Update-contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String processUpdateHandler(@ModelAttribute Contact contact, @RequestParam ("profileImage") MultipartFile file,Model modle, HttpSession session
			,Principal principal){
		System.out.println("Contact Name"+contact.getName());
		System.out.println("Contact Id"+contact.getcId());
		try {
			//old contact details
		Contact oldContactdetail=	this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//fie rewrite
			   //delete old picture
				File deleteFile=new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,oldContactdetail.getImage());
                file1.delete();
				
				//update new picture
				
				File savFile=new ClassPathResource("static/img").getFile();
			
				Path path=Paths.get(savFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				System.out.println("Image is updated");
			}
			else {
				contact.setImage(null);
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated..", "success"));
			
		} catch (Exception e) {
			System.out.println("error  "+e.getMessage());
			// TODO: handle exception
		}
		return "redirect:/users/contact/"+contact.getcId();
	}
	//Your Profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		model.addAttribute("title","Prfile page");
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		System.out.println("user-profile-->"+user);
		model.addAttribute("user", user);
		return "normal/profile";
		
	}
}
