package de.dwslab.petar.jena.io;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class QueryManager {

	public static void runQuery(String repoDir, String queryString) {
		Dataset dataset = TDBFactory.createDataset(repoDir);

		Model model = dataset.getDefaultModel();

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		ResultSetFormatter.out(System.out, results, query);
		// iterate all municipal districts
		while (results.hasNext()) {
			QuerySolution result = results.next();

		}

		// Important - free up resources used running the query
		qe.close();
	}

	public static void main(String[] args) {
		runQuery(args[0], args[1]);
	}
}
