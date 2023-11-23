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
package com.braintribe.doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;

import com.vladsch.flexmark.ast.Document;

public class MarkdownDocumentLoader {
	private final File sourceRootFolder;
	private final JavadocMerger javadoc;

	public MarkdownDocumentLoader(File sourceRootFolder, JavadocMerger javadoc) {
		this.sourceRootFolder = sourceRootFolder;
		this.javadoc = javadoc;
	}

	public Document load(MarkdownFile markdownFile) {
		UniversalPath docRelativeLocation = markdownFile.getDocRelativeLocation();
		UniversalPath documentParentPath = docRelativeLocation.getParent();
		Path rootPath = sourceRootFolder.toPath();
		DocumentProcessor loader = new DocumentProcessor(rootPath, documentParentPath, markdownFile, true, javadoc);
		File file = rootPath.resolve(docRelativeLocation.toPath()).toFile();

		Document includedDocument = DocUtils.simpleParse(file);
		loader.process(includedDocument, documentParentPath, docRelativeLocation.toString());

		return includedDocument;
	}

	public Document load(String documentContent, UniversalPath documentParentPath) {
		Path rootPath = sourceRootFolder.toPath();

		DocumentProcessor loader = new DocumentProcessor(rootPath, documentParentPath, null, true, javadoc);

		Document document = DocUtils.FLEXMARK_PARSER.parse(documentContent);
		loader.process(document, "<DYNAMICALLY CREATED FILE>");

		return document;
	}
	
	public MarkdownFile compileBody(MarkdownFile markdownFile, File targetFile) {
		Document document = load(markdownFile);
		if (markdownFile.getWizard() == null)
			DocumentProcessor.ensureToc(document);

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8")) {
			DocUtils.FLEXMARK_RENDERER.render(document, writer);
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException("Can't write body of markdown file into " + targetFile.getAbsolutePath(), e);
		}
		
		return markdownFile;

		
	}
}
