# UMD Fcrepo Broadcast

* Routes the messages from an "input.stream" queue/topic to one or more "message.recipients" queues
* Multiple service instances can be created

## Configuration

A service instance is created by adding an "edu.umd.lib.fcrepo.camel.broadcast-[ROUTE_IDENTIFIER].cfg"
into the Karaf etc/ directory, where [ROUTE_IDENTIFIER] is a unique name for
the route.

### Example Configuration:

The following configuration files demonstrate that it is possible to have
two completely different service instances, using different "input.stream" and
"message.recipients", simply by creating appropriately named configuration
files in the Karaf etc/ directory.

#### Service Instance 1 - edu.umd.lib.fcrepo.camel.broadcast-fixitysuccess.cfg

A service instance that receives messages from the "activemq:queue:fixitysuccess"
input stream, and rebroadcast copies to each of the recipients in the "message.recipients"
property (in this case, two different files in /tmp). In these configurations,
the [ROUTE_IDENTIFIER] was chosen to be the related to the "input.stream" value,
but this is entirely arbitrary.

```
# Which queue/topic to listen to on the above broker
input.stream=activemq:queue:fixitysuccess

# Comma separated list of recipient queues to broadcast the incoming messages to
# for example: "broker:queue:fcrepo-serialization,broker:queue:fcrepo-indexing-triplestore"
message.recipients=file:/tmp/?fileName=umd-broadcast-fixitysuccess.log&fileExist=Append,file:/tmp/?fileName=fixitysuccess.log&fileExist=Append

# In the event of failure, the maximum number of times a redelivery will be attempted.
error.maxRedeliveries=10
```

#### Service Instance 2 - edu.umd.lib.fcrepo.camel.broadcast-fixityfailure.cfg

A service instance that receives messages from the "activemq:queue:fixityfailure"
input stream, and rebroadcast copies to each of the recipients in the "message.recipients"
property (in this case, two different files in /tmp).

```
# Which queue/topic to listen to on the above broker
input.stream=activemq:queue:fixityfailure

# Comma separated list of recipient queues to broadcast the incoming messages to
# for example: "broker:queue:fcrepo-serialization,broker:queue:fcrepo-indexing-triplestore"
message.recipients=file:/tmp/?fileName=umd-broadcast-fixityfailure.log&fileExist=Append,file:/tmp/?fileName=fixityfailure.log&fileExist=Append

# In the event of failure, the maximum number of times a redelivery will be attempted.
error.maxRedeliveries=10
```

In the Karaf "route-list", the routes will show as distinct routes, named
for the "input.stream" they are using, i.e.:

```
UmdFcrepoBroadcast         Broadcast activemq:queue:fixityfailure
UmdFcrepoBroadcast         Broadcast activemq:queue:fixitysuccess
```
