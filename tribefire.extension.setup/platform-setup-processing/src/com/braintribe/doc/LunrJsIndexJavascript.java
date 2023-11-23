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
import java.io.IOException;
import java.util.stream.Collectors;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.IOTools;


public class LunrJsIndexJavascript implements LunrJsIndex {
	private final Value array;
	private final Context context;

	public LunrJsIndexJavascript() {
		context = Context.newBuilder().build();
		
	    array = eval("var idxContent = []; idxContent;");
	}

	@Override
	public void serialize(File serializedIndexFile) throws IOException {
		String lunr_js = IOTools.slurp(MarkdownCompiler.class.getResource("res/lunr.js"), "UTF-8");

		eval(lunr_js);

		String lunrIndex = eval("JSON.stringify(lunr(function () { this.ref('id'); this.field('body'); this.field('title'); this.field('headings'); this.field('tags'); idxContent.forEach(function (doc) { this.add(doc) }, this) }));")
				.asString();

		IOTools.spit(serializedIndexFile, "serializedIndex = ", "UTF-8", false);
		IOTools.spit(serializedIndexFile, lunrIndex, "UTF-8", true);

	}

	@Override
	public void add(MarkdownFile markdownFile) {
		String id = markdownFile.getDocRelativeHtmlFileLocation().toSlashPath();
		String content = markdownFile.unloadContentText();
		String title = markdownFile.getTitle();
		
		Value indexObject = eval("new Object();");

		indexObject.putMember("id", id);
		indexObject.putMember("body", content);
		indexObject.putMember("title", title);
		indexObject.putMember("tags", markdownFile.getTags().stream().map(Tag::getName).collect(Collectors.joining(" ")));
		indexObject.putMember("headings", String.join(" ", markdownFile.getHeadings()));

		Long size = array.getArraySize();
		array.setArrayElement(size, indexObject);
	}
	
	private Value eval(String source) {
		try {
			return context.eval("js", source);
		} catch (PolyglotException e) {
			throw Exceptions.unchecked(e, "Could not build up fulltext index with lunr.js script engine");
		}
	}

	@Override
	public void close() {
		context.close();
	}
}
