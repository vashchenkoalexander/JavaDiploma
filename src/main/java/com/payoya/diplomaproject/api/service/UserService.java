package com.payoya.diplomaproject.api.service;

import com.payoya.diplomaproject.api.entity.User;
import com.payoya.diplomaproject.api.enums.Role;
import com.payoya.diplomaproject.api.exceptions.UsernameExistException;
import com.payoya.diplomaproject.api.repository.IUserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final IUserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(IUserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createNewUser (User user) throws RuntimeException {

        if(userRepository.findUserByUsername(user.getUsername()).isPresent()){
            throw new UsernameExistException("user with this login is exist: " + user.getUsername());
        }

        if(user.getDateOfCreate() == null){
            user.setDateOfCreate();
        }

        if(user.getRole() == null){
            user.setRole(Role.USER);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'MODERATOR')")
    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

    //@PostAuthorize("returnObject.username == principal.username")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(new User());
    }

    public User updateUserById(Long id, User user){
        if(userRepository.findById(id).isPresent()){
            User userUpd = userRepository.findById(id).get();

            userUpd.setUsername(user.getUsername());
            userUpd.setPassword(user.getPassword());
            userUpd.setFirstName(user.getFirstName());
            userUpd.setLastName(user.getLastName());
            userUpd.setRole(user.getRole());

            return userRepository.save(userUpd);
        }else {
            return createNewUser(user);
        }

    }

    @PostAuthorize("returnObject.id == principal.id")
    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }

    /*
    return a user by his username or else return UsernameNotFoundException exception
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(()
                -> new UsernameNotFoundException("user not found with this username" + username));
    }

//    public List<User> findAllUsersByFirstName(String firstName, Pageable pageable){
//        return userRepository.findAllByFirstName(firstName, pageable);
//    }

    public List<User> findAllUsersByFirstName(Pageable pageable){
        return userRepository.findAll(pageable).stream().toList();
    }

}
