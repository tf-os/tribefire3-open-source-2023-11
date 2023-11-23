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
package com.braintribe.devrock.zed.core.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.braintribe.devrock.zed.api.comparison.ComparisonContext;
import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.AnnotationValueContainer;
import com.braintribe.zarathud.model.data.AnnotationValueContainerType;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.ComparisonIssueType;

/**
 * compares two annotations 
 * @author pit
 */
public class StatefulAnnotationComparator {
	
	private ComparisonContext context;

	public StatefulAnnotationComparator(ComparisonContext context) {
		this.context = context;
		
	}
	
	/**
	 * @param base
	 * @param other
	 */
	public void compare( AnnotationEntity base, AnnotationEntity other) {
		// annotations are not 'real types', but actually attachements.. the declaring type reference would be it..
		/*
		if (context.isProcessed( base.getName()))
			return;
		
		context.addProcessed( base.getName());
		*/	
		
		Map<String, AnnotationValueContainer> baseMembers = base.getMembers();
		Map<String, AnnotationValueContainer> otherMembers = other.getMembers();
		
		
		context.pushCurrentOther(other);
		context.pushCurrentEntity(base);
		
		compare(baseMembers, otherMembers);
		
		context.popCurrentEntity();
		context.popCurrentOther();
	}

	/**
	 * @param baseMembers
	 * @param otherMembers
	 */
	private void compare(Map<String, AnnotationValueContainer> baseMembers, Map<String, AnnotationValueContainer> otherMembers) {
		List<String> missing = new ArrayList<>();
		
		for (Map.Entry<String, AnnotationValueContainer> baseEntry : baseMembers.entrySet()) {
			AnnotationValueContainer baseAvc = baseEntry.getValue();
			
			AnnotationValueContainer otherAvc = otherMembers.get( baseEntry.getKey());
			if (otherAvc == null) {
				missing.add( baseEntry.getKey());
				continue;
			}
			compare( baseAvc, otherAvc);			
		}
		
		if (missing.size() > 0) {
			// create fingerprint for missing annotation value containers
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.missingAnnotationContainers);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData(missing);
			context.addFingerPrint(fp);
		}
		
		List<String> surplus = new ArrayList<>( otherMembers.keySet());
		surplus.removeAll( baseMembers.keySet());
		if (surplus.size() > 0) {
			// create fingerprint for surplus annotation value containers
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.surplusAnnotationContainers);			
			fp.setEntityComparisonTarget( context.getCurrentOther());
			fp.setIssueData( surplus);
			context.addFingerPrint(fp);			
		}
	}

	/**
	 * @param baseAvc
	 * @param otherAvc
	 */
	private void compare(AnnotationValueContainer baseAvc, AnnotationValueContainer otherAvc) {
		AnnotationValueContainerType bContainerType = baseAvc.getContainerType();
		AnnotationValueContainerType oContainerType = otherAvc.getContainerType();
		
		// compare type of annotation value 
		if (bContainerType != oContainerType) {
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.annotationValueMismatch);			
			fp.setEntityComparisonTarget( context.getCurrentOther());					
			context.addFingerPrint(fp);
			return;
		}
		
		// compare values
		boolean mismatch = false;
		switch (bContainerType) {
			case annotation:
				new StatefulAnnotationComparator(context).compare( baseAvc.getOwner(), otherAvc.getOwner());				
				break;
			case collection:			
				System.out.println("Unsupported yet : collection of values" );
				break;				
			case s_boolean:
				mismatch = (baseAvc.getSimpleBooleanValue() != 	otherAvc.getSimpleBooleanValue());				
				break;
			case s_date:
				mismatch = (baseAvc.getSimpleDateValue().compareTo( otherAvc.getSimpleDateValue()) != 0);									
				break;
			case s_double:
				mismatch = baseAvc.getSimpleDoubleValue() != otherAvc.getSimpleDoubleValue();								
				break;
			case s_float:
				mismatch = 	baseAvc.getSimpleFloatValue() != otherAvc.getSimpleFloatValue();
				break;
			case s_int:
				mismatch = baseAvc.getSimpleIntegerValue() != otherAvc.getSimpleIntegerValue();
				break;
			case s_long:
				mismatch = 	baseAvc.getSimpleLongValue() != otherAvc.getSimpleLongValue();
				break;
			case s_string:			
				mismatch =  baseAvc.getSimpleStringValue().compareTo(otherAvc.getSimpleStringValue()) != 0;
				break;
			default:
				break;		
		}
		
		if (mismatch) {		
			FingerPrint fp = FingerPrintExpert.build( context.getCurrentEntity(), ComparisonIssueType.annotationValueMismatch);			
			fp.setEntityComparisonTarget( context.getCurrentOther());					
			context.addFingerPrint(fp);
		}
		
	}
}
