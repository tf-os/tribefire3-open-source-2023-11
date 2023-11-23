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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class ClipboardCopier implements HasTreeTokens {
	private final static int buflen = 40;
	
	public static String copyToClipboard( Tree tree) {
		TreeItem [] items = tree.getItems();
		StringBuilder builder = new StringBuilder();
		int depth = 0;
		for (TreeItem item : items) {
			builder.append( process( item, depth));
		}
		return builder.toString();
	}

	public static String processArtifact( Solution solution, int depth) {
		Set<String> processedLocations = new HashSet<String>();
		StringBuilder builder = new StringBuilder();
		for (Part part : solution.getParts()) {
			String location = part.getLocation();			
			if (processedLocations.contains( location))
				continue;
				processedLocations.add(location);
			if (builder.length() > 0) {
				builder.append( "\n");
			}
			for (int i = 0; i < depth; i++) {
				builder.append("\t");
			}					
			String name = new File( location).getName();
			if (name.contains("pom.gf")) {
				name = part.getArtifactId() + "-" + VersionProcessor.toString( part.getVersion()) + ".pom";
			}
			int nameLen = name.length();
			for (int i = nameLen; i < buflen; i++) {
				name += " ";
			}
			builder.append( name);
			builder.append("\t");
			builder.append( location);
		}
		return builder.toString();
	}
	
	private static Object process(TreeItem item, int depth) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			builder.append("\t");
		}
		builder.append( item.getText());
		String ttip = (String) item.getData(KEY_TOOLTIP);
		if (ttip != null) { 
			builder.append("\t" + ttip);
		}
		
		builder.append( "\n");
		TreeItem[] items = item.getItems();
		if (items != null) {
			for (TreeItem child : items) {
				
				builder.append( process( child, depth+1));
			}
		}
		return builder.toString();
	}
}
