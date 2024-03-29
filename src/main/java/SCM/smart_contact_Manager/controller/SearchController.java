package SCM.smart_contact_Manager.controller;

import SCM.smart_contact_Manager.dao.ContactRepository;
import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.Contact;
import SCM.smart_contact_Manager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    // Principal -> get current Login
    //search Handler
    @GetMapping("/search/{query}")
    public ResponseEntity<?>search(@PathVariable("query") String query, Principal principal){
        System.out.println(query);

        User user = this.userRepository.getUsersByUserName(principal.getName());
        List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
        return ResponseEntity.ok(contacts);
    }
}
