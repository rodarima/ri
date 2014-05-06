package p3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class QueryLauncher
{
	private static void usage()
	{
		String use = "QueryLauncher: Realiza operaciones de consulta en índices.\n" +
		"\n"+
		"Uso del programa:\n"+
		" java p3.QueryLauncher INDEX [OPTIONS] QUERY\n"+
		"\n"+
		"Índice (INDEX):\n"+
		" -index DIR          Especifica el índice a emplear\n"+
		"Opciones (OPTIONS):\n"+
		" -showfield FIELD    Muestra el id, la puntuación y el campo FIELD\n"+
		" -out DIR            Especifica el lugar donde se almacenará el nuevo índice\n"+
		"Consulta (QUERY) sólo especificar una:\n"+
		" -query FIELD QUERY  Resultados de la consulta QUERY sobre el campo FIELD.\n"+
		" -multiquery {F Q}   Resultados de las consultas sobre su respectivo campo.\n"+
		" -progquery F -and {T} -or {T} -not {T} Realiza una consulta empleando los\n"+
		"                     operadores lógicos con los términos especificados.\n"+
		"\n"+
		"Ejemplos:\n"+
		" java p3.QueryLauncher index/ -showfield content -query content fish\n"+
		" java p3.QueryLauncher index/ -query content \"tropical fish\"\n"+
		" java p3.QueryLauncher index/ -multiquery content tropical content fish\"\n"+
		" java p3.QueryLauncher index/ -progquery content -and tropical fish -or trees\n"+
		"   -not water\"\n"+
		"\n"+
		"";
		System.out.print(use);
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length == 0 || "-h".equals(args[0]) || 
			"-help".equals(args[0]) || "--help".equals(args[0]))
		{
			usage();
			System.exit(0);
		}

		File file_index = null;
		File file_out = null;
		String showfield = null;
		String out_path = null;
		Query query = null;

		boolean index_status = false;
		boolean showfield_status = false;
		boolean out_status = false;
		boolean query_status = false;

		IndexReader reader = null;
		IndexSearcher searcher = null;
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);


		for(int i = 0; i<args.length; i++)
		{
			String arg = args[i];

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
				
				file_index = new File(path);
				reader = DirectoryReader.open(FSDirectory.open(file_index));
				searcher = new IndexSearcher(reader);
				i++;
			}
			else if("-showfield".equals(arg))
			{
				if(showfield_status)
				{
					System.out.print("Opción \"-out\" repetida\n");
					return;
				}
				showfield_status = false;

				if(!testArgs(args, i, 1)) return;
				showfield = args[++i];
			}
			else if("-out".equals(arg))
			{
				if(out_status)
				{
					System.out.print("Opción \"-out\": Carpeta de índice de salida repetida\n");
					return;
				}
				out_status = true;
				if(!testArgs(args, i, 1)) return;
				String path = args[i+1];
				if(!is_readable_file(path))
				{
					System.out.print("Opción \"-out\": Carpeta de índice de salida inexistente o ilegible\n");
					return;
				}
				if(!is_dir(path))
				{
					System.out.print("Opción \"-out\": No es una carpeta\n");
					return;
				}
				
				file_out = new File(path);
				i++;
			}
			else if("-query".equals(arg))
			{
				if(query_status)
				{
					System.out.print("Opción de consulta ya especificada\n");
					return;
				}
				query_status = true;
				if(!testArgs(args, i, 2)) return;
				String field = args[++i];
				String query_txt = args[++i];
				QueryParser parser = new QueryParser(Version.LUCENE_40, field, analyzer);
				query = parser.parse(query_txt);
				i++;
			}
		}


		if(!index_status)
		{
			System.out.print("Opción \"-index\" no especificada\n");
			return;
		}

		if(!query_status)
		{
			System.out.print("Opción de consulta no especificada\n");
			return;
		}

		search(searcher, query);
	}

	private static void search(IndexSearcher searcher, Query query) throws IOException
	{
		int hitsPerPage = 50;
		TopDocs results = searcher.search(query, hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = results.totalHits;
		
		int end = Math.min(numTotalHits, hitsPerPage);
		for(int i = 0; i < end; i++)
		{
			Document doc = searcher.doc(hits[i].doc);
			System.out.println("Title: " + doc.get("title"));
		}
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
}
