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
package com.braintribe.model.processing.query.autocompletion.impl.lexer.container;

public enum QueryLexerToken {
	// Multi or no keyword Tokens
	Unknown, Invalid, Space(false, " ", "\u00A0"), Operator(false, "=", "!=", "<=", ">=", "<", ">"), OperatorWithSpace(true, "like", "ilike", "in"),
	Boolean(true, "true", "false"),
	Number(false, true, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), FloatDelimiter(false, true, "f", "F"), LongDelimiter(false, true, "l", "L"), DoubleDelimiter(false, true, "d", "D"),
	BigDecimalDelimiter(false, true, "b", "B"),
	// Enter value Tokens
	StringInput, String, EscapedKeywordInput, Alias, AliasPropertyDelimiter, AliasPropertyInput, AliasProperty, AliasEntityProperty,
	Integer(true), Long(true), Float(true), Double(true), BigDecimal(true),
	// Tokens with space around
	Select(true), WildChar(true, "*"), From(true), Join(true), Where(true), OrderBy(true, "order by"), GroupBy(true, "group by"), Limit(true), Offset(true), Distinct(true), Property(true),
	// Tokens with no space around
	StringDelimiter(false, "'"), StringEscape(false, "\\"), EscapedKeywordDelimiter(false, "\""), OpenBracket(false, "("), CloseBracket(false, ")"), Dot(false, "."), Comma(false, ","),
	Minus(false, true, "-");

	private final String[] keywords;
	private final boolean spaceNeeded;
	private final boolean numberRelated;

	/***
	 * This will create a simple enum entry with no keywords.
	 */
	private QueryLexerToken() {
		this.keywords = new String[0];
		this.spaceNeeded = false;
		this.numberRelated = false;
	}

	/***
	 * This will create a enum entry with its name as a keyword. Define if a space is needed before and after the keyword.
	 */
	private QueryLexerToken(final boolean spaceNeeded) {
		this.keywords = new String[] { name().toLowerCase() };
		this.spaceNeeded = spaceNeeded;
		this.numberRelated = false;
	}
	
	/***
	 * This will create a enum entry with its name as a keyword. Define if a space is needed before and after the keyword.
	 * Also define if a token is number related. If so, the filterString will NOT be cleared when matching the keyword.
	 */
	private QueryLexerToken(final boolean spaceNeeded, boolean numberRelated) {
		this.keywords = new String[] { name().toLowerCase() };
		this.spaceNeeded = spaceNeeded;
		this.numberRelated = numberRelated;
	}
	
	/***
	 * This will create a enum entry with defined keywords. Define if a space is needed before and after the keyword.
	 */
	private QueryLexerToken(final boolean spaceNeeded, final String... keyword) {
		this(spaceNeeded, false, keyword);
	}

	/***
	 * This will create a enum entry with defined keywords. Define if a space is needed before and after the keyword.
	 * Also define if a token is number related. If so, the filterString will NOT be cleared when matching the keyword.
	 */
	private QueryLexerToken(final boolean spaceNeeded, boolean numberRelated, final String... keyword) {
		this.keywords = keyword;
		this.spaceNeeded = spaceNeeded;
		this.numberRelated = numberRelated;
	}

	/***
	 * Get the defined keywords of the enum.
	 *
	 * @return Array of defined keywords.
	 */
	public String[] getKeywords() {
		return this.keywords;
	}

	/***
	 * Is a space needed before and after the keyword?
	 */
	public boolean isSpaceNeeded() {
		return this.spaceNeeded;
	}
	
	/**
	 * Is the token number related?
	 */
	public boolean isNumberRelated() {
		return numberRelated;
	}
}
