package com.company.approval_system;

import com.company.approval_system.entity.Role;
import com.company.approval_system.entity.User;
import com.company.approval_system.repository.RoleRepository;
import com.company.approval_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class ApprovalSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApprovalSystemApplication.class, args);
	}
	@Bean
	CommandLineRunner initData(UserRepository userRepo,
							   RoleRepository roleRepo,
							   PasswordEncoder encoder) {
		return args -> {

			// ROLES
			if (roleRepo.count() == 0) {
				roleRepo.save(new Role(null, "ADMIN"));
				roleRepo.save(new Role(null, "EMPLOYEE"));
				roleRepo.save(new Role(null, "MANAGER"));
				roleRepo.save(new Role(null, "HR"));
				roleRepo.save(new Role(null, "FINANCE"));
			}

			// ADMIN USER
			if (userRepo.findByEmail("admin@test.com").isEmpty()) {

				Role adminRole =
						roleRepo.findByName("ADMIN")
								.orElseThrow();

				User admin = new User();
				admin.setName("Super Admin");
				admin.setEmail("admin@test.com");
				admin.setPassword(encoder.encode("admin123"));
				admin.setActive(true);
				admin.setRoles(Set.of(adminRole));

				userRepo.save(admin);
			}
		};
	}
}
