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
package com.braintribe.devrock.importer.scanner.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * a helper class to support navigation in the group logic 
 * @author pit
 *
 */
public class GroupSplitter {
	private List<String> tokens;
	
	/**
	 * @param group - the full groupId
	 */
	public GroupSplitter(String group) {
		String [] values = group.split("\\.");
		tokens = new ArrayList<String>( values.length);
		for (String value : values) {
			tokens.add( value);
		}
	}
	
	/**
	 * @return - the {@link List} of the elements within the group
	 */
	public List<String> getGroupTokens() {
		return tokens;
	}
	
	/**
	 * @param index - the index up to which the elements should be returned
	 * @return - combine the elements up to the index in 
	 */
	public String getTokensUpTo( int index) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < index && i < tokens.size(); i++) {
			if (builder.length() > 0)
				builder.append(".");
			builder.append( tokens.get(i));
		}
		return builder.toString();
	}
	
	/**
	 * @param index - the index from which elements should be returned
	 * @return - the group elements after the index
	 */
	public List<String> getTokensAfter( int index) {
		return tokens.subList(index, tokens.size());
	}
	
	/**
	 * @return - number of elements in the full group
	 */
	public int getLength() {
		return tokens.size();
	}

}
