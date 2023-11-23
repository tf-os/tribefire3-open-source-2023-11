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

import java.math.BigInteger;

/**
 * helper to get a grip on the 'non conform' parts of a version
 * 
 * @author pit
 *
 */
public class Part {
	public String delimiter;
	public String qualifier;
	public BigInteger number = new BigInteger("0"); // initialize to 0 (as int does automatically) 
	public int p;
	
	/**
	 * helper to safely compare two string
	 * @param d1
	 * @param d2
	 * @return
	 */
	private static int compare(String d1, String d2) {
		if (d1 != null) {
			if (d2 == null)
				return 1;
			else { 
				int i = d1.compareTo( d2);
				if (i != 0)
					return i;
			}					
		}
		else {
			if (d2 != null) 
				return -1;
		}		
		return 0;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (delimiter != null)
			sb.append( delimiter);
		else {
			sb.append( "<no delimiter>");
		}
		if (qualifier != null)
			sb.append( qualifier);
		else {
			sb.append( "<no qualifier>");
		}		
		sb.append( number);
		
		return sb.toString();
	}
	
	/**
	 * compare two parts
	 * @param p1 - first {@link Part}
	 * @param p2 - second {@link Part}
	 * @return - the comparison value
	 */
	public static int compare( Part p1, Part p2) {
		String d1 = p1.delimiter;
		String d2 = p2.delimiter;
		int d = compare( d1, d2);
		if (d != 0)
			return d;
		
		String q1 = p1.qualifier;
		String q2 = p2.qualifier;
		
		d = compareQualifier(q1, q2);				
		if (d != 0) 
			return d;
		
		return p1.number.compareTo( p2.number);  
	}
	
	/**
	 * parses a {@link Part} of a non conform string
	 * @param nc - the full non conform {@link String}
	 * @param p - the position within the {@link String} to start
	 * @return - the parsed {@link Part}
	 */
	public static Part parse( String nc, int p) {
		Part part = new Part();
		part.p = -1;
		String r = nc.substring(p+1);
		int state = 0;
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < r.length(); i++) {
			char c = r.charAt(i);
			if (Character.isAlphabetic(c)) { // alphabetic (qualifier string)
				if (state == 2) {
					part.p = p + i;				
					if (sb.length() > 0) {
						//part.number = Integer.parseInt( sb.toString());
						part.number = new BigInteger( sb.toString());
					}
					return part;
				}
				state = 1;
				sb.append( c);
			}
			else if (Character.isDigit(c)) {  // digit : build number
				if (state == 1) {
					part.qualifier = sb.toString();
					sb.delete(0, sb.length());				
				}
				state = 2;
				sb.append( c);
			}
			else {	// neither alphabetic nor digit -> delimiter				
				if (state == 1) {
					part.p = p + i;
					part.qualifier = sb.toString();
					return part;
				}
				else if (state == 2) {
					//part.number = Integer.parseInt( sb.toString());
					part.number = new BigInteger(sb.toString());
					part.p = p + i;
					return part;
				}
				part.delimiter = "" + c;
			}
		}
		// finalizing 
		if (sb.length() > 0) {
			switch (state) {
				case 2: // last state was number
					//part.number = Integer.parseInt( sb.toString());
					part.number = new BigInteger(sb.toString());
					break;
				case 1: // last state was qualifier
					part.qualifier = sb.toString();
					break;
				default:
					break;
			}	
		}
		
		return part;
	}
	
	/**
	 * compares two non-conform parts of versions 
	 * @param nc1 - the first non-conform part as a {@link String}
	 * @param nc2 - the second non-conform part as a {@link String}
	 * @return - the comparison value, 0 if match
	 */
	public static int compareNonConform( String nc1, String nc2) {
		if (nc1 != null) {
			if (nc2 == null) 
				return 1;			
		}
		else if (nc2 != null) {
			return 1;
		}
		else {
			return 0;
		}
		int p1 = -1;
		int p2 = -1;
		do {
			Part pa1 = Part.parse(nc1, p1);
			Part pa2 = Part.parse( nc2, p2);
			int c = Part.compare( pa1, pa2);
			if (c != 0) {
				return c;
			}
			p1 = pa1.p;
			p2 = pa2.p;
		} while (p1 > 0 && p2> 0);
		return 0;
	}
	
	/**
	 * internal helper function for the parser - please ignore in the API-context
	 * see https://octopus.com/blog/maven-versioning-explained
	 * @param q1 - the first qualifier
	 * @return - a value for the qualifier
	 */
	 public static int rateQualifier( String qualifier) {
		String q = qualifier;
		if (q == null) {
			q = "";
		}		
		switch (q) {
			case "alpha":
			case "a":
				return 1;
			case "beta":
			case "b":
				return 2;
			case "milestone":
			case "m":
				return 3;
			case "rc":
			case "cr":
			case "pc": // on the same level as rc/cr
				return 4;
			case "SNAPSHOT":		
				return 5;
			case "":
			case "ga":
			case "final":
				return 7;
			case "sp":
				return 8;
			default : 
				return -1;
		}
	}
	 
	/**
	 * compares two qualifiers
	 * @param q1 - the first qualifier
	 * @param q2 - the second qualifier
	 * @return - 0 if matches..
	 */
	public static int compareQualifier(String q1, String q2) { 
		int retval;
		// rate the qualifiers - well known qualifiers 
		int q1Rating = Part.rateQualifier( q1);	
		int q2Rating = Part.rateQualifier( q2);
		if (q1Rating < 0 &&  q2Rating < 0) {
			if (q1 != null && q2 != null) { 
				// both don't have a rating -> compare alphabetically 
				retval = q1.compareToIgnoreCase(q2);
				if (retval != 0) {
					return retval;
				}				
			}					
		}
		else if (q1Rating > 0 && q2Rating < 0) { // unknown qualifier wins against well-known qualifier -> q2 is greater
			return -1;
		}
		else if (q1Rating < 0 && q2Rating > 0) { // unknown qualifier wins against well-known qualifier -> q1 is greater
			return 1;
		}
		else {
			retval = Integer.compare( q1Rating, q2Rating); // compare rating
			if (retval != 0)
				return retval;
		}			
		return 0;
	}
	
}
 