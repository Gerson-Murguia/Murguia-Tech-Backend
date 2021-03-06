package com.example.securityjwt.resource;

import com.example.securityjwt.exception.domain.*;
import com.example.securityjwt.model.AppUser;
import com.example.securityjwt.model.AppUserDetails;
import com.example.securityjwt.model.HttpResponse;
import com.example.securityjwt.service.UserService;
import com.example.securityjwt.utility.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.example.securityjwt.constant.FileConstant.*;
import static com.example.securityjwt.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;


@RequiredArgsConstructor
@RestController
@RequestMapping(path = {"/","/user"})
@CrossOrigin("http://localhost:4200")
public class UserResource extends ExceptionHandling {

    public static final String USUARIO_ELIMINADO = "Usuario eliminado";
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestBody AppUser user) throws UserNotFoundException, UsernameExistsException, EmailExistsException, MessagingException {

        AppUser userRegistro=userService.registro(user.getFirstName(),user.getLastName(),user.getUsername(),user.getEmail());
        return new ResponseEntity<>(userRegistro, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AppUser> login(@RequestBody AppUser user)  {

        //validacion
        authenticate(user.getUsername(),user.getPassword());
        AppUser loginUser=userService.findUserByUsername(user.getUsername());
        AppUserDetails userDetails=new AppUserDetails(loginUser);
        HttpHeaders jwtHeader=getJwtHeaders(userDetails);

        //body,headers,status
        return new ResponseEntity<>(loginUser,jwtHeader, HttpStatus.OK);
    }

    //add dentro de la aplicacion
    @PostMapping("/add")
    public ResponseEntity<AppUser> addUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") boolean isActive,
                                           @RequestParam("isNotLocked") boolean isNonLocked,
                                           @RequestParam(value="profileImage",required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        //EXPLAIN: la imagen de perfil es opcional
        AppUser usuarioNuevo=userService.addNuevoUser(firstName,lastName,username,email,role,isNonLocked,isActive,profileImage);

        return new ResponseEntity<>(usuarioNuevo,HttpStatus.CREATED);
    }

    @PostMapping("/update")
    public ResponseEntity<AppUser> updateUser(@RequestParam("currentUsername") String currentUsername,
                                            @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNotLocked") String isNonLocked,
                                           @RequestParam(value="profileImage",required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        //EXPLAIN: la imagen de perfil es opcional
        AppUser updatedUser=userService.updateUser(currentUsername,firstName,lastName,username,email,role,Boolean.parseBoolean(isNonLocked),Boolean.parseBoolean(isActive),profileImage);

        return new ResponseEntity<>(updatedUser,HttpStatus.OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<AppUser> findUser(@PathVariable("username") String username){
        AppUser user=userService.findUserByUsername(username);

        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AppUser>> getAllUser(){
        List<AppUser> users=userService.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
        userService.resetPassword(email);
        return response(HttpStatus.OK,"Email enviado a: "+email);
    }

    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> delete(@PathVariable String username) throws IOException {
        //body,headers,status
        userService.deleteUser(username);
        return response(HttpStatus.OK, USUARIO_ELIMINADO);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<AppUser> updateProfileImage(
                                              @RequestParam("username") String username,
                                              @RequestParam(value="profileImage") MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, NotAnImageException {
        //EXPLAIN: la imagen de perfil es opcional
        AppUser user=userService.updateProfileImage(username,profileImage);

        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    //seleccionar la imagen ya creada desde el sistema
    @GetMapping(value = "/image/profile/{username}/{fileName}",produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username,
                                  @PathVariable("fileName") String fileName) throws IOException {

        //user.home/user/Gerson/asd.jpg
        return Files.readAllBytes(Paths.get(USER_FOLDER+username+FORWARD_SLASH+fileName));
    }

    //seleccionar la imagen ya creada cuando no pasan imagenes
    @GetMapping(value = "/image/profile/{username}",produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url=new URL(TEMP_PROFILE_IMAGE_BASE_URL+username+"?set=set4");
        //leer los datos del url
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        try(InputStream inputStream =url.openStream()){
            int bytesRead;
            byte[] chunk=new byte[1024];
            //si los bytes leidos son mayor que 0 se escribe en el output
            while((bytesRead=inputStream.read(chunk))>0){
                byteArrayOutputStream.write(chunk,0,bytesRead);
            }
        }
        //user.home/user/Gerson/asd.jpg
        return byteArrayOutputStream.toByteArray();
    }




    private HttpHeaders getJwtHeaders(AppUserDetails userDetails) {
        HttpHeaders headers=new HttpHeaders();
        //el header no se muestra al frontend por defecto
        headers.add(JWT_TOKEN_HEADER,jwtTokenProvider.generateJWTToken(userDetails));
        return headers;
    }

    private void authenticate(String username, String password) {
        //autenticando en spring security
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus status, String mensaje) {
        HttpResponse httpResponse=new HttpResponse(status.value(),status,status.getReasonPhrase(),mensaje);

        return new ResponseEntity<>(httpResponse,null,status);
    }
}
