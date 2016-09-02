package uk.ac.ebi.spot.zooma.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.BiologicalEntity;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Repository
public interface BiologicalEntityRepository extends MongoRepository<BiologicalEntity, String> {
}
