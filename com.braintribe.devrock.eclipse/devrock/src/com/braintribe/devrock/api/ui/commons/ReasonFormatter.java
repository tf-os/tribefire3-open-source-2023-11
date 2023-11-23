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
package com.braintribe.devrock.api.ui.commons;

import java.util.List;

import com.braintribe.gm.model.reason.Reason;

public class ReasonFormatter {
	public static final String HTML_SUFFIX = "</p></body></html>";
	public static final String HTML_PREFIX = "<!DOCTYPE html><html><body><p style=\"color:block;font-size:12px;\">";
	
	public static String toHtmlFormattedText( Reason reason, int offset) {
		if (reason == null)
			return "<null>";
		StringBuilder builder = new StringBuilder();
		if (offset != 0) {
			builder.append("<br/>");
		}
		for ( int i = 0; i < offset; i++) { 
			builder.append("&nbsp;");
		}
		
		builder.append( reason.getText());
		List<Reason> attachedReasons = reason.getReasons();
		if (!attachedReasons.isEmpty()) {
			for (Reason child : attachedReasons) {
				builder.append( toHtmlFormattedText( child, offset+1));
			}
		}
		return builder.toString();
	}
	
	public static String toHtmlSnippet( Reason reason) {
		StringBuilder embedder = new StringBuilder();
		embedder.append( HTML_PREFIX);
		embedder.append( ReasonFormatter.toHtmlFormattedText( reason, 0));
		embedder.append( HTML_SUFFIX);		     	
		return embedder.toString();
	}
		
}
