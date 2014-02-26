package reuters21578indexing;

import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParsePosition;
import java.util.Date;

import org.apache.lucene.document.DateTools;

public class Reuters21578Parser {

	/*
	 * Project testlucene 3.6.0, the Reuters21578Parser class parses the
	 * collection.
	 */

	private static final String END_BOILERPLATE_1 = " Reuter\n&#3;";
	private static final String END_BOILERPLATE_2 = " REUTER\n&#3;";

	// private static final String[] TOPICS = { "acq", "alum", "austdlr",
	// "barley", "bean", "belly", "bfr", "bop", "cake", "can", "carcass",
	// "castor", "castorseed", "cattle", "chem", "citruspulp", "cocoa",
	// "coconut", "coffee", "copper", "copra", "corn", "cornglutenfeed",
	// "cotton", "cottonseed", "cpi", "cpu", "crude", "cruzado", "debt",
	// "dfl", "dkr", "dlr", "dmk", "earn", "f", "feed", "fishmeal",
	// "fuel", "fx", "gas", "gnp", "gold", "grain", "groundnut", "heat",
	// "hk", "hog", "housing", "income", "instal", "interest",
	// "inventories", "ipi", "iron", "jet", "jobs", "l", "lead", "lei",
	// "lin", "linseed", "lit", "livestock", "lumber", "meal", "metal",
	// "money", "naphtha", "nat", "nickel", "nkr", "nzdlr", "oat", "oil",
	// "oilseed", "orange", "palladium", "palm", "palmkernel", "peseta",
	// "pet", "platinum", "plywood", "pork", "potato", "propane", "rand",
	// "rape", "rapeseed", "red", "reserves", "retail", "rice", "ringgit",
	// "rubber", "rupiah", "rye", "saudriyal", "sfr", "ship", "silver",
	// "skr", "sorghum", "soy", "soybean", "steel", "stg", "strategic",
	// "sugar", "sun", "sunseed", "supply", "tapioca", "tea", "tin",
	// "trade", "veg", "wheat", "wool", "wpi", "yen", "zinc" };

	public static List<List<String>> parseString(StringBuffer fileContent) {
		/* First the contents are converted to a string */
		String text = fileContent.toString();

		/*
		 * The method split of the String class splits the strings using the
		 * delimiter which was passed as argument Therefor lines is an array of
		 * strings, one string for each line
		 */
		String[] lines = text.split("\n");

		/*
		 * For each Reuters article the parser returns a list of strings where
		 * each element of the list is a field (TITLE, BODY, TOPICS, DATELINE).
		 * Each *.sgm file that is passed in fileContent can contain many
		 * Reuters articles, so finally the parser returns a list of list of
		 * strings, i.e, a list of reuters articles, that is what the object
		 * documents contains
		 */

		List<List<String>> documents = new LinkedList<List<String>>();

		/* The tag REUTERS identifies the beginning and end of each article */

		for (int i = 0; i < lines.length; ++i) {
			if (!lines[i].startsWith("<REUTERS"))
				continue;
			StringBuilder sb = new StringBuilder();
			while (!lines[i].startsWith("</REUTERS")) {
				sb.append(lines[i++]);
				sb.append("\n");
			}
			/*
			 * Here the sb object of the StringBuilder class contains the
			 * Reuters article which is converted to text and passed to the
			 * handle document method that will return the document in the form
			 * of a list of fields
			 */
			documents.add(handleDocument(sb.toString()));
		}
		return documents;
	}

