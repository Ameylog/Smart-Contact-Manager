package SCM.smart_contact_Manager.controller;


import SCM.smart_contact_Manager.dao.ContactRepository;
import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.Contact;
import SCM.smart_contact_Manager.entities.User;
import SCM.smart_contact_Manager.helper.Message;
import jakarta.servlet.http.HttpSession;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    public UserController(UserRepository userRepository, ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    // method for adding common data to response
    @ModelAttribute         // call automatically when model used
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("username :- " + userName);
        User user = userRepository.getUsersByUserName(userName);

        System.out.println("USER " + user);

        model.addAttribute("user", user);

    }

    // dashboard home
    @RequestMapping("/index")           // with help of principal we will get username,other data of user..
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "User dashboard");

        // get the user using username
        return "normal/user_dashboard";
    }

    // open add from handler
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

            if (file.isEmpty()) {
                System.out.println("File not found");
                contact.setImage("contact.jpg");

            } else {
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
            user.getContacts().add(contact);
            contact.setUser(user);

            this.userRepository.save(user);

            System.out.println("Data " + contact);
            System.out.println("Added to data base..");

            // message success....
            session.setAttribute("message", new Message("Your Contact is added !! Add More..", "success"));

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
            // message error
            session.setAttribute("message", new Message("something is wrong !! try again", "danger"));
        }
        return "normal/add_contact_form";
    }

    // show contacts handler
    //per page =5[n]
    //current page=0 [page]
    @GetMapping("/show-contacts/{page}")
    public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "This is Show Contact");

        // send contact list
//        String userName=principal.getName();
//        User user=this.userRepository.getUsersByUserName(userName);

        //we have to find contact of all contact of login user
        String userName = principal.getName();
        User user = this.userRepository.getUsersByUserName(userName);

        // page request ->
        Pageable pageable = PageRequest.of(page, 6);
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
        System.out.println("CID" + cId);

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
            , HttpSession session,Principal principal) {

        System.out.println("CID" + cId);
        Contact contact = this.contactRepository.findById(cId).get();

        //check...Assignment.
        System.out.println("Contact" + contact.getcId());

        //demapping from user so null user

        User user=this.userRepository.getUsersByUserName(principal.getName());
        user.getContacts().remove(contact);

        this.userRepository.save(user);

        // remove Image from img folder;
        File file2 = new File("target/classes/static/img", contact.getImage());
        boolean deleted = file2.delete();
        System.out.println(deleted);


        this.contactRepository.delete(contact);
        System.out.println("Deleted..");
        session.setAttribute("message", new Message("Contact deleted successfully...", "success"));

        return "redirect:/user/show-contacts/0";
    }

    // open  update contact details [open Update Detail form]
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model m) {
        m.addAttribute("title", "Update Contact");

        Contact contact = this.contactRepository.findById(cId).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    // update contact Handle for saving data in database
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
                                Model m, HttpSession session, Principal principal) {
        try {
            //old contact details

            Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

            //image
            if (!file.isEmpty()) {

                //   delete file
                 File deleteFile=new ClassPathResource("static/img").getFile();
                 File file1 =new File(deleteFile,oldContactDetails.getImage());
                 file1.delete();

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

        System.out.println("Contact NAme " + contact.getName());
        System.out.println("Contact No" + contact.getPhone());
        System.out.println("Contact Id:- "+contact.getcId());

        return "redirect:/user/"+contact.getcId()+"/contact";
    }

}
