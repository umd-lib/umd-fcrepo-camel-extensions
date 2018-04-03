package edu.umd.lib.fcrepo.camel.notification;


import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

public class NotificationRouter extends RouteBuilder  {

  private static final Logger LOGGER = getLogger(NotificationRouter.class);

  @Override
  public void configure() throws Exception {

    /**
     * A generic error handler (specific to this RouteBuilder)
     */
    onException(Exception.class)
    .maximumRedeliveries("{{error.maxRedeliveries}}")
    .log("Notification Routing Error: ${routeId}");

    /**
     * Handle fixity events
     */
    from("{{input.stream}}")
    .routeId("Notification {{input.stream}}")
    .log(LoggingLevel.INFO, LOGGER, "Sending to {{notification.recipients}}")
    .process(new Processor() { 
    	public void process(Exchange exchange) throws Exception {
    		Message in = exchange.getIn();
    		String body = in.getBody(String.class);
    		String uri = (String) in.getHeader("CamelFcrepoUri");
    		exchange.getOut().setBody("Notification for the following URI: " + uri + "\n\n\n" + body );
    	}
    })
    .to("{{notification.recipients}}");
  }

 
}