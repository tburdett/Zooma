package uk.ac.ebi.spot.zooma.model;

import java.net.URI;

/**
 * Created by olgavrou on 05/08/2016.
 */
public class SimpleDatabaseAnnotationSource extends SimpleAnnotationSource {
    public SimpleDatabaseAnnotationSource(URI source, String name) {
        super(source, name, Type.DATABASE);
    }
}
