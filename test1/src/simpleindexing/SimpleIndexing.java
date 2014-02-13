package simpleindexing;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public class SimpleIndexing {

	/**
	 * Project testlucene4.0.0 SimpleIndexing class write a lucene index with
	 * some small documents. If the index already exists, the documents are
	 * appended to the index
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java SimpleIndexing indexFolder");
			return;
		}

		String modelRef[] = new String[4];

		String modelDescription[] = new String[4];
		String modelAcronym[] = new String[4];
		int theoreticalContent = 10;
		int practicalContent = 10;

		modelRef[0] = "RM000";
		modelRef[1] = "RM001";
		modelRef[2] = "RM002";
		modelRef[3] = "RM003";

		modelAcronym[0] = "BM";
		modelAcronym[1] = "VSM";
		modelAcronym[2] = "CPM";
		modelAcronym[3] = "LM";

		modelDescription[0] = "The boolean model is a simple retrieval model where queries are interpreted as boolean expressions and documents are bag of words";
		modelDescription[1] = "The vector space model is a simple retrieval model where queries and documents are vectors of terms and similarity of queries and documents is computed with the cosine distance";
		modelDescription[2] = "In the classic probabilistic retrieval model the probability of relevance of a document given a query is computed under the binary and independence assumptions";
		modelDescription[3] = "The use of language models for retrieval implies the estimation of the probability of generating a query given a document";

		String indexFolder = args[0];

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40,
				new StandardAnalyzer(Version.LUCENE_40));
		IndexWriter writer = null;

		try {
			writer = new IndexWriter(FSDirectory.open(new File(indexFolder)),
					config);
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (LockObtainFailedException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

		/*
		 * With these calls to IndexWriterConfig and IndexWriter this program
		 * creates a new index if one does not exist, otherwise it opens the
		 * index and documents will be appended with writer.addDocument(doc).
		 */

		for (int i = 0; i < modelRef.length; i++) {
			Document doc = new Document();
			/*
			 * Each document has five fields. modelRef is a StringField which is
			 * indexed and not tokenized. modelAcronym is a StringField which is
			 * indexed and not tokenized, addtionally it is stored.
			 * modelDescription is a TextField which is indexed and tokenized,
			 * additionally it is stored. theoreticalContent is a NumericField
			 * that is indexed. practicalContent is a NumericField that is
			 * indexed, additionally it is stored.
			 */
			doc.add(new StringField("modelRef", modelRef[i], null));
			doc.add(new Field("modelAcronym", modelAcronym[i],
					StringField.TYPE_STORED));

			doc.add(new TextField("modelDescription", modelDescription[i],
					Field.Store.YES));

			doc.add(new IntField("theoreticalContent", theoreticalContent++,
					Field.Store.NO));
			doc.add(new IntField("practicalContent", practicalContent++,
					Field.Store.YES));

			try {

				writer.addDocument(doc);
			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}

			System.out.println("wrote document " + i + " in the index");

		}

		try {
			writer.commit();
			writer.close();
		} catch (CorruptIndexException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}

	}
}
