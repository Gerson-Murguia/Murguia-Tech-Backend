package com.example.securityjwt.service;

import com.example.securityjwt.exception.domain.EmailExistsException;
import com.example.securityjwt.exception.domain.EmailNotFoundException;
import com.example.securityjwt.exception.domain.UserNotFoundException;
import com.example.securityjwt.exception.domain.UsernameExistsException;
import com.example.securityjwt.model.AppUser;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    AppUser registro(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException;
    List<AppUser> getUsers();
    AppUser findUserByUsername(String username);
    AppUser findUserByEmail(String email);
    //dentro de la aplicacion con mas informacion
    AppUser addNuevoUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException;
    AppUser updateUser(String currentUsername,String nuevoFirstName, String nuevoLastName, String nuevoUsername, String nuevoEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException;
    void deleteUser(String username);
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;
    AppUser updateProfileImage(String username,MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException;
}
