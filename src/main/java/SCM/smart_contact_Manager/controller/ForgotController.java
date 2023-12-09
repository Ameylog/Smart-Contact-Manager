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
    Random random = new Random(100000);

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

        //generating otp of f4 digit

        int otp = random.nextInt(999999);
        System.out.println("OTP:- " + otp);

        String subject = "OTP from SCM";
        String message = ""
                + "<div style='border:1px solid #e2e2e2; padding:20px'>"
                + "<h1>"
                + "OTP is : "
                + "<b>" + otp
                + "</b>"
                + "</h1>"
                + "</div>";
        String to = email;

        boolean flag = this.emailService.sendEmail(subject, message, to);
        if (flag) {
            session.setAttribute("otp", otp);
            session.setAttribute("email", email);
            return "varify_otp";
        } else {
            session.setAttribute("message", "Check your email id");
            return "forgot_email_form";
        }
    }

    //verify otp handler
    @PostMapping("/varify-otp")
    public String varifyOTP(@RequestParam("otp") int otp, HttpSession session) {
        int myotp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");

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

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User user = userRepository.getUsersByUserName(email);
        user.setPassword(bcryptEncoder.encode(newpassword));

        //changing whether old password is same as new password
        if (bcryptEncoder.matches(newpassword, user.getPassword())) {
            session.setAttribute("message", "Sorry! You Entered An old Password.Try another password..");
            System.out.println("error...");
            return "change_password";
        }
        userRepository.save(user);
        return "redirect:/login?chnage=password chnage sucessfully";
    }
}
