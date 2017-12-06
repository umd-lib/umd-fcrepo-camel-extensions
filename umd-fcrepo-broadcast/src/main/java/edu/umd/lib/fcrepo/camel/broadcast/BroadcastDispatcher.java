package edu.umd.lib.fcrepo.camel.broadcast;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A broadcast service instance, typically created and controlled via the BroadcastFactory.
 * 
 * This implementation is inspired by the code in Chapter 2, Recipe 6 of
 * 
 *  Nierbeck A., "Apache Karaf Cookbook : Over 60 Recipes to Help You Get the Most Out of Apache Karaf Deployments.",
 *  Birmingham, UK: Packt Pub; 2014.
 */
public class BroadcastDispatcher {

    private String inputStream;
    private String messageRecipients;
    private RouteBuilder rb;
    private CamelContext cc;

    private Logger log = LoggerFactory.getLogger(BroadcastDispatcher.class);

    public BroadcastDispatcher() { }

    /**
     * Builds and starts the broadcast route.
     */
    public void start() {
        try {
            rb = buildBroadcastRouter();
            log.info("Route " + rb + " starting..."); 
            cc.start();
            cc.addRoutes(rb);
        } catch (Exception ex) {
            log.error("Could not process Broadcast " + ex); 
        }
    }

    /**
     * Returns a RouteBuilder, using the values from the instance variables
     * 
     * @return 
     * @throws Exception
     */
    protected RouteBuilder buildBroadcastRouter() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
              from(inputStream)
              .routeId("Broadcast " + inputStream)
              .description("Broadcast messages from one queue/topic to other specified queues/topics.")
              .log("Distributing message: ${headers[org.fcrepo.jms.timestamp]}: " +
                      "${headers[org.fcrepo.jms.identifier]}:${headers[org.fcrepo.jms.eventType]}")
              .recipientList(simple(messageRecipients)).ignoreInvalidEndpoints();
            }
        };
    }

    /**
     * Stops the broadcast route.
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
     * @param messageRecipients the message recipients for the route
     */
    public void setMessageRecipients(String messageRecipients) {
        this.messageRecipients = messageRecipients;
    }
}
