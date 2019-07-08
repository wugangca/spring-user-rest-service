package com.example.jpaservices.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.example.jpaservices.controller.UserController;
import com.example.jpaservices.model.User;
import com.example.jpaservices.service.EmailService;
import com.example.jpaservices.service.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserService userService;

	@MockBean
	private EmailService emailService;

	@Test
	public void getAllUsersTest() throws Exception {
		List<User> allUsers = new ArrayList<User>();
		User user1 = new User();
		user1.setUsername("Name1");
		user1.setId(1);
		allUsers.add(user1);

		User user2 = new User();
		user2.setId(2);
		user2.setUsername("Name2");
		allUsers.add(user2);

		BDDMockito.given(userService.getAllUsers()).willReturn(allUsers);

		mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(2)))
				.andExpect(jsonPath("$[0].username", Matchers.is(user1.getUsername())))
				.andExpect(jsonPath("$[1].username", Matchers.is(user2.getUsername())));
	}

	@Test
	public void registerTest() throws Exception {

		User user1 = new User();
		user1.setUsername("wugan");
		user1.setId(1);

		BDDMockito.given(userService.registerUser(user1)).willReturn(true);

		mvc.perform(post("/users/register")
				.content("{\"username\": \"wugan\",  \"email\": \"gang.wu@example.com\", \"password\": \"aaa\"}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

	}

	@Test
	public void confirmTest() throws Exception {
		String token = "token";
		User user1 = new User();
		user1.setUsername("wugan");
		user1.setId(1);
		BDDMockito.given(userService.confirmrUser(token)).willReturn(user1);
		mvc.perform(get("/users/confirm?token=\"token\"")).andExpect(status().isOk());
	}

	@Test
	public void loginTest() throws Exception {
		User user = new User();
		user.setUsername("wugan");
		user.setPassword("wugan");

		User user1 = new User();
		user1.setUsername("wugan");
		user1.setPassword("wugan");
		user1.setRole("ROLE_ADMIN");

		BDDMockito.given(userService.loginUser(user)).willReturn(user1);
		mvc.perform(post("/users/login").content("{\"username\": \"wugan\", \"password\": \"wugan\"}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.username", Matchers.is(user1.getUsername())))
				.andExpect(jsonPath("$.role", Matchers.is(user1.getRole())));

	}

	@Test
	public void resetTest() throws Exception {

		User user = new User();
		user.setUsername("wugan");
		user.setPassword("wugan");

		User user1 = new User();
		user1.setUsername("wugan");
		user1.setPassword("wugan");
		user1.setRole("ROLE_ADMIN");
		
		BDDMockito.given(userService.resetUser(user)).willReturn(user1);

		mvc.perform(post("/users/reset").content("{\"username\": \"wugan\", \"email\": \"gang.wu@example.com\"}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
				
	}
	@Test
	public void changePasswordTest() throws Exception {
		User user = new User();
		user.setUsername("wugan");
		user.setPassword("wugan");

		User user1 = new User();
		user1.setUsername("wugan");
		user1.setPassword("wugan");
		user1.setRole("ROLE_ADMIN");
		
		BDDMockito.given(userService.changeUserPassword(user)).willReturn(user1);

		mvc.perform(post("/users/changepwd").content("{\"username\": \"wugan\", \"email\": \"gang.wu@example.com\"}")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	
}
