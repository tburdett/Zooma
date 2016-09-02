package uk.ac.ebi.spot.zooma.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.zooma.model.Annotation;
import uk.ac.ebi.spot.zooma.model.Property;

/**
 * Created by olgavrou on 03/08/2016.
 */
public interface AnnotationRepository extends MongoRepository<Annotation, String> {
    Annotation findByAnnotatedProperty(@Param("annotatedProperty") Property annotatedProperty);
}
