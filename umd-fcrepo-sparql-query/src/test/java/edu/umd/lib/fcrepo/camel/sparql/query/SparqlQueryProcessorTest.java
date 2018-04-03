package edu.umd.lib.fcrepo.camel.sparql.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

public class SparqlQueryProcessorTest {
  public static final Property RDF_TYPE = ResourceFactory
      .createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
  public static final String PREMIS_NS = "http://www.loc.gov/premis/rdf/v1#";
  public static final Resource EVENT = ResourceFactory.createResource(PREMIS_NS + "Event");
  public static final Resource EVENT_OUTCOME_DETAIL = ResourceFactory.createResource(PREMIS_NS + "EventOutcomeDetail");
  public static final Resource FIXITY = ResourceFactory.createResource(PREMIS_NS + "Fixity");
  public static final Property HAS_EVENT_TYPE = ResourceFactory.createProperty(PREMIS_NS + "hasEventType");
  public static final Property HAS_EVENT_RELATED_OBJECT = ResourceFactory
      .createProperty(PREMIS_NS + "hasEventRelatedObject");
  public static final Property HAS_EVENT_DATE_TIME = ResourceFactory.createProperty(PREMIS_NS + "hasEventDateTime");
  public static final Property HAS_EVENT_OUTCOME = ResourceFactory.createProperty(PREMIS_NS + "hasEventOutcome");
  public static final Property HAS_EVENT_OUTCOME_DETAIL = ResourceFactory
      .createProperty(PREMIS_NS + "hasEventOutcomeDetail");
  public static final Property HAS_MESSAGE_DIGEST = ResourceFactory.createProperty(PREMIS_NS + "hasMessageDigest");
  public static final Property HAS_MESSAGE_DIGEST_ALGORITHM = ResourceFactory
      .createProperty(PREMIS_NS + "hasMessageDigestAlgorithm");
  public static final Property HAS_SIZE = ResourceFactory.createProperty(PREMIS_NS + "hasSize");
  public static final Resource EXTERNAL_EVENT = ResourceFactory
      .createResource("http://fedora.info/definitions/v4/audit#ExternalEvent");
  public static final Resource INSTANTANEOUS_EVENT = ResourceFactory
      .createResource("http://www.w3.org/ns/prov#InstantaneousEvent");
  public static final Resource PASSED = ResourceFactory.createResource("http://www.w3.org/ns/earl#passed");
  public static final Resource FAILED = ResourceFactory.createResource("http://www.w3.org/ns/earl#failed");

  private String query;
  private String constructQuery;

  @Before
  public void setUp() {
    query = String.join("\n",
        "prefix xsd: <http://www.w3.org/2001/XMLSchema#> ",
        "SELECT (now() as ?time) ?uri (group_concat(?outcome;separator='|') as ?outcomes) ?size ?messageDigest ",
        "      WHERE { ",
        "        ?uri <http://www.loc.gov/premis/rdf/v1#hasFixity> ?fixity_uri . ",
        "        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasEventOutcome> ?outcome . ",
        "        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasSize> ?_size . ",
        "        ?fixity_uri <http://www.loc.gov/premis/rdf/v1#hasMessageDigest> ?messageDigest ",
        "        bind( xsd:integer(?_size) as ?size) ",
        "      } ",
        "      group by ?uri ?size ?messageDigest");

    constructQuery = String.join("\n",
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>",
        "PREFIX premis: <http://www.loc.gov/premis/rdf/v1#>",
        "PREFIX audit: <http://fedora.info/definitions/v4/audit#>",
        "PREFIX prov: <http://www.w3.org/ns/prov#>",
        "PREFIX earl: <http://www.w3.org/ns/earl#>",
        "CONSTRUCT {",
        "  ?audit a premis:Event, audit:ExternalEvent, prov:InstantaneousEvent ;",
        "  premis:hasEventType <http://id.loc.gov/vocabulary/preservation/eventType/fix> ;",
        "  premis:hasEventRelatedObject ?target ;",
        "  premis:hasEventDateTime ?now ;",
        "  premis:hasEventOutcome ?outcome_term ;",
        "  premis:hasEventOutcomeDetail ?s .",
        "  ?s ?p ?o .",
        "}",
        "WHERE {",
        "  {",
        "    SELECT ?s ?p ?o",
        "      (uri(strbefore(str(?s), \"#\")) AS ?target)",
        "      (now() as ?now)",
        "      (uri(concat(\"https://fcrepolocal/audit/\", str(?uuid))) as ?audit)",
        "      (if(?outcome = \"SUCCESS\", earl:passed, earl:failed) as ?outcome_term)",
        "    WHERE {",
        "      ?s ?p ?o",
        "      { SELECT ?s (uuid() as ?uuid) WHERE { ?s a premis:Fixity } LIMIT 1 }",
        "      { SELECT ?outcome WHERE { ?x premis:hasEventOutcome ?outcome } LIMIT 1 }",
        "    }",
        "  }",
        "}");
  }

