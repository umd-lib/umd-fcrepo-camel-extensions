package edu.umd.lib.fcrepo.camel.triplestore;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A triplestore router, typically created by a TriplestoreFactory. It expects
 * N-Triples formatted RDF as the body of the input message, and sends that RDF,
 * wrapped in a SPARQL <code>INSERT DATA { ... }</code> statement, in a POST
 * request to the http: or https: URL set in <code>triplestore.baseUrl</code>.
 * The Content-Type of that request is "application/sparql-update".
 *
 * This implementation is inspired by the code in Chapter 2, Recipe 6 of
 *
 * Nierbeck A., "Apache Karaf Cookbook : Over 60 Recipes to Help You Get the
 * Most Out of Apache Karaf Deployments.", Birmingham, UK: Packt Pub; 2014.
 */
public class TriplestoreRouter {

  private String inputStream;
  private String triplestoreBaseUrl;
  private int maxRedeliveries;
  private RouteBuilder rb;
  private CamelContext cc;

  private Logger log = LoggerFactory.getLogger(TriplestoreRouter.class);

  public TriplestoreRouter() { }

  /**
   * Builds and starts the triplestore route.
   */
  public void start() {
    try {
      rb = buildTriplestoreRouter();
      log.info("Route " + rb + " starting...");
      cc.start();
      cc.addRoutes(rb);
    } catch (Exception ex) {
      log.error("Could not process Triplestore " + ex);
    }
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables
   *
   * @return
   * @throws Exception
   */
  protected RouteBuilder buildTriplestoreRouter() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {

        /**
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
        .maximumRedeliveries(maxRedeliveries)
        .log("Event Routing Error: ${routeId}");

        from(inputStream)
        .routeId("Triplestore " + inputStream)
        .description("Send RDF triples to a triplestore.")
        .process(new SparqlInsertDataWrapper())
        .to(triplestoreBaseUrl + "?useSystemProperties=true");
      }
    };
  }

  /**
   * Stops the triplestore route.
   */
  public void stop() {
    if (rb != null) {
      try {
        cc.removeRoute(rb.toString());
      } catch (Exception e) {
        log.error("Could not remove route " + rb + " " + e);
      }
    }
  }

  /**
   * Sets the CamelContext for the route.
   *
   * @param cc the CamelContext for the route.
   */
  public void setCamelContext(CamelContext cc) {
    this.cc = cc;
  }

  /**
   * Sets the input stream for the route.
   *
   * @param inputStream the input stream for the route
   */
  public void setInputStream(String inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Sets the message recipients for the route.
   *
   * @param triplestoreBaseUrl the message recipients for the route
   */
  public void setTriplestoreBaseUrl(String triplestoreBaseUrl) {
    this.triplestoreBaseUrl = triplestoreBaseUrl;
  }

  /**
   * Sets the maximum number of redeliveries on error for the route
   *
   * @param maxRedeliveries
   */
  public void setMaxRedeliveries(int maxRedeliveries) {
    this.maxRedeliveries = maxRedeliveries;
  }

  /**
   * Simple processor whose only function is to wrap the incoming message body
   * in <code>INSERT DATA { ... }</code> and set the Content-Type and HTTP
   * method headers to "application/sparql-update" and "POST", respectively.
   */
  private class SparqlInsertDataWrapper implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
      Message in = exchange.getIn();
      in.setHeader(Exchange.CONTENT_TYPE, "application/sparql-update");
      in.setHeader(Exchange.HTTP_METHOD, "POST");
      String rdf = (String) in.getBody();
      in.setBody("INSERT DATA { " + rdf + " }");
      log.debug((String) in.getBody());
    }
  }
}
