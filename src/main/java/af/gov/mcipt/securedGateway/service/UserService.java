package af.gov.mcipt.securedGateway.service;

import af.gov.mcipt.securedGateway.domain.Role;
import af.gov.mcipt.securedGateway.domain.User;
import af.gov.mcipt.securedGateway.security.AuthoritiesConstants;
import af.gov.mcipt.securedGateway.security.SecurityUtils;
import af.gov.mcipt.securedGateway.service.dto.RegisterRequest;
import af.gov.mcipt.securedGateway.repository.RoleRepository;
import af.gov.mcipt.securedGateway.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;
/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private  UserRepository userRepository;
    
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository=roleRepository;
    }

    // public Optional<User> activateRegistration(String key) {
    //     log.debug("Activating user for activation key {}", key);
    //     return userRepository
    //         .findOneByActivationKey(key)
    //         .map(user -> {
    //             // activate given user for the registration key.
    //             user.setActivated(true);
    //             user.setActivationKey(null);
    //             this.clearUserCaches(user);
    //             log.debug("Activated user: {}", user);
    //             return user;
    //         });
    // }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository
            .findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
            .findOneByEmailIgnoreCase(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                return user;
            });
    }

    public User registerUser(RegisterRequest userDTO, String password) {
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new EmailAlreadyUsedException();
                }
            });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setEmail(userDTO.getEmail().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstname(userDTO.getFirstname());
        newUser.setLastname(userDTO.getLastname());
        newUser.setIsActivated(false);
        // userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

      public User createUser(RegisterRequest userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setResetKey(generateResetKey());
        user.setResetDate(Instant.now());
        user.setIsActivated(false);
        user.setCreatedBy(userDTO.getCreatedBy());
        // if (userDTO.getRoles() != null) {
        //     Set<Role> roles = userDTO.getRoles()
        //     .stream()
        //     .map(role->roleRepository.findById(role.getId()))
        //     .filter(Optional::isPresent)
        //     .map(Optional::get)
        //     .collect(Collectors.toSet());
        //  user.setRoles(roles);
        // }
        user.setRoles(userDTO.getRoles());
        userRepository.save(user);
        return user;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }

 

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    // public Optional<RegisterRequest> updateUser(RegisterRequest userDTO) {
    //     return Optional
    //         .of(userRepository.findOneByEmail(userDTO.getEmail()))
    //         .filter(Optional::isPresent)
    //         .map(Optional::get)
    //         .map(user -> {
    //             this.clearUserCaches(user);
    //             user.setEmail(userDTO.getEmail().toLowerCase());
    //             user.setFirstname(userDTO.getFirstname());
    //             user.setLastname(userDTO.getLastname());
    //             user.setIsActivated(false);
    //             userRepository.save(user);
    //             this.clearUserCaches(user);
    //             log.debug("Changed Information for User: {}", user);
    //             return user;
    //         })
    //         .map(RegisterRequest::new);
    // }

    public void deleteUser(String login) {
        userRepository
            .findOneByEmail(login)
            .ifPresent(user -> {
                userRepository.delete(user);
                log.debug("Deleted User: {}", user);
            });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByEmail)
            .ifPresent(user -> {
                user.setFirstname(firstName);
                user.setLastname(lastName);;
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByEmail)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<User> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // @Transactional(readOnly = true)
    // public Page<User> getAllPublicUsers(Pageable pageable) {
    //     return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable);
    // }

    // @Transactional(readOnly = true)
    // public Optional<User> getUserWithAuthoritiesByLogin(String login) {
    //     return userRepository.findOneWithAuthoritiesByLogin(login);
    // }

    // @Transactional(readOnly = true)
    // public Optional<User> getUserWithAuthorities() {
    //     return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    // }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    // @Scheduled(cron = "0 0 1 * * ?")
    // public void removeNotActivatedUsers() {
    //     userRepository
    //         .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
    //         .forEach(user -> {
    //             log.debug("Deleting not activated user {}", user.getEmail());
    //             userRepository.delete(user);
    //             this.clearUserCaches(user);
    //         });
    // }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    // @Transactional(readOnly = true)
    // public List<String> getAuthorities() {
    //     return authorityRepository.findAll().stream().map(Authority::getName).toList();
    // }

   
        public static String generateResetKey() {
            UUID uuid = UUID.randomUUID();
            String resetKey = uuid.toString().replace("-", "");
            
            if (resetKey.length() > 20) {
                resetKey = resetKey.substring(0, 20);
            }
            
            return resetKey;
        }
    
}
