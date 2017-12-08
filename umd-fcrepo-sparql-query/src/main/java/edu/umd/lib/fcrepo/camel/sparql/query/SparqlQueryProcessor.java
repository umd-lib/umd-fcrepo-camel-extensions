package edu.umd.lib.fcrepo.camel.sparql.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlQueryProcessor implements Processor {
  private Logger log = LoggerFactory.getLogger(SparqlQueryProcessor.class);

  public static final String CSV_WITHOUT_HEADER = "csvWithoutHeader";

  private final String query;
  private final String resultsFormatName;
  private final ResultsFormat resultsFormat;

  public SparqlQueryProcessor(String query, String resultsFormatName) {
    this.query = query;
    this.resultsFormatName = resultsFormatName.trim();
    resultsFormat = ResultsFormat.lookup(resultsFormatName);

    if ((resultsFormat == null) &&
        !(CSV_WITHOUT_HEADER.toLowerCase().equals(resultsFormatName.toLowerCase()))) {
      throw new IllegalArgumentException("Unknown resultFormatName: " + resultsFormatName);
    }
  }

  @Override
  public void process(final Exchange exchange) throws IOException {
    final Object body = exchange.getIn().getBody();
    InputStream in = new ByteArrayInputStream((byte[]) body);

    String result = executeQuery(in);
    exchange.getIn().setBody(result);
  }

  protected String executeQuery(InputStream in) {
    log.debug("Executing query: {}, resultFormatName: {}", query, resultsFormatName);
    Model model = ModelFactory.createDefaultModel();
    model.read(in, null);

    // Create a new query
    Query q = QueryFactory.create(query);

    // Execute the query and obtain results
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (QueryExecution qe = QueryExecutionFactory.create(q, model)) {
      ResultSet results = qe.execSelect();

      ResultsFormat resultsFormat = ResultsFormat.lookup(resultsFormatName);
      if (resultsFormat == null) {
        if ((resultsFormatName != null) && CSV_WITHOUT_HEADER.toLowerCase().equals(resultsFormatName.toLowerCase())) {
          CsvWithoutHeaderOutput csvOutput = new CsvWithoutHeaderOutput();
          csvOutput.format(out, results);
        } else {
          throw new IllegalArgumentException("Unknown resultFormatName: " + resultsFormatName);
        }
      } else {
        ResultSetFormatter.output(out, results, resultsFormat);
      }
    }

    return out.toString();
  }
}
