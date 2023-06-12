package cn.biq.mn.admin.rbac.admin;


import cn.biq.mn.admin.entity.admin.Admin;
import cn.biq.mn.admin.repository.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.admin.security.JwtUtils;
import cn.biq.mn.admin.utils.SessionUtil;
import cn.biq.mn.base.exception.ItemExistsException;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SessionUtil sessionUtil;

    public boolean add(AdminAddForm form) {
        if (adminRepository.findOneByUsername(form.getUsername()).isPresent()) {
            throw new ItemExistsException();
        }
        Admin admin = new Admin();
        admin.setUsername(form.getUsername());
        admin.setPassword(passwordEncoder.encode("111111"));
        adminRepository.save(admin);
        return true;
    }

    @Transactional(readOnly = true)
    public String login(LoginForm form) {
        var token = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
        var authentication = authenticationManager.authenticate(token);
        // Filter中设置context，这里不需要
        return jwtUtils.createToken(authentication);
    }

    @Transactional(readOnly = true)
    public InitStateResponse getInitState() {
        var initState = new InitStateResponse();
        initState.setAdmin(AdminMapper.toDetails(sessionUtil.getCurrentAdmin()));
        return initState;
    }

}
