package p2;

import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParsePosition;
import java.util.Date;
import java.lang.Integer;
import java.io.File;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
//import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
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
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.ByteBuffer;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.HashSet;
//import java.io.InputStreamReader;
//import java.util.Date;

public class IndexesReader
/* BEGIN OF CLASS */
{
/* BEGIN OF CLASS */

public static void usage()
{
	String usage = 
	"Este programa realiza operaciones de lectura en índices sobre la \n"+
	"colección Reuters.\n"+
	"\n"+
	"Uso del programa:\n"+
	"    java p2.IndexesReader INDEX OPERATION\n"+
	"\n"+
	"La opción \"INDEX\" es la siguiente:\n"+
	"\n"+
	"-index PATH       PATH es la carpeta donde se encuentra el índice.\n"+
	"\n"+
	"La opción \"OPERATION\" es una y sólo una de las siguientes:\n"+
	"\n"+
	"-doc I            Muestra los contenidos del documento \"I\".\n"+
	"\n"+
	"-docs I J         Muestra los contenidos de los documentos del rango \"I\" a \"J\".\n"+
	"\n"+
	"-write FILE       Vuelca los contenidos del índice en formato texto plano\n"+
	"                  sobre el fichero \"file\"\n"+
	"\n"+
	"-t+ N FIELD       Muestra los términos del campo \"FIELD\" con docFreq mayor o igual\n"+
	"                  que \"N\". Equivale a -termsdfmorethan\n"+
	"\n"+
	"-t- N FIELD       Muestra los términos del campo \"FIELD\" con docFreq menor o igual\n"+
	"                  que \"N\". Equivale a -termsdflessthan\n"+
	"\n"+
	"-t MIN MAX FIELD  Muestra los términos del campo \"FIELD\" con docFreq entre \"MIN\"\n"+
	"                  y \"MAX\". Equivale a -termsdfrango\n"+
	"\n"+
	"-i+ N FIELD PATH  Construye un índice en la carpeta \"PATH\" con los docs en los que\n"+
	"                  aparecen los términos del campo \"FIELD\" con docFreq mayor o igual\n"+
	"                  que \"N\". Equivale a -indexdocstermsdfmorethan\n"+
	"\n"+
	"-i- N FIELD PATH  Construye un índice en la carpeta \"PATH\" con los docs en los que\n"+
	"                  aparecen los términos del campo \"FIELD\" con docFreq menor o igual\n"+
	"                  que \"N\". Equivale a -indexdocstermsdflessthan\n"+
	"\n"+
	"-i MIN MAX FIELD PATH\n"+
	"                  Construye un índice en la carpeta \"PATH\" con los docs en los que\n"+
	"                  aparecen los términos del campo \"FIELD\" con docFreq entre \"MIN\"\n"+
	"                  y \"MAX\". Equivale a -indexdocstermsdfrango\n"+
	"\n"+
	"-id I J PATH      Construye un índice en la carpeta \"PATH\" con los docs del rango \"I\"\n"+
	"                  a \"J\". Equivale a -indexdocsij\n"+
	"\n"+
	"-merge A B        Fusiona el índice con el contenido en la carpeta \"A\", el resultado\n"+
	"                  fusionado está en \"B\". Equivale a -mergeindexes\n"+
	"";
	
	System.out.print(usage);
}

public static boolean testArgs(String[] args, int i, int n)
{
	if(i+n >= args.length)
	{
		System.out.print("Opción \""+args[i]+"\": Faltan argumentos\n");
		return false;
	}
	return true;
}

public static void main(String[] args)
{
	if(args.length == 0)
	{
		usage();
		return;
	}

	String index_path = null;
	int op = 0;
	String arg1 = null;
	String arg2 = null;
	String arg3 = null;
	String arg4 = null;
	int n1 = -1;
	int n2 = -1;
	int n3 = -1;

	boolean index_status = false;
	boolean operation_status = false;

	for(int i = 0; i<args.length; i++)
	{
		String arg = args[i];

		if(operation_status && index_status)
		{
			System.out.print("No se esperaban más argumentos\n");
			return;
		}
		
		if("-index".equals(arg))
		{
			if(index_status)
			{
				System.out.print("Opción \"-index\": Carpeta de índice repetida\n");
				return;
			}
			index_status = true;
			if(!testArgs(args, i, 1)) return;
			String path = args[i+1];
			if(!is_readable_file(path))
			{
				System.out.print("Opción \"-index\": Carpeta de índice inexistente o ilegible\n");
				return;
			}
			if(!is_dir(path))
			{
				System.out.print("Opción \"-index\": No es una carpeta\n");
				return;
			}
			
			index_path = path;
			i++;
		}
		else if("-doc".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 1)) return;
			op = 1;
			arg1 = args[++i];
			if(!is_integer(arg1)) return;
			n1 = Integer.parseInt(arg1);
		}
		else if("-docs".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 2;
			arg1 = args[++i];
			arg2 = args[++i];
			if(!is_integer(arg1)) return;
			if(!is_integer(arg2)) return;
			n1 = Integer.parseInt(arg1);
			n2 = Integer.parseInt(arg2);
		}
		else if("-write".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 1)) return;
			op = 3;
			arg1 = args[++i];
		}
		else if("-t+".equals(arg) || "-termsdfmorethan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 4;
			arg1 = args[++i];
			arg2 = args[++i];
			if(!is_integer(arg1)) return;
			n1 = Integer.parseInt(arg1);
		}
		else if("-t-".equals(arg) || "-termsdflessthan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 5;
			arg1 = args[++i];
			arg2 = args[++i];
			if(!is_integer(arg1)) return;
			n1 = Integer.parseInt(arg1);
		}
		else if("-t".equals(arg) || "-termsdfrango".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 6;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
			if(!is_integer(arg1)) return;
			if(!is_integer(arg2)) return;
			n1 = Integer.parseInt(arg1);
			n2 = Integer.parseInt(arg2);
		}
		else if("-i+".equals(arg) || "-indexdocstermsdfmorethan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 7;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
			if(!is_integer(arg1)) return;
			n1 = Integer.parseInt(arg1);
		}
		else if("-i-".equals(arg) || "-indexdocstermsdflessthan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 8;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
			if(!is_integer(arg1)) return;
			n1 = Integer.parseInt(arg1);
		}
		else if("-i".equals(arg) || "-indexdocstermsdfrango".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 4)) return;
			op = 9;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
			arg4 = args[++i];
			if(!is_integer(arg1)) return;
			if(!is_integer(arg2)) return;
			n1 = Integer.parseInt(arg1);
			n2 = Integer.parseInt(arg2);
		}
		else if("-id".equals(arg) || "-indexdocsij".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 10;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
			if(!is_integer(arg1)) return;
			if(!is_integer(arg2)) return;
			n1 = Integer.parseInt(arg1);
			n2 = Integer.parseInt(arg2);
		}
		else if("-merge".equals(arg) || "-mergeindexes".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 11;
			arg1 = args[++i];
			arg2 = args[++i];
		}
		else
		{
			System.out.print("Opción \""+arg+"\" desconocida\n");
			return;
		}
	}
	if(!index_status)
	{
		System.out.print("Falta la opción \"-index\"\n");
		return;
	}
	if(!operation_status)
	{
		System.out.print("Falta la operación\n");
		return;
	}

	//System.out.print("Argumentos correctos\n");
	DirectoryReader reader = null;
	try
	{
		reader = indexReader(index_path);
	}
	catch (IOException e)
	{
		System.out.print("No se pudo abrir el índice para leer\n");
		return;
	}

	switch(op)
	{
		case 1:	showDoc(reader, n1, n1+1);
			break;
		case 2:	showDoc(reader, n1, n2+1);
			break;
		case 3: dumpDocs(reader, arg1);
			break;
		
		case 4: showFreq(reader, n1, -1, arg2);
			break;
		case 5: showFreq(reader, -1, n1, arg2);
			break;
		case 6: showFreq(reader, n1, n2, arg3);
			break;

		case 7: indexFreq(reader, n1, -1, arg2, arg3);
			break;
		case 8: indexFreq(reader, -1, n1, arg2, arg3);
			break;
		case 9: indexFreq(reader, n1, n2, arg3, arg4);
			break;

		case 10:indexDocs(reader, n1, n2+1, arg3);
			break;

		case 11:mergeDocs(reader, arg1, arg2);
			break;

		default:
			System.out.println("PANIC\n");
			return;
	}
	try
	{
		reader.close();
	}
	catch (IOException e)
	{
		System.out.print("No se pudo cerrar el índice\n");
		return;
	}
}

