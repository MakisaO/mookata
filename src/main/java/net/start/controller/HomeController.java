package net.start.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	  
	
	  @GetMapping(path = { "/", "/index" })
	  public String home(Model model) {
		
	        //model.addAttribute("messages", messageRepository.findAll());
	        return "/index";
	  }
}
