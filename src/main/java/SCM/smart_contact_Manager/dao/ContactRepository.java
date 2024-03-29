package SCM.smart_contact_Manager.dao;

import SCM.smart_contact_Manager.entities.Contact;
import SCM.smart_contact_Manager.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// fetch all data from contact


public interface ContactRepository extends JpaRepository<Contact,Integer> {

    // implement for also pagination
    @Query("from Contact as c where c.user.id=:userId")
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pePageable);

    // Pageable have two things
    // currenPage-page ,contact per-page 5

    // NameContaning --> all contact give that match name and  those name they are login
    public List<Contact>findByNameContainingAndUser(String name, User user);
}
