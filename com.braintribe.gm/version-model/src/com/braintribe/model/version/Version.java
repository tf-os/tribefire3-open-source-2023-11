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
import java.util.StringTokenizer;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the quintessential version..
 *  
 *  as defined by Maven : {@code <major>.<minor>[.<revision>][-<qualifier>][-<buildNumber>][<nonConform>]}
 *  <br/>
 *  where all but qualifier and nonConform are int.
 *  <br/> 
 * @author pit
 *
 */
public interface Version extends HasMajorMinorRevision, VersionInterval, Comparable<Version> {
	
	EntityType<Version> T = EntityTypes.T(Version.class);
	static final String qualifier = "qualifier";
	static final String buildNumber = "buildNumber";
	static final String nonConform = "nonConform";
	String anomalousExpression = "anomalousExpression";
	
	
	/**
	 * @return - the qualifier if any 
	 */
	String getQualifier();
	void setQualifier( String qualifier);
	
	/**
	 * @return - the build number (0 can mean none)
	 */
	int getBuildNumber();
	void setBuildNumber( int buildNumber);
	
	
	/**
	 * @return - the part of the version that was found invalid at parsing 
	 */
	String getNonConform();
	void setNonConform( String nonconform);
	
	/**
	 * @return - non null if the expression the version is built on is anomalous in the
	 * sense that it cannot be reproduced from the data parsed. Mostly due to 
	 * major/minor/revision values that lose 'data' (leading 0s) when converted to integer. 
	 */
	String getAnonmalousExpression();
	void setAnonmalousExpression(String value);


	/**	
	 * creates a new {@link Version} instance, 
	 * uses major/minor, revision is 0
	 * @param major - the value for the major
	 * @param minor - the value for the minor
	 * @return - a fresh {@link Version}
	 */
	static Version create( int major) {
		Version v = Version.T.create();
		v.setMajor(major);
		return v;
	}
	
	/**	
	 * creates a new {@link Version} instance, 
	 * uses major/minor, revision is 0
	 * @param major - the value for the major
	 * @param minor - the value for the minor
	 * @return - a fresh {@link Version}
	 */
	static Version create( int major, int minor) {
		Version v = Version.T.create();
		v.setMajor(major);
		v.setMinor(minor);
		return v;
	}
	
	/**
	 * creates a new {@link Version} instance,
	 * uses all three values
	 * @param major - the value for the major
	 * @param minor - the value for the minor
	 * @param revision - the value for the revision
	 * @return - a fresh {@link Version}
	 */
	static Version create( int major, int minor, int revision) {
		Version v = Version.T.create();
		v.setMajor(major);
		v.setMinor(minor);
		v.setRevision(revision);
		return v;
	}
	
	/**
	 * creates a new {@link Version} instance,
	 * by retrieving the major/minor. revision is set to 0
	 * @param v - the {@link HasMajorMinor} the provides major/minor
	 * @return - a fresh {@link Version}
	 */
	static Version from( HasMajorMinor v) {
		Version fv = Version.T.create();
		fv.setMajor( v.getMajor());
		fv.setMinor( v.getMinor());		
		return fv;
	}
	
	/**
	 * creates a new {@link Version} instance,
	 * by retrieving the major/minor/revision from the {@link HasMajorMinorRevision} instance.
	 * @param v - the {@link HasMajorMinorRevision} the provides major/minor/revision
	 * @return - a fresh {@link Version}
	 */
	static Version from( HasMajorMinorRevision v) {
		Version fv = Version.T.create();
		fv.setMajor( v.getMajor());
		fv.setMinor( v.getMinor());		
		fv.setRevision( v.getRevision());		
		return fv;
	}
	
	/**
	 * self copier for {@link Version}
	 * @return - a fresh copy 
	 */
	default Version copy() {
		Version to = Version.T.create();
		to.setMajor( this.getMajor());
		to.setMinor( this.getMinor());
		to.setRevision( this.getRevision());
		to.setQualifier( this.getQualifier());
		to.setBuildNumber( this.getBuildNumber());
		to.setNonConform( this.getNonConform());
		return to;
	}
	
	/**
	 * convenience access to the qualifier 
	 * @param qualifier - sets the qualifier 
	 * @return - the {@link Version} itself
	 */
	default Version qualifier( String qualifier) {
		this.setQualifier(qualifier);	
		return this;		
	}
	
	/**
	 * convenience access to the build number 
	 * @param buildNumber - sets the build number
	 * @return - the {@link Version} itself
	 */
	default Version buildNumber( int buildNumber) {	
		this.setBuildNumber(buildNumber);
		return this;		
	}
	
