package FrenchGuide.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "vocabulary")
@Getter
@Setter
@NoArgsConstructor
public class Vocabulary {

	@Id
	private String id;
}
