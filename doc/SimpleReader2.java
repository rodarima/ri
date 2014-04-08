package simpleindexing;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CompositeReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SimpleReader2 {

	/**
	 * Project testlucene4.0.0 SimpleReader class reads the index SimpleIndex
	 * created with the SimpleIndexing class
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.out.println("Usage: java SimpleReader SimpleIndex");
			return;
		}
		// SimpleIndex is the folder where the index SimpleIndex is stored

		File file = new File(args[0]);

		Directory dir = null;
		IndexReader indexReader = null;
		SlowCompositeReaderWrapper atomicReader = null;
		/*
		 * SlowCompositeReaderWrapper emulates an atomic reader allowing the
		 * access to fields and terms
		 */

		Fields fields = null;
		Terms terms = null;
		TermsEnum termsEnum = null;

		try {
			dir = FSDirectory.open(file);
			indexReader = DirectoryReader.open(dir);
			atomicReader = new SlowCompositeReaderWrapper(
					(CompositeReader) indexReader);

		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}

		fields = atomicReader.fields();
		for (String field : fields) {
			if (!(field.equals("practicalContent") || field
					.equals("theoreticalContent"))) {
				System.out.println("Field = " + field);
				terms = fields.terms(field);
				termsEnum = terms.iterator(null);
				while (termsEnum.next() != null) {
					String tt = termsEnum.term().utf8ToString();
					System.out.println(tt + " totalFreq()="
							+ termsEnum.totalTermFreq() + " docFreq="
							+ termsEnum.docFreq());

				}
			}
		}

		// totalFreq equals -1 if the value was not stored in the codification
		// of this index

		/*
		 * Terms are ByteRef in lucene 4.x (see Class ByteRef of lucene api),
		 * for this reason the above code is not the best for printing numeric
		 * fields
		 */

		Term t = new Term("modelDescription", "probability");
		Document d = new Document();
		DocsEnum docsEnum = atomicReader.termDocsEnum(t);
		int doc;
		while ((doc = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
			System.out
					.println("El término (field=modelDescription text=probability) aparece en el documento número "
							+ doc);
			d = atomicReader.document(doc);
			System.out
					.println("modelDescription= " + d.get("modelDescription"));
		}

		try {
			atomicReader.close();
			indexReader.close();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}

	}
}
