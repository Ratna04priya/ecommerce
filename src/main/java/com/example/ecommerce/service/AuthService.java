package com.example.ecommerce.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.dto.AuthResponse;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.RoleName;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ApiException;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtService;
import com.example.ecommerce.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
			throw new ApiException("Email is already registered", HttpStatus.CONFLICT);
		}

		RoleName roleName = request.getRole() != null ? request.getRole() : RoleName.CUSTOMER;
		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new ApiException("Role not found: " + roleName, HttpStatus.INTERNAL_SERVER_ERROR));

		User user = User.builder()
				.email(request.getEmail().toLowerCase().trim())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName().trim())
				.enabled(true)
				.roles(Set.of(role))
				.build();

		userRepository.save(user);
		return toAuthResponse(user);
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail().toLowerCase().trim(),
						request.getPassword()
				)
		);

		User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
				.orElseThrow(() -> new ApiException("User not found", HttpStatus.UNAUTHORIZED));

		return toAuthResponse(user);
	}

	private AuthResponse toAuthResponse(User user) {
		UserPrincipal principal = new UserPrincipal(user);
		String token = jwtService.generateToken(principal);

		return AuthResponse.builder()
				.token(token)
				.tokenType("Bearer")
				.userId(user.getId())
				.email(user.getEmail())
				.fullName(user.getFullName())
				.roles(user.getRoles().stream()
						.map(role -> role.getName().name())
						.collect(Collectors.toSet()))
				.build();
	}
}
