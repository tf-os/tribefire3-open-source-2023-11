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
package edu.ycp.cs.dh.acegwt.client.ace;


/**
 * A segment of a completion snippet
 * */
public class AceCompletionSnippetSegmentTabstopItem implements AceCompletionSnippetSegment {
	
	private String tabstopText;

	/**
	 * Text that should fit inside a tabstop, the first tabstop is selected after a substitution, and subsequent tabstops are moved between by
	 * pressing the tab button. Tabstops are identified using an id.
	 * @param tabstopText The literal text that makes up part of the tab stop. This does not need to be escaped, escaping will be handled automatically.
	 */
	public AceCompletionSnippetSegmentTabstopItem(String tabstopText) {
		this.tabstopText = tabstopText;
	}
	
	@Override
	public String getPreparedText(int tabstopNumber) {
		
		// Special characters need escaping so that we can support tokens, see demo to see how this works in practice
		
		final String escapedText =
				tabstopText
					.replace("\\", "\\\\") // backslash becomes double backslash 
					.replace("$", "\\$")   // dollar becomes backslash dollar
					.replace("}", "\\}");  // right brace becones backslash right brace
		return "${" + tabstopNumber + ":" + escapedText + "}";
	}
	
}
