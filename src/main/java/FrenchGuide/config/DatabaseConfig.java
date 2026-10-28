package FrenchGuide.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackages = "FrenchGuide.entity.postgres")
@EnableJpaRepositories(basePackages = "FrenchGuide.repository.postgres")
@EnableMongoRepositories(basePackages = "FrenchGuide.repository.mongo")
public class DatabaseConfig {
}
