package cn.biq.mn.admin.security;

import cn.biq.mn.admin.rbac.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final AdminRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new MyUserDetails(userRepository.findOneByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("username " + username + " is not found"))
        );
    }

}
