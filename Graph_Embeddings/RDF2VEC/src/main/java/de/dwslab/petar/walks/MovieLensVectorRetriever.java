package de.dwslab.petar.walks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MovieLensVectorRetriever {
	public static Map<String, String> instancesInfo = new HashMap<String, String>();

	public static void retrieveVectors(String modelInput,
			String inpuInstancesFile, String outputFile) {

		List<String> instances = new ArrayList<String>();
		instances = readInstances(inpuInstancesFile);
		BufferedReader br = null;
		int counterLInes = 0;
		int foundInstances = 0;
		int totalInstances = instances.size();
		try {
			InputStream fileStream = new FileInputStream(modelInput);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile, false), "utf-8"));
			br = new BufferedReader(decoder);

			String line = "";

			while ((line = br.readLine()) != null) {
				counterLInes++;
				if (counterLInes % 1000 == 0)
					System.out.println("processed lines: " + counterLInes);

				String res = line.split(" ")[0];
				if (instances.contains(res)) {
					instances.remove(res);
					res = res.replace("dbr:", "http://dbpedia.org/resource/");
					writer.write(instancesInfo.get(res)
							+ "\t"
							+ line.replace(" ", "\t").replace("dbr:",
									"http://dbpedia.org/resource/") + "\n");
					foundInstances++;
					System.out.println("FOUND instances: " + foundInstances
							+ " out of " + totalInstances);
				}
				if (instances.size() == 0)
					break;
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<String> readInstances(String inpuInstancesFile) {
		List<String> instances = new ArrayList<String>();
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(inpuInstancesFile));

			String line = "";

			while ((line = br.readLine()) != null) {
				String parts[] = line.split("\t");
				String info = parts[0] + "\t" + parts[1];
				String res = parts[2];
				instancesInfo.put(res, info);
				instances.add(res.replace("http://dbpedia.org/resource/",
						"dbr:"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return instances;
	}

	public static void main(String[] args) {
		retrieveVectors(args[0], args[1], args[2]);
	}

}
