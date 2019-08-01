package edu.umd.lib.fcrepo.camel.premis;

import edu.umd.lib.osgi.service.AbstractManagedServiceInstance;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A component that runs a AuditPremisProcessor against the input stream to create a PREMIS-formatted
 * audit event message suitable for recording in a triplestore.
 */
public class PremisComponent extends AbstractManagedServiceInstance {
  private String inputStream;
  private String outputStream;
  private String eventBaseURI;

  private static final String EVENT_BASE_URI = "CamelAuditEventBaseUri";
  private static final Logger logger = getLogger(PremisComponent.class);

  public PremisComponent() {
  }

  /**
   * Returns a RouteBuilder, using the values from the instance variables
   *
   * @return a RouteBuilder incorporating an AuditPremisProcessor
   */
  @Override
  protected RouteBuilder buildRoute() {
    return new RouteBuilder() {
      @Override
      public void configure() {
        from(inputStream)
            .routeId("AuditPremisRouter " + inputStream)
            .log(LoggingLevel.DEBUG, logger, "PremisComponent: ${header.CamelFcrepoAgent}")
            .setHeader(EVENT_BASE_URI, simple(eventBaseURI))
            .process(new AuditPremisProcessor())
            .log(LoggingLevel.INFO, "org.fcrepo.camel.audit",
                "Audit Event: ${headers.CamelFcrepoUri} :: ${headers[CamelAuditEventUri]}")
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
   * Sets the base URI to use for events
   *
   * @param eventBaseURI base URI for events
   */
  public void setEventBaseURI(String eventBaseURI) {
    this.eventBaseURI = eventBaseURI;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName() + " [inputStream=");
    builder.append(inputStream);
    builder.append(", outputStream=");
    builder.append(outputStream);
    builder.append(", routeId=");
    builder.append(routeId);
    builder.append("]");
    return builder.toString();
  }
}
