package simpleindexing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SimpleReader1 {

	/**
	 * Project testlucene4.0.0 SimpleReader class reads the index SimpleIndex
	 * created with the SimpleIndexing class
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java SimpleReader SimpleIndex");
			return;
		}
		// SimpleIndex is the folder where the index SimpleIndex is stored

		File file = new File(args[0]);

		Directory dir = null;
		DirectoryReader indexReader = null;
		Document doc = null;

		List<IndexableField> fields = null;

		try {
			dir = FSDirectory.open(file);
			indexReader = DirectoryReader.open(dir);
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

		for (int i = 0; i < indexReader.maxDoc(); i++) {

			try {
				doc = indexReader.document(i);
			} catch (CorruptIndexException e1) {
				System.out.println("Graceful message: exception " + e1);
				e1.printStackTrace();
			} catch (IOException e1) {
				System.out.println("Graceful message: exception " + e1);
				e1.printStackTrace();
			}
			System.out.println("Documento " + i);
			System.out.println("modelRef = " + doc.get("modelRef"));
			System.out.println("modelAcronym = " + doc.get("modelAcronym"));
			System.out.println("modelDescription = "
					+ doc.get("modelDescription"));
			System.out.println("theoreticalContent = "
					+ doc.get("theoreticalContent"));
			System.out.println("practicalContent = "
					+ doc.get("practicalContent"));
		}

		/**
		 * Note doc.get() returns null for the fields that were not stored
		 */

		for (int i = 0; i < indexReader.maxDoc(); i++) {

			try {
				doc = indexReader.document(i);
			} catch (CorruptIndexException e1) {
				System.out.println("Graceful message: exception " + e1);
				e1.printStackTrace();
			} catch (IOException e1) {
				System.out.println("Graceful message: exception " + e1);
				e1.printStackTrace();
			}

			System.out.println("Documento " + i);

			fields = doc.getFields();

			for (IndexableField field : fields) {
				String fieldName = field.name();
				System.out.println(fieldName + ": " + doc.get(fieldName));

			}

		}

		try {
			indexReader.close();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}

	}
}
