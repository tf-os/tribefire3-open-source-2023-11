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
package com.braintribe.devrock.zed.forensics.fingerprint;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.data.ModelEntityReference;
import com.braintribe.zarathud.model.forensics.data.ModelEnumReference;
import com.braintribe.zarathud.model.forensics.data.ModelPropertyReference;
import com.braintribe.zarathud.model.forensics.findings.ComparisonIssueType;
import com.braintribe.zarathud.model.forensics.findings.IssueType;

/**
 * a "finger print" identifies the point (and actual issue) of an issue raised by the forensics
 *  
 * @author pit
 *
 */
public class FingerPrintExpert implements HasFingerPrintTokens{
	private static Logger log = Logger.getLogger(FingerPrintExpert.class);
	
	
	/**
	 * assigns a value to a slot 
	 * @param slot - the slot key 
	 * @param value
	 */
	private static void assignToSlot( FingerPrint fingerPrint, String slot, String value) {
		
		if (slot.equalsIgnoreCase(GROUP)) {
			fingerPrint.getSlots().put(GROUP,value);			
		}
		else if (slot.equalsIgnoreCase(ARTIFACT)) {
			fingerPrint.getSlots().put(ARTIFACT, value);
		}
		else if (slot.equalsIgnoreCase(PACKAGE)) {
			fingerPrint.getSlots().put(PACKAGE, value);
		}
		else if (slot.equalsIgnoreCase(TYPE)) {
			fingerPrint.getSlots().put(TYPE, value);			
		}
		else if (slot.equalsIgnoreCase(FIELD)) {
			fingerPrint.getSlots().put( FIELD, value);
		}
		else if (slot.equalsIgnoreCase(METHOD)) {
			fingerPrint.getSlots().put(METHOD, value);
		}
		else if (slot.equalsIgnoreCase( ENTITY)) {
			fingerPrint.getSlots().put(ENTITY, value);
		}
		else if (slot.equalsIgnoreCase(PROPERTY)) {
			fingerPrint.getSlots().put( PROPERTY, value);			
		}		
		else if (slot.equalsIgnoreCase(ISSUE)) {
			int bs = value.indexOf('(');
			if (bs < 0) {
				fingerPrint.getSlots().put( ISSUE, value);
			}
			else {
				int br = value.indexOf( ')');				
				fingerPrint.getSlots().put(ISSUE, value.substring(0, bs));
				String exp = value.substring(bs+1, br);				
				for (String e : exp.split( ",")) {
					fingerPrint.getIssueData().add( e);
				}
			}
		}	
		else {
			throw new IllegalStateException("slot [" + slot + "] isn't a valid slot key");
		}
		
	}
	
	public static int numMatches(FingerPrint lock, FingerPrint key) {
		int i = 0;
		for (Entry<String,String> slot : key.getSlots().entrySet()) {
			String lockValue = lock.getSlots().get( slot.getKey());
			if (lockValue != null) {
				if (lockValue.equalsIgnoreCase( slot.getValue())) {			
					i++;
				}
				else {
					return -1;
				}
			}
		}
		return i;
	}
	
	/**
	 * @param token - the expression for a single value
	 * @return - a {@link Pair} consisting of the slot name and the associated value
	 */
	private static Pair<String, String> getSlotValue( String token) {
		int p = token.indexOf(':');
		if (p < 0) {
			return Pair.of( token,  null);
		}
		else {
			
			String second = token.substring(p+1);
			if (second.equalsIgnoreCase("*")) {
				second = null;
			}
			return Pair.of( token.substring(0, p), second);
		}
	}
	
	public static FingerPrint from(FingerPrint sibling, String ... slotsToIgnore) {
		Set<String> slotsNotToTransfer = new HashSet<>();
		for (String str : slotsToIgnore) {
			slotsNotToTransfer.add(str);
		}
		FingerPrint fingerPrint = FingerPrint.T.create();
		
		if (sibling.getIssueData() != null) {
			fingerPrint.getIssueData().addAll( sibling.getIssueData());
		}
		fingerPrint.setEntitySource( sibling.getEntitySource());
		
		for (Map.Entry<String, String> entry : sibling.getSlots().entrySet()) {
			if (slotsNotToTransfer.contains( entry.getKey()))
				continue;
			fingerPrint.getSlots().put(entry.getKey(), entry.getValue());
		}
		return fingerPrint;
	}
	
