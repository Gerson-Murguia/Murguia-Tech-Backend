package com.example.securityjwt.service;

import com.example.securityjwt.configuration.UserPasswordEncoder;
import com.example.securityjwt.enumeration.Role;
import com.example.securityjwt.exception.domain.*;
import com.example.securityjwt.model.AppUser;
import com.example.securityjwt.model.AppUserDetails;
import com.example.securityjwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static com.example.securityjwt.constant.FileConstant.*;
import static com.example.securityjwt.constant.UserImplConstant.*;
import static com.example.securityjwt.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.*;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepo;
    private final UserPasswordEncoder userPasswordEncoder;
    private final LoginAttempService loginAttempService;
    private final EmailService emailService;

    //busca al user en la bd cuando se intenta loguear y lo pasa a spring security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user= userRepo.findByUsername(username);
        if (user==null) {
            log.error("Usuario no encontrado por username: {}",username);
            throw new UsernameNotFoundException("Usuario no encontrado por username: "+username);
        }else {
            //chequea al user encontrado
            validateLoginAttempt(user);
            //la fecha que mostraremos antes de actualizar la ultima fecha de logeo
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(LocalDate.now());
            userRepo.save(user);
            AppUserDetails userDetails=new AppUserDetails(user);
            log.info("Retornando security user encontrado por username: "+username);
            return userDetails;
        }

    }

    @Override
    public AppUser registro(String firstName, String lastName, String username, String email) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
        AppUser user=new AppUser();
        user.setUserId(generateUserId());
        String password=generatePassword();
        String encodedPassword=encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(LocalDate.now());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        //por defecto role es user
        user.setRoles(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));

        //temporal, ya que el password se encripta
        log.info("Contraseña de nuevo usuario: "+password);
        emailService.sendNewPasswordEmail(firstName,password,email);

        return userRepo.save(user);
    }

    private void validateLoginAttempt(AppUser user)  {
        //TODO: Implementar desbloquear al user despues de un tiempo o a pedido del user
        if (user.isNotLocked()){
            if (loginAttempService.exceedMaxAttempts(user.getUsername())){
               user.setNotLocked(false);
                log.info("Se bloqueo la cuenta del user");
            }else{
                user.setNotLocked(true);
                log.info("No excedio los intentos maximos");
            }
            return;
        }else{
            loginAttempService.evictUserFromLoginAttempCache(user.getUsername());
        }
    }

    @Override
    public List<AppUser> getUsers() {
        return userRepo.findAll();
    }

    @Override
    public AppUser findUserByUsername(String username) {

        return userRepo.findByUsername(username);
    }

    @Override
    public AppUser findUserByEmail(String email) {

        return userRepo.findByEmail(email);
    }

    //todo: cuando un nuevo usuario se añade si no tiene foto, se agrega foto temporal, sino la original como ruta
    @Override
    public AppUser addNuevoUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,username,email);
        AppUser user=new AppUser();
        String password=generatePassword();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setUserId(generateUserId());
        user.setEmail(email);
        user.setRoles(getRoleEnumName(role).name());
        user.setJoinDate(LocalDate.now());
        user.setNotLocked(isNonLocked);
        user.setActive(isActive);
        user.setPassword(encodePassword(password));
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        //image temporal
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepo.save(user);
        log.info(password);
        saveProfileImage(user,profileImage);
        return user;
    }



    @Override
    public AppUser updateUser(String currentUsername, String nuevoFirstName, String nuevoLastName, String nuevoUsername, String nuevoEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        AppUser currentUser = validateNewUsernameAndEmail(currentUsername,nuevoUsername,nuevoEmail);
        //TODO:asegurarse user not null
        if (currentUser==null) throw new UserNotFoundException("Usuario no encontrado");

        currentUser.setFirstName(nuevoFirstName);
        currentUser.setLastName(nuevoLastName);
        currentUser.setUsername(nuevoUsername);
        currentUser.setUserId(generateUserId());
        currentUser.setEmail(nuevoEmail);
        currentUser.setJoinDate(LocalDate.now());
        currentUser.setNotLocked(isNonLocked);
        currentUser.setActive(isActive);
        currentUser.setRoles(getRoleEnumName(role).name());
        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        currentUser.setProfileImageUrl(getTemporaryProfileImageUrl(nuevoUsername));
        userRepo.save(currentUser);
        //si el user tiene un profile image
        saveProfileImage(currentUser,profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(String username) throws IOException {
        AppUser user =userRepo.findByUsername(username);
        Path userFolder=Paths.get(USER_FOLDER+user.getUsername()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(userFolder.toFile());
        userRepo.deleteById(user.getId());
    }

    @Override
    public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
        AppUser user=userRepo.findByEmail(email);
        if (user==null){
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL+email);
        }
        String password=generatePassword();
        user.setPassword(encodePassword(password));
        userRepo.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(),password,email);
        log.info("Nuevo password enviado");
    }

    @Override
    public AppUser updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        AppUser user=validateNewUsernameAndEmail(username,null,null);
        saveProfileImage(user,profileImage);
        return user;
    }



    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    //EXPLAIN: En el metodo generamos nosotros la contraseña, tambien
    //    se puede obtener la contraseña desde el formulario y ya no usar este metodo
    private String generatePassword() {

        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String encodePassword(String password) {

        return userPasswordEncoder.passwordEncoder().encode(password);
    }

    private AppUser validateNewUsernameAndEmail(String usernameActual,String usernameNuevo,String emailNuevo) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
    //el username actual es por si el user esta logeado

        //no deberia encontrar el usuario si se esta registrando
        AppUser userByNewUsername=findUserByUsername(usernameNuevo); //es null
        AppUser userByEmail=findUserByEmail(emailNuevo); //es null


        if(StringUtils.isNotBlank(usernameActual)){
            //buscamos el user por username para ver si ya esta registrado
            AppUser currentUser =findUserByUsername(usernameActual);
            log.info("usuario actual encontrado: "+ currentUser.getUsername());
            if(currentUser==null){
                throw  new UserNotFoundException("No se encontro el usuario por su username: "+ usernameActual);
            }
            //check si ya existe un usuario con ese nuevo username
            if (userByNewUsername !=null && !userByNewUsername.getUserId().equals(currentUser.getUserId())){
                throw new UsernameExistsException(USERNAME_YA_EXISTE);
            }
            //check si ya existe un usuario con ese nuevo email
            if (userByEmail !=null && !userByNewUsername.getUserId().equals(userByEmail.getUserId())){
                throw new EmailExistsException(EMAIL_YA_EXISTE);
            }
            return currentUser;
        }else{
            if (userByNewUsername!=null){
                throw new UsernameExistsException(USERNAME_YA_EXISTE);
            }
            if (userByEmail!=null){
                throw new UsernameExistsException(EMAIL_YA_EXISTE);
            }
            return null;
        }
    }
    private void saveProfileImage(AppUser user, MultipartFile profileImage) throws IOException, NotAnImageException {
        if (profileImage!=null){
            //si la imagen no es de ninguno de estos contentType
            if (!Arrays.asList(IMAGE_JPEG_VALUE,IMAGE_PNG_VALUE,IMAGE_GIF_VALUE).contains(profileImage.getContentType())){
                throw new NotAnImageException(profileImage.getOriginalFilename()+" no es una imagen. Por favor, suba una imagen");
            }
            // user/home/murguiatech/gerson
            Path userFolder= Paths.get(USER_FOLDER+user.getUsername()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                log.info("Directorio creado:"+userFolder.toString());
            }
            //si existe la imagen, la borra
            Files.deleteIfExists(Paths.get(userFolder+user.getUsername()+DOT+JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(),userFolder.resolve(user.getUsername()+DOT+JPG_EXTENSION),REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepo.save(user);
            log.info("Archivo guardado en el sistema"+profileImage.getOriginalFilename());
        }

    }

    private String setProfileImageUrl(String username) {
        //ubicacion real de la imagen
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH+username+FORWARD_SLASH+username+DOT+JPG_EXTENSION).toUriString();
    }

    private String getTemporaryProfileImageUrl(String username) {
        //Consumir una api desde el userResource para obtener imagenes ui-avatar
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH+username).toUriString();
    }

    //si pasamos user, retorna ROLE_USER
    private Role getRoleEnumName(String role) {

        return Role.valueOf(role.toUpperCase());
    }
}
