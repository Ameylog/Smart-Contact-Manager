package SCM.smart_contact_Manager.controller;


import SCM.smart_contact_Manager.dao.ContactRepository;
import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.Contact;
import SCM.smart_contact_Manager.entities.User;
import SCM.smart_contact_Manager.helper.Message;
import jakarta.servlet.http.HttpSession;

import org.springframework.cglib.SpringCglibInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserRepository userRepository, ContactRepository contactRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // method for adding common data to response
    @ModelAttribute        // call automatically when model used
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
//        System.out.println("username :- " + userName);
        User user = userRepository.getUsersByUserName(userName);
//        System.out.println("USER " + user);
        model.addAttribute("user", user);
    }

    // Implementing of dashboard [home]
    @RequestMapping("/index")    // with help of principal we will get username,other data of user..
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "User dashboard");
        // get the user using username
        return "normal/user_dashboard";
    }

    // Implementing for adding contact [from handler]
    @GetMapping("/add-contact")
    public String openAddContactFrom(Model model) {

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    //processing add contact form   [ Stored data of add contact details in database]
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal, HttpSession session) {

        try {
            String name = principal.getName();
            User user = this.userRepository.getUsersByUserName(name);
            // processing image file
            if (file.isEmpty()) {
                System.out.println("File not found");
                contact.setImage("contact.jpg");

            } else {
                // if file has data we add file to folder and name to the database
                // set image name
                contact.setImage(file.getOriginalFilename());

                // save file at specific folder
                File saveFile = new ClassPathResource("static/img").getFile();

                // create file
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                // copy file and taking path if image is present then replace
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }
            //bidirectional
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
//            System.out.println("Data " + contact);
//            System.out.println("Added to data base..");

            // message success....
            session.setAttribute("message", new Message("Your Contact is added !! Add More..", "success"));

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            // message error
            session.setAttribute("message", new Message("something is wrong !! try again", "danger"));
            e.printStackTrace();
        }
        return "normal/add_contact_form";
    }

    // show contacts handler  //per page =5[n]    //current page=0 [page]
    @GetMapping("/show-contacts/{page}")
    public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "This is Show Contact");

        //we have to find contact of all contact of login user
        String userName = principal.getName();
        User user = this.userRepository.getUsersByUserName(userName);

        // page request ->
        Pageable pageable = PageRequest.of(page, 6);
        //contact find by user Id
        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contacts";
    }

    // showing particular contact details  [it is showing User other details like description]
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model
            model, Principal principal) {
//        System.out.println("CID" + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);

        if (contactOptional.isPresent()) {
            Contact contact = contactOptional.get();

            // only showing users contact block other user contact access from id
            String userName = principal.getName();
            User user = this.userRepository.getUsersByUserName(userName);

            if (user.getId() == contact.getUser().getId())
                model.addAttribute("contact", contact);
                 model.addAttribute("title", contact.getName());
        }
        return "normal/contact_details";
    }

    // delete contact details handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model
            , HttpSession session, Principal principal) {

//        System.out.println("CID" + cId);
        Contact contact = this.contactRepository.findById(cId).get();

        //check...Assignment.
        System.out.println("Contact" + contact.getcId());

        //mapping from user so null user
        User user = this.userRepository.getUsersByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);

        // remove Image from img folder;
        if (!contact.getImage().equals("contact.jpg")) {
            File file2 = new File("target/classes/static/img", contact.getImage());
            boolean deleted = file2.delete();
            System.out.println(deleted);
        }

        this.contactRepository.delete(contact);
        System.out.println("Deleted..");
        session.setAttribute("message", new Message("Contact deleted successfully...", "success"));

        return "redirect:/user/show-contacts/0";
    }

    // update contact details [open Update Detail form]
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model m) {
        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cId).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    // update contact Handle for saving data in database
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("customImage") MultipartFile file,
                                Model m, HttpSession session, Principal principal) {
        try {
            //old contact details
            Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

            //image
            if (!file.isEmpty()) {
                //   delete file
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldContactDetails.getImage());

                if(!oldContactDetails.getImage().equals("contact.jpg")) {
                    file1.delete();
                }

                // update new Photo
                // save file at specific folder  // rewrite file[replace]

                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());


            } else {
                //file is empty
                contact.setImage(oldContactDetails.getImage());
            }
            String userName = principal.getName();
            User user1 = this.userRepository.getUsersByUserName(userName);

            contact.setUser(user1);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your contact s updated...", "success"));

        } catch (Exception e) {
            e.printStackTrace();

        }
//        System.out.println("Contact NAme " + contact.getName());
//        System.out.println("Contact No" + contact.getPhone());
//        System.out.println("Contact Id:- " + contact.getcId());

        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //update user profile handler
    @PostMapping("/update-user")
    public String updateUser(Model model ,Principal principal){
        User user=userRepository.getUsersByUserName(principal.getName());
        model.addAttribute("user",user);
        return "normal/update_user";
    }

    // user-update process
    @PostMapping("/user-update-process")
    public String updatedUserProcess(@ModelAttribute User user,@RequestParam("customImage") MultipartFile file,
            Model model,Principal principal,HttpSession session) throws IOException {

        User old_user=userRepository.getUsersByUserName(principal.getName());
        if(!file.isEmpty()){
            //deleting old user image

            File deleteFile = new ClassPathResource("/static/img").getFile();
            File deletFilenow = new File(deleteFile, old_user.getImageUrl());

            //default image not be deleted
            if(!old_user.getImageUrl().equals("contact.jpg")) {
                deletFilenow.delete();
            }

            //save user image
            File saveFile=new ClassPathResource("static/img").getFile();
            Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
            Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
            user.setImageUrl(file.getOriginalFilename());
        }else{
            user.setImageUrl(old_user.getImageUrl());
        }
        userRepository.save(user);
        session.setAttribute("message",new Message("Profile updated successfully login again!","success"));
        return "redirect:/user/index";
    }


    //your profile handler
    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile page");
        return "normal/profile";
    }

    // setting handler
    @GetMapping("/settings")
    public String openSetting() {
        return "normal/settings";
    }

    // change password handler
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,
                                 Principal principal, HttpSession session) {

        // principal --> Gives current login user
//        System.out.println("Old Password:- " + oldPassword);
//        System.out.println("New Password:- " + newPassword);

        User currentUser = this.userRepository.getUsersByUserName(principal.getName());
//        System.out.println(currentUser.getPassword());   // print in current user password from database

        // match oldpassword[entered password] and currentUser.getPassword()[already stored in database]
        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            // change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(currentUser);

            session.setAttribute("message", new Message("Password updated successfully", "success"));
        } else {
            // if password is wrong
            session.setAttribute("message", new Message("Invalid Old password!,enter correct password", "danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }


}