private static IndexWriter create_index_writer(String indexPath,
	IndexWriterConfig.OpenMode openMode) throws IOException
{
	Directory dir = FSDirectory.open(new File(indexPath));
	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
	IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
	iwc.setOpenMode(openMode);
	iwc.setRAMBufferSizeMB(256.0);
	IndexWriter writer = new IndexWriter(dir, iwc);

	return writer;
	//indexDocs(writer, docDir);
}

private static void mergeDocs(DirectoryReader reader, String A, String B)
{
	IndexWriter writer = null;
	try
	{
		writer = create_index_writer(B, IndexWriterConfig.OpenMode.CREATE);
		Document doc = null;
		for (int i = 0; i < reader.maxDoc(); i++)
		{
			writer.addDocument(reader.document(i));
		}
		DirectoryReader readerA = indexReader(A);
		for (int i = 0; i < readerA.maxDoc(); i++)
		{
			writer.addDocument(readerA.document(i));
		}

		readerA.close();
		writer.commit();
		writer.close();
	}
	catch(Exception e)
	{
		System.out.println("Error al crear el índice");
		e.printStackTrace();
		return;
	}
}

private static void indexDocs(DirectoryReader reader, int min, int max, String path)
{
	if(reader.maxDoc() < max) max = reader.maxDoc();
	IndexWriter writer = null;
	try
	{
		writer = create_index_writer(path, IndexWriterConfig.OpenMode.CREATE);
		Document doc = null;
		for (int i = min; i < max; i++)
		{
			writer.addDocument(reader.document(i));
		}
		writer.commit();
		writer.close();
	}
	catch(Exception e)
	{
		System.out.println("Error al crear el índice");
		return;
	}
}

