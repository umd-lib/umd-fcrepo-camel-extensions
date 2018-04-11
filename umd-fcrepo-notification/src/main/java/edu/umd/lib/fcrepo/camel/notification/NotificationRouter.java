package edu.umd.lib.fcrepo.camel.notification;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import edu.umd.lib.osgi.service.AbstractManagedServiceInstance;

public class NotificationRouter extends AbstractManagedServiceInstance {

  private String inputStream;
  private String notificationRecipients;
  private String maxRedeliveries;

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
            .log("Notification Routing Error: ${routeId}");

        from(inputStream)
            .routeId("NotificationRouter " + inputStream)
            .log(LoggingLevel.INFO, log, "Sending to " + notificationRecipients)
            .to(notificationRecipients)
            .process(new Processor() {
              @Override
              public void process(Exchange exchange) throws Exception {
                Message in = exchange.getIn();
                String body = in.getBody(String.class);
                String uri = (String) in.getHeader("CamelFcrepoUri");
                exchange.getOut().setBody("Notification for the following URI: " + uri + "\n\n\n" + body);
              }
            });
            
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
   * Sets the notification recipients for the route.
   *
   * @param notificationRecipients
   *          the notification recipients for the route
   */
  public void setNotificationRecipients(String notificationRecipients) {
    this.notificationRecipients = notificationRecipients;
  }

  /**
   * Sets the maximum number of times to attempt delivery.
   *
   * @param maxRedeliveries
   *          the maximum number of times to attempt delivery..
   */
  public void setMaxRedeliveries(String maxRedeliveries) {
    this.maxRedeliveries = maxRedeliveries;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getName() + " [log=");
    builder.append(", inputStream=");
    builder.append(inputStream);
    builder.append(", notificationRecipients=");
    builder.append(notificationRecipients);
    builder.append(", maxRedeliveries=");
    builder.append(maxRedeliveries);
    builder.append(", routeId=");
    builder.append(routeId);
    builder.append("]");
    return builder.toString();
  }
}