	@Override
	default String asString() {
		// if an anomalous expression has been stored, just return it
		String anomalousExpression = getAnonmalousExpression();
		if (anomalousExpression != null) {
			return anomalousExpression;
		}
	
		StringBuilder builder = new StringBuilder();
		
		builder.append( getMajor());
		
		Integer minor = getMinor();
		Integer revision = getRevision();
		
		if (minor != null) {
			builder.append( '.');
			builder.append( minor);
			
			if (revision != null) {
				builder.append( '.');
				builder.append( revision);
			}
		}
		else {
			if (revision != null) {
				builder.append( ".0.");
				builder.append( revision);
			}
		}
		
		String qualifier = getQualifier();
		if (qualifier != null) {
			builder.append( '-');
			builder.append( qualifier);			
			
			int buildNumber = getBuildNumber(); // requires a qualifier
			if (buildNumber != 0) {
				builder.append( '-');
				builder.append( buildNumber);
			}
		}
		
		String nonConform = getNonConform();
		if (nonConform != null) {
			builder.append( nonConform);
		}
		
		return builder.toString();
	}
	
	// TODO : once switched to Java 9, make this private 	
	/**
	 * internal helper function for the parser - please ignore in the API-context
	 * @param buffer - the {@link StringBuilder} that holds the value's string representation 
	 * @param version - the version to set it 
	 * @param index - 0 : major, 1: minor, 2 : revision, 3 : buildNumber
	 */
	static int bufferToVersion( StringBuilder buffer, Version version, int index) {
		int sl = buffer.length();
		// empty buffer - index not set
		if (sl == 0)
			return 0;
		
		// number as string too long for int 
		if (sl > 8) {
			return -1;
		}
						
		int v = Integer.parseInt(buffer.toString());
		switch( index) {
			case 0:
				version.setMajor(v);									 
				break;
			case 1:
				version.setMinor(v);
				break;
			case 2:
				version.setRevision(v);
				break;
			case 3:
				version.setBuildNumber(v);
				break;
			default:
				throw new IllegalStateException("invalid index for version number access");				
		}		
		buffer.delete(0, sl);
		return v;
	}
		
	/**
	 * @param expression
	 * @return
	 */
	static List<String> tokenize(String expression) {
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		boolean insideAlpha = false;
		boolean insideDigits = false;
		for (int p = 0; p < expression.length(); p++) {
			char c = expression.charAt( p);			
			if (c == '-') {
				if (sb.length() > 0) {
					result.add( sb.toString());
					sb.delete(0, sb.length());
					insideAlpha = false;
				}				
			}
			else if (Character.isDigit(c)) { 
				insideDigits = true;
				if (insideAlpha) {
					result.add( sb.toString());
					sb.delete(0, sb.length());
					sb.append( c);
					insideAlpha = false;
				}
				else {
					sb.append( c);
				}
			}
			else {
				if (c != '.') {
					if (insideDigits) {
						result.add( sb.toString());
						sb.delete(0, sb.length());						
						insideDigits = false;
					}
					insideAlpha = true;
				}
				sb.append( c);
			}
		}
		if (sb.length() > 0) {
			result.add( sb.toString());
		}
		return result;
	}
	
