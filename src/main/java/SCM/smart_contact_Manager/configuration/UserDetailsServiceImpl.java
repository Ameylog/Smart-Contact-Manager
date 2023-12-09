package SCM.smart_contact_Manager.configuration;

import SCM.smart_contact_Manager.dao.UserRepository;
import SCM.smart_contact_Manager.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // fetching user from database
        User user = userRepository.getUsersByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("Could not found user");
        }
        CustomeUserDetails customeUserDetails = new CustomeUserDetails(user);
        return customeUserDetails;
    }

}
