
package edu.umd.lib.fcrepo.camel.sparql.query;

import java.util.Dictionary;

import org.apache.camel.CamelContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.osgi.service.AbstractManagedServiceFactory;

/**
 * AbstractManagedServiceFactory implementation for creating SparqlQueryComponent instances.
 */
public class SparqlQueryFactory extends AbstractManagedServiceFactory<SparqlQueryComponent> {
  private Logger log = LoggerFactory.getLogger(SparqlQueryFactory.class);

  public final static String INPUT_STREAM = "input.stream";
  public final static String OUTPUT_STREAM = "output.stream";
  public final static String QUERY = "query";
  public final static String RESULTS_FORMAT = "results.format";

  /**
   * Creates and returns a new SparqlQueryComponent from the given parameters.
   *
   * @param inputStream
   *          the input stream for the component
   * @param outputStream
   *          the output stream for the component
   * @param camelContext
   *          the CamelContext for the component
   * @param dict
   *          the Dictionary containing the blueprint properties.
   * @return a new SparqlQueryComponent from the given parameters.
   */
  @Override
  protected SparqlQueryComponent createServiceInstance(CamelContext camelContext, Dictionary<String, ?> dict)
      throws ConfigurationException {

    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);
    String outputStream = getDictionaryEntry(dict, OUTPUT_STREAM);

    String query = getDictionaryEntry(dict, QUERY);
    String resultsFormat = getDictionaryEntry(dict, RESULTS_FORMAT);

    log.debug("inputStream: " + inputStream);
    log.debug("outputStream: " + outputStream);
    log.debug("query: " + query);
    log.debug("resultsFormat: " + resultsFormat);

    // Configuration was verified above, now create engine.
    SparqlQueryComponent engine = new SparqlQueryComponent();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setOutputStream(outputStream);
    engine.setQuery(query);
    engine.setResultsFormat(resultsFormat);

    return engine;
  }
}
