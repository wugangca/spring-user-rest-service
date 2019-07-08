package com.example.jpaservices.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jpaservices.exception.BadRequestException;
import com.example.jpaservices.model.User;
import com.example.jpaservices.repository.UserRepository;

@Service
@EnableJpaAuditing

public class UserService {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder encoder;
	
	private static final Random RANDOM = new SecureRandom();
	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	public List<User> getAllUsers() {
		List<User> users = userRepository.findAll();
		for (User user : users) {
			user.setPassword("");
		}
		return users;
	}

	public boolean registerUser(User user) {
		String password = user.getPassword();
		if (password.isEmpty()) {
			throw new BadRequestException("Invalid password.");
		}

		String encodedPassword = encoder.encode(password);
		user.setPassword(encodedPassword);
		if (user.getUsername().isEmpty()) {
			user.setUsername(user.getEmail());
		}

		User userExists = userRepository.findByUsername(user.getUsername());

		if (userExists != null) {
			throw new BadRequestException(user.getUsername() + " already registered.");
		}

		// Disable user until they click on confirmation link in email
		user.setEnabled(false);
		user.setRole("ROLE_USER");

		// Generate random 36-character string token for confirmation link
		user.setConfirmationToken(UUID.randomUUID().toString());

		userRepository.save(user);

		return true;
	}

	public User resetUser(User user) {
		if (user.getUsername().isEmpty()) {
			user.setUsername(user.getEmail());
		}
		User userExists = userRepository.findByUsername(user.getUsername());

		if (userExists == null) {
			throw new BadRequestException(user.getUsername() + " is not registered.");
		}
		
		if (userExists.getEmail().isEmpty()) {
			throw new BadRequestException(user.getUsername() + " does not have a valid email address.");
		}

		String password = generatePassword(10);
		String encodedPassword = encoder.encode(password);
		userExists.setPassword(encodedPassword);
		userExists.setTempPassword(true);
				
		userRepository.save(userExists);
		
		// return the user with plain password so that we can send it to the user's email.
		userExists.setPassword(password); 

		return userExists;
	}
	
	public User changeUserPassword(User user) {
		User userExists = userRepository.findByUsername(user.getUsername());

		if (userExists == null) {
			throw new BadRequestException(user.getUsername() + " is not registered.");
		}
		
		String oldPassword = user.getPassword();
		if (!encoder.matches(oldPassword, userExists.getPassword())) {
			throw new BadRequestException("Invalid current password.");
		}

		if (!userExists.getEnabled()) {
			throw new BadRequestException("The user is not enabled.");
		}
		
		String newPassword = user.getConfirmationToken();
		String encodedPassword = encoder.encode(newPassword);
		userExists.setPassword(encodedPassword);
		userExists.setTempPassword(false);
		
		userRepository.save(userExists);

		userExists.setPassword("");
		userExists.setId(0);
		return userExists;
	}

	public User confirmrUser(String token) {
		User user = userRepository.findByConfirmationToken(token);

		if (user == null) {
			throw new BadRequestException("Invalid token.");
		}
		// Token found
		user.setEnabled(true);
		user.setConfirmationToken("");

		// Save user
		userRepository.save(user);
		return user;
	}

	public User loginUser(User user) {
		User userExists = userRepository.findByUsername(user.getUsername());

		if (userExists == null) {
			throw new BadRequestException("Invalid user name.");
		}

		String password = user.getPassword();
		if (!encoder.matches(password, userExists.getPassword())) {
			throw new BadRequestException("Invalid user name and password combination.");
		}

		if (!userExists.getEnabled()) {
			throw new BadRequestException("The user is not enabled.");
		}

		userExists.setPassword("");
		userExists.setId(0);
		return userExists;
	}

    public static String generatePassword(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }
}
