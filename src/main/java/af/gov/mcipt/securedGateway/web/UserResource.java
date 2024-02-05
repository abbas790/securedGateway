package af.gov.mcipt.securedGateway.web;

import af.gov.mcipt.securedGateway.domain.User;
import af.gov.mcipt.securedGateway.repository.UserRepository;
import af.gov.mcipt.securedGateway.security.AuthoritiesConstants;
import af.gov.mcipt.securedGateway.service.EmailAlreadyUsedException;
import af.gov.mcipt.securedGateway.service.UserService;
import af.gov.mcipt.securedGateway.service.dto.RegisterRequest;
import af.gov.mcipt.securedGateway.web.errors.BadRequestAlertException;
import af.gov.mcipt.securedGateway.web.errors.LoginAlreadyUsedException;
import jakarta.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/admin")
public class UserResource {


    // private final Logger log = LoggerFactory.getLogger(UserResource.class);


    @Autowired
     UserService userService;
    @Autowired
    UserRepository userRepository;


    @PostMapping("/registerUser")
    public ResponseEntity<User> createUser(@RequestBody RegisterRequest userData){
        if (userRepository.findOneByEmailIgnoreCase(userData.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else {
            User newUser = userService.createUser(userData);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }
    }

 

    // @PostMapping("/users")
    // public ResponseEntity<RegisterRequest> createUser(@RequestBody RegisterRequest userDTO) throws URISyntaxException {
        // return new ResponseEntity<>(userDTO, null);
        // if (userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
        //     throw new EmailAlreadyUsedException();
        // } else {
        //     // User newUser = userService.createUser(userDTO);
        //     // mailService.sendCreationEmail(newUser);
        //     // return ResponseEntity
        //     //     .created(new URI("/api/admin/users/" + newUser.getLogin()))
        //     //     .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", newUser.getLogin()))
        //     //     .body(newUser);
        //     return new ResponseEntity<>(userDTO, null);
        // }
    // }

 
    // @GetMapping("/users")
    // @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // public ResponseEntity<List<User>> getAllUsers(Pageable pageable) {
    //     log.debug("REST request to get all User for an admin");
    //     if (!onlyContainsAllowedProperties(pageable)) {
    //         return ResponseEntity.badRequest().build();
    //     }

    //     final Page<User> page = userService.getAllManagedUsers(pageable);
    //     HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
    //     return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    // }

    // private boolean onlyContainsAllowedProperties(Pageable pageable) {
    //     return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    // }


    // @GetMapping("/users/{login}")
    // @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // public ResponseEntity<User> getUser(@PathVariable  String email) {
    //     log.debug("REST request to get User : {}", email);
    //     return ResponseUtil.wrapOrNotFound(userService.getUserWithAuthoritiesByEmail(email).map(User::new));
    // }

    // @DeleteMapping("/users/{login}")
    // @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    // public ResponseEntity<Void> deleteUser(@PathVariable  String email) {
    //     log.debug("REST request to delete User: {}", email);
    //     userService.deleteUser(email);
    //     return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", email)).build();
    // }
}
