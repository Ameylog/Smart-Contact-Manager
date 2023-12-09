package SCM.smart_contact_Manager.controller;


import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.User;
import SCM.smart_contact_Manager.helper.Message;
import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    // Home handler
    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    // about handle
    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact manager");
        return "about";
    }

    // SignUp handler
    @RequestMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute("title", "Register - Smart Contact manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    // handle for do_register registering data
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement",
            defaultValue = "false") boolean agreement, Model model, HttpSession session, Message message) {
        try {
            if (!agreement) {
//                System.out.println("You have not agreed the terms and conditions");
                throw new Exception("You have not agreed the terms and conditions");
            }

            if (result1.hasErrors()) {
                System.out.println("ERROR" + result1.toString());
                model.addAttribute("user", user);
                return "signup";

            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("contact.jpg");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

//            System.out.println("Agreement" + agreement);
//            System.out.println("USER" + user);
            User result = this.userRepository.save(user);
            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("something went wrong !!" + e.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    // handle for custom Login
    @GetMapping("/signin")
    public String customeLogin(Model model) {
        model.addAttribute("title", "Login Page");
        return "login";
    }

    @GetMapping("/logout")
    public String logoutUser(){
        return "redirect:/";
    }

}
