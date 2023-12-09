package SCM.smart_contact_Manager.configuration;

import SCM.smart_contact_Manager.entities.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

public class CustomeUserDetails implements UserDetails {

    // Contributor based dependency injection
    private User user;

    public CustomeUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        SimpleGrantedAuthority simpleGrantedAuthority=new SimpleGrantedAuthority(user.getRole()); // give the role
        return List.of(simpleGrantedAuthority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();   // return the user password
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // return user email
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;    // true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // true
    }

    @Override
    public boolean isEnabled() {
        return true; // true
    }
}
