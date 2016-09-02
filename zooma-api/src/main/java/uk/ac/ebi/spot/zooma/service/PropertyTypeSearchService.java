package uk.ac.ebi.spot.zooma.service;


import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows searching over the {@link uk.ac.ebi.spot.model.TypedProperty}s known to ZOOMA. This
 * service supports search queries for property types by a string searching algorithm defined by each implementation
 *
 * @author Tony Burdett
 * @date 08/03/12
 */
public interface PropertyTypeSearchService {
    /**
     * Search the set of properties known to ZOOMA to identify those property types that match the supplied pattern
     *
     * @param propertyTypePattern the property type that should be searched for
     * @return a collection of matching property type strings
     */
    Collection<String> search(String propertyTypePattern, URI... sources);

    /**
     * Search the set of properties known to ZOOMA to identify those property types that match the supplied pattern
     * <p/>
     * Results should always match based on an exact prefix match (essentially, {@link String#startsWith(String)}).
     *
     * @param propertyTypePrefix the property value that should be searched for
     * @return a collection of matching property type strings
     */
    Collection<String> searchByPrefix(String propertyTypePrefix, URI... sources);
}