package de.dwslab.petar.walks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PathCleaner {

	public static void cleanPaths(String inputFolder, String outputFolder) {

		File inputFol = new File(inputFolder);
		for (File f : inputFol.listFiles()) {
			if (f.getName().contains("page-links"))
				continue;
			BufferedReader br = null;
			Writer writer = null;
			try {
				Map<String, Integer> seenPaths = new HashMap<String, Integer>();
				InputStream fileStream = new FileInputStream(
						f.getAbsolutePath());
				InputStream gzipStream = new GZIPInputStream(fileStream);
				Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
				writer = new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(outputFolder + "/" + f.getName(),
								false)), "utf-8");

				br = new BufferedReader(decoder);

				String line = "";

				int counter = 0;

				while ((line = br.readLine()) != null) {
					counter++;
					if (counter % 100000 == 0)
						System.out.println("Line nm: " + counter);
					String newLine = "";
					try {
						String parts[] = line.split("->");
						String lastPart = parts[parts.length - 1];
						if (lastPart.length() > 0
								&& (!lastPart.contains("dbo:")
										|| !lastPart.contains("dbr:")
										|| lastPart.contains("@")
										|| lastPart.contains("^^") || parts[parts.length - 2]
										.contains("wikiPageExternalLink"))) {
							// for (int i = 0; i < parts.length - 2; i++) {
							// newLine += parts[i] + "->";
							// }
							newLine = line.substring(0, line.lastIndexOf("->"));
							newLine = newLine.substring(0,
									newLine.lastIndexOf("->"));

						} else {
							newLine = line;
						}
						if (!seenPaths.containsKey(newLine)) {
							seenPaths.put(newLine, 1);
							writer.write(newLine.replace("->", " ") + "\n");
							// System.out.println(newLine);
						}

					} catch (Exception e) {
						// TODO: handle exception
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public static void main(String[] args) {

		cleanPaths(args[0], args[1]);
		// cleanPaths("C:/Users/petar/Desktop/input",
		// "C:/Users/petar/Desktop/output");
	}
}
