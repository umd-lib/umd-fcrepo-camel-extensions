package edu.umd.lib.osgi.service;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for services that may have multiple instances.
 * <p>
 * This implementation is inspired by the code in Chapter 2, Recipe 6 of
 * <p>
 * Nierbeck A., "Apache Karaf Cookbook : Over 60 Recipes to Help You Get the Most Out of Apache Karaf Deployments.",
 * Birmingham, UK: Packt Pub; 2014.
 */
public abstract class AbstractManagedServiceInstance {
  /**
   * The id of the route for this instance
   */
  protected String routeId;

  /**
   * The CamelContext associated with this instance.
   */
  protected CamelContext cc;

  private Logger log = LoggerFactory.getLogger(AbstractManagedServiceInstance.class);

  /**
   * Default constructor
   */
  public AbstractManagedServiceInstance() {
  }

  /**
   * Builds and starts the route.
   */
  public void start() {
    try {
      RouteBuilder rb = buildRoute();
      routeId = rb.toString();
      log.info("Route " + routeId + " starting...");
      cc.start();
      cc.addRoutes(rb);
    } catch (Exception ex) {
      log.error("Could not create route " + ex);
    }
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables in the configuration file.
   *
   * @return a RouteBuilder incorporating the service instance
   * @throws Exception
   *           if an exception occurs.
   */
  protected abstract RouteBuilder buildRoute() throws Exception;

  /**
   * Stops the route.
   */
  public void stop() {
    if (routeId != null) {
      try {
        cc.removeRoute(routeId);
      } catch (Exception e) {
        log.error("Could not remove route " + routeId + " " + e);
      }
    }
  }

  /**
   * Sets the CamelContext for the route.
   *
   * @param cc
   *          the CamelContext for the route.
   */
  public void setCamelContext(CamelContext cc) {
    this.cc = cc;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + " [routeId=" + routeId + "]";
  }
}
