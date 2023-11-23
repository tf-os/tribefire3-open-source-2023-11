// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.gwt.symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.braintribe.utils.StringTools;

public class SymbolTransformer {

	public static void main(String[] args) {
		
		try {
			File extraFolder = new File(args[0]);
			//File extraFolder = new File("C:\\svn\\artifacts\\com\\braintribe\\gwt\\custom\\GwtItwTests\\1.1\\extras\\BtClientCustomization\\");
			File symbolFolder = new File(extraFolder, "symbolMaps");
			File soycFolder = new File(extraFolder, "soycReport");
			
			File []symbolFiles = symbolFolder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".symbolMap");
				}
			});
			
			Set<File> simpleSymbolFiles = new HashSet<File>();
			for (File symbolFile: symbolFiles) {
				simpleSymbolFiles.add(transform(symbolFile, soycFolder));
			}
		
			Map<String, Object> lines = new LinkedHashMap<String, Object>();
			List<String> compilationIds = new ArrayList<String>();
			int permutationIndex = 0;
			for (File simpleSymbolFile: simpleSymbolFiles) {
				String fileName = simpleSymbolFile.getName();
				String compliationId = fileName.substring(0, fileName.indexOf('.'));
				compilationIds.add(compliationId);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(simpleSymbolFile), "ISO-8859-1"));
				try {
					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						String key = extractKey(line);
						
						String cid = String.valueOf(permutationIndex);
						LineForCompilation lfc = new LineForCompilation(line, cid);
						Object oldValue = lines.get(key);
						if (oldValue != null) {
							List<LineForCompilation> lfcs = null;
							if (oldValue instanceof LineForCompilation) {
								LineForCompilation existingLfc = (LineForCompilation) oldValue;
								lfcs = new ArrayList<LineForCompilation>(simpleSymbolFiles.size());
								lfcs.add(existingLfc);
								lines.put(key, lfcs);
							}
							else if (oldValue instanceof List) {
								List<LineForCompilation> existingLfcs = (List<LineForCompilation>)oldValue;
								lfcs = existingLfcs;
							}
							
							boolean merged = false;
							for (LineForCompilation existingLfc: lfcs) {
								if (existingLfc.line.equals(lfc.line)) {
									existingLfc.compilationIds += ";" + lfc.compilationIds;
									merged = true;
									break;
								}
							}
							if (!merged)
								lfcs.add(lfc);
						}
						else {
							lines.put(key, lfc);
						}
					}
				}
				finally {
					bufferedReader.close();
				}
				permutationIndex++;
			}
			
			File mergedSymbolFile = new File(symbolFolder, "mergedSymbols");
			Writer writer = new OutputStreamWriter(new FileOutputStream(mergedSymbolFile));
			writer.write("# " + StringTools.join(",", compilationIds));
			try {
				for (Object entry: lines.values()) {
					if (entry instanceof List) {
						List<LineForCompilation> lfcs = (List<LineForCompilation>) entry;
						for (LineForCompilation lfc: lfcs) {
							writer.write('\n');
							writer.write(lfc.line);
							writer.write("," + lfc.compilationIds);
						}
					}
					else {
						LineForCompilation lfc = (LineForCompilation) entry;
						writer.write('\n');
						writer.write(lfc.line);
						writer.write("," + lfc.compilationIds);
					}
				}
			}
			finally {
				writer.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}
	
	private static class LineForCompilation {
		public LineForCompilation(String line, String compilationIds) {
			super();
			this.line = line;
			this.compilationIds = compilationIds;
		}
		public String line;
		public String compilationIds;
	}
	
	protected static String extractKey(String line) {
		int index = line.indexOf(',');
		String key = line.substring(0,index);
		return key;
	}

	
	public static File transform(File symbolFile, File soycFolder) throws Exception {
		String name = symbolFile.getName();
		String compilationId = name.substring(0, name.length() - 10);
		File simpleSymbolFile = new File(symbolFile.getParentFile(), compilationId + ".simpleSymbolMap");
		BufferedReader symbolMapReader = new BufferedReader(new InputStreamReader(new FileInputStream(symbolFile), "ISO-8859-1"));
		Writer writer = new OutputStreamWriter(new FileOutputStream(simpleSymbolFile), "ISO-8859-1");
		//SortedSet<String> lines = new TreeSet<String>();
		//NamePool namePool = new NamePool();
		try {
			String line = symbolMapReader.readLine();
			
			int s = line.indexOf('{');
			int e = line.lastIndexOf('}');
			int cid = Integer.valueOf(line.substring(s + 1, e).trim());
			
			File storyFile = new File(soycFolder, "stories" + cid + ".xml.gz");
			SizeReader sizeReader = new SizeReader(storyFile);
			
			boolean first = true;
			while ((line = symbolMapReader.readLine()) != null) {
				
				if (line.startsWith("#"))
					continue;
				
				String parts[] = line.split(",");
				String jsName = parts[0];
				String jsniIdent = parts[1];
				String className = parts[2];
				String memberName = parts[3];
				String lineNumber = parts[5];
				
				if (jsniIdent.indexOf('(') != -1 && sizeReader.isValid(jsniIdent)) {
				//if (jsniIdent.indexOf('(') != -1 && !(jsName.startsWith(memberName))) {
					String outputClassName = className; //namePool.acquireClassNameReference(className, lines);
					
					if (first) {
						first = false;
					}
					else {
						writer.write('\n');	
					}
					
					writer.write(jsName);
					writer.write(',');
					writer.write(outputClassName);
					writer.write(',');
					writer.write(memberName);
					writer.write(',');
					writer.write(lineNumber);
				}
				
			}
			
			/*writeNormalizedLine(writer, "# " + maxLineLength, maxLineLength);
			for (String newLine: lines) {
				writer.write(newLine);
				writeNormalizedLine(writer, newLine, maxLineLength);
			}*/
		}
		finally {
			symbolMapReader.close();
			writer.close();
		}
	
		return simpleSymbolFile;
	}
	
	/*private static class NamePool {
		private int sequence = 0;
		private Map<String, Integer> nameRefs = new HashMap<String, Integer>();
		public String acquireClassNameReference(String className, Collection<String> lines) {
			int index = className.lastIndexOf('.');
			
			if (index != -1) {
				String packageName = className.substring(0, index);
				String simpleClassName = className.substring(index + 1);
				
				return acquireNameReference(packageName, lines) + "." + acquireNameReference(simpleClassName, lines);
			}
			else {
				return acquireNameReference(className, lines);
			}
			
		}
		
		public String acquireNameReference(String name, Collection<String> lines) {
			Integer ref = nameRefs.get(name);
			if (ref == null) {
				ref = sequence++;
				nameRefs.put(name, ref);
				lines.add("#" + ref + ","+name);
			}
			
			return "#" + ref;
		}
	}*/
	
	/*private static void writeNormalizedLine(Writer writer, String line, int lineLength) throws IOException {
		writer.write(line);
		int paddingCount = lineLength - line.length() - 1;
		char padding[] = new char[paddingCount];
		Arrays.fill(padding, ' ');
		writer.write(padding);
		writer.write('\n');
	}*/
	
	static class SizeReader extends DefaultHandler {
		
		private Map<String, Integer> sizeMap = new HashMap<String, Integer>();
		private boolean active = true;
		
		public SizeReader(File file) throws Exception {
			active = file.exists();
			
			if (active) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(file));
				try {
					SAXParserFactory.newInstance().newSAXParser().parse(gzipInputStream, this);
				}
				finally {
					gzipInputStream.close();
				}
			}
		}
		
		public boolean isValid(String jsniIdent) {
			return active? sizeMap.containsKey(jsniIdent): true;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("size")) {
				Integer size = Integer.decode(attributes.getValue("size"));
				String ref = attributes.getValue("ref");
				sizeMap.put(ref, size);
			}
		}
	}
}
