package com.example.ecommerce.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.RoleName;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;

@Configuration
public class DataSeeder {

	@Bean
	CommandLineRunner seedRolesAndUsers(
			RoleRepository roleRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			for (RoleName roleName : RoleName.values()) {
				if (!roleRepository.existsByName(roleName)) {
					roleRepository.save(Role.builder().name(roleName).build());
				}
			}

			seedUserIfMissing(userRepository, roleRepository, passwordEncoder,
					"admin@shop.com", "Admin User", "admin123", RoleName.ADMIN);
			seedUserIfMissing(userRepository, roleRepository, passwordEncoder,
					"customer@shop.com", "Demo Customer", "customer123", RoleName.CUSTOMER);
			seedUserIfMissing(userRepository, roleRepository, passwordEncoder,
					"warehouse@shop.com", "Warehouse Staff", "warehouse123", RoleName.WAREHOUSE_STAFF);
		};
	}

	private void seedUserIfMissing(
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,
			String email,
			String fullName,
			String rawPassword,
			RoleName roleName) {

		if (userRepository.existsByEmail(email)) {
			return;
		}

		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalStateException("Missing role: " + roleName));

		userRepository.save(User.builder()
				.email(email)
				.fullName(fullName)
				.password(passwordEncoder.encode(rawPassword))
				.enabled(true)
				.roles(Set.of(role))
				.build());
	}
}
