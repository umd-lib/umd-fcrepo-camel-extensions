
package edu.umd.lib.fcrepo.camel.notification;

import java.util.Dictionary;

import org.apache.camel.CamelContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.osgi.service.AbstractManagedServiceFactory;

/**
 * ManagedServiceFactory implementation for creating NotificationRouter instances.
 */
public class NotificationFactory extends AbstractManagedServiceFactory<NotificationRouter> {
  private Logger log = LoggerFactory.getLogger(NotificationFactory.class);

  public final static String INPUT_STREAM = "input.stream";
  public final static String NOTIFICATION_RECIPIENTS = "notification.recipients";
  public final static String ERROR_MAX_REDELIVERIES = "error.maxRedeliveries";

  @Override
  protected NotificationRouter createServiceInstance(CamelContext camelContext,
      Dictionary<String, ?> dict) throws ConfigurationException {
    String notificationRecipients = getDictionaryEntry(dict, NOTIFICATION_RECIPIENTS);
    String inputStream = getDictionaryEntry(dict, INPUT_STREAM);
    String maxRedeliveries = getDictionaryEntry(dict, ERROR_MAX_REDELIVERIES);

    log.debug("inputStream: " + inputStream);
    log.debug("notificationRecipients: " + notificationRecipients);
    log.debug("errorMaxRedeliveries: " + maxRedeliveries);

    // Configuration was verified above, now create engine.
    NotificationRouter engine = new NotificationRouter();
    engine.setCamelContext(camelContext);
    engine.setInputStream(inputStream);
    engine.setNotificationRecipients(notificationRecipients);
    engine.setMaxRedeliveries(maxRedeliveries);
    return engine;
  }
}
