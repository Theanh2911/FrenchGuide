package FrenchGuide.repository.postgres;

import FrenchGuide.entity.postgres.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
