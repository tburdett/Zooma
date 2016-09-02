package uk.ac.ebi.spot.zooma.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.zooma.model.Property;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;

/**
 * Created by olgavrou on 03/08/2016.
 */
public interface AnnotationRepository extends MongoRepository<SimpleAnnotation, String> {

    SimpleAnnotation findByAnnotatedProperty(@Param("annotatedProperty") Property annotatedProperty);

    SimpleAnnotation findById(@Param("Id") String id);

}
