package com.example.jpaservices.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.jpaservices.exception.BadRequestException;
import com.example.jpaservices.model.User;
import com.example.jpaservices.repository.UserRepository;
import com.example.jpaservices.service.UserService;

@RunWith(SpringRunner.class)
public class UserServiceTest {

	@TestConfiguration
	static class UserServiceTestContextConfiguration {

		@Bean
		public UserService userService() {
			return new UserService();
		}
	}

	@Autowired
	private UserService userService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PasswordEncoder encoder;

	@Before
	public void setUp() {
		User user = new User();
		user.setId(1);
		user.setUsername("wugan");
		user.setEmail("gang.wu@example.com");
		user.setPassword("password");
		user.setRole("ROLE_USER");
		user.setEnabled(false);
		user.setConfirmationToken("token");

		List<User> users = new ArrayList<User>();
		users.add(user);

		User user1 = new User();
		user1.setId(2);
		user1.setUsername("wugan1");
		user1.setEmail("gang1.wu@example.com");
		user1.setPassword("password1");
		user1.setRole("ROLE_USER");
		user1.setEnabled(true);
		user1.setConfirmationToken("");
		users.add(user1);

		Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
		Mockito.when(userRepository.findByConfirmationToken(user.getConfirmationToken())).thenReturn(user);
		Mockito.when(userRepository.findAll()).thenReturn(users);
		Mockito.when(encoder.matches(user.getPassword(), "password")).thenReturn(true);

		Mockito.when(userRepository.findByUsername(user1.getUsername())).thenReturn(user1);
		Mockito.when(encoder.encode(user.getPassword())).thenReturn("encoded_password");
		Mockito.when(encoder.matches(user1.getPassword(), "password1")).thenReturn(true);
	}

	@Test
	public void getAllUsersTest() {
		List<User> users = userService.getAllUsers();

		assertThat(users.size()).isEqualTo(2);
	}

	@Test
	public void registerUserTest() {

		User user = new User();
		user.setId(1);
		user.setUsername("wugang");
		user.setEmail("gang.wu@java.com");
		user.setPassword("password");
		user.setConfirmationToken("");
		user.setEnabled(true);
		user.setRole("");

		boolean result = userService.registerUser(user);
		assertThat(result).isEqualTo(true);
		assertThat(user.getPassword()).isEqualTo("encoded_password");
		assertThat(user.getRole()).isEqualTo("ROLE_USER");
		assertThat(user.getEnabled()).isEqualTo(false);
		assertThat(user.getConfirmationToken()).isNotEmpty();

		user.setUsername("wugan");
		try {
			result = userService.registerUser(user);
			fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage()).isEqualTo("wugan already registered.");
		}

		user.setUsername("wugang");
		user.setPassword("");
		try {
			result = userService.registerUser(user);
			fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid password.");
		}
	}

	@Test
	public void resetUserTest() {
		User user = new User();
		user.setId(1);
		user.setUsername("wugan");

		user = userService.resetUser(user);
		assertThat(user.getPassword()).isNotEmpty();
		assertThat(user.isTempPassword()).isEqualTo(true);
	}

	@Test
	public void changeUserPasswordTest() {
		User user = new User();
		user.setUsername("wugan1");
		user.setPassword("password1");
		user.setConfirmationToken("newpassword");
		user.setTempPassword(true);

		user = userService.changeUserPassword(user);
		assertThat(user.isTempPassword()).isEqualTo(false);
	}

	@Test
	public void confirmrUserTest() {
		User user = userService.confirmrUser("token");
		assertThat(user.getEnabled()).isEqualTo(true);
		assertThat(user.getConfirmationToken()).isEmpty();
	}

	@Test
	public void loginUserTest() {
		User user = new User();
		user.setUsername("wugan1");
		user.setPassword("password1");
		User loginUser = userService.loginUser(user);
		
		assertThat(loginUser.getEnabled()).isEqualTo(true);
		assertThat(loginUser.getConfirmationToken()).isEmpty();
		assertThat(loginUser.getPassword()).isEqualTo("");
		assertThat(loginUser.getEmail()).isEqualTo("gang1.wu@example.com");
		
		user.setUsername("wugan");
		user.setPassword("password");
		try {
			loginUser = userService.loginUser(user);
			fail();
		} catch (BadRequestException e) {
			assertThat(e.getMessage()).isEqualTo("The user is not enabled.");
		}

	}
}
