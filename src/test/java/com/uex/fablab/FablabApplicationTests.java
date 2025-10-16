package com.uex.fablab;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FablabApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		assertThat(dataSource).isNotNull();
		assertThat(jdbcTemplate).isNotNull();
	}

	@Test
	void databaseConnectionIsHealthy() throws Exception {
		try (var conn = dataSource.getConnection()) {
			assertThat(conn.isValid(2)).isTrue();
		}
	}

	@Test
	void databaseRespondsToSimpleQuery() {
		Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		assertThat(one).isEqualTo(1);
	}

}
