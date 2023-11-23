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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a version range, i.e something like '[1.0,1.1)'
 * @author pit
 *
 */
public interface VersionRange extends VersionInterval, Comparable<VersionRange> {
	
	EntityType<VersionRange> T = EntityTypes.T(VersionRange.class);
	static final String upperBound = "upperBound";
	static final String upperBoundExclusive="upperBoundExclusive";
	static final String lowerBound = "lowerBound";
	static final String lowerBoundExclusive="lowerBoundExclusive";
		

	/**
	 * @return - upper bound of the range 
	 */
	Version getUpperBound();
	void setUpperBound( Version upperBound);
	
	/**
	 * @return - whether the upper bound is *not* part of the range
	 */
	boolean getUpperBoundExclusive();
	void setUpperBoundExclusive( boolean exclusive);
	
	
	/**
	 * @return - lower bound of the range
	 */
	Version getLowerBound();
	void setLowerBound( Version lowerBound);
	
	/**
	 * @return - whether the lower bound is *not* part of the range
	 */
	boolean getLowerBoundExclusive();
	void setLowerBoundExclusive( boolean exclusive);
	
	@Override
	default VersionRange toRange() {	
		return this;
	}
	
	@Override
	default Version lowerBound() {
		return getLowerBound();
	}
	@Override
	default boolean lowerBoundExclusive() {
		return getLowerBoundExclusive();
	}
	@Override
	default Version upperBound() {
		return getUpperBound();
	}
	@Override
	default boolean upperBoundExclusive() {		
		return getUpperBoundExclusive();
	}
	
	/**
	 * returns a standard range ( [major.minor, major.(minor+1))
	 * @param version  - the {@link Version}
	 * @return - the resulting {@link VersionRange}
	 */
	static VersionRange toStandardRange(Version version) {
		return from(Version.from(version), false, version.successor(Version.T::createRaw), true);
	}
	
	/**
	 * returns a narrow range -> [major.minor.revision, major.(minor+1))
	 * @param version - the {@link Version}
	 * @return - the resulting {@link VersionRange}
	 */
	static VersionRange toNarrowRange(Version version) {
VersionRange range = VersionRange.T.create();
		
		range.setLowerBound( version);
		range.setLowerBoundExclusive( false);
		
		range.setUpperBound( Version.create(version.getMajor(), version.minor()+1));
		range.setUpperBoundExclusive(true);
		
		return range;
	}
	
	/**
	 * parse a string into a {@link VersionRange}
	 * @param string - the string notation 
	 * @return - the {@link VersionRange}
	 */
	static VersionRange parse( String string) {		
		string = string.trim();
		if (string.length() < 2) {
			throw new IllegalStateException( "[" + string + "] is not a valid range");
		}
		VersionRange range = VersionRange.T.create();
		
		char leC = string.charAt( 0);
		if (leC == '(') {
			range.setLowerBoundExclusive(true);
		}
		
		char ueC = string.charAt( string.length()-1);
		if (ueC == ')') {
			range.setUpperBoundExclusive(true);
		}

		String vs = string.substring(1, string.length()-1).trim();		
		int c = vs.indexOf( ',');
		if (c < 0) {
			// actually a version in string, which would be represented by a Version(hard).. but still
			Version v = Version.parse( vs);
			range.setLowerBound(v);			
			range.setUpperBound(v);
			return range;
		}
		String trim = vs.substring( c+1).trim();
		if (c == 0) { // starts with a ','
			if (trim.length() > 0) // still has some non-whitespace -> only an upper bounds 
				range.setUpperBound( Version.parse( trim));
		}
		else if (c == vs.length()-1) {
			String toParse = vs.substring( 0, vs.length()-1).trim();
			range.setLowerBound( Version.parse( toParse));
		}
		else { // two version format 
			String toParse = vs.substring(0, c).trim();
			range.setLowerBound( Version.parse( toParse));
			range.setUpperBound( Version.parse( trim));
		}
				
		return range;
	}
	
	/**
	 * string representation of the {@link VersionRange}
	 * @param range - the {@link VersionRange} to print
	 * @return - a standard string notation
	 */
	@Override
	default String asString() {
		StringBuilder vs = new StringBuilder();
		if (this.getLowerBoundExclusive()) {
			vs.append("(");
		} else {
			vs.append("[");
		}
		
		Version lowerBound = this.getLowerBound();
		if (lowerBound != null) {
			vs.append( lowerBound.asString());
		}
		vs.append(",");
		Version upperBound = this.getUpperBound();
		if (upperBound != null) {
			vs.append( upperBound.asString());
		}
		if (this.getUpperBoundExclusive()) {
			vs.append(")");
		}
		else {
			vs.append( "]");
		}		
		return vs.toString();
	}
	
	@Override
	default boolean matches( Version version) {
		if (version == null) {
			throw new IllegalStateException("version may not be null here");
		}
		if (this.getLowerBound() != null) {
			int v = this.getLowerBound().compareTo(version);
			if (v > 0) 
				return false;
			if (v == 0 && this.getLowerBoundExclusive()) 
				return false;
		}
		
		if (this.getUpperBound() != null) {
			int v = this.getUpperBound().compareTo(version);
			if (v < 0) 
				return false;
			if (v == 0 && this.getUpperBoundExclusive()) 
				return false;
		}
		return true;
	}
	
//	static VersionRange createEmpty() {
//		
//	}
	
	/**
	 * produces a {@link VersionRange} as defined by the two versions
	 * @param lower - lower boundary {@link Version}
	 * @param lowerExclusive - lower border hit doesn't count as match 
	 * @param upper - upper boundary {@link Version}
	 * @param upperExclusive - upper border hit doesn't count as match
	 * @return - a fresh {@link VersionRange}
	 */
	static VersionRange from( Version lower, boolean lowerExclusive, Version upper, boolean upperExclusive) {
		VersionRange range = VersionRange.T.create();
		range.setUpperBound(upper);
		range.setLowerBoundExclusive(lowerExclusive);
		range.setLowerBound(lower);
		range.setUpperBoundExclusive(upperExclusive);
		return range;
	}
	
	@Override
	default int compareTo(VersionRange o) {

		int lb = this.getLowerBound().compareTo(o.getLowerBound());
		if (lb != 0)
			return lb;

		// test on whether the boundary are within or outside range -> 
		if (this.getLowerBoundExclusive()) {
			if (!o.getLowerBoundExclusive()) {
				return -1; // this is less as it excludes the boundary, yet o doesn't  
			}			
		}
		else {
			if (o.getLowerBoundExclusive()) {
				return 1; // this is more as it includes the boundary, yet ot doesn't
			}
		}
		
		int ub = this.getUpperBound().compareTo(o.getUpperBound());
		if (ub != 0)
			return ub;

		// test on whether the boundary are within or outside range -> 
		if (this.getUpperBoundExclusive()) {
			if (!o.getUpperBoundExclusive()) {
				return -1; // this is less as it excludes the boundary, yet o doesn't  
			}			
		}
		else {
			if (o.getUpperBoundExclusive()) {
				return 1; // this is more as it includes the boundary, yet ot doesn't
			}
		}
		
		return 0;
	}
	
	
}
