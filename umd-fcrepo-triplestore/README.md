# UMD Fcrepo Triplestore

* Takes the message body from an "input.stream" queue/topic and sends it
  as a SPARQL Update `INSERT DATA { ... }` request to a triplestore via HTTP.
* Multiple service instances can be created

## Configuration

A service instance is created by adding an `edu.umd.lib.fcrepo.camel.triplestore-[ROUTE_IDENTIFIER].cfg`
into the Karaf etc/ directory, where `[ROUTE_IDENTIFIER]` is a unique name for
the route.

### Example Configuration

```
# Which queue/topic to listen to
input.stream=activemq:queue:triplestore.audit.data

# Triplestore URI that can accept HTTP POST requests
# with Content-Type: application/sparql-update
triplestore.baseUrl=http://example.com/fuseki/dataset/update

# In the event of failure, the maximum number of times a redelivery
# will be attempted.
error.maxRedeliveries=10
```
