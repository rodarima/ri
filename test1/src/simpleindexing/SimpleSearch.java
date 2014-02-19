package simpleindexing;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SimpleSearch {

	/**
	 * Project testlucene4.0.0 SimpleSearch class reads the index SimpleIndex
	 * created with the SimpleIndexing class, creates and Index Searcher and
	 * search for documents which contain the word "probability" in the field
	 * "modelDescription" using the StandardAnalyzer
	 * 
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java SimpleSearch SimpleIndex");
			return;
		}
		// SimpleIndex is the folder where the index SimpleIndex is stored

		File file = new File(args[0]);

		IndexReader reader = null;
		Directory dir = null;
		IndexSearcher searcher = null;
		QueryParser parser;
		Query query = null;

		try {
			dir = FSDirectory.open(file);
			reader = DirectoryReader.open(dir);

		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

		searcher = new IndexSearcher(reader);
		parser = new QueryParser(Version.LUCENE_40, "modelDescription",
				new StandardAnalyzer(Version.LUCENE_40));

		try {
			query = parser.parse("probability");
		} catch (ParseException e) {

			e.printStackTrace();
		}

		TopDocs topDocs = null;

		try {
			topDocs = searcher.search(query, 10);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		System.out
				.println("\n"
						+ topDocs.totalHits
						+ " results for query \""
						+ query.toString()
						+ "\" showing for the first "
						+ 10
						+ " documents the doc id, score and the content of the modelDescription field");

		for (int i = 0; i < Math.min(10, topDocs.totalHits); i++) {
			try {
				System.out.println(topDocs.scoreDocs[i].doc
						+ " -- score: "
						+ topDocs.scoreDocs[i].score
						+ " -- "
						+ reader.document(topDocs.scoreDocs[i].doc).get(
								"modelDescription"));
			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

		}

		// follows and example using a numeric field for sorting the results
		// by default this does not compute the scores of docs, since the
		// ranking is imposed by the sorting

		boolean reverse = true;
		try {
			topDocs = searcher.search(query, 10, new Sort(new SortField(
					"practicalContent", SortField.Type.INT, reverse)));
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		System.out
				.println("\n"
						+ topDocs.totalHits
						+ " results for query \""
						+ query.toString()
						+ "\" in the sort given by the field practicalContent, "
						+ "\" showing for the first "
						+ 10
						+ " documents the doc id, score and the content of the modelDescription field");

		for (int i = 0; i < Math.min(10, topDocs.totalHits); i++) {
			try {
				System.out.println(topDocs.scoreDocs[i].doc
						+ " -- score: "
						+ topDocs.scoreDocs[i].score
						+ " -- "
						+ reader.document(topDocs.scoreDocs[i].doc).get(
								"modelDescription"));
			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

		}

		// follows an example of a simple programmatic query

		BooleanQuery booleanQuery = new BooleanQuery();
		Query vector = new TermQuery(new Term("modelDescription", "vector"));
		Query space = new TermQuery(new Term("modelDescription", "space"));

		booleanQuery.add(vector, Occur.MUST);
		booleanQuery.add(space, Occur.MUST);

		try {
			topDocs = searcher.search(booleanQuery, 10);
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		System.out
				.println("\n"
						+ topDocs.totalHits
						+ " results for query \""
						+ booleanQuery.toString()
						+ "\" showing for the first "
						+ 10
						+ " documents the doc id, score and the content of the modelDescription field");

		for (int i = 0; i < Math.min(10, topDocs.totalHits); i++) {
			try {
				System.out.println(topDocs.scoreDocs[i].doc
						+ " -- score: "
						+ topDocs.scoreDocs[i].score
						+ " -- "
						+ reader.document(topDocs.scoreDocs[i].doc).get(
								"modelDescription"));
			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

		}

		try {
			// searcher.close(); It was necessary in Lucene 3.x
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
