# UMD Fcrepo SPARQL Query

* Runs a SPARQL query on the messages from the "input.stream" queue/topic
sending the query results to the "output.stream" queue/topic.
* Multiple service instances can be created
* The format of the SPARQL query results is configurable.

## Configuration

A service instance is created by adding an
"edu.umd.lib.fcrepo.camel.sparql.query-[ROUTE_IDENTIFIER].cfg" file
into the Karaf etc/ directory, where [ROUTE_IDENTIFIER] is a unique name for
the route.

### Example Configuration:

For our example, the component will listen for Fedora fixity check results on
the "activemq:queue:fixitysuccess" queue, and send the SPARQL results in
CSV format to the "/tmp/umd-sparl-query-fixityresult.log" file.

The following is an example of a Fedora fixity check result (in this case
a failure, where the file had an incorrect size and checksum):

```
<rdf:RDF
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:premis="http://www.loc.gov/premis/rdf/v1#" >
  <rdf:Description rdf:about="https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3#fixity/1512751599416">
    <premis:hasSize rdf:datatype="http://www.w3.org/2001/XMLSchema#long">32277713</premis:hasSize>
    <premis:hasMessageDigest rdf:resource="urn:sha1:63edda4d89dc93b578c44013cada91aab492614b"/>
    <premis:hasMessageDigestAlgorithm>SHA-1</premis:hasMessageDigestAlgorithm>
    <premis:hasEventOutcome>BAD_SIZE</premis:hasEventOutcome>
    <premis:hasEventOutcome>BAD_CHECKSUM</premis:hasEventOutcome>
    <rdf:type rdf:resource="http://www.loc.gov/premis/rdf/v1#EventOutcomeDetail"/>
    <rdf:type rdf:resource="http://www.loc.gov/premis/rdf/v1#Fixity"/>
  </rdf:Description>
  <rdf:Description rdf:about="https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3">
    <premis:hasFixity rdf:resource="https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3#fixity/1512751599416"/>
  </rdf:Description>
</rdf:RDF>
```

The configuration file looks like:

```
# The queue/topic to listen to
input.stream=activemq:queue:fixitysuccess

# The queue/topic to send the SPARQL results to, for example:
# for example: "file:/tmp/?fileName=umd-sparl-query-result.log&fileExist=Append"
output.stream=file:/tmp/?fileName=umd-sparl-query-fixityresult.log&fileExist=Append

# The format for the results. The format should use one of the names in the
# org.apache.jena.sparql.resultset.ResultsFormat class (i.e., "csv", "tuples",
# "turtle", "n-triples", etc.)
#
# Also available is a "csvWithoutHeader" format, which the same as the
# "csv" format, except it does not print the header.
results.format=csvWithoutHeader

# In the event of failure, the maximum number of times a redelivery will be attempted.
error.maxRedeliveries=10

# The SPARQL query to run. Multiple line queries should be continued with "\"
# at the end of the line.
query= \
      prefix xsd: <http://www.w3.org/2001/XMLSchema#> \
      SELECT (now() as ?time) ?uri (group_concat(?outcome;separator='|') as ?outcomes) ?size ?messageDigest \
      WHERE { \
        ?uri <http://www.loc.gov/premis/rdf/v1#hasFixity> ?fixity_uri . \
        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasEventOutcome> ?outcome . \
        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasSize> ?_size . \
        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasMessageDigest> ?messageDigest \
        bind( xsd:integer(?_size) as ?size) \
      } \
      group by ?uri ?size ?messageDigest
```

For the Fedora fixity result above, this configuration will add the following
line to the "/tmp/umd-sparl-query-fixityresult.log" file:

```
2017-12-08T16:45:41.915Z,https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3,BAD_CHECKSUM|BAD_SIZE,32277713,urn:sha1:63edda4d89dc93b578c44013cada91aab492614b
```
This line includes each of the variables in the SPARQL SELECT statement
(a timestamp, the URI of the checked file, the event outcomes (grouped together
and separated by a "|"), the file size, and the message digest).

### Result Formats

The "results.format" property will accept:

* Any names in the org.apache.jena.sparql.resultset.ResultsFormat class (i.e.,
"csv", "tuples", "turtle", "n-triples", etc.)
* The custom "csvWithoutHeader" value, which works like the "cvs" format in the
Jena ResultsFormat format class, only without printing a header row.
