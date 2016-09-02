package uk.ac.ebi.spot.zooma.util;

import java.net.URI;

/**
 * This class returns primary labels for any given URI in the knowledgebase
 *
 * @author Simon Jupp
 * @date 08/11/2012 Functional Genomics Group EMBL-EBI
 */
public class LabelUtils {
    private final OntologyUtils ontologyUtils;

    public LabelUtils(OntologyUtils ontologyUtils) {
        this.ontologyUtils = ontologyUtils;
    }

    public String getPrimaryLabel(URI concept) {
        if (ontologyUtils != null) {
            return ontologyUtils.getSemanticTagLabel(concept);
        }
        else {
            throw new IllegalStateException("Unable to perform label lookup - no OntologyDAO has been set");
        }
    }
}
