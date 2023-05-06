package cn.biq.mn.admin.user;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> query(UserQueryForm form, Pageable page) {
        return userRepository.findAll(form.buildPredicate(), page);
    }

}
