package SCM.smart_contact_Manager.controller;

import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.User;
import SCM.smart_contact_Manager.helper.Message;
import SCM.smart_contact_Manager.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    //email id form open handler
    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    //send otp handler
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session) {

        System.out.println("EMAIL " + email);
        User name= userRepository.getUsersByUserName(email);

        //generating otp of 6 digit
        long otp = 100000 + new Random().nextInt(900000);
        System.out.println("OTP:- " + otp);

        String subject = "OTP from SCM";
        String message = "<p>Hello " + name.getName() + ",</p>\n\n"+
                "<p>We received a request to reset the password for your account. " +
                "If you did not make this request, please ignore this email. " +
                "This is your OTP:</p>\n\n" + ""+otp+
                "<p>Thank you,</p>\n" +
                "<p>Smart Contact Manager Team</p>";
        String to = email;

        boolean flag = emailService.sendEmail(subject, message, to);
        if (flag) {
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "varify_otp";
        } else {
            session.setAttribute("message", "Check your email id");
            return "forgot_email_form";
        }
    }

    //verify otp handler
    @PostMapping("/varify-otp")
    public String varifyOTP(@RequestParam("otp") long otp, HttpSession session) {
        long myotp = (long) session.getAttribute("myotp");
        String email =(String) session.getAttribute("email");

        if (myotp == otp) {
            //change password form
            User user = userRepository.getUsersByUserName(email);

            if (user == null) {
                //send error message
                session.setAttribute("message", "User with this email doesn't exits !!");

                return "forgot_email_form";
            } else {
                //send change password form
                return "change_password";
            }

        } else {
            session.setAttribute("message", "OTP Invalid !");
            return "varify_otp";
        }
    }

    @PostMapping("/user-change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = userRepository.getUsersByUserName(email);

        //changing whether old password is same as new password
        if (bcryptEncoder.matches(newpassword, user.getPassword())) {
            session.setAttribute("message", "Sorry! You Entered An old Password.Try another password..");
            System.out.println("error...");
            return "change_password";
        }
        user.setPassword(bcryptEncoder.encode(newpassword));
        userRepository.save(user);
        return "redirect:/signin?change=password change successfully";
    }
}
