package uk.ac.ebi.spot.zooma.util;

import uk.ac.ebi.spot.zooma.datasource.OntologyDAO;

import java.net.URI;

/**
 * This class returns primary labels for any given URI in the knowledgebase
 *
 * @author Simon Jupp
 * @date 08/11/2012 Functional Genomics Group EMBL-EBI
 */
public class LabelUtils {
    private static OntologyDAO ontologyDAO;

    public OntologyDAO getOntologyDAO() {
        return ontologyDAO;
    }

    public void setOntologyDAO(OntologyDAO ontologyDAO) {
        LabelUtils.ontologyDAO = ontologyDAO;
    }

    public static String getPrimaryLabel(URI concept) {
        if (ontologyDAO != null) {
            return ontologyDAO.getSemanticTagLabel(concept);
        }
        else {
            throw new IllegalStateException("Unable to perform label lookup - no OntologyDAO has been set");
        }
    }
}
