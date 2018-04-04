package de.dwslab.ar.jena.io;

import java.io.File;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class BulkLoader {

	public static void loadDataset(String storageDir, String inputDir) {

		System.out.println("WE start the code!!");
		Dataset dataset = TDBFactory.createDataset(storageDir);

		// assume we want the default model, or we could get a named model here
		Model tdb = dataset.getDefaultModel();

		// read the input file - only needs to be done once

		File folder = new File(inputDir);
		if (folder.listFiles().length > 0)
			System.out.println("We are at the correct file");
		for (final File fileEntry : folder.listFiles()) {

			System.out.println(fileEntry.getName());
			if (fileEntry.getName().contains(".bz2"))
				continue;
			System.out.println(fileEntry.getAbsolutePath());
			System.out.println(fileEntry.getPath());
			try {
				FileManager.get().readModel(tdb, fileEntry.getPath());
			} catch (Exception e) {
				System.out.println("File didn't finish");
				e.printStackTrace();
			}
		}
		try {
			// run a query
			String q = "SELECT DISTINCT ?entity WHERE {\n"
					+ "        ?entity <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type .\n"
					+ "        }";
			Query query = QueryFactory.create(q);
			QueryExecution qexec = QueryExecutionFactory.create(query, tdb);
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.out(System.out, results, query);
		} catch (Exception e) {
			System.out.println("The stupid query failed");
		}
		dataset.close();
	}

	public static void main(String[] args) {
		String storageDir = args[0];
		String inputDir = args[1];
		System.out.println("Storage Dir: "+storageDir);
		System.out.println("Input Dir: "+inputDir);
		loadDataset(args[0], args[1]);
		// loadDBpeida(
		// "dbpediaTDBrepo",
		// "C:\\Users\\petar\\Documents\\ProjectsFiles\\DeepLearning\\Datasets\\Original\\DBpediaTTsadL");
//		Dataset dataset = TDBFactory.createDataset(args[0]);
//		Model tdb = dataset.getDefaultModel();
//		try {
//			// run a query
//			String q = "SELECT DISTINCT ?author WHERE {\n"
//					+ "        ?paper <http://xmlns.com/foaf/0.1/maker> ?author .\n"
//					+ "        }";
//			Query query = QueryFactory.create(q);
//			QueryExecution qexec = QueryExecutionFactory.create(query, tdb);
//			ResultSet results = qexec.execSelect();
//			ResultSetFormatter.out(System.out, results, query);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
