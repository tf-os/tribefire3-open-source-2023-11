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
package com.braintribe.doc.pages;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import com.braintribe.doc.UniversalPath;
import com.braintribe.utils.IOTools;

public class IndexPageRenderer extends AbstractPageRenderer {
	private File greetingsHtmlFile;
	String index_html = "index.html";

	public IndexPageRenderer(File greetingsHtmlFile) {
		super("index.ftlh");
		this.greetingsHtmlFile = greetingsHtmlFile;
	}

	@Override
	protected Map<String, Object> dataModel(PageRenderingContext context) {

		Map<String, Object> dataModel = context.newDataModel("");

		if (greetingsHtmlFile != null && greetingsHtmlFile.exists()) {
			String greetingsHtmlContent;
			try {
				greetingsHtmlContent = IOTools.slurp(greetingsHtmlFile, "UTF-8");
			} catch (IOException e) {
				throw new UncheckedIOException("Error while loading the greeting text to include in " + index_html, e);
			}
			dataModel.put("greeting", greetingsHtmlContent);
		}

		return dataModel;
	}

	@Override
	protected UniversalPath targetFile(PageRenderingContext context) {
		return UniversalPath.start(index_html);
	}
}
