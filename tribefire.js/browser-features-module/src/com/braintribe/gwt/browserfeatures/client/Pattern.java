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
package com.braintribe.gwt.browserfeatures.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Dirk Scheffler
 * 
 */
public class Pattern {

	/**
	 * Declares that regular expressions should be matched across line borders.
	 */
	public final static int MULTILINE = 1;

	/**
	 * Declares that characters are matched regardless of case.
	 */
	public final static int CASE_INSENSITIVE = 2;

	public final static int GLOBAL = 4;

	private JavaScriptObject regExp;

	private static JavaScriptObject createExpression(String pattern, int flags) {
		String sFlags = "";
		if ((flags & GLOBAL) != 0)
			sFlags += "g";
		if ((flags & MULTILINE) != 0)
			sFlags += "m";
		if ((flags & CASE_INSENSITIVE) != 0)
			sFlags += "i";
		return _createExpression(pattern, sFlags);
	}

	private static native JavaScriptObject _createExpression(String pattern,
			String flags)/*-{
							return new $wnd.RegExp(pattern, flags);
	}-*/;

	private native int _match(String text, List<?> matches)/*-{
		var regExp = this.@com.braintribe.gwt.browserfeatures.client.Pattern::regExp;
		var result = text.match(regExp);
		if (result == null) return -1;
		for (var i=0;i<result.length;i++)
		matches.@java.util.ArrayList::add(Ljava/lang/Object;)(result[i]);
		return result.index;
	}-*/;

	/**
	 * Determines wether the specified regular expression is validated by the
	 * provided input.
	 * 
	 * @param regex
	 *            Regular expression
	 * @param input
	 *            String to validate
	 * @return <code>true</code> if matched.
	 */
	public static boolean matches(String regex, String input) {
		return new Pattern(regex).matches(input);
	}

	/**
	 * Escape a provided string so that it will be interpreted as a literal in
	 * regular expressions. The current implementation does escape each
	 * character even if not necessary, generating verbose literals.
	 */
	public static String quote(String input) {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			output.append("\\" + input.charAt(i));
		}
		return output.toString();
	}

	/**
	 * Class constructor
	 * 
	 * @param pattern
	 *            Regular expression
	 */
	public Pattern(String pattern) {
		this(pattern, 0);
	}

	/**
	 * Class constructor
	 * 
	 * @param pattern
	 *            Regular expression
	 */
	public Pattern(String pattern, int flags) {
		regExp = createExpression(pattern, flags);
	}

	/**
	 * This method is borrowed from the JavaScript RegExp object. It parses a
	 * string and returns as an array any assignments to parenthesis groups in
	 * the pattern's regular expression
	 * 
	 * @return Array of strings following java's Pattern convention for groups:
	 *         Group 0 is the entire input string and the remaining groups are
	 *         the matched parenthesis. In case nothing was matched an empty
	 *         array is returned.
	 */
	@SuppressWarnings("rawtypes")
	public Match match(String text) {
		List matches = new ArrayList();
		int index = _match(text, matches);
		if (matches.size() == 0) return null;
		String arr[] = new String[matches.size()];
		for (int i = 0; i < matches.size(); i++)
			arr[i] = matches.get(i).toString();
		
		return new Match(arr, index);
	}

	public static class Match {
		private String matches[];
		private int index;

		public Match(String matches[], int index) {
			super();
			this.matches = matches;
			this.index = index;
		}
		
		public Match(String match, int index) {
			super();
			this.matches = new String[]{match};
			this.index = index;
		}

		public String[] getMatches() {
			return matches;
		}
		
		public String getFirstMatch() {
			return matches[0];
		}
		
		public int getIndex() {
			return index;
		}
	}

	/**
	 * Determines wether a provided text matches the regular expression
	 */
	public boolean matches(String text) {
		return matches(text, 0) != null;
	}
	
	public native Match matches(String text, int startIndex)/*-{
		var regExp = this.@com.braintribe.gwt.browserfeatures.client.Pattern::regExp;
		regExp.lastIndex = startIndex;
		var success = regExp.test(text);
		if (success) {
		    var matchStr = RegExp.lastMatch; 
			var index = regExp.lastIndex - matchStr.length;
			return @com.braintribe.gwt.browserfeatures.client.Pattern.Match::new(Ljava/lang/String;I)(matchStr, index);
		}
		else return null;
	}-*/;

	/**
	 * Returns the regular expression for this pattern
	 */
	public native String pattern()/*-{
		var regExp = this.@com.braintribe.gwt.browserfeatures.client.Pattern::regExp;
		return regExp.source;
	}-*/;

	private native void _split(String input, List<?> results)/*-{
		var regExp = this.@com.braintribe.gwt.browserfeatures.client.Pattern::regExp;
		var parts = input.split(regExp);
		for (var i=0;i<parts.length;i++)
		results.@java.util.ArrayList::add(Ljava/lang/Object;)(parts[i]  );
	}-*/;

	/**
	 * Split an input string by the pattern's regular expression
	 * @return Array of strings
	 */
	@SuppressWarnings("rawtypes")
	public String[] split(String input) {
		List results = new ArrayList();
		_split(input, results);
		String[] parts = new String[results.size()];
		for (int i = 0; i < results.size(); i++)
			parts[i] = (String) results.get(i);
		return parts;
	}

}
