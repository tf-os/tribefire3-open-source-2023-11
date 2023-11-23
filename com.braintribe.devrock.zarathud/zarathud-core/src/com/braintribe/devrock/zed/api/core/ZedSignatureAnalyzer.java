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
package com.braintribe.devrock.zed.api.core;

import com.braintribe.devrock.zed.api.context.ZedAnalyzerProcessContext;
import com.braintribe.devrock.zed.scan.asm.MethodSignature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;

/**
 * @author pit
 *
 */
public interface ZedSignatureAnalyzer {
	/**
	 * @param context - the {@link ZedAnalyzerProcessContext}
	 * @param signature - the signature of the method
	 * @return - a modelled {@link MethodSignature}
	 */
	MethodSignature extractMethodSignature( ZedAnalyzerProcessContext context, String signature);
	
	/**
	 * @param context - the {@link ZedAnalyzerProcessContext}
	 * @param desc - the desc 
	 * @param expectation - the expected {@link EntityType} 
	 * @return - the {@link TypeReferenceEntity} 
	 */
	
	TypeReferenceEntity analyzeReference( ZedAnalyzerProcessContext context, String desc, EntityType<? extends ZedEntity> expectation);
	/**
	 * @param context - the {@link ZedAnalyzerProcessContext}
	 * @param desc - the type desc
	 * @param expectation - the expected {@link EntityType}
	 * @return - the {@link TypeReferenceEntity}
	 */
	TypeReferenceEntity processType(ZedAnalyzerProcessContext context, String desc, EntityType<? extends ZedEntity> expectation);
}
