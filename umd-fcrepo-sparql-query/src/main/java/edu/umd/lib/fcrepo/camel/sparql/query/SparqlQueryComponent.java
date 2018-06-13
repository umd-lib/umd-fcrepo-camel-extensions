package edu.umd.lib.fcrepo.camel.sparql.query;

import org.apache.camel.builder.RouteBuilder;

import edu.umd.lib.osgi.service.AbstractManagedServiceInstance;

/**
 * A component that runs a SPARQL query against the input, and sending the result to the specified output.
 * <p>
 * The output can be formatted using any of the names in the
 * <code>org.apache.jena.sparql.resultset.ResultsFormat<code> class (i.e., "csv", "tuples",
 * "turtle", "n-triples" etc.), or a "csvWithoutHeader" format, which the same as the
 * "csv" format, except it does not print the header.
 */
public class SparqlQueryComponent extends AbstractManagedServiceInstance {
  private String inputStream;
  private String outputStream;
  private String query;
  private String resultsFormatName;

  public SparqlQueryComponent() {
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables
   *
   * @return a RouteBuilder incorporating a SparqlQueryProcessor
   * @throws Exception
   *           if an exception occurs.
   */
  @Override
  protected RouteBuilder buildRoute() throws Exception {
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName() + " [inputStream=");
    builder.append(inputStream);
    builder.append(", outputStream=");
    builder.append(outputStream);
    builder.append(", query=");
    builder.append(query);
    builder.append(", resultsFormatName=");
    builder.append(resultsFormatName);
    builder.append(", routeId=");
    builder.append(routeId);
    builder.append("]");
    return builder.toString();
  }
}