  @Test
  public void testFixitySuccess() throws FileNotFoundException {
    File file = new File(getClass().getResource("/fixity-success.rdf").getFile());
    InputStream in = new FileInputStream(file);

    SparqlQueryProcessor processor = new SparqlQueryProcessor(query, SparqlQueryProcessor.CSV_WITHOUT_HEADER);
    String output = processor.executeQuery(in);

    Pattern pattern = Pattern
        .compile("(?<time>.+),(?<uri>.+),(?<outcomes>.+),(?<size>.+),(?<messageDigest>.+)");
    Matcher matcher = pattern.matcher(output.trim());
    assertTrue(matcher.matches());

    assertEquals(
        "https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3",
        matcher.group("uri"));
    assertEquals("SUCCESS", matcher.group("outcomes"));
    assertEquals("32277709", matcher.group("size"));
    assertEquals("urn:sha1:b267e0490a0985ff7a721858b01e354a12e63cac", matcher.group("messageDigest"));
  }

  @Test
  public void testFixityFailure() throws FileNotFoundException {
    File file = new File(getClass().getResource("/fixity-failure.rdf").getFile());
    InputStream in = new FileInputStream(file);

    SparqlQueryProcessor processor = new SparqlQueryProcessor(query, SparqlQueryProcessor.CSV_WITHOUT_HEADER);
    String output = processor.executeQuery(in);

    Pattern pattern = Pattern
        .compile("(?<time>.+),(?<uri>.+),(?<outcomes>.+),(?<size>.+),(?<messageDigest>.+)");
    Matcher matcher = pattern.matcher(output.trim());
    assertTrue(matcher.matches());

    assertEquals(
        "https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3",
        matcher.group("uri"));
    assertEquals("BAD_CHECKSUM|BAD_SIZE", matcher.group("outcomes"));
    assertEquals("32277713", matcher.group("size"));
    assertEquals("urn:sha1:63edda4d89dc93b578c44013cada91aab492614b", matcher.group("messageDigest"));
  }

  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidResultFormat() {
    SparqlQueryProcessor processor = new SparqlQueryProcessor(query, "NOT_A_RESULT_FORMAT_NAME");
  }

