
package edu.umd.lib.fcrepo.camel.broadcast;

import java.util.Dictionary;

import org.apache.camel.CamelContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.osgi.service.AbstractManagedServiceFactory;

/**
 * ManagedServiceFactory implementation for creating BroadcastDispatcher instances.
 */
public class BroadcastFactory extends AbstractManagedServiceFactory<BroadcastDispatcher> {
  private Logger log = LoggerFactory.getLogger(BroadcastFactory.class);

  public final static String INPUT_STREAM = "input.stream";
  public final static String MESSAGE_RECIPIENTS = "message.recipients";

  @Override
  protected BroadcastDispatcher createServiceInstance(CamelContext camelContext,
      Dictionary<String, ?> dict) throws ConfigurationException {
    String messageRecipients = getDictionaryEntry(dict, MESSAGE_RECIPIENTS);
    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);

    log.debug("inputStream: " + inputStream);
    log.debug("messageRecipents: " + messageRecipients);

    // Configuration was verified above, now create engine.
    BroadcastDispatcher engine = new BroadcastDispatcher();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setMessageRecipients(messageRecipients);
    return engine;
  }
}