	/**
	 * create a {@link FingerPrintExpert} from a String expression 
	 * @param expression - the String representation of the FingerPrint
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( String expression) {
		FingerPrint fingerPrint = FingerPrint.T.create();
		String [] tokens = expression.split("/");
		if (tokens.length > 1) {
			for (String token : tokens) {
				Pair<String,String> pair = getSlotValue(token);
				assignToSlot( fingerPrint, pair.first(), pair.second());
			}
		}
		else {
			Pair<String,String> pair = getSlotValue( expression);
			assignToSlot( fingerPrint, pair.first(), pair.second());
		}
		return fingerPrint;
		
	}
	
	/**
	 * attaches the slots from the expression to the fingerprint passed
	 * @param fingerPrint - the {@link FingerPrint} to expand
	 * @param expression - the {@link String} with the expression 
	 * @return - the enhanced {@link FingerPrint}
	 */
	public static FingerPrint attach( FingerPrint fingerPrint, String expression) {
		String [] tokens = expression.split("/");
		if (tokens.length > 1) {
			for (String token : tokens) {
				Pair<String,String> pair = getSlotValue(token);
				assignToSlot( fingerPrint, pair.first(), pair.second());
			}
		}
		else {
			Pair<String,String> pair = getSlotValue( expression);
			assignToSlot( fingerPrint, pair.first(), pair.second());
		}
		return fingerPrint;
	}
	
	/**
	 * creates a {@link FingerPrint} for an {@link Artifact}
	 * @param artifact
	 * @param issue
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( Artifact artifact, String issue) {
		return build(artifact, issue, null);
	}
	
	/**
	 * creates a finger print for an issue 
	 * @param code - the {@link IssueType}
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( IssueType code) {
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(ISSUE, code.name());
		return fingerPrint;
	}

	/**
	 * finger print for an artifact related issue 
	 * @param artifact - the {@link Artifact}
	 * @param issue - the issue (value of {@link IssueType})
	 * @param issueData - additional values to add  to the issue
	 * @return - a freshly instantiated {@link FingerPrint} 
	 */
	public static FingerPrint build( Artifact artifact, String issue, List<String> issueData) {		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setIssueData(issueData);
		fingerPrint.setEntitySource(artifact);
		return fingerPrint;
	}
	
	private static Pair<String, String> splitQualifiedTypeName( ZedEntity ze) {
		int p = ze.getName().lastIndexOf('.');
		String packageName = ze.getName().substring(0, p);
		String typeName = ze.getName().substring( p+1);
		return Pair.of( packageName, typeName);
	}
	/**
	 * finger print for an type related issue (can't think of one right now)
	 * @param ze - the {@link ZedEntity} reference 
	 * @param issue - the issue (value of {@link ForensicsFindingCode})
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( ZedEntity ze, String issue) {
		Artifact artifact = ze.getArtifacts().get(0);
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(ze);
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setEntitySource(ze);
		return fingerPrint;
				
	}
	 
	public static FingerPrint build( ZedEntity ze, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build(ze, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;					
	}
	
	/**
	 * finger print for a method related issue (can't think of one right now)
	 * @param me - the {@link MethodEntity}
	 * @param issue
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( MethodEntity me, String issue) {
		ZedEntity ze = (ZedEntity) me.getOwner();
		Artifact artifact = ze.getArtifacts().get(0);

		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(ze);
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);
		fingerPrint.getSlots().put(ISSUE, issue);
		
		
		// actually : create simplified signature (named types, no generics)
		StringBuilder sb = new StringBuilder();
		sb.append( " ");
		sb.append( me.getReturnType().getReferencedType().getName());
		sb.append( " ");
		sb.append( me.getName());		
		sb.append( '(');
		boolean first = true;
		for (TypeReferenceEntity rt : me.getArgumentTypes()) {
			sb.append( rt.getReferencedType().getName());
			if (first) {
				first = false;
			}
			else {
				sb.append(',');
			}
		}
		sb.append( ')');
		fingerPrint.getSlots().put( METHOD, sb.toString());
				
		fingerPrint.setEntitySource( me);
		
		return fingerPrint;
	}
	
	public static FingerPrint build( MethodEntity me, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( me, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}

	/**
	 * finger print for a model property related issue 
	 * @param mpr - the {@link ModelPropertyReference}
	 * @param issue - the issue
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( ModelEntityReference mpr, String issue) {	
		Artifact artifact = mpr.getArtifact();		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(mpr.getType());
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);			
		fingerPrint.setEntitySource(mpr);
		return fingerPrint;				
	}
	
	public static FingerPrint build( ModelEntityReference me, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( me, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}
	
	public static FingerPrint build( ModelEnumReference mer, String issue) {	
		Artifact artifact = mer.getArtifact();		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(mer.getType());
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);		
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setEntitySource(mer);
		return fingerPrint;				
	}

	public static FingerPrint build( ModelEnumReference er, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( er, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}
	
	/**
	 * finger print for a model property related issue 
	 * @param mpr - the {@link ModelPropertyReference}
	 * @param issue - the issue
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( EnumEntity mpr, String issue) {	
		Artifact artifact = mpr.getArtifacts().get(0);		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(mpr);
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);		
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setEntitySource(mpr);
		return fingerPrint;				
	}
	
	public static FingerPrint build( EnumEntity ee, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( ee, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}
	
	
	/**
	 * finger print for a model property related issue 
	 * @param mpr - the {@link ModelPropertyReference}
	 * @param issue - the issue
	 * @return - a freshly instantiated {@link FingerPrint}
	 */
	public static FingerPrint build( ModelPropertyReference mpr, String issue) {
		ModelEntityReference owner = mpr.getOwner();
		Artifact artifact = owner.getArtifact();
		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName(owner.getType());
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);

