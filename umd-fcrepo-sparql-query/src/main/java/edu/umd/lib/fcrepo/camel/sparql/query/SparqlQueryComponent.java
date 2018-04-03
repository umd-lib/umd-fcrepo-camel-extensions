package edu.umd.lib.fcrepo.camel.sparql.query;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component that runs a SPARQL query against the input, and sending the result to the specified output.
 * <p>
 * The output can be formatted using any of the names in the
 * <code>org.apache.jena.sparql.resultset.ResultsFormat<code> class (i.e., "csv", "tuples",
 * "turtle", "n-triples" etc.), or a "csvWithoutHeader" format, which the same as the
 * "csv" format, except it does not print the header.
 */
public class SparqlQueryComponent {

  private String inputStream;
  private String outputStream;
  private String query;
  private String resultsFormatName;

  private RouteBuilder rb;
  private CamelContext cc;

  private Logger log = LoggerFactory.getLogger(SparqlQueryComponent.class);

  public SparqlQueryComponent() {
  }

  /**
   * Builds and starts the SPARQL query route.
   */
  public void start() {
    try {
      rb = buildSparqlQueryProcessor();
      log.info("Route " + rb + " starting...");
      cc.start();
      cc.addRoutes(rb);
    } catch (Exception ex) {
      log.error("Could not create SPARQL query route " + ex);
    }
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables
   *
   * @return a RouteBuilder incorporating a SparqlQueryProcessor
   * @throws Exception
   *           if an exception occurs.
   */
  protected RouteBuilder buildSparqlQueryProcessor() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(inputStream)
            .routeId("SparqlQuery " + inputStream)
            .description("Runs SPARQL query against the RDF input and sends results to the output.")
            .log("Parsing message: ${headers[org.fcrepo.jms.timestamp]}: " +
                "${headers[org.fcrepo.jms.identifier]}:${headers[org.fcrepo.jms.eventType]}")
            .process(new SparqlQueryProcessor(query, resultsFormatName))
            .to(outputStream);
      }
    };
  }

  /**
   * Stops the SPARQL query route.
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
   * @param cc
   *          the CamelContext for the route.
   */
  public void setCamelContext(CamelContext cc) {
    this.cc = cc;
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
   * Sets the output stream for the route.
   *
   * @param outputStream
   *          the output stream for the route
   */
  public void setOutputStream(String outputStream) {
    this.outputStream = outputStream;
  }

  /**
   * The SPARQL query to execute against the input.
   *
   * @param query
   *          the SPARQL query to execute against the input.
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * The format of the results that are sent to the output.
   *
   * @param resultsFormatName
   *          any of the names in the
   *          <code>org.apache.jena.sparql.resultset.ResultsFormat<code> class (i.e., "csv", "tuples",
   *          "turtle", "n-triples" etc.), or a "csvWithoutHeader" format, which the same as the
   *          "csv" format, except it does not print the header.
   */
  public void setResultsFormat(String resultsFormatName) {
    this.resultsFormatName = resultsFormatName;
  }
}
