package FrenchGuide.repository.mongo;

import FrenchGuide.entity.mongo.Vocabulary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VocabularyRepository extends MongoRepository<Vocabulary, String> {
}
