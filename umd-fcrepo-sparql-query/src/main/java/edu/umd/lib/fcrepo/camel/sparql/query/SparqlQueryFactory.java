
package edu.umd.lib.fcrepo.camel.sparql.query;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ManagedServiceFactory implementation for creating SparqlQueryComponent instances.
 */
public class SparqlQueryFactory implements ManagedServiceFactory {
  public final static String INPUT_STREAM = "input.stream";
  public final static String OUTPUT_STREAM = "output.stream";
  public final static String QUERY = "query";
  public final static String RESULTS_FORMAT = "results.format";

  private BundleContext bundleContext;
  private CamelContext camelContext;
  private ServiceRegistration registration;
  private ServiceTracker tracker;
  private Logger log = LoggerFactory.getLogger(SparqlQueryFactory.class);
  private String configurationPid;
  private Map<String, SparqlQueryComponent> dispatchEngines = Collections
      .synchronizedMap(new HashMap<String, SparqlQueryComponent>());

  /**
   * Override ManagedServiceFactory interfaces.
   */
  @Override
  public String getName() {
    return configurationPid;
  }

  @Override
  public void updated(String pid, Dictionary<String, ?> dict) throws ConfigurationException {
    log.info("updated " + pid + " with " + dict.toString());
    SparqlQueryComponent engine = null;

    if (dispatchEngines.containsKey(pid)) {
      engine = dispatchEngines.get(pid);

      if (engine != null) {
        destroyEngine(engine);
      }
      dispatchEngines.remove(pid);
    }

    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);
    String outputStream = getDictionaryEntry(dict, OUTPUT_STREAM);
    String query = getDictionaryEntry(dict, QUERY);
    String resultsFormat = getDictionaryEntry(dict, RESULTS_FORMAT);

    log.debug("inputStream: " + inputStream);
    log.debug("outputStream: " + outputStream);
    log.debug("query: " + query);
    log.debug("resultsFormat: " + resultsFormat);

    // Configuration was verified above, now create engine.
    engine = new SparqlQueryComponent();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setOutputStream(outputStream);
    engine.setQuery(query);
    engine.setResultsFormat(resultsFormat);

    dispatchEngines.put(pid, engine);
    log.debug("Start the engine...");
    engine.start();
  }

  @Override
  public void deleted(String pid) {
    if (dispatchEngines.containsKey(pid)) {
      SparqlQueryComponent engine = dispatchEngines.get(pid);

      if (engine != null) {
        destroyEngine(engine);
      }
      dispatchEngines.remove(pid);
    }
    log.info("deleted " + pid);
  }

  /**
   * Retrieve the value at the given key from the given Dictionary, or throw a ConfigurationException
   * if the key is not found or the value is the empty string.
   *
   * @param dict
   *          the Dictionary to retrieve the value from
   * @param key
   *          the key to retrieve the value of
   * @return the String value at the given key
   * @throws ConfigurationException
   *           if the key is not found, or the value is an empty string.
   */
  protected String getDictionaryEntry(Dictionary<String, ?> dict, String key)
      throws ConfigurationException {
    String value = (String) dict.get(key);
    if ((value != null) && (!value.trim().isEmpty())) {
      log.debug(key + " set to " + value);
      return value;
    } else {
      throw new ConfigurationException(key, "Key is missing or empty");
    }
  }

  private void destroyEngine(SparqlQueryComponent engine) {
    engine.stop();
  }

  /**
   * Initialization method called via the blueprint
   */
  public void init() {
    log.info("Starting " + this.getName());
    Dictionary<String, String> servProps = new Hashtable<String, String>();
    servProps.put(Constants.SERVICE_PID, configurationPid);
    registration = bundleContext.registerService(ManagedServiceFactory.class.getName(), this, servProps);
    tracker = new ServiceTracker(bundleContext, ConfigurationAdmin.class.getName(), null);
    tracker.open();
    log.info("Started " + this.getName());
  }

  /**
   * Destroy method called via the blueprint
   */
  public void destroy() {
    log.info("Destroying SparqlQueryFactory " + configurationPid);
    registration.unregister();
    tracker.close();
  }

  /**
   * Sets the configuration pid from the blueprint
   *
   * @param configurationPid
   *          the configuration pid from the blueprint
   */
  public void setConfigurationPid(String configurationPid) {
    this.configurationPid = configurationPid;
  }

  /**
   * Sets the bundle context from the blueprint
   *
   * @param bundleContext
   *          the bundle context from the blueprint
   */
  public void setBundleContext(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
  }

  /**
   * Sets the Camel context from the blueprint
   *
   * @param camelContext
   *          the Camel context from the blueprint
   */
  public void setCamelContext(CamelContext camelContext) {
    this.camelContext = camelContext;
  }
}
