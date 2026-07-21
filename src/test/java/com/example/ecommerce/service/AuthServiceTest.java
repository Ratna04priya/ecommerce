package com.example.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.RoleName;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@Mock
	private AuthenticationManager authenticationManager;

	@InjectMocks
	private AuthService authService;

	@Test
	void register_createsCustomerByDefault() {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("new@shop.com");
		request.setPassword("secret12");
		request.setFullName("New User");

		Role customerRole = Role.builder().id(2L).name(RoleName.CUSTOMER).build();

		when(userRepository.existsByEmail("new@shop.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
		when(passwordEncoder.encode("secret12")).thenReturn("encoded");
		when(userRepository.save(any(User.class))).thenAnswer(inv -> {
			User user = inv.getArgument(0);
			user.setId(50L);
			return user;
		});
		when(jwtService.generateToken(any())).thenReturn("jwt-token");

		AuthResponse response = authService.register(request);

		assertThat(response.getToken()).isEqualTo("jwt-token");
		assertThat(response.getEmail()).isEqualTo("new@shop.com");
		assertThat(response.getRoles()).contains("CUSTOMER");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void register_duplicateEmail_throwsConflict() {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("admin@shop.com");
		request.setPassword("secret12");
		request.setFullName("Admin");

		when(userRepository.existsByEmail("admin@shop.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("already registered");
	}
}
