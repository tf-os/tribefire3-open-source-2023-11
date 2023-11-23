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
public class AceCompletionSnippetSegmentLiteral implements AceCompletionSnippetSegment {
	
	private String literalText;

	/**
	 * The literal text that makes up part of the snippet segment
	 * @param literalText The literal text that makes up part of the snippet. This does not need to be escaped, escaping will be handled automatically.
	 */
	public AceCompletionSnippetSegmentLiteral(String literalText) {
		this.literalText = literalText;
	}

	@Override
	public String getPreparedText(int tabstopNumber) {
		final String escapedText = literalText.replace("\\", "\\\\").replace("$", "\\$");
		return escapedText;
	}
	
	
}
