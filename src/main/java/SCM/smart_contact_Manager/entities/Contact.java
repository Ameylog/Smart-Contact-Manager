package SCM.smart_contact_Manager.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "CONTACT")

public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cId;

    @NotBlank(message = "Name Field is required !!")
    @Size(min = 2, max = 20, message = "min 2 and 20 characters are allow !!")
    private String name;

    @NotBlank(message = "Name Field is required !!")
    @Size(min = 2, max = 20, message = "min 2 and 20 characters are allow !!")
    private String secondName;

    private String work;

    @Email(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$", message = "Enter Valid Email !!")
    private String email;

    @NotBlank(message = "Enter the Phone No")
    @Size(min = 10, max = 10)
    private String phone;

    private String image;

    @Column(length = 5000)
    private String description;

    @ManyToOne
    private User user;

    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Contact(int cId, String name, String secondName, String work, String email, String phone, String image, String description, User user) {
        this.cId = cId;
        this.name = name;
        this.secondName = secondName;
        this.work = work;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.description = description;
        this.user = user;
    }

    public Contact() {
    }

    @Override
    public boolean equals(Object obj) {
        return this.cId==((Contact)obj).getcId();
    }
}