	/**
	 * <major>.<minor>[.<revision>][-qualifier][-buildNumber][nonConform]
	 * create a version by parsing a string
	 * @param string - the string to parse
	 * @return - a fresh version
	 */
	static Version parse( String string) {
		if (string == null)
			throw new IllegalArgumentException("expression cannot be null at this time");
		
		Version version = Version.T.create();
		
		StringTokenizer tokenizer = new StringTokenizer(string, "-", false);
		
		int state = 0; // overall state control 
		StringBuilder sb = new StringBuilder(); // common string builder instance
		StringBuilder ncb = new StringBuilder(); // non-conform string builder 
		
		while (tokenizer.hasMoreTokens()) {
			
			String token = tokenizer.nextToken();					
			switch (state) {
				case 0 : { // numbers part
					int p = 0;					
					int l = token.length();
					char lc = 'n';
					for (int i = 0; i < l; i++) {						
						if (p > 2) { // too many numbers in this number part.
							state = -1;
							ncb.append( token.substring( i-1));
							break;
						}
						char c = token.charAt(i);
						if (Character.isDigit( c)) {
							sb.append(c);
						}
						else if (c == '.') {
							if (bufferToVersion(sb, version, p) < 0) {
								version.setAnonmalousExpression(string);
								version.setNonConform(string);
								if (p == 0) {
									version.setMajor(0);
								}
								return version;
							}
							p++;						
						}
						else {
							// neither digit nor '.' -> non-conform 				
							if (bufferToVersion(sb, version, p) < 0) {
								version.setAnonmalousExpression(string);
								version.setNonConform(string);
								return version;
							}
							// switch the parse to non-conform handling
							if (lc == '.') {
								ncb.append( token.substring(i-1));
							}
							else {
								ncb.append( token.substring(i));
							}
							state = -1;
							break;
						}
						lc = c;
					}					
					if (bufferToVersion(sb, version, p) < 0) {
						version.setAnonmalousExpression(string);
						version.setNonConform(string);
						return version;
					}
					
					if (state >= 0) {
						state++;
					}
				}
				break;
		 		case 1 : // qualifier		 		
		 			int qualifierBuildNumberPos = findBuildNumberPosInQualifier(token);
		 			if (qualifierBuildNumberPos > 0) {
		 				state = 3; // jump to non conform part
		 				ncb.append("-");
						ncb.append( token);
		 			}
		 			else {
		 				version.setQualifier(token);
		 			}
		 			
					state++;
					break;
				case 2 : // build number
					sb.delete(0, sb.length());
					int l = token.length();					
					for (int i = 0; i < l; i++) {
						char c = token.charAt(i);
						if (Character.isDigit( c)) {
							sb.append(c);
						}
						else {
							// no digit - non-conform
							if (bufferToVersion(sb, version, 3) < 0) {
								version.setAnonmalousExpression(string);
								version.setNonConform(string);
								return version;
							}
							ncb.append( token.substring(i));
							state = -1;
							break;
						}						
					}
					if (bufferToVersion(sb, version, 3) < 0) {
						version.setAnonmalousExpression(string);
						version.setNonConform(string);
						return version;
					}
					if (state > 0) {
						state++;
					}
					break;			
				default:
					// non-conform handling
					ncb.append("-");
					ncb.append( token);
					break;
			}
		}
		// collect possible non-conform parts
		if (ncb.length() > 0) {
			version.setNonConform( ncb.toString());
		}
		
		// check on anomalous expressions -> store it
		if (!string.equals( version.asString())) {
			version.setAnonmalousExpression(string);
		}
		
		return version;
	}

	/**
	 * helper function: do not use outside this interface, will be made private in future JAVA
	 * @param qualifier - the qualifier to find a build number in 
	 * @return - the position of a valid build number in the qualifier
	 */
	static int findBuildNumberPosInQualifier( String qualifier) {
		if (qualifier == null)
			return -1;
		for (int p = 0; p < qualifier.length(); p++) {
			char qc = qualifier.charAt(p);
			if (Character.isDigit(qc)) {
				for (int x = p+1; x < qualifier.length(); x++) {
					char xc = qualifier.charAt( x);
					if (!Character.isDigit(xc)) {
						return -1;
					}
				}
				return p;
			}
		}
		return -1;
	}

	/**
	 * @return <tt>true</tt> if given version is <tt>null</tt> or lower than this version. Note that <tt>1.1</tt> is higher than <tt>1.1-pc</tt> and
	 *         both are higher than <tt>1.0.9</tt>
	 */
	default boolean isHigherThan(Version o) {
		return o == null || compareTo(o) > 0;
	}

	/**
	 * Returns a comparison result with given version. The returned value is positive iff this version is higher than given version, 0 if the two
	 * versions are equal and negative if this is lower than given version.
	 */
	@Override
	default int compareTo(Version o) {
		// numbers 
		int retval = Integer.compare(getMajor(), o.getMajor());
		if (retval != 0)
			return retval;

		retval = Integer.compare(minor(), o.minor());
		if (retval != 0)
			return retval;
		
		retval = Integer.compare(revision(), o.revision());
		if (retval != 0)
			return retval;
		
		// qualifier 		
		String myQualifier = getQualifier();		
		String theirQualifier = o.getQualifier();							
		retval = Part.compareQualifier( myQualifier, theirQualifier);
		if (retval != 0)
			return retval;
			
		// build number 
		Integer myBuildNumberToUse = getBuildNumber();
		Integer theirBuildNumberToUse = o.getBuildNumber();

		retval = Integer.compare(myBuildNumberToUse,  theirBuildNumberToUse);
		if (retval != 0)
			return retval;
			
		// non-conform
		String myNonConform = getNonConform();
		String theirNonConform = o.getNonConform();		
		
		return Part.compareNonConform(myNonConform, theirNonConform);		
	}
	@Override
	default Version lowerBound() {
		return this;
	}
	@Override
	default boolean lowerBoundExclusive() {
		return false;
	}
	@Override
	default Version upperBound() {
		return this;
		
	}
	@Override
	default boolean upperBoundExclusive() {		
		return false;
	}
	@Override
	default boolean matches(Version version) {	
		return compareTo(version) == 0;
	}
	
	default boolean isSnapshot() {
		return "SNAPSHOT".equals(getQualifier());
	}
	
	default boolean isPreliminary() {
		Version monadeNeutral = copy();

		monadeNeutral.setBuildNumber(0);
		monadeNeutral.setQualifier(null);
		
		return monadeNeutral.compareTo(this) > 0;
	}
}
