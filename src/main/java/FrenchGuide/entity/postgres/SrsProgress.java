package FrenchGuide.entity.postgres;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "srs_progress")
@Getter
@Setter
@NoArgsConstructor
public class SrsProgress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
}
