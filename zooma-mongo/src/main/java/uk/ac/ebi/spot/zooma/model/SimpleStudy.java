package uk.ac.ebi.spot.zooma.model;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Document(collection = "studies")
public class SimpleStudy extends AbstractIdentifiableDocument implements Study {
    private String accession;

    public SimpleStudy(String id, String accession) {
        super(id);
        this.accession = accession;
    }

    @Override public String getAccession() {
        return accession;
    }

    @Override public String toString() {
        return "SimpleStudy {\n" +
                "  accession='" + accession + "'\n}";
    }

}
