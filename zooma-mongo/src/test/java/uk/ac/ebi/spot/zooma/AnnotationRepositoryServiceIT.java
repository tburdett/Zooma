package uk.ac.ebi.spot.zooma;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.zooma.model.*;
import uk.ac.ebi.spot.zooma.config.MongoConfig;
import uk.ac.ebi.spot.zooma.repositories.AnnotationRepository;
import uk.ac.ebi.spot.zooma.repositories.BiologicalEntityRepository;
import uk.ac.ebi.spot.zooma.repositories.PropertyRepository;
import uk.ac.ebi.spot.zooma.repositories.StudyRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Created by olgavrou on 04/08/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MongoConfig.class)
public class AnnotationRepositoryServiceIT {

    @Autowired
    AnnotationRepository annotationRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    BiologicalEntityRepository biologicalEntityRepository;

    @Autowired
    StudyRepository studyRepository;

    @Before
    public void setup(){
        //Create an Annotation and store it in mongodb

        SimpleStudy simpleStudy = new SimpleStudy("SS1", "Accession1");

        Collection<Study> studies = new ArrayList<>();
        studies.add(simpleStudy);

        simpleStudy = new SimpleStudy("SS2", "Accession2");
        studies.add(simpleStudy);

        Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
        SimpleBiologicalEntity biologicalEntity = new SimpleBiologicalEntity("BE1", "GSM374548", studies);
        biologicalEntities.add(biologicalEntity);
        biologicalEntity = new SimpleBiologicalEntity("BE2", "newEntity", studies);
        biologicalEntities.add(biologicalEntity);

        Property property = new SimpleTypedProperty("TestProperty", "disease", "lung cancer");
        URI semanticTag = java.net.URI.create("http://www.ebi.ac.uk/efo/EFO_0001071");
        Collection<URI> semanticTags = new ArrayList<>();
        semanticTags.add(semanticTag);

        //create provenance
        SimpleOntologyAnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create("http://www.ebi.ac.uk/gxa"), "atlas","","");

        SimpleAnnotationProvenance annotationProvenance = new SimpleAnnotationProvenance(annotationSource,
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                "http://www.ebi.ac.uk/gxa", new Date(), "Laura Huerta", new Date());

        SimpleAnnotation annotationDocument = new SimpleAnnotation("TestStringId", biologicalEntities,
                property,
                semanticTags,
                annotationProvenance,
                null,
                null);

        annotationRepository.save(annotationDocument);
    }

    @After
    public void teardown(){

        //remove property
        propertyRepository.delete(propertyRepository.findOne("TestProperty"));

        //remove studies
        studyRepository.delete(studyRepository.findOne("SS1"));
        studyRepository.delete(studyRepository.findOne("SS2"));

        //remove biological entities
        biologicalEntityRepository.delete(biologicalEntityRepository.findOne("BE1"));
        biologicalEntityRepository.delete(biologicalEntityRepository.findOne("BE2"));

        //remove the annotation from the database
        SimpleAnnotation annotationDocument = annotationRepository.findOne("TestStringId");

        annotationRepository.delete(annotationDocument);

        assertNull(annotationRepository.findOne(annotationDocument.getId()));
    }

    @Test
    public void testGetAllDocuments() throws Exception {
        List<SimpleAnnotation> annotationDocumentList = annotationRepository.findAll();
        Collection<URI> sem = annotationDocumentList.get(0).getSemanticTags();
        assertThat("Not empty list", annotationDocumentList.size(), is(not(0)));
    }

    @Test
    public void testUpdate() throws Exception {
        SimpleAnnotation annotationDocument = annotationRepository.findOne("TestStringId");
        annotationDocument.setAnnotatedProperty(new SimpleTypedProperty("TestProperty", "New Parameter", "New Value"));
        annotationRepository.save(annotationDocument);

        annotationDocument = annotationRepository.findOne("TestStringId");

        assertThat("Value is New Value", annotationDocument.getAnnotatedProperty().getPropertyValue(), is("New Value"));
    }

    @Test
    public void testGetByAnnotatedProperty() throws Exception {
        Property property = new SimpleTypedProperty("TestProperty", "disease", "lung cancer");
        SimpleAnnotation annotationDocument = annotationRepository.findByAnnotatedProperty(property);

        assertThat("The Id is TestStringId", annotationDocument.getId(), is("TestStringId"));
    }

}