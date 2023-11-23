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
package com.braintribe.gwt.gmview.client;

import java.util.Set;

import com.braintribe.model.generic.path.ModelPathElementInstanceKind;
import com.braintribe.model.generic.path.ModelPathElementType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.selector.MetaDataSelector;

public interface ViewSituationSelector extends MetaDataSelector {
	
	final EntityType<ViewSituationSelector> T = EntityTypes.T(ViewSituationSelector.class);

	public TypeCondition getValueType();
	public void setValueType(TypeCondition valueType);
	
	public ModelPathElementType getPathElementType();	
	public void setPathElementType(ModelPathElementType pathElementType);
	
	public Double getConflictPriority(); 	
	public void setConflictPriority(Double conflictPriority);
	
	public String getMetadataTypeSignature();
	public void setMetadataTypeSignature(String metadataTypeSignature);
	
	public ModelPathElementInstanceKind getElementInstanceKind();
	public void setElementInstanceKind(ModelPathElementInstanceKind elementInstanceKind);
	
	public Set<String> getUseCases();
	public void setUseCases(Set<String> useCases);
	
}