  @Test
  public void testConstructFixitySuccess() throws FileNotFoundException {
    File file = new File(getClass().getResource("/fixity-success.rdf").getFile());
    InputStream in = new FileInputStream(file);
  
    SparqlQueryProcessor processor = new SparqlQueryProcessor(constructQuery, "N-Triples");
    String output = processor.executeQuery(in);
    InputStream rdf = new ByteArrayInputStream(output.getBytes());
  
    Model model = ModelFactory.createDefaultModel();
    model.read(rdf, "", "N-Triples");
  
    Resource fixityResult = model.createResource(
        "https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3#fixity/1512758476451");
  
    List<Resource> subjects = model.listResourcesWithProperty(RDF_TYPE, EVENT).toList();
    Resource auditEvent = subjects.get(0);
  
    assertTrue(model.contains(auditEvent, RDF_TYPE, EVENT));
    assertTrue(model.contains(auditEvent, RDF_TYPE, EXTERNAL_EVENT));
    assertTrue(model.contains(auditEvent, RDF_TYPE, INSTANTANEOUS_EVENT));
    assertTrue(model.contains(auditEvent, HAS_EVENT_TYPE,
        model.createResource("http://id.loc.gov/vocabulary/preservation/eventType/fix")));
    assertTrue(model.contains(auditEvent, HAS_EVENT_RELATED_OBJECT,
        model.createResource("https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3")));
    assertTrue(model.contains(auditEvent, HAS_EVENT_OUTCOME, PASSED));
    assertTrue(model.contains(auditEvent, HAS_EVENT_DATE_TIME));
    assertTrue(model.contains(auditEvent, HAS_EVENT_OUTCOME_DETAIL, fixityResult));
    assertTrue(model.contains(fixityResult, RDF_TYPE, EVENT_OUTCOME_DETAIL));
    assertTrue(model.contains(fixityResult, RDF_TYPE, FIXITY));
    assertTrue(model.contains(fixityResult, HAS_EVENT_OUTCOME, "SUCCESS"));
    assertTrue(model.contains(fixityResult, HAS_MESSAGE_DIGEST,
        model.createResource("urn:sha1:b267e0490a0985ff7a721858b01e354a12e63cac")));
    assertTrue(model.contains(fixityResult, HAS_MESSAGE_DIGEST_ALGORITHM, "SHA-1"));
    assertTrue(model.contains(fixityResult, HAS_SIZE, model.createTypedLiteral(32277709)));
  }

  @Test
  public void testConstructFixityFailure() throws FileNotFoundException {
    File file = new File(getClass().getResource("/fixity-failure.rdf").getFile());
    InputStream in = new FileInputStream(file);
  
    SparqlQueryProcessor processor = new SparqlQueryProcessor(constructQuery, "N-Triples");
    String output = processor.executeQuery(in);
    InputStream rdf = new ByteArrayInputStream(output.getBytes());
  
    Model model = ModelFactory.createDefaultModel();
    model.read(rdf, "", "N-Triples");
  
    Resource fixityResult = model.createResource(
        "https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3#fixity/1512758673019");
  
    List<Resource> subjects = model.listResourcesWithProperty(RDF_TYPE, EVENT).toList();
    Resource auditEvent = subjects.get(0);
  
    assertTrue(model.contains(auditEvent, RDF_TYPE, EVENT));
    assertTrue(model.contains(auditEvent, RDF_TYPE, EXTERNAL_EVENT));
    assertTrue(model.contains(auditEvent, RDF_TYPE, INSTANTANEOUS_EVENT));
    assertTrue(model.contains(auditEvent, HAS_EVENT_TYPE,
        model.createResource("http://id.loc.gov/vocabulary/preservation/eventType/fix")));
    assertTrue(model.contains(auditEvent, HAS_EVENT_RELATED_OBJECT,
        model.createResource("https://fcrepolocal/fcrepo/rest/pcdm/fc/75/16/91/fc751691-b55e-46d9-b7da-a5c7bf3f40f3")));
    assertTrue(model.contains(auditEvent, HAS_EVENT_OUTCOME, FAILED));
    assertTrue(model.contains(auditEvent, HAS_EVENT_DATE_TIME));
    assertTrue(model.contains(auditEvent, HAS_EVENT_OUTCOME_DETAIL, fixityResult));
    assertTrue(model.contains(fixityResult, RDF_TYPE, EVENT_OUTCOME_DETAIL));
    assertTrue(model.contains(fixityResult, RDF_TYPE, FIXITY));
    assertTrue(model.contains(fixityResult, HAS_EVENT_OUTCOME, "BAD_CHECKSUM"));
    assertTrue(model.contains(fixityResult, HAS_EVENT_OUTCOME, "BAD_SIZE"));
    assertTrue(model.contains(fixityResult, HAS_MESSAGE_DIGEST,
        model.createResource("urn:sha1:63edda4d89dc93b578c44013cada91aab492614b")));
    assertTrue(model.contains(fixityResult, HAS_MESSAGE_DIGEST_ALGORITHM, "SHA-1"));
    assertTrue(model.contains(fixityResult, HAS_SIZE, model.createTypedLiteral(32277713)));
  }
}
