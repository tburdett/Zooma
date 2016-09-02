package uk.ac.ebi.spot.zooma.model;

import org.springframework.data.annotation.Id;

import java.net.URI;

/**
 * Created by olgavrou on 04/08/2016.
 */
public abstract class AbstractIdentifiableDocument implements Identifiable {
    @Id
    private String id;

    private URI uri;

    public AbstractIdentifiableDocument(URI uri) {

    }

    public URI getUri() {
        return this.uri;
    }
}
