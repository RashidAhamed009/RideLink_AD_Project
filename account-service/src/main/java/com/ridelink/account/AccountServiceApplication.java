package com.ridelink.account;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.User;
import com.ridelink.account.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AccountServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    /**
     * Seeds an initial administrator account on startup.
     * Profile "!test" ensures slice tests do not require database connections.
     */
    @Bean
    @Profile("!test")
    public CommandLineRunner initAdminAccount(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@ridelink.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User(
                        "System Administrator",
                        adminEmail,
                        passwordEncoder.encode("AdminSecure2026!"),
                        Role.ADMIN
                );
                admin.setAccountStatus(AccountStatus.ACTIVE);
                userRepository.save(admin);
                log.info("Initialized default admin account: {} (Credentials: admin@ridelink.com / AdminSecure2026!)", adminEmail);
            }
        };
    }
}
