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
package com.braintribe.devrock.zed.ui.transposer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.ClassEntity;
import com.braintribe.zarathud.model.data.EnumEntity;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.InterfaceEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class TopologyExpert {

	public static ZedEntity findBaseTopOwner( FingerPrint fp) {
		GenericEntity entitySource = fp.getEntitySource();
		if (entitySource == null  || entitySource instanceof Artifact)
			return null;
		return findTopOwner( entitySource);
	}
	
	public static ZedEntity findOtherTopOwner( FingerPrint fp) {
		GenericEntity entitySource = fp.getEntityComparisonTarget();
		if (entitySource == null || entitySource instanceof Artifact)
			return null;
		return findTopOwner( entitySource);
	}
	
	public static ZedEntity findTopOwner( GenericEntity entitySource) {		
		GenericEntity passed = entitySource;
		ZedEntity ze = _findTopOwner(entitySource);
		if (ze != null)
			return ze;
		return (ZedEntity) passed;
	}
	
	public static ZedEntity _findTopOwner( GenericEntity entitySource) {
		
		if (entitySource instanceof MethodEntity) {
			MethodEntity me = (MethodEntity) entitySource;
			return _findTopOwner(me.getOwner());
		}
		else if (entitySource instanceof FieldEntity) {
			FieldEntity fe = (FieldEntity) entitySource;
			return _findTopOwner(fe.getOwner());
		}
		else if (entitySource instanceof AnnotationEntity) {
			AnnotationEntity ae = (AnnotationEntity) entitySource;
			if (ae.getOwner() == null) {
				return ae;
			}
			return _findTopOwner(ae.getOwner());
		}
		else if (entitySource instanceof ClassEntity) {
			return (ZedEntity) entitySource;
		}
		else if (entitySource instanceof InterfaceEntity){
			return (ZedEntity) entitySource;
		}
		else if (entitySource instanceof EnumEntity) {
			return (ZedEntity) entitySource;
		}
		else {		
			return null;
		}
	}
}
