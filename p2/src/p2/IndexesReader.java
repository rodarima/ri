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
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.ByteBuffer;
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
	"-merge A B        Fusiona el índice contenido en la carpeta \"A\" con el contenido en la\n"+
	"                  carpeta \"B\", el resultado fusionado está en \"B\". Equivale a\n"+
	"                  -mergeindexes\n"+
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
		}
		else if("-docs".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 2;
			arg1 = args[++i];
			arg2 = args[++i];
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
		}
		else if("-t-".equals(arg) || "-termsdflessthan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 2)) return;
			op = 5;
			arg1 = args[++i];
			arg2 = args[++i];
		}
		else if("-t".equals(arg) || "-termsdfrango".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 6;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
		}
		else if("-i+".equals(arg) || "-indexdocstermsdfmorethan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 7;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
		}
		else if("-i-".equals(arg) || "-indexdocstermsdflessthan".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 8;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
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
		}
		else if("-id".equals(arg) || "-indexdocsij".equals(arg))
		{
			operation_status = true;
			if(!testArgs(args, i, 3)) return;
			op = 10;
			arg1 = args[++i];
			arg2 = args[++i];
			arg3 = args[++i];
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

	System.out.print("Argumentos correctos\n");

	try
	{
		DirectoryReader reader = indexReader(index_path);
	}
	catch (IOException e)
	{
		System.out.print("No se pudo abrir el índice para leer\n");
	}

}

private static DirectoryReader indexReader(String file) throws IOException
{
	Directory dir = FSDirectory.open(new File(file));
	DirectoryReader reader = DirectoryReader.open(dir);
	return reader;
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
