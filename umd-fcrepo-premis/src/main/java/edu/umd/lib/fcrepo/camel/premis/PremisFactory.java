package edu.umd.lib.fcrepo.camel.premis;

import edu.umd.lib.osgi.service.AbstractManagedServiceFactory;
import org.apache.camel.CamelContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * AbstractManagedServiceFactory implementation for creating PremisComponent instances.
 */
public class PremisFactory extends AbstractManagedServiceFactory<PremisComponent> {
  private Logger log = LoggerFactory.getLogger(PremisFactory.class);

  private final static String INPUT_STREAM = "input.stream";
  private final static String OUTPUT_STREAM = "output.stream";
  private final static String EVENT_BASE_URI = "event.baseURI";

  /**
   * Creates and returns a new PremisComponent from the given parameters.
   *
   * @param camelContext
   *          the CamelContext for the component
   * @param dict
   *          the Dictionary containing the blueprint properties.
   * @return a new PremisComponent from the given parameters.
   */
  @Override
  protected PremisComponent createServiceInstance(CamelContext camelContext, Dictionary<String, ?> dict)
      throws ConfigurationException {

    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);
    String outputStream = getDictionaryEntry(dict, OUTPUT_STREAM);
    String eventBaseURI = getDictionaryEntry(dict, EVENT_BASE_URI);

    log.debug("inputStream: " + inputStream);
    log.debug("outputStream: " + outputStream);
    log.debug("eventBaseURI: " + eventBaseURI);

    // Configuration was verified above, now create engine.
    PremisComponent engine = new PremisComponent();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setOutputStream(outputStream);
    engine.setEventBaseURI(eventBaseURI);

    return engine;
  }
}
