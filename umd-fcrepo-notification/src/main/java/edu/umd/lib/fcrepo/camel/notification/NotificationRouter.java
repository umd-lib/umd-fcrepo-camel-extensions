package edu.umd.lib.fcrepo.camel.notification;


import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

public class NotificationRouter extends RouteBuilder  {

	private static final Logger LOGGER = getLogger(NotificationRouter.class);
	
	 public void configure() throws Exception {

	        /**
	         * A generic error handler (specific to this RouteBuilder)
	         */
	        onException(Exception.class)
	            .maximumRedeliveries("{{error.maxRedeliveries}}")
	            .log("Index Routing Error: ${routeId}");

	        /**
	         * Handle fixity events
	         */
	        from("{{notification.sender}}")
	        	.log(LoggingLevel.WARN, LOGGER,
	        		"Sending to ${{notification.recipient}}")
	                    .to("{{notification.recipient}}");
	    }
}
