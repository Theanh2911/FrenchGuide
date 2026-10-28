package FrenchGuide;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import FrenchGuide.entity.postgres.SrsProgress;
import FrenchGuide.entity.postgres.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.sql.init.mode=never"
})
class ApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Test
	void contextLoads() {
	}

	@Test
	void repositoriesUseTheirOwnDatabase() {
		assertEquals(Set.of("userRepository", "srsProgressRepository"),
				Set.copyOf(Arrays.asList(applicationContext.getBeanNamesForType(JpaRepository.class))));
		assertEquals(Set.of("vocabularyRepository", "grammarRepository"),
				Set.copyOf(Arrays.asList(applicationContext.getBeanNamesForType(MongoRepository.class))));
		assertEquals(Set.of(User.class, SrsProgress.class), entityManagerFactory.getMetamodel().getEntities()
				.stream().map(EntityType::getJavaType).collect(Collectors.toSet()));
	}

	@Test
	void postgresConnectionWorks() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			connection.setReadOnly(true);
			try (Statement statement = connection.createStatement()) {
				statement.setQueryTimeout(10);
				try (ResultSet result = statement.executeQuery("SELECT 1")) {
					assertTrue(result.next());
					assertEquals(1, result.getInt(1));
				}
			}
		}
	}

	@Test
	void mongoConnectionWorks() {
		Document result = mongoTemplate.executeCommand(new Document("ping", 1));
		assertEquals(1.0, ((Number) result.get("ok")).doubleValue());
	}

}