private static void indexFreq(DirectoryReader reader, int min, int max, String f, String path)
{
	IndexWriter writer = null;
	try
	{
		writer = create_index_writer(path, IndexWriterConfig.OpenMode.CREATE);
		indexFreqWriter(reader, writer, min, max, f);
		writer.commit();
		writer.close();
	}
	catch(Exception e)
	{
		System.out.println("Error al crear el índice");
		return;
	}
}

private static void indexFreqWriter(DirectoryReader reader, IndexWriter writer, int min, int max, String f)
{
	SlowCompositeReaderWrapper atomicReader;
	try
	{
		atomicReader = new SlowCompositeReaderWrapper((CompositeReader) reader);

		Fields fields = null;
		Terms terms = null;
		TermsEnum termsEnum = null;
		HashSet<Integer> added = new HashSet<Integer>();
		fields = atomicReader.fields();
		for (String field : fields)
		{
			if(field.equals(f))
			{
				terms = fields.terms(field);
				termsEnum = terms.iterator(null);
				while (termsEnum.next() != null)
				{
					int docFreq = termsEnum.docFreq();
					if((min >= 0) && (docFreq < min)) continue;
					if((max >= 0) && (docFreq > max)) continue;
					String tt = termsEnum.term().utf8ToString();
					System.out.println(termsEnum.totalTermFreq() + "\t" + termsEnum.docFreq() + "\t" + tt);
					DocsEnum docsEnum = termsEnum.docs(null, null);
					int doc;
					while ((doc = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS)
					{
						if(added.contains(doc)) continue;
						
						added.add(doc);
						writer.addDocument(reader.document(doc));

						//System.out.println(doc);
					}
				}
			}
		}
		atomicReader.close();
	}
	catch(Exception e)
	{
		System.out.println("Error al abrir el índice");
		return;
	}
}

private static void showDoc(DirectoryReader reader, int min, int max)
{
	showDocOn(reader, min, max, System.out);
}

private static void dumpDocs(DirectoryReader reader, String path)
{
	PrintStream writer;
	try
	{
		writer = new PrintStream(path);
	}
	catch(FileNotFoundException e)
	{
		System.out.println("Error al abrir el fichero " + path);
		return;
	}
	showDocOn(reader, 0, reader.maxDoc(), writer);
	writer.close();
}

private static void showFreq(DirectoryReader reader, int min, int max, String f)
{
	SlowCompositeReaderWrapper atomicReader;
	try
	{
		atomicReader = new SlowCompositeReaderWrapper((CompositeReader) reader);

		Fields fields = null;
		Terms terms = null;
		TermsEnum termsEnum = null;

		fields = atomicReader.fields();
		for (String field : fields)
		{
			if(field.equals(f))
			{
				terms = fields.terms(field);
				termsEnum = terms.iterator(null);
				while (termsEnum.next() != null)
				{
					int docFreq = termsEnum.docFreq();
					if((min >= 0) && (docFreq < min)) continue;
					if((max >= 0) && (docFreq > max)) continue;
					String tt = termsEnum.term().utf8ToString();
					System.out.println(termsEnum.totalTermFreq() + "\t" + termsEnum.docFreq() + "\t" + tt);
				}
			}
		}
		atomicReader.close();
	}
	catch(Exception e)
	{
		System.out.println("Error al abrir el índice");
		return;
	}
}

private static void showDocOn(DirectoryReader reader, int min, int max, PrintStream out)
{
	if(reader.maxDoc() < max) max = reader.maxDoc();
	Document doc = null;
	for (int i = min; i < max; i++)
	{
		try
		{
			doc = reader.document(i);
		}
		catch(CorruptIndexException e1)
		{
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		catch(IOException e1)
		{
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		out.println("Document: " + i);
		out.println("Title: " + doc.get("title"));
		out.println("Date: " + doc.get("date"));
		out.println("Topics: " + doc.get("topics"));
		out.println("Contents: " + doc.get("contents"));
	}
}

private static DirectoryReader indexReader(String file) throws IOException
{
	Directory dir = FSDirectory.open(new File(file));
	DirectoryReader reader = DirectoryReader.open(dir);
	return reader;
}

private static boolean is_integer(String n_txt)
{
	int n = -1;
	try
	{
		n = Integer.parseInt(n_txt);
		return true;
	}
	catch(Exception e)
	{
		System.out.print("La cadena "+n_txt+" no se pudo convertir a un entero\n");
		return false;
	}
}

private static boolean is_readable_file(String path)
{
	File f = new File(path);
	if (!f.exists() || !f.canRead())
	{
		return false;
	}
	return true;
}
private static boolean is_dir(String path)
{
	File f = new File(path);
	return f.isDirectory();
}
/* END OF CLASS */
}