	public static List<String> handleDocument(String text) {

		/*
		 * This method returns the Reuters article that is passed as text as a
		 * list of fields
		 */

		/* The fields TOPICS, TITLE, DATELINE and BODY are extracted */
		/* Each topic inside TOPICS is identified with a tag D */
		/* If the BODY ends with boiler plate text, this text is removed */

		String topics = extract("TOPICS", text, true);
		String title = extract("TITLE", text, true);
		String dateline = extract("DATELINE", text, true);
		String body = extract("BODY", text, true);
		String text_date = extract("DATE", text, true);
		
		if (body.endsWith(END_BOILERPLATE_1))
		{
			int last = body.length() - END_BOILERPLATE_1.length();
			body = body.substring(0, last);
		}
		else if (body.endsWith(END_BOILERPLATE_2))
		{
			int last = body.length() - END_BOILERPLATE_2.length();
			body = body.substring(0, last);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SS", Locale.US);
		Date date = dateFormat.parse(text_date, new ParsePosition(0));
		String date_string = DateTools.dateToString(date, DateTools.Resolution.MILLISECOND);

		List<String> document = new LinkedList<String>();
		document.add(title);
		document.add(body);
		document.add(
			topics.
			replaceAll("\\<D\\>", " ").
			replaceAll("\\<\\/D\\>","")
		);
		document.add(date_string);
		return document;
	}

	private static String extract(String elt, String text, boolean allowEmpty) {

		/*
		 * This method find the tags for the field elt in the String text and
		 * extracts and returns the content
		 */
		/*
		 * If the tag does not exists and the allowEmpty argument is true, the
		 * method returns the null string, if allowEmpty is false it returns a
		 * IllegalArgumentException
		 */

		String startElt = "<" + elt + ">";
		String endElt = "</" + elt + ">";
		int startEltIndex = text.indexOf(startElt);
		if (startEltIndex < 0) {
			if (allowEmpty)
				return "";
			throw new IllegalArgumentException("no start, elt=" + elt
					+ " text=" + text);
		}
		int start = startEltIndex + startElt.length();
		int end = text.indexOf(endElt, start);
		if (end < 0)
			throw new IllegalArgumentException("no end, elt=" + elt + " text="
					+ text);
		return text.substring(start, end);
	}

}


//package org.apache.lucene.demo;
//
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.LongField;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.document.TextField;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig.OpenMode;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Version;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Date;
//
///** Index all text files under a directory.
// * <p>
// * This is a command-line application demonstrating simple Lucene indexing.
// * Run it with no command-line arguments for usage information.
// */
//public class IndexFiles {
//	
//	private IndexFiles() {}
//
//	/** Index all text files under a directory. */
//	public static void main(String[] args) {
//		String usage = 
//"java reuters21578indexing.Reuters21578Parser\n" +
//"[-openmode MODE]  Especifica MODE = { \"append\", \"create\", \"create_or_append\" }\n"+
//"                  con el que se abrirá el índice. Si no se especifica se usa el\n"+
//"                  valor por defecto \"create\"\n"+
//"\n"+
//"-index PATH       PATH es la carpeta en la que se almacenará el índice.\n"+
//"\n"+
//"-files PATH       PATH es la carpeta que contiene los archivos \"*.sgm\".\n"
//"\n"+
//"[-onlyfiles N]    N es el número que especifica el fichero \"reut2-0NN.sgm\""+
//"                  donde N es un entero de 0 a 21 inclusive. Si no se\n"+
//"                  especifica se usarán todos los ficheros \"*.sgm\" que se\n"+
//"                  encuentren en el directorio.\n"+
//"\n"+
//"[-addsgmfile]     Incluye un campo adicional en el índice que contiene el\n"+
//"                  nombre del archivo \"*.sgm\" donde se encuentra el artículo\n"+
//"                  Reuters\n"+
//"\n"+
//"[-delete T F]     Borra los documentos del índice que contienen el término \"T\"\n"
//"                  en el campo \"F\"";
//
//
//
//		String indexPath = "index";
//		String docsPath = null;
//		boolean create = true;
//		for(int i=0;i<args.length;i++) {
//			if ("-index".equals(args[i])) {
//				indexPath = args[i+1];
//				i++;
//			} else if ("-docs".equals(args[i])) {
//				docsPath = args[i+1];
//				i++;
//			} else if ("-update".equals(args[i])) {
//				create = false;
//			}
//		}
//
//		if (docsPath == null) {
//			System.err.println("Usage: " + usage);
//			System.exit(1);
//		}
//
//		final File docDir = new File(docsPath);
//		if (!docDir.exists() || !docDir.canRead()) {
//			System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
//			System.exit(1);
//		}
//		
//		Date start = new Date();
//		try {
//			System.out.println("Indexing to directory '" + indexPath + "'...");
//
//			Directory dir = FSDirectory.open(new File(indexPath));
//			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
//			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
//
//			if (create) {
//				// Create a new index in the directory, removing any
//				// previously indexed documents:
//				iwc.setOpenMode(OpenMode.CREATE);
//			} else {
//				// Add new documents to an existing index:
//				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
//			}
//
//			// Optional: for better indexing performance, if you
//			// are indexing many documents, increase the RAM
//			// buffer.	But if you do this, increase the max heap
//			// size to the JVM (eg add -Xmx512m or -Xmx1g):
//			//
//			// iwc.setRAMBufferSizeMB(256.0);
//
//			IndexWriter writer = new IndexWriter(dir, iwc);
//			indexDocs(writer, docDir);
//
//			// NOTE: if you want to maximize search performance,
//			// you can optionally call forceMerge here.	This can be
//			// a terribly costly operation, so generally it's only
//			// worth it when your index is relatively static (ie
//			// you're done adding documents to it):
//			//
//			// writer.forceMerge(1);
//
//			writer.close();
//
//			Date end = new Date();
//			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
//
//		} catch (IOException e) {
//			System.out.println(" caught a " + e.getClass() +
//			 "\n with message: " + e.getMessage());
//		}
//	}
//
//	/**
//	 * Indexes the given file using the given writer, or if a directory is given,
//	 * recurses over files and directories found under the given directory.
//	 * 
//	 * NOTE: This method indexes one document per input file.	This is slow.	For good
//	 * throughput, put multiple documents into your input file(s).	An example of this is
//	 * in the benchmark module, which can create "line doc" files, one document per line,
//	 * using the
//	 * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
//	 * >WriteLineDocTask</a>.
//	 *	
//	 * @param writer Writer to the index where the given file/dir info will be stored
//	 * @param file The file to index, or the directory to recurse into to find files to index
//	 * @throws IOException If there is a low-level I/O error
//	 */
//	static void indexDocs(IndexWriter writer, File file)
//		throws IOException {
//		// do not try to index files that cannot be read
//		if (file.canRead()) {
//			if (file.isDirectory()) {
//				String[] files = file.list();
//				// an IO error could occur
//				if (files != null) {
//					for (int i = 0; i < files.length; i++) {
//						indexDocs(writer, new File(file, files[i]));
//					}
//				}
//			} else {
//
//				FileInputStream fis;
//				try {
//					fis = new FileInputStream(file);
//				} catch (FileNotFoundException fnfe) {
//					// at least on windows, some temporary files raise this exception with an "access denied" message
//					// checking if the file can be read doesn't help
//					return;
//				}
//
//				try {
//
//					// make a new, empty document
//					Document doc = new Document();
//
//					// Add the path of the file as a field named "path".	Use a
//					// field that is indexed (i.e. searchable), but don't tokenize 
//					// the field into separate words and don't index term frequency
//					// or positional information:
//					Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
//					doc.add(pathField);
//
//					// Add the last modified date of the file a field named "modified".
//					// Use a LongField that is indexed (i.e. efficiently filterable with
//					// NumericRangeFilter).	This indexes to milli-second resolution, which
//					// is often too fine.	You could instead create a number based on
//					// year/month/day/hour/minutes/seconds, down the resolution you require.
//					// For example the long value 2011021714 would mean
//					// February 17, 2011, 2-3 PM.
//					doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));
//
//					// Add the contents of the file to a field named "contents".	Specify a Reader,
//					// so that the text of the file is tokenized and indexed, but not stored.
//					// Note that FileReader expects the file to be in UTF-8 encoding.
//					// If that's not the case searching for special characters will fail.
//					doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
//
//					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
//						// New index, so we just add the document (no old document can be there):
//						System.out.println("adding " + file);
//						writer.addDocument(doc);
//					} else {
//						// Existing index (an old copy of this document may have been indexed) so 
//						// we use updateDocument instead to replace the old one matching the exact 
//						// path, if present:
//						System.out.println("updating " + file);
//						writer.updateDocument(new Term("path", file.getPath()), doc);
//					}
//					
//				} finally {
//					fis.close();
//				}
//			}
//		}
//	}
//}
