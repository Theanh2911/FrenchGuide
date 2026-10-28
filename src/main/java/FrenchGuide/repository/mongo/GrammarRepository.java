package FrenchGuide.repository.mongo;

import FrenchGuide.entity.mongo.Grammar;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GrammarRepository extends MongoRepository<Grammar, String> {
}
