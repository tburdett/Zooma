package uk.ac.ebi.spot.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A collection of cascade for working with URIs in ZOOMA.  Some common utilities to expand/contract between prefixed
 * ("qName" style) forms and the full URI are included.
 * <p/>
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 30/05/12
 */
public class URIUtils {
    /**
     * The default setting for PREFIX_CREATION_MODE
     */
    public static final PrefixCreationMode DEFAULT_PREFIX_CREATION_MODE = PrefixCreationMode.DO_NOT_CREATE;
    /**
     * The default setting for SHORTFORM_STRICTNESS
     */
    public static final ShortformStrictness DEFAULT_SHORTFORM_STRICTNESS = ShortformStrictness.ALLOW_HASHES;

    private static final Map<String, String> prefixMappings;
    private static final String uninitializedKey = "UNINITIALIZED";

    /*
     * URI that indicates that we don't want the source type associated with this URI to be searched for the query term
     */
    public static final URI SEARCH_NONE = URI.create("None");

    static {
        // initialize final map prefixMappings...
        prefixMappings = Collections.synchronizedMap(new HashMap<String, String>());
        // ...and add a key to indicate it has not yet been loaded
        prefixMappings.put(uninitializedKey, "true");
    }

    private static Logger log = LoggerFactory.getLogger(URIUtils.class);

    protected static Logger getLog() {
        return log;
    }

    /**
     * Loads prefix to namespace mappings into a map, and returns them.  Mappings are loaded from the prefix.properties
     * file, which should be located under config/naming/prefix.properties in your environments $ZOOMA_HOME directory.
     *
     * @return a mapping of prefix to namespace values
     */
    public synchronized static Map<String, String> getPrefixMappings() {
        // if prefixMappings contains the key that indicates it has never been loaded, then load
        if (prefixMappings.containsKey(uninitializedKey)) {
            loadPrefixMappings();
        }
        return prefixMappings;
    }

