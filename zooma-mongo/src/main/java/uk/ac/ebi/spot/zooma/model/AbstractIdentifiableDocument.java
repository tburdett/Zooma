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

    AbstractIdentifiableDocument() {
        // uri will be assigned from id
    }

    AbstractIdentifiableDocument(String id) {
        this.id = id;
    }

    AbstractIdentifiableDocument(String id, URI uri) {
        this(id);
        this.uri = uri;
    }

    @Override public String getId() {
        return id;
    }

    @Override public URI getUri() {
        return this.uri;
    }
}
