# UMD Fcrepo Notification

Takes a message body from 'notification.sender' and sends it to the a 'notification.recipient' ( such as a 
SMTP url )

## Configuration

Create a service instance
`edu.umd.lib.fcrepo.camel.notification.cfg` into the karaf
etc directory.

## Example Configuration

```
# Which queue/topic to listen to on the above broker
notification.sender=activemq:queue:fixityfailure

# Where to send this...see the camel-mail documentation
notification.recipient=smtp://localhost?to=jdixon@umd.edu&subject=Fixity\ Failure

```
