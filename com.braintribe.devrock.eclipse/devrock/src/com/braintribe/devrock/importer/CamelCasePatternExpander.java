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
package com.braintribe.devrock.importer;


/**
 * helper class to expand the input into search patterns
 * 
 * @author pit
 *
 */
public class CamelCasePatternExpander {

	private static final String WILDCARD = ".*";
	
	/**
	 * checks whether a string has a leading "'" and a trailing "'", in this case, it returns the string without the "'". If not, it returns null 
	 * @param input - a string in either with leading and trailing single quotes  
	 * @return - null if the input doesn't have a leading and trailing single quote or the input string without single quotes 
	 */
	public boolean isPrecise( String input) {
		if (
				input.startsWith( "'") &&
				input.endsWith( "'")
		    ) {
			return true;
		} 		
		return false;
	}
	
	/**
	 * @param input - remove single quotes from the string
	 * @return - the string without single quotes
	 */
	public String sanitize( String input) {
		if (input.contains( "'") == false)
			return input;		
		return input.replace("'", "");				
	}
	
	/**
	 * expanding the input into a "likeable" string for the query - handles camel casing etc   
	 * @param input - the string as entered by the user 
	 * @return - {@link String} formatted for the query 
	 */
	public String expand( String input) {
		if (input.length() == 0)
			return WILDCARD;
		String pattern = input;
		if (isAllLowerCase(input) && hasNoHyphen(input)) {
			if (input.contains( "*") == false) {
				pattern = "*" + input +"*";			
			}
		} else {
			StringBuilder builder = new StringBuilder();
			boolean prefixAsterics = input.startsWith("*") ? true : false;		
			for (int i = 0; i < input.length(); i++) {
				String c = input.substring(i, i+1);
				
				if (c.matches( "-")) {
					if (builder.length() > 0)
						builder.append( WILDCARD);
					builder.append( c);					
					continue;
				}
				
				if (c.matches( "[A-Z]")) {
					if (builder.length() > 0)
						builder.append(WILDCARD);
					if (i == 0) {
						builder.append( c.toLowerCase());
					}					
					else {
						builder.append( "-" + c.toLowerCase());
					}
					continue;
				} 
				if (c.matches( "[a-z0-9-_\\.]")) {				
					builder.append( c);
				}
			}
			pattern = builder.toString() + WILDCARD;
			if (prefixAsterics)
				pattern = WILDCARD + pattern;
		}
		return pattern;
	}
	
	/**
	 * returns whether there are only lower case characters in the string 
	 * @param suspect - the {@link String} to check 
	 * @return - true if lowercase, false otherwise 
	 */
	private boolean isAllLowerCase( String suspect) {
		if (suspect.matches( "[a-z0-9\\*\\?\\.]*"))
			return true;
		return false;
	}
	
	/**
	 * @param suspect - the {@link String} to test
	 * @return - true if no hyphen exist in string
	 */
	private boolean hasNoHyphen( String suspect) {
		if (suspect.matches( "^[-]*"))
			return true;
		return false;
	}
	
	
	public static void main( String args[]) {
		CamelCasePatternExpander expander = new CamelCasePatternExpander();
		for (String arg : args) {
			System.out.println( arg + "\t\t" + expander.expand(arg));
		}
	}

}
