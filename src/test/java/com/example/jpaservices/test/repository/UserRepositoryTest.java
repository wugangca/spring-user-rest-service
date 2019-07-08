package com.example.jpaservices.test.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.jpaservices.model.User;
import com.example.jpaservices.repository.UserRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@EnableJpaAuditing
public class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByUserNameTest() {
		// given
		User user = new User();
		user.setUsername("wugan");
		user.setEmail("gang.wu@bhge.com");
		user.setPassword("password");
		user.setRole("ROLE_ADMIN");
		user.setEnabled(true);

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByUsername(user.getUsername());

		// then
		assertThat(found.getUsername()).isEqualTo(user.getUsername());

		found = userRepository.findByUsername("N/A");
		assertThat(found).isEqualTo(null);
	}
	
	@Test
	public void findByComfirmationTokenTest() {
		// given
		User user = new User();
		user.setUsername("wugan");
		user.setEmail("gang.wu@bhge.com");
		user.setPassword("password");
		user.setRole("ROLE_USER");
		user.setEnabled(false);
		user.setConfirmationToken("token");

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByConfirmationToken(user.getConfirmationToken());

		// then
		assertThat(found.getConfirmationToken()).isEqualTo(user.getConfirmationToken());

		found = userRepository.findByConfirmationToken("N/A");
		assertThat(found).isEqualTo(null);
	}

}