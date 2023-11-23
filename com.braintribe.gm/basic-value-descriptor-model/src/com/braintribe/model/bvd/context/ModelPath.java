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
package com.braintribe.model.bvd.context;


import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.type.ImplicitlyTypedDescriptor;

/**
 * A {@link ValueDescriptor} addressing elements of the current model path.
 *
 */
public interface ModelPath extends ImplicitlyTypedDescriptor {

	final EntityType<ModelPath> T = EntityTypes.T(ModelPath.class);
	
	@Initializer("enum(com.braintribe.model.bvd.context.ModelPathElementAddressing,last)")
	ModelPathElementAddressing getAddressing();
	void setAddressing(ModelPathElementAddressing addressing);

	/**
	 * Specifies the offset for the addressing. Only applies when addressing is either set to first or last.
	 */
	@Initializer("0")
	int getOffset();
	void setOffset(int offset);

	/**
	 * Specifies whether the root or the selection model path should be inspected. 
	 */
	boolean getUseSelection();
	void setUseSelection(boolean useSelection);
	
}