    public synchronized static Map<String, String> loadPrefixMappings() {
        getLog().debug("Attempting to load prefix mappings from properties files...");
        prefixMappings.clear();
        try {
            String zooma_home = System.getProperty("zooma.home");
            if (zooma_home != null) {
                File prefixPropertyFile = FileSystems.getDefault()
                        .getPath(zooma_home, "config", "naming", "prefix.properties")
                        .toFile();
                if (prefixPropertyFile.exists()) {
                    Properties prefixProperties = new Properties();
                    prefixProperties.load(new FileInputStream(prefixPropertyFile));
                    for (String prefix : prefixProperties.stringPropertyNames()) {
                        String namespace = prefixProperties.getProperty(prefix);
                        getLog().debug("Next prefix mapping: " + prefix + " = " + namespace);
                        prefixMappings.put(prefix, namespace);
                    }
                }
                else {
                    getLog().warn("Could not locate prefix mappings (no prefix.properties file " +
                                          "at " + prefixPropertyFile.getAbsolutePath() + "); " +
                                          "prefix mappings will be empty");
                }
            }
            else {
                getLog().warn("Could not locate prefix mappings (no ZOOMA home directory set); " +
                                      "prefix mappings will be empty");
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to read zooma prefix properties", e);
        }
        return prefixMappings;
    }

    /**
     * Gets the shortened version of the given URI, using the the mappings (prefix = "namespace") in the file
     * zooma/prefix.properties if available on the classpath. This is equivalent to calling {@link
     * #getShortform(Map, URI, ShortformStrictness, PrefixCreationMode)} with the results of {@link
     * #getPrefixMappings()} as the first parameter and DEFAULT_SHORTFORM_STRICTNESS as the third parameter.
     *
     * @param uri the URI to find the shortform for
     * @return the shortened, qualified name
     */
    public static String getShortform(URI uri) {
        return getShortform(getPrefixMappings(), uri, DEFAULT_SHORTFORM_STRICTNESS, DEFAULT_PREFIX_CREATION_MODE);
    }

    /**
     * Gets the shortened version of the given URI, using the supplied shortform strictness parameter and the mappings
     * (prefix = "namespace") in the file zooma/prefix.properties if available on the classpath. This is equivalent to
     * calling {@link #getPrefixMappings()} and passing the results to {@link #getShortform(Map, URI,
     * ShortformStrictness, PrefixCreationMode)} as the first parameter.
     *
     * @param uri        the URI to find the shortform for
     * @param strictness how strict to be when creating a shortform of the given URI
     * @return the shortened, qualified name
     */
    public static String getShortform(URI uri, ShortformStrictness strictness) {
        return getShortform(getPrefixMappings(), uri, strictness, DEFAULT_PREFIX_CREATION_MODE);
    }

    /**
     * Gets the shortened version of the given URI, using the supplied shortform strictness and prefix creation mode
     * parameters, and the mappings (prefix = "namespace") in the file zooma/prefix.properties if available on the
     * classpath. This is equivalent to calling {@link #getPrefixMappings()} and passing the results to {@link
     * #getShortform(Map, URI, ShortformStrictness, PrefixCreationMode)} as the first parameter.
     *
     * @param uri        the URI to find the shortform for
     * @param strictness how strict to be when creating a shortform of the given URI
     * @return the shortened, qualified name
     */
    public static String getShortform(URI uri, ShortformStrictness strictness, PrefixCreationMode prefixCreationMode) {
        return getShortform(getPrefixMappings(), uri, strictness, prefixCreationMode);
    }

    /**
     * Gets the shortened version of the given URI, using the supplied prefix mappings.  Using this form of the method,
     * you can supply your own mappings (and develop your own caching strategies) if required, but the default shortform
     * strictness is used.
     *
     * @param prefixMappings the prefix mappings to consider when getting the short form
     * @param uri            the URI to find the short form for
     * @return the qualified short name
     * @throws IllegalArgumentException if the URI cannot be shortened using the current mode and prefixMappings
     */
    public static String getShortform(final Map<String, String> prefixMappings, URI uri) {
        return getShortform(prefixMappings, uri, DEFAULT_SHORTFORM_STRICTNESS, DEFAULT_PREFIX_CREATION_MODE);
    }

    /**
     * Gets the shortened version of the given URI, using the supplied prefix mappings, strictness and prefix creation
     * mode.  Using this form of the method, you can supply your own mappings (and develop your own caching strategies)
     * if required.
     *
     * @param prefixMappings the prefix mappings to consider when getting the short form
     * @param uri            the URI to find the short form for
     * @return the qualified short name
     * @throws IllegalArgumentException if the URI cannot be shortened using the current mode and prefixMappings
     */
    public static String getShortform(final Map<String, String> prefixMappings,
                                      URI uri,
                                      ShortformStrictness strictness,
                                      PrefixCreationMode prefixCreationMode) {
        if (uri == null) {
            return null;
        }

        getLog().trace("Attempting to extract shortened form of URI '" + uri + "'");
        uri = normalizeURI(uri);
        String uriStr = uri.toString();
        if (uri.getPath().isEmpty()) {
            throw new IllegalArgumentException("URI '" + uri.toString() + "' contains only a domain, " +
                                                       "and cannot therefore be shortened");
        }

        // parts of the uri/shortform
        synchronized (prefixMappings) {
            String prefix = getPrefix(prefixMappings, uri, "/", "#");
            String namespace;
            if (prefix == null) {
                // null if no prefix that results in a "good" shortform can be found - test prefixing mode
                getLog().trace("Failed to identify best quality shortform from prefix mappings. " +
                                       "Trying again after assessing prefix creation mode and shortform strictness");
                // test prefix creation mode
                String bestPrefix;
                String[] result;
                switch (prefixCreationMode) {
                    case CREATE:
                        // get prefix for longest possible namespace match, if any
                        bestPrefix = getPrefix(prefixMappings, uri);
                        // use incremental strategy but do NOT cache
                        result = createPrefixNamespaceMapping(prefixMappings, bestPrefix, false, uri);
                        prefix = result[0];
                        namespace = result[1];
                        getLog().trace("Created new prefix/namespace mapping for <" + uriStr + "> " +
                                               "(" + prefix + " -> " + namespace + "). " +
                                               "This mapping will NOT be cached, though");
                        break;
                    case CREATE_AND_CACHE:
                        // get prefix for longest possible namespace match, if any
                        bestPrefix = getPrefix(prefixMappings, uri);
                        // use incremental strategy and cache
                        result = createPrefixNamespaceMapping(prefixMappings, bestPrefix, true, uri);
                        prefix = result[0];
                        namespace = result[1];
                        getLog().trace("Created new prefix/namespace mapping for <" + uriStr + "> " +
                                               "(" + prefix + " -> " + namespace + ")");
                        break;
                    case DO_NOT_CREATE:
                        // test strictness
                        switch (strictness) {
                            case STRICT:
                                // can't do anything
                                throw new IllegalArgumentException(
                                        "The URI <" + uri.toString() + "> cannot be shortened " +
                                                "using any known prefix, and the creation mode is " +
                                                "set to NOT create new prefixes.");
                            case ALLOW_HASHES:
                                // try to get prefix, but allow hashes in the shortform
                                prefix = getPrefix(prefixMappings, uri, "/");
                                namespace = prefixMappings.get(prefix);
                                getLog().trace("Got prefix for <" + uriStr + "> by using non-standard shortform " +
                                                       "(allows hashes)");
                                break;
                            case ALLOW_SLASHES_AND_HASHES:
                                // try to get prefix, but allow hashes and slashes in the shortform
                                prefix = getPrefix(prefixMappings, uri);
                                namespace = prefixMappings.get(prefix);
                                getLog().trace("Got prefix for <" + uriStr + "> by using non-standard shortform " +
                                                       "(allows and/or slashes)");
                                break;
                            default:
                                // not sure what to do!
                                throw new IllegalArgumentException(
                                        "The URI <" + uri.toString() + "> cannot be shortened " +
                                                "using any known prefix, and the shortform strictness level is " +
                                                "unknown.");
                        }
                        break;
                    default:
                        // not sure what to do!
                        throw new IllegalArgumentException("The URI <" + uri.toString() + "> cannot be shortened " +
                                                                   "using any known prefix, and the creation mode is " +
                                                                   "unknown.");
                }
            }
            else {
                namespace = prefixMappings.get(prefix);
                if (namespace == null && prefix.contains("resource")) {
                    String resourcename = prefix.replace("resource", "");
                    namespace = prefixMappings.get("zoomaresource").concat(resourcename).concat("/");
                }
            }

            if (prefix != null && namespace != null) {
                String localname = uriStr.replaceAll(namespace, "");
                if (!localname.equals(uriStr)) {
                    String shortform = prefix + ":" + localname;
                    getLog().trace("Shortform: '" + uriStr + "' -> '" + shortform + "'");
                    return shortform;
                }
                else {
                    throw new RuntimeException("Shortening the URI didn't seem to work (was " + uriStr + ", " +
                                                       "became " + prefix + ":" + uriStr.replaceAll(namespace, ""));
                }
            }
            else {
                String msg = "The URI <" + uri.toString() + "> cannot be shortened using any known prefix " +
                        "or creation strategy";
                getLog().error(msg);
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * Gets the full URI of the given shortened form, first looking for any declared prefix mappings on the classpath.
     * This is equivalent to calling {@link #getPrefixMappings()} and passing the results to {@link
     * #getURI(Map, String)} as the first parameter.
     *
     * @param shortform the shortform to find the URI for
     * @return the full URI
     */
    public static URI getURI(String shortform) {
        return getURI(getPrefixMappings(), shortform);
    }

    /**
     * Gets the full URI of the given shortened form. If the file zooma/prefix.properties is located on the classpath,
     * the mappings (prefix = "namespace") in this file will be used.  Note that the declared properties are not cached,
     * so can be updated and used in real time.
     * <p/>
     * It is not always possible to reliably extract the full URI from a shortform; in shortening it is possible to have
     * multiple namespaces mapped to the same prefix.  In this situation, this method throws an illegal argument
     * exception, signifying that the provided shortform is not unique.  It is the responsibility of the developer to
     * ensure that a ZOOMA instance is adequately configured to prevent namespace collisions.
     *
     * @param prefixMappings the prefix mappings to consider when getting the short form
     * @param shortform      the shortform to find the URI for
     * @return the full URI
     */
    public static URI getURI(final Map<String, String> prefixMappings, String shortform) {
        // is this a CHEBI ID?  This is an ungodly hack to work with broken CHEBI IDs, remove if possible
        if (shortform.startsWith("CHEBI:")) {
            return URI.create("http://www.ebi.ac.uk/chebi/searchId.do;?chebiId=" + shortform);
        }

        // otherwise, do we have a registered prefix?
        if (shortform.contains(":")) {
            String[] tokens = shortform.split(":");
            String prefix = tokens[0];
            String localName = "";
            if (tokens.length > 1) {
                localName = tokens[1];
            }

            synchronized (prefixMappings) {
                String namespace;
                if (prefixMappings.containsKey(prefix)) {
                    namespace = prefixMappings.get(prefix);
                    if (!namespace.endsWith("/") && !namespace.endsWith("#") && !"".equals(localName)) {
                        // no separator at end of namespace - could be / or # or something else, so just have to guess
                        namespace = namespace + "/";
                    }
                }
                else if (prefix.endsWith("resource")) {
                    String resourceName = prefix.replace("resource", "");
                    if (prefixMappings.containsKey(resourceName)) {
                        namespace = prefixMappings.get("zoomaresource").concat(resourceName).concat("/");
                    }
                    else {
                        throw new IllegalArgumentException("Unknown resource '" + resourceName + "' - it is not " +
                                                                   "possible to reconstruct this URI");
                    }
                }
                else {
                    // if we get to here, we cannot resolve prefix
                    throw new IllegalArgumentException("Unknown prefix '" + prefix + "' - it is not " +
                                                               "possible to reconstruct this URI");
                }
                return URI.create(namespace + localName);
            }
        }
        else {
            throw new IllegalArgumentException("Cannot expand '" + shortform + "' - " +
                                                       "this does not appear to be an prefixed URI (No ':' present)");
        }
    }

    /**
     * Returns the full URI of the namespace associated with this shortform.  The shortform supplied should be of the
     * form "foo:bar" where "foo" represents the prefixed form of the namespace, and "bar" represents the term fragment.
     * In this case, foo is expanded to it's full namespace and returned.  This method uses the default prefix mappings
     * ({@link #getPrefixMappings()}).
     *
     * @param shortform the shortform to extract the namespace for
     * @return the full URI of the namespace for the prefix in the shortform
     */
    public static URI getNamespace(String shortform) {
        return getNamespace(getPrefixMappings(), shortform);
    }

    /**
     * Returns the full URI of the namespace associated with this shortform.  The shortform supplied should be of the
     * form "foo:bar" where "foo" represents the prefixed form of the namespace, and "bar" represents the term fragment.
     * In this case, foo is expanded to it's full namespace and returned.
     *
     * @param prefixMappings the mappings from prefix to shortform
     * @param shortform      the shortform to extract the namespace for
     * @return the full URI of the namespace for the prefix in the shortform
     */
    public static URI getNamespace(final Map<String, String> prefixMappings, String shortform) {
        String prefix = shortform.split(":")[0];
        synchronized (prefixMappings) {
            if (prefixMappings.containsKey(prefix)) {
                String namespace = prefixMappings.get(prefix);
                if (!namespace.endsWith("/") && !namespace.endsWith("#")) {
                    // no separator at end of namespace - could be / or # or something else, so just have to guess
                    namespace = namespace + "/";
                }
                return URI.create(namespace);
            }
            else {
                // we can't resolve the prfix - but we might be able to infer it if it's a resource
                if (prefix.contains("resource")) {
                    String resourcename = prefix.replace("resource", "");
                    return URI.create(prefixMappings.get("zoomaresource").concat(resourcename).concat("/"));
                }
                else {
                    throw new IllegalArgumentException("Unknown prefix '" + prefix + "' - it is not " +
                                                               "possible to reconstruct this URI");
                }
            }
        }
    }

    /**
     * Returns the fragment of the URI represented by this shortform.  The shortform supplied should be of the form
     * "foo:bar" where "foo" represents the prefixed form of the namespace, and "bar" represents the term fragment.  In
     * this case, this method will simply return "bar"
     *
     * @param shortform the shortform to extract the fragment for
     * @return the fragment name of this shortform
     */
    public static String getFragment(String shortform) {
        return shortform.split(":")[1];
    }

    /**
     * Returns the sort-of namespace of the supplied URI, stripping out everything except the final URI fragment (if
     * present) of else the final part of the path.  This is used to extract the part of the URI that is commonly used
     * in the ontology world as the namespace.  The most commonly used schemes are "http://my.ontology.org/ontology/name"
     * or "http://my.ontology.org/ontology#name", and this method considers the namespace in both cases to be
     * ""http://my.ontology.org/ontology".
     *
     * @param uri the uri to extract the namespace from
     * @return the resulting namespace
     */
    public static URI extractNamespace(URI uri) {
        String uriStr = normalizeURI(uri).toString();
        String namespace = uriStr.contains("#")
                ? uriStr.substring(0, uriStr.indexOf('#') + 1)
                : uriStr.substring(0, uriStr.lastIndexOf('/') + 1);
        return URI.create(namespace);
    }

    /**
     * Returns the sort-of fragment of the supplied URI, stripping it down to just the URI fragment (if present) or else
     * the final part of the path.  This is to extract the commonly used identifier names of a URI for concepts within
     * an ontology.  The most commonly used schemes are "http://my.ontology.org/ontology/name" or
     * "http://my.ontology.org/ontology#name", but this method also captures the older naming convention used in CHEBI
     * (and similar) where the name is of the form "http://my.ontology.org/ontology/search?id=name"
     *
     * @param uri the URI to shorten
     * @return the fragment name of the supplied concept
     */
    public static String extractFragment(URI uri) {
        // convert uri to string
        getLog().trace("Attempting to extract fragment name of URI '" + uri + "'");
        String termURI = uri.toString();

        String fragmentName;

        // chebi uris, everything after last '='
        if (termURI.contains("http://www.ebi.ac.uk/chebi/searchId")) {
            getLog().trace("Extracting fragment name using CHEBI rule");
            fragmentName = termURI.substring(termURI.lastIndexOf("=") + 1);
        }
        else {
            // if it's something other than chebi, we want the real final part of the URI...
            if (uri.getFragment() != null) {
                // a uri with a non-null fragment, so use this...
                getLog().trace("Extracting fragment name using URI fragment (" + uri.getFragment() + ")");
                fragmentName = uri.getFragment();
            }
            else if (uri.getPath() != null) {
                // no fragment, but there is a path so try and extract the final part...
                if (uri.getPath().contains("/")) {
                    getLog().trace("Extracting fragment name using final part of the path of the URI");
                    fragmentName = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
                }
                else {
                    // no final path part, so just return whole path
                    getLog().trace("Extracting fragment name using the path of the URI");
                    fragmentName = uri.getPath();
                }
            }
            else {
                // not a chebi id, no fragment, path is null, we've run out of rules so don't shorten
                getLog().trace("No rules to shorten this URI could be found (" + termURI + ")");
                fragmentName = termURI;
            }
        }

        getLog().trace("URI: " + uri + " -> fragment name: " + fragmentName);
        return fragmentName;
    }

    /**
     * Performs a lookup against known prefix mappings for the given URI, and returns true if a prefix mapping for this
     * URI already exists.  False is returned if there is no known prefix mapping for this URI (in other words,
     * <code>getPrefixMappings().containsValue(uri);</code>
     *
     * @param uri the URI to lookup prefix mappings for
     * @return true if a prefix mapping for this namespace exists, false otherwise
     */
    public static boolean isNamespaceKnown(URI uri) {
        return getPrefixMappings().containsValue(uri.toString()) ||
                getPrefixMappings().containsValue(normalizeURI(uri).toString());
    }

    public static URI normalizeURI(String uriStr) {
        if (uriStr.endsWith("#") || uriStr.endsWith("/")) {
            uriStr = uriStr.substring(0, uriStr.length() - 1);
        }
        return URI.create(uriStr);
    }

    public static URI normalizeURI(URI uri) {
        return normalizeURI(uri.toString());
    }

    /**
     * Generates a shortform given some known prefixes.  If any of the character sequences present in
     * <code>excludedChars</code> occur in the shortened form, this shortened form is rejected.
     *
     * @param prefixMappings the prefix mappings to consider when getting the short form
     * @param uri            the URI to find the short form for
     * @param excludedChars  a variable argument of the characters that are not allowed to occur in the shortform
     * @return the best matching prefix for the given URI
     * @throws IllegalArgumentException if the URI cannot be shortened using the current mode and prefixMappings
     */
    private static String getPrefix(final Map<String, String> prefixMappings,
                                    URI uri,
                                    CharSequence... excludedChars) {
        String uriStr = uri.toString();
        String namespace = null;
        String prefix = null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        sb.append("[");
        while (i < excludedChars.length) {
            sb.append("'").append(excludedChars[i++]).append("'");
            if (i < excludedChars.length) {
                sb.append(", ");
            }
        }
        sb.append("]");
        String disallowedStr = sb.toString();

        for (String nextPrefix : prefixMappings.keySet()) {
            String nextNamespace = prefixMappings.get(nextPrefix);

            // does this prefix map to our URI?
            if (uriStr.contains(nextNamespace)) {
                if (namespace == null || namespace.length() < nextNamespace.length()) {
                    // we have a match, but is it complete?
                    String localname = uriStr.replaceAll(nextNamespace, "");
                    boolean ineligible = false;
                    for (CharSequence cs : excludedChars) {
                        if (localname.contains(cs)) {
                            getLog().trace("Namespace matched uri '" + uriStr + "'," +
                                                   " but would result in a disallowed shortform " +
                                                   "'" + nextPrefix + ":" + localname + "' (includes " +
                                                   "one of " + disallowedStr + ")");
                            ineligible = true;
                            break;
                        }
                    }

                    if (!ineligible) {
                        prefix = nextPrefix;
                        namespace = nextNamespace;
                    }
                    else if (nextPrefix.equals("zoomaresource")) {
                        // there is an exception - if prefix is zoomaresource, infer prefix from convention
                        if (localname.contains("/")) {
                            String resourceName = localname.substring(0, localname.indexOf("/"));
                            prefix = resourceName.concat("resource");
                            namespace = nextNamespace.concat(resourceName).concat("/");
                        }
                    }
                }
            }
        }

        if (prefix == null) {
            getLog().trace("The URI <" + uri.toString() + "> cannot be shortened to a form that does not include one " +
                                   "of the disallowed characters " + disallowedStr);
        }
        return prefix;
    }

    /**
     * Returns a string array of two elements, result[0] is the prefix and result[1] is the namespace this equates to.
     *
     * @param prefixMappings allowed, known prefix mappings to use in prefix creation
     * @param bestPrefix     the best prefix so far
     * @param canCache       whether or not we can cache this result in memory
     * @param uri            the URI to create a prefix -> namespace mapping for
     * @return a String array where the first element is the created prefix and the second element is the namespace it
     * maps to
     */
    private static String[] createPrefixNamespaceMapping(final Map<String, String> prefixMappings,
                                                         final String bestPrefix,
                                                         final boolean canCache,
                                                         final URI uri) {
        String prefix;
        String namespace;
        if (bestPrefix == null) {
            // if we have no best prefix, use first five letters of the URI domain
            String authority = uri.getAuthority();
            String[] parts = authority.split("\\.");
            if (parts.length > 1) {
                prefix = parts[1].substring(0, 5);
            }
            else {
                prefix = authority.substring(0, 5);
            }
            namespace = extractNamespace(uri).toString();
        }
        else {
            namespace = extractNamespace(uri).toString();
            int prefixIncrementor = 1;
            prefix = bestPrefix + prefixIncrementor;
            while (prefixMappings.containsKey(prefix)) {
                prefix = bestPrefix + prefixIncrementor;
                prefixIncrementor++;
            }
        }

        getLog().trace("Failed to identify shortform from prefix mappings: " +
                               "namespace = " + namespace + "; " +
                               "created new prefix = " + prefix + ".");
        if (canCache) {
            // add this prefix to our cache - this way, our cache improves as we generate new prefixes
            getLog().trace("Caching " + prefix + " -> " + namespace);
            prefixMappings.put(prefix, namespace);
        }

        String[] result = new String[2];
        result[0] = prefix;
        result[1] = namespace;
        return result;
    }

    public enum PrefixCreationMode {
        /**
         * Allows {@link URIUtils} to create new prefixes for previously unseen namespaces.  Creation of prefixes will
         * obey standard, reproducable rules.  However, if the algorithm used to generate the prefix encounters a
         * collision with an existing namespace, prefix generation will fail.
         */
        CREATE,
        /**
         * Allows {@link URIUtils} to create new prefixes for previously unseen namespaces.  Newly generated namespaces
         * will be cached in JVM allocated memory.  This should be used with caution, as it implies that different
         * instances of ZOOMA can generate colliding namespaces for different prefixes.
         */
        CREATE_AND_CACHE,
        /**
         * Never allows {@link URIUtils} to generate new prefixes for previously unseen namespaces.  If a previously
         * unseen namespace is encountered, then an error will be thrown.
         */
        DO_NOT_CREATE
    }

    public enum ShortformStrictness {
        STRICT,
        ALLOW_HASHES,
        ALLOW_SLASHES_AND_HASHES
    }
}
