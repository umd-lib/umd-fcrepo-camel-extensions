
package edu.umd.lib.fcrepo.camel.triplestore;

import java.util.Dictionary;

import org.apache.camel.CamelContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.osgi.service.AbstractManagedServiceFactory;

/**
 * ManagedServiceFactory implementation for creating TriplestoreRouter
 * instances.
 */
public class TriplestoreFactory extends AbstractManagedServiceFactory<TriplestoreRouter> {
  private Logger log = LoggerFactory.getLogger(TriplestoreFactory.class);

  public final static String INPUT_STREAM = "input.stream";
  public final static String TRIPLESTORE_BASE_URL = "triplestore.baseUrl";
  public final static String ERROR_MAX_REDELIVERIES = "error.maxRedeliveries";

  @Override
  protected TriplestoreRouter createServiceInstance(CamelContext camelContext, Dictionary<String, ?> dict)
      throws ConfigurationException {
    TriplestoreRouter engine = null;

    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);
    String triplestoreBaseUrl = getDictionaryEntry(dict, TRIPLESTORE_BASE_URL);
    String maxRedeliveries = getDictionaryEntry(dict, ERROR_MAX_REDELIVERIES);

    log.debug("inputStream: " + inputStream);
    log.debug("triplestoreBaseUrl: " + triplestoreBaseUrl);
    log.debug("errorMaxRedeliveries: " + maxRedeliveries);
    // Configuration was verified above, now create engine.

    engine = new TriplestoreRouter();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setTriplestoreBaseUrl(triplestoreBaseUrl);
    engine.setMaxRedeliveries(Integer.parseInt(maxRedeliveries));
    return engine;
  }
}