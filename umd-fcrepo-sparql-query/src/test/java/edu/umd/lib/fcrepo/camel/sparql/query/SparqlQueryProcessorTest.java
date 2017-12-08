package edu.umd.lib.fcrepo.camel.sparql.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

public class SparqlQueryProcessorTest {
  private String query;

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
}
