package FrenchGuide.repository.postgres;

import FrenchGuide.entity.postgres.SrsProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SrsProgressRepository extends JpaRepository<SrsProgress, Long> {
}
