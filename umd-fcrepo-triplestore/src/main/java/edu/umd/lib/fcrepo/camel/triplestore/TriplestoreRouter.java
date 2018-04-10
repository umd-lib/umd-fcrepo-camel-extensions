package edu.umd.lib.fcrepo.camel.triplestore;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.osgi.service.AbstractManagedServiceInstance;

/**
 * A triplestore router, typically created by a TriplestoreFactory. It expects
 * N-Triples formatted RDF as the body of the input message, and sends that RDF,
 * wrapped in a SPARQL <code>INSERT DATA { ... }</code> statement, in a POST
 * request to the http: or https: URL set in <code>triplestore.baseUrl</code>.
 * The Content-Type of that request is "application/sparql-update".
 */
public class TriplestoreRouter extends AbstractManagedServiceInstance {
  private Logger log = LoggerFactory.getLogger(TriplestoreRouter.class);

  private String inputStream;
  private String triplestoreBaseUrl;
  private int maxRedeliveries;

  public TriplestoreRouter() {
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables
   *
   * @return
   * @throws Exception
   */
  @Override
  protected RouteBuilder buildRoute() throws Exception {
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
   * Sets the input stream for the route.
   *
   * @param inputStream
   *          the input stream for the route
   */
  public void setInputStream(String inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Sets the message recipients for the route.
   *
   * @param triplestoreBaseUrl
   *          the message recipients for the route
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName() + " [inputStream=");
    builder.append(inputStream);
    builder.append(", triplestoreBaseUrl=");
    builder.append(triplestoreBaseUrl);
    builder.append(", maxRedeliveries=");
    builder.append(maxRedeliveries);
    builder.append(", routeId=");
    builder.append(routeId);
    builder.append("]");
    return builder.toString();
  }
}
