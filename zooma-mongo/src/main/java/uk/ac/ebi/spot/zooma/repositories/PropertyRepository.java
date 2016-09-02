package uk.ac.ebi.spot.zooma.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.Property;

/**
 * Created by olgavrou on 09/08/2016.
 */
@Repository
public interface PropertyRepository extends MongoRepository<Property, String> {
}
