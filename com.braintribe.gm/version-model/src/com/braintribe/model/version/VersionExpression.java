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
package com.braintribe.model.version;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * an abstract type that represents any version expression, i.e. a single version, a version-range, list of multiple version ranges
 * @author pit/dirk
 *
 */
@Abstract
public interface VersionExpression extends GenericEntity {
	
	EntityType<VersionExpression> T = EntityTypes.T(VersionExpression.class);
	static final String expression="expression";

	/**
	 * @return - the expression as a string.. 
	 */
	String getExpression();
	void setExpression( String expression);
	
	/**
	 * @param str - the string to tokenize
	 * @return - a List of single expressions (at least one entry)
	 */
	static List<String> tokenize( String str) {
		int length = str.length();
		List<String> result = new ArrayList<>();
		StringBuilder expression = new StringBuilder();
		boolean insideToken = false;
		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			if (c == '(' || c == '[') {
				insideToken = true;
				int elen = expression.length();
				if (elen > 0) {
					result.add( expression.toString());
					expression.delete(0, elen);
				}
				expression.append(c);
			}
			else if (c == ')' || c == ']') {
				expression.append(c);
				insideToken = false;
			}
			else {
				if (insideToken) {
					expression.append(c);
				}
			}
		}
		result.add( expression.toString());
		return result;
	}
	
	/**
	 * @param str - the {@link VersionInterval} as a string 
	 * @return - a fresh instance of a {@link VersionInterval}, {@link Version}, {@link FuzzyVersion}, {@link VersionRange} 
	 */
	static VersionInterval parseVersionInterval( String str) {
		// definitively not a range 
		if (str.contains( ",") == false) {
			// filter out any brackets and parenthesis
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == '[' || c == '(' || c == ']' || c == ')') {
					continue;
				}
				sb.append(c);
			}
			return Version.parse(sb.toString());
		}
		
		VersionRange range = VersionRange.parse(str);

		// deduce a fuzzy version
		
		if (range.getLowerBoundExclusive() || !range.getUpperBoundExclusive())
			return range;
		
		Version lB = range.getLowerBound();
		Version uB = range.getUpperBound();
		
		// check if there is a lower and a upper bound
		if (lB == null || uB == null) {		
			return range;
		}	
		
		int lPrecision = lB.continuousPrecision();
		int uPrecision = uB.continuousPrecision();
		
		if (lPrecision != uPrecision)
			return range;
		
		int commonPrecision = lPrecision;
		
		// test equality of fuzzy range basis
		for (int i = 0; i <= commonPrecision; i++) {
			int lV = lB.readNumericField(i);  
			int uV = uB.readNumericField(i);
			
			if (i < commonPrecision) {
				 if (lV != uV)
					 return range;
			}
			else {
				if (uV - lV != 1) 
					return range;
			}
		}
		
		return FuzzyVersion.fromMajorMinorRevision(lB);
	}
	
	/**
	 * 
	 * @param str - the string to parse
	 * @return - an instance of {@link VersionExpression} that emulates the string
	 */
	static VersionExpression parse( String str) {
		return parse( str, new Consumer<Boolean>() {

			@Override
			public void accept(Boolean t) {							
			}
			
		});
	}
	
	/**
	 * @param str - the string to parse 
	 * @param versionIsNegotiableConsumer - the consumer to receive the 'version reference is SOFT/HARD' message
	 * @return - an instance of {@link VersionExpression} that emulates the string
	 */
	static VersionExpression parse( String str, Consumer<Boolean> versionIsNegotiableConsumer) {
		if (str == null)
			return null;
		char c = str.charAt( 0);
		if (c == '(' || c == '[') {
			 List<String> tokens = tokenize( str);
			 if (tokens.size() > 1) {
				 VersionIntervals intervals = VersionIntervals.T.create();
				 for (String token : tokens) {
					 char r = token.charAt( 0);
					 if (r == '(' || r == '[') {
						 intervals.getElements().add( parseVersionInterval( token));
					 }
					 else {
						 throw new IllegalStateException("[" + str + "] is not a valid expression, as in multiple range sets, only ranges are allowed");
					 }
				 }
				 return intervals;
			 }
			 else {
				 char r = tokens.get(0).charAt( 0);
				 if (r == '(' || r == '[') {
					 VersionInterval parseVersionInterval = parseVersionInterval( tokens.get(0));

					 if (versionIsNegotiableConsumer != null) {
						 // somebody wants to know whether this version is a 'non negoatiable version'
					 	if (parseVersionInterval instanceof Version) { 
					 		versionIsNegotiableConsumer.accept( true);
					 	}
					 }
					return parseVersionInterval;
				 }
			 }
		}
		else {
			if (versionIsNegotiableConsumer != null) {
				versionIsNegotiableConsumer.accept( true);
			}
			return Version.parse(str);
		}
		
		return null;
	}
	
	/**	  
	 * @param version - the {@link Version} to check 
	 * @return - true if the version matches, false otherwise
	 */
	boolean matches( Version version);
	
	@Override
	default String stringify() {
		return asString();
	}
	
	/**
	 * @return - as string representation
	 */
	default String asString() {
		return getExpression();
	}
		
	List<VersionInterval> asVersionIntervalList();

	default String asShortNotation() {		
		List<VersionInterval> versionIntervals = asVersionIntervalList();
		
		if (versionIntervals.size() > 1)
			return asString();

		VersionInterval range = versionIntervals.get(0);
		
		Version lower = range.lowerBound();
		Version upper = range.upperBound();
		
		if (range.lowerBoundExclusive() || !range.upperBoundExclusive() || lower.compareTo(upper) == 0) {
			return asString();
		} else {
			if (upper.getMajor() == (lower.getMajor() + 1) && lower.getMinor() == 0 && upper.getMinor() == 0) {
				return lower.getMajor() + "~";
			}
			else if (upper.getMajor() == lower.getMajor() && upper.getMinor() == (lower.getMinor() + 1)) {
				return lower.getMajor() + "." + lower.getMinor() + "~";
			}
			else
				return asString();
		}
	}

}
