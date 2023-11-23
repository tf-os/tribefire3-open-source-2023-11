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
package com.braintribe.web.repository.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/**
 * Basic implementation of {@link RepoletWriter} with hardcoded html structure
 */
public class RepoletBasicWriter implements RepoletWriter<PrintWriter> {

	@Override
	public void writeList(String path, Collection<BreadCrumb> breadCrumbs, Collection<String> entries, PrintWriter writer, Map<String, Object> attributes) throws IOException {

		writer.println("<html>");
		writer.print("<title>Collection: ");
		writer.print(path);
		writer.println("</title>");
		writer.println("</head>");
		writer.println("<body>");
			
		if (breadCrumbs != null) {
			writer.print("<h3>");
			for (BreadCrumb breadCrumb : breadCrumbs) {
				if (breadCrumb.getLink() != null || !breadCrumb.getLink().trim().isEmpty()) {
					writer.print("<a href=\"");
					writer.print(breadCrumb.getLink());
					writer.print("\">");
					writer.print(breadCrumb.getName());
					writer.print("</a>/");
				} else {
					writer.print(breadCrumb.getName());
				}
			}
			writer.println("</h3>");
		}
			
		writer.println("<ul>");

		for (String entry : entries) {
			writer.print("<li><a href=\"");
			writer.print(entry);
			writer.print("\">");
			writer.print(entry);
			writer.println("</a></li>");
		}

		writer.println("</ul>");
		writer.println("</body>");
		writer.println("</html>");
	}

	@Override
	public void writeNotFound(String path, boolean printInspectedPaths, Collection<String> inspectedPaths, PrintWriter writer, Map<String, Object> attributes) throws IOException {

		writer.println("<html>");
		writer.println("<head>");
		writer.println("<title>Error 404 Not a valid request path</title>");
		writer.println("</head>");
		writer.println("<body>");
		writer.println("<h3>HTTP ERROR 404</h3>");
		writer.print("<p>");
		writer.print(path);
		writer.println(" not found</p>");
		
		if (printInspectedPaths) {
			writer.println("<p>inspected paths:</p>");
			writer.println("<ol>");
			
			for (String inspectedPath : inspectedPaths) {
				writer.print("<li>");
				writer.print(inspectedPath);
				writer.println("</li>");
			}
			
			writer.println("</ol>");
		}
		
		writer.println("</body>");
		writer.println("</html>");

	}

}
