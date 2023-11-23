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
package com.braintribe.zarathud.model.forensics.findings;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

public enum ComparisonIssueType implements IssueType, EnumBase {
	noContentToCompare,				// corresponding entity not found  
	typeMismatch, 					// wrong/differing type
	
	additionalContent,				// new entities in other 
	
	genericityMismatch,
	
	// modifiers
	abstractModifierMismatch, 		// abstract declaration mismatch 
	accessModifierMismatch, 		// public/protected/private/package-private declaration mismatch
	scopeModifierMismatch, 			// final/volatile/default declaration mismatch	
	staticModifierMismatch,			// static declaration mismatch
	synchronizedModifierMismatch,	// synchronized declaration mismatch
	
	
	
	referencedTypeMismatch,			// referenced type differs
	
	// class / enum
	superTypeMismatch,				// wrong differing type as super-type
	missingSubTypes,
	surplusSubTypes,
	
	missingImplementedInterfaces,
	surplusImplementedInterfaces,
	
	missingMethods,
	surplusMethods,
	
	// enum
	missingEnumValues,
	surplusEnumValues,
	
	// interfaces
	missingImplementingClasses,
	surplusImplementingClasses,
	
	missingSuperInterfaces,
	surplusSuperInterfaces,
	
	missingSubInterfaces,
	surplusSubInterfaces,
	
	// method
	missingMethodArguments,
	surplusMethodArguments,
	methodReturnTypeMismatch,
	missingMethodExceptions,
	surplusMethodExceptions,
	methodMismatch,					// method issue ? (split: argumentMismatch, returnTypeMismatch?) 
	
	// fields
	missingFields,
	surplusFields,
	fieldTypeMismatch,					// field issue ?
	
	//
	missingAnnotations,
	surplusAnnotations,
	missingAnnotationContainers,
	surplusAnnotationContainers,
	annotationValueMismatch,
	
	// template parameters
	missingTemplateParameters,
	surplusTemplateParameters,
	
	missingInCollection,			// entry misses (arguments/sub-types/implemented-interfaces)? 
	surplusInCollection,			// entry surplus (arguments/sub-types/implemented-interfaces)?
	;
	
	public static EnumType T = EnumTypes.T(ComparisonIssueType.class);

	
	@Override
	public EnumType type() {
		return T;
	}
	
}
