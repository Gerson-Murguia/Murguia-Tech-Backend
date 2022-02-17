package com.example.securityjwt.service;

import com.example.securityjwt.exception.domain.*;
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
    AppUser addNuevoUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException;
    AppUser updateUser(String currentUsername,String nuevoFirstName, String nuevoLastName, String nuevoUsername, String nuevoEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException;
    void deleteUser(String username) throws IOException;
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;
    AppUser updateProfileImage(String username,MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException;
}
