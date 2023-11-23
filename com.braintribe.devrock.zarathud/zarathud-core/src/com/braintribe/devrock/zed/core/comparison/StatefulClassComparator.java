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

import com.braintribe.devrock.zed.api.comparison.ComparisonContext;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.forensics.findings.ComparisonProcessFocus;

/**
 * 
 * @author pit
 */
public class StatefulClassComparator {
	
	private String baseName;
	private ComparisonContext context;
	
	public StatefulClassComparator(ComparisonContext context) {
		this.context = context;
	}
	
	/**
	 * compares a class 
	 * @param base
	 * @param other
	 * @return
	 */
	public void compare( ClassEntity base, ClassEntity other) {
		baseName = base.getName();
		
		if (context.isProcessed(baseName))
			return;			
		context.addProcessed( baseName);
					
		// abstract nature
		context.pushCurrentProcessFocus( ComparisonProcessFocus.abstractModifier);
		CommonStatelessComparators.compareAbstractNature(context, base, other);
		context.popCurrentProcessFocus();
		
		// access modifiers
		context.pushCurrentProcessFocus( ComparisonProcessFocus.accessModifier);
		CommonStatelessComparators.compareAccessModifiers(context, base, other);
		context.popCurrentProcessFocus();
		
		// template parameters
		
		// super type				
		TypeReferenceEntity baseSuperTypeReference = base.getSuperType();
		TypeReferenceEntity otherSuperTypeReference = other.getSuperType();								
		
		context.pushCurrentProcessFocus( ComparisonProcessFocus.superType);
		CommonStatelessComparators.compareTypeReferenceNullSafe(context, baseSuperTypeReference, otherSuperTypeReference);
		context.popCurrentProcessFocus();
	
		// implemented interfaces		
		context.pushCurrentProcessFocus( ComparisonProcessFocus.implementedInterfaces);
		CommonStatelessComparators.compareTypeReferences( context, base.getImplementedInterfaces(), other.getImplementedInterfaces());
		context.popCurrentProcessFocus();
									
		// fields			
		context.pushCurrentProcessFocus( ComparisonProcessFocus.fields);
		CommonStatelessComparators.compareFields(context, base.getFields(), other.getFields());
		context.popCurrentProcessFocus();
		
		// methods
		context.pushCurrentProcessFocus( ComparisonProcessFocus.methods);
		CommonStatelessComparators.compareMethods(context, base.getMethods(), other.getMethods());
		context.popCurrentProcessFocus();
		
		// annotations 
		context.pushCurrentProcessFocus( ComparisonProcessFocus.annotations);
		CommonStatelessComparators.compareAnnotations( context, base.getAnnotations(), other.getAnnotations());
		context.popCurrentProcessFocus();
		
		// template parameters
		context.pushCurrentProcessFocus( ComparisonProcessFocus.templateParameters);
		CommonStatelessComparators.compareTemplateParameters(context, base.getTemplateParameters(), other.getTemplateParameters());
		context.popCurrentProcessFocus();
		
				
	}
			
}
