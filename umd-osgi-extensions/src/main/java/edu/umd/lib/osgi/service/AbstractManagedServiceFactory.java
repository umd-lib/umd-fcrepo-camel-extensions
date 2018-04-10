
package edu.umd.lib.osgi.service;

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
 * Abstract implementation of ManagedServiceFactory that provides commonly used code for constructing
 * service instances.
 * <p>
 * This implementation is inspired by the code in Chapter 2, Recipe 6 of
 * <p>
 * Nierbeck A., "Apache Karaf Cookbook : Over 60 Recipes to Help You Get the Most Out of Apache Karaf Deployments.",
 * Birmingham, UK: Packt Pub; 2014.
 *
 * @param <T>
 *          the type of the ManagedServiceInstance implementation that will be created
 */
public abstract class AbstractManagedServiceFactory<T extends AbstractManagedServiceInstance>
    implements ManagedServiceFactory {
  private Logger log = LoggerFactory.getLogger(AbstractManagedServiceFactory.class);

  protected BundleContext bundleContext;
  protected CamelContext camelContext;
  protected ServiceRegistration<?> registration;
  protected ServiceTracker<T, T> tracker;
  protected String configurationPid;
  protected Map<String, T> dispatchEngines = Collections.synchronizedMap(new HashMap<String, T>());

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
    T engine = null;

    if (dispatchEngines.containsKey(pid)) {
      engine = dispatchEngines.get(pid);

      if (engine != null) {
        destroyEngine(engine);
      }
      dispatchEngines.remove(pid);
    }

    // Configuration was verified above, now create engine.
    engine = createServiceInstance(camelContext, dict);

    dispatchEngines.put(pid, engine);
    log.debug("Start the engine...");
    engine.start();
  }

  /**
   * Creates and returns a new instance of the service from the given parameters.
   *
   * @param camelContext
   *          the CamelContext for the component
   * @param dict
   *          the Dictionary containing the blueprint properties.
   * @return a new instance of the service, configured using the given parameters.
   */
  protected abstract T createServiceInstance(CamelContext camelContext, Dictionary<String, ?> dict)
      throws ConfigurationException;

  @Override
  public void deleted(String pid) {
    if (dispatchEngines.containsKey(pid)) {
      T engine = dispatchEngines.get(pid);

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

  private void destroyEngine(T engine) {
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
    tracker = new ServiceTracker<T, T>(bundleContext, ConfigurationAdmin.class.getName(), null);
    tracker.open();
    log.info("Started " + this.getName());
  }

  /**
   * Destroy method called via the blueprint
   */
  public void destroy() {
    log.info("Destroying " + this.getName());
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName() + " [configurationPid=");
    builder.append(configurationPid);
    builder.append("]");
    return builder.toString();
  }
}
