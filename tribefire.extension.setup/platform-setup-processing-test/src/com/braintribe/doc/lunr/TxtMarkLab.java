// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.doc.lunr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.utils.FileTools;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

public class TxtMarkLab {
	public static void main(String[] args) {
		try {
			File outputFolder = new File("html");
			
			if (outputFolder.exists())
				FileTools.deleteDirectoryRecursively(outputFolder);
			
			outputFolder.mkdirs();
			
			compile(new File("markdown"), outputFolder, new Stack<>());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void compile(File folder, File targetFolder, Stack<String> path) {
		for (File file: folder.listFiles()) {
			if (file.isDirectory()) {
				path.push(file.getName());
				try {
					compile(file, targetFolder, path);
				}
				finally {
					path.pop();
				}
			}
			else if (file.getName().endsWith(".md")){
				try {
					
				    Parser PARSER = Parser.builder().build();
				    HtmlRenderer RENDERER = HtmlRenderer.builder().build();
				    
				    
					String targetName = toHtmlName(file.getName());
					
					File targetFile = new File(targetFolder, Stream.concat(path.stream(), Stream.of(targetName)).collect(Collectors.joining(File.separator)));
					targetFile.getParentFile().mkdirs();

					
					try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")) {
						
						Document document = PARSER.parseReader(reader);
						RENDERER.render(document, writer);
					}
					
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}
	
	private static String toHtmlName(String name) {
		int index = name.lastIndexOf('.');
		String htmlName = name.substring(0, index) + ".html";
		return htmlName;
	}

}