		fingerPrint.getSlots().put(PROPERTY, mpr.getName());
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setEntitySource(mpr);
		return fingerPrint;				
	}
	
	public static FingerPrint build( ModelPropertyReference mpr, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( mpr, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}
	
	/**
	 * create a finger print for field related issue 
	 * @param fe - the {@link FieldEntity}
	 * @param issue - the issue 
	 * @return - a freshly instantiated {@link FingerPrint} 
	 */
	public static FingerPrint build( FieldEntity fe, String issue) {
		ZedEntity z = (ZedEntity) fe.getOwner();
		Artifact artifact = z.getArtifacts().get(0);
		
		FingerPrint fingerPrint = FingerPrint.T.create();
		fingerPrint.getSlots().put(GROUP, artifact.getGroupId());
		fingerPrint.getSlots().put(ARTIFACT, artifact.getArtifactId());
		Pair<String,String> splitName = splitQualifiedTypeName((ZedEntity) fe.getOwner());
		fingerPrint.getSlots().put( PACKAGE, splitName.first);
		fingerPrint.getSlots().put(TYPE, splitName.second);
		fingerPrint.getSlots().put( FIELD, fe.getName());		
		fingerPrint.getSlots().put(ISSUE, issue);
		fingerPrint.setEntitySource(fe);
		
		return fingerPrint;
	}
	
	public static FingerPrint build( FieldEntity fe, ComparisonIssueType issue) {
		FingerPrint fingerPrint = build( fe, issue.name());
		fingerPrint.setComparisonIssueType(issue);
		return fingerPrint;
	}
	
	/**
	 * @param ge
	 * @param issue
	 * @return
	 */
	public static FingerPrint build( GenericEntity ge, String issue) {
		if (ge instanceof ZedEntity) {
			return build( (ZedEntity) ge, issue);
		}
		else if (ge instanceof MethodEntity) {
			return build( (MethodEntity) ge, issue);
		}
		else if (ge instanceof FieldEntity) {
			return build( (FieldEntity) ge, issue);
		}
		else {
			log.error("unexpected type [" + ge.getClass().getName() + "] to derive a finger print from ");
			return null;
		}		
	}
	
	public static FingerPrint build( GenericEntity ge, ComparisonIssueType issue) {
		if (ge instanceof ZedEntity) {
			return build( (ZedEntity) ge, issue);
		}
		else if (ge instanceof MethodEntity) {
			return build( (MethodEntity) ge, issue);
		}
		else if (ge instanceof FieldEntity) {
			return build( (FieldEntity) ge, issue);
		}
		else {
			log.error("unexpected type [" + ge.getClass().getName() + "] to derive a finger print from ");
			return null;
		}		
	}
	
	
	/**
	 * matches two String values, while 'null' means a wildcard 
	 * @param lock - the value taken from the lock {@link FingerPrint}
	 * @param key - the value taken from the key {@link FingerPrint}
	 * @return
	 */
	private static boolean match(String lock, String key) {
		if (key != null) {
			if (lock == null) {
				return false;
			}
			if (!lock.equalsIgnoreCase( key)) {
				return false;
			}
		}
		return true;		
	}
	
	/**
	 * match a slot of two {@link FingerPrint}
	 * @param slot - the slot to match
	 * @param lock - the {@link FingerPrint} that acts as the lock
	 * @param key - the {@link FingerPrint} that acts as the key 
	 * @return - true if the lock's slot value matches the key's slot value
	 */
	private static boolean match( String slot, FingerPrint lock, FingerPrint key) {
		return match( lock.getSlots().get(slot), key.getSlots().get( slot));
	}
	
	
	/**
	 * matches the key {@link FingerPrintExpert} to the lock {@link FingerPrintExpert}
	 * @param lock - the {@link FingerPrintExpert} to test 
	 * @param two - the {@link FingerPrintExpert} containing the match data
	 * @return - true if matches 
	 */
	public static boolean matches( FingerPrint lock, FingerPrint key) {
		// group id
		if (!match( GROUP, lock, key)) {
			return false;
		}
		
		// artifact id 
		if (!match( ARTIFACT, lock, key)) {
			return false;
		}
		
		if (!match( PACKAGE, lock, key)) {
			return false;
		}
		
		// type id
		if (!match( TYPE, lock, key)) {
			return false;
		}
		
		// method id
		if (!match( METHOD, lock, key)) {
			return false;
		}
		
		// entity (model entity : GenericEntity) 
		if (!match( ENTITY, lock, key)) {
			return false;
		}
		
		// property id
		if (!match( PROPERTY, lock, key)) {
			return false;
		}
		
		// field
		if (!match( FIELD, lock, key)) {
			return false;
		}
				
		// issue id
		if (!match( ISSUE, lock, key)) {
			return false;
		}
		
		if (!match(ISSUE_KEY, lock, key)) {
			return false;
		}

		
		if (key.getIssueData() != null) {
			if (lock.getIssueData() == null) {
				return false;
			}
			for (String d : key.getIssueData()) {
				if (!lock.getIssueData().contains( d))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * builds a string value from a FingerPrint's slot
	 * @param f - the {@link FingerPrint}
	 * @param slot - the slot to get 
	 * @return - a String representation of the value in the slot 
	 */
	private static boolean buildValue( StringBuilder sb, FingerPrint f, String slot) {
		String value = f.getSlots().get( slot);
		if (value != null) {
			sb.append(slot + ":" + value);
			return true;
		}				
		return false;
	}
 	
	/**
	 * build a string representation of a {@link FingerPrint}
	 * @param f
	 * @return
	 */
	public static String toString( FingerPrint f) {
		StringBuilder sb = new StringBuilder();
		
		if (buildValue( sb, f, GROUP))
			sb.append( "/");
		
		if (buildValue( sb, f, ARTIFACT))
			sb.append( "/");
		
		if (buildValue( sb, f, PACKAGE))
			sb.append( "/");
		
		if (buildValue( sb, f, TYPE))
			sb.append( "/");
		
		if (buildValue( sb, f, FIELD))
			sb.append( "/");
		
		if (buildValue( sb, f, METHOD))
			sb.append( "/");
		
		if (buildValue( sb, f, ENTITY))
			sb.append( "/");
		
		if (buildValue( sb, f, PROPERTY))
			sb.append( "/");
		
		buildValue( sb, f, ISSUE);
		
		List<String> issueData = f.getIssueData();
	
		if (issueData != null && issueData.size() > 0) {			
			sb.append('(');
			sb.append( issueData.stream().collect(Collectors.joining(",")));
			sb.append( ')');			
		}
		
		return sb.toString();
	}

	public static FingerPrint build(GenericEntity ge) {		
		return build(ge, (String) null);
	}
}
