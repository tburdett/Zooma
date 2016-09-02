package uk.ac.ebi.spot.zooma.model;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents any entity that can be uniquely identified with a {@link URI}.  For identifiable objects, the URI is
 * considered to be immutable: implementation must not supply a public <code>setURI()</code> method.
 *
 * @author Tony Burdett
 * @date 13/03/12
 */
public interface Identifiable extends Serializable {
    /**
     * Returns the internal ID of this identifiable object. This will map to the final path of the URI if this
     * identifiable object is identified under the Zooma namespace, but if this identifiable object has a pre-existing
     *
     * @return the internal Id
     */
    String getId();

    /**
     * Returns the uniform resource identifier of this identifiable object.
     *
     * @return the URI of this entity
     */
    URI getUri();
}
