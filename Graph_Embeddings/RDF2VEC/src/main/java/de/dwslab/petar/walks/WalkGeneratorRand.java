package de.dwslab.petar.walks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class WalkGeneratorRand {
	public static final Logger log = LoggerFactory
			.getLogger(WalkGeneratorRand.class);

	public static String directPropsQuery = "SELECT ?p ?o WHERE {$ENTITY$ ?p ?o . FILTER(!isLiteral(?o))}";

	/**
	 * defines the depth of the walk (only nodes are considered as a step)
	 */
	public static int depthWalk = 7;
	/**
	 * defines the number of walks from each node
	 */
	public static int numberWalks = 200;

	/**
	 * the query for extracting paths
	 */
	public static String walkQuery = "";

	public static int processedEntities = 0;
	public static int processedWalks = 0;
	public static int fileProcessedLines = 0;

	public static long startTime = 0;

	/**
	 * the rdf model
	 */
	public static Model model;

	public static Dataset dataset;

	public static String fileName = "walks.txt";

	/**
	 * file writer for all the paths
	 */
	public static Writer writer;

	public void generateWalks(String repoLocation, String outputFile,
			int nmWalks, int dpWalks, int nmThreads, int offset, int limit) {
		// set the parameters
		numberWalks = nmWalks;
		depthWalk = dpWalks;
		fileName = outputFile;

		// int the writer
		try {
			writer = new OutputStreamWriter(new GZIPOutputStream(
					new FileOutputStream(outputFile, false)), "utf-8");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// generate the query
		walkQuery = generateQuery(depthWalk, numberWalks);

		// open the dataset
		dataset = TDBFactory.createDataset(repoLocation);

		model = dataset.getDefaultModel();
		System.out.println("SELECTING all entities from repo");
		List<String> entities = selectAllEntities(offset, limit);

		System.out.println("Total number of entities to process: "
				+ entities.size());
		ThreadPoolExecutor pool = new ThreadPoolExecutor(nmThreads, nmThreads,
				0, TimeUnit.SECONDS,
				new java.util.concurrent.ArrayBlockingQueue<Runnable>(
						entities.size()));

		startTime = System.currentTimeMillis();
		for (String entity : entities) {

			EntityThread th = new EntityThread(entity);

			pool.execute(th);

		}

		pool.shutdown();
		try {
			pool.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Selects all entities from the repo
	 * 
	 * @return
	 */
	public static List<String> selectAllEntities(int offset, int limit) {
		List<String> allEntities = new ArrayList<String>();

		// String queryStrign =
		// "Select ?s Where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing>} OFFSET "
		// + offset + "LIMIT " + limit;
		// use this for wikidat
		String queryStrign = "Select distinct ?s Where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t . FILTER(STRSTARTS(STR(?s), \"http://www.wikidata.org/entity/Q\"))} OFFSET "
				+ offset + "LIMIT " + limit;

		Query query = QueryFactory.create(queryStrign);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		while (results.hasNext()) {
			QuerySolution result = results.next();
			allEntities.add(result.get("s").toString());
		}
		qe.close();
		System.out.println("TOTAL ENTITIES: " + allEntities.size());
		return allEntities;
	}

	/**
	 * Adds new walks to the list; If the list is filled it is written to the
	 * file
	 * 
	 * @param tmpList
	 */
	public synchronized static void writeToFile(List<String> tmpList) {
		processedEntities++;
		processedWalks += tmpList.size();
		fileProcessedLines += tmpList.size();
		for (String str : tmpList)
			try {
				writer.write(str + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (processedEntities % 100 == 0) {
			System.out
					.println("TOTAL PROCESSED ENTITIES: " + processedEntities);
			System.out.println("TOTAL NUMBER OF PATHS : " + processedWalks);
			System.out.println("TOTAL TIME:"
					+ ((System.currentTimeMillis() - startTime) / 1000));
		}
		// flush the file
		if (fileProcessedLines > 3000000) {
			fileProcessedLines = 0;
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int tmpNM = (processedWalks / 3000000);
			String tmpFilename = fileName.replace(".txt", tmpNM + ".txt");
			try {
				writer = new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(tmpFilename, false)), "utf-8");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * generates the query with the given depth
	 * 
	 * @param depth
	 * @return
	 */
	public static String generateQuery(int depth, int numberWalks) {
		String selectPart = "SELECT ?p ?o1";
		String mainPart = "{ $ENTITY$ ?p ?o1  ";
		String query = "";
		int lastO = 1;
		for (int i = 1; i < depth; i++) {
			mainPart += ". ?o" + i + " ?p" + i + "?o" + (i + 1);
			selectPart += " ?p" + i + "?o" + (i + 1);
			lastO = i + 1;
		}
		String lastOS = "?o" + lastO;
		query = selectPart + " WHERE " + mainPart + " . FILTER(!isLiteral("
				+ lastOS
				+ ")). BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT "
				+ numberWalks;
		return query;
	}

	static class EntityThread implements Runnable {

		private String entity;

		private List<String> finalList;

		public EntityThread(String entity) {
			this.entity = entity;
			finalList = new ArrayList<String>();
		}

		@Override
		public void run() {
			processEntity();
			writeToFile(finalList);

		}

		private void processEntity() {

			// get all the walks
			List<String> tmpList = new ArrayList<String>();
			String queryStr = walkQuery.replace("$ENTITY$", "<" + entity + ">");
			executeQuery(queryStr, finalList);

			// get all the direct properties
			queryStr = directPropsQuery.replace("$ENTITY$", "<" + entity + ">");
			executeQuery(queryStr, finalList);

		}

		public void executeQuery(String queryStr, List<String> walkList) {
			Query query = QueryFactory.create(queryStr);
			dataset.begin(ReadWrite.READ);
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet resultsTmp = qe.execSelect();
			String entityShort = entity.replace("http://dbpedia.org/resource/",
					"dbr:");
			ResultSet results = ResultSetFactory.copyResults(resultsTmp);
			qe.close();
			dataset.end();
			while (results.hasNext()) {
				QuerySolution result = results.next();
				String singleWalk = entityShort + "->";
				// construct the walk from each node or property on the path
				for (String var : results.getResultVars()) {
					try {
						// clean it if it is a literal
						if (result.get(var) != null
								&& result.get(var).isLiteral()) {
							String val = result.getLiteral(var).toString();
							val = val.replace("\n", " ").replace("\t", " ")
									.replace("->", "");
							singleWalk += val + "->";
						} else if (result.get(var) != null) {
							singleWalk += result
									.get(var)
									.toString()
									.replace("http://dbpedia.org/resource/",
											"dbr:")
									.replace("http://dbpedia.org/ontology/",
											"dbo:")
									.replace(
											"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
											"rdf:")
									.replace(
											"http://www.w3.org/2000/01/rdf-schema#",
											"rdfs:").replace("->", "")
									+ "->";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				walkList.add(singleWalk.substring(0, singleWalk.length() - 2));
			}

		}
	}

	public static void main(String[] args) {

		System.out
				.println("USAGE:  repoLocation outputFile nmWalks dpWalks nmThreads");
		WalkGeneratorRand generator = new WalkGeneratorRand();
		generator.generateWalks(args[0], args[1], Integer.parseInt(args[2]),
				Integer.parseInt(args[3]), Integer.parseInt(args[4]),
				Integer.parseInt(args[5]), Integer.parseInt(args[6]));
	}
}
