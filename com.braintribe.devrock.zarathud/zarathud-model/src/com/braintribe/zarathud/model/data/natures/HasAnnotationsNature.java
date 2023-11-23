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
package com.braintribe.zarathud.model.data.natures;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;

/**
 * represents the feature of having annotations
 * 
 * @author pit
 *
 */
@Abstract
public interface HasAnnotationsNature extends GenericEntity {
	
	EntityType<HasAnnotationsNature> T = EntityTypes.T(HasAnnotationsNature.class);
	String annotations = "annotations";
	
	/**
	 * @return - the attached annotations, as a {@link Set} of {@link AnnotationEntity}
	 */
	Set<TypeReferenceEntity> getAnnotations();
	void setAnnotations( Set<TypeReferenceEntity> annotations);
}
