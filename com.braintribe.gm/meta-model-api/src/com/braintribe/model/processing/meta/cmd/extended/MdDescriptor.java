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
package com.braintribe.model.processing.meta.cmd.extended;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * @author peter.gazdik
 */
@Abstract
public interface MdDescriptor extends MetaData, QualifiedMetaData {

	EntityType<MdDescriptor> T = EntityTypes.T(MdDescriptor.class);

	MetaData getResolvedValue();
	void setResolvedValue(MetaData resolvedValue);

	/** The model on which this meta data was declared. Only null if the MD is configured as default. */
	GmMetaModel getOwnerModel();
	void setOwnerModel(GmMetaModel ownerModel);

	boolean getResolvedAsDefault();
	void setResolvedAsDefault(boolean resolvedAsDefault);

	/** If given MD is a {@link Predicate}, this is it's corresponding boolean value. */
	boolean getIsTrue();
	void setIsTrue(boolean isTrue);

	@Override
	default MetaData metaData() {
		return this; // This is QualifiedMetaData so that it is compatible with MetaDataBox
	}

	@Override
	default GmModelElement ownerElement() {
		return null; // no need for this value, the MdDescriptor contains the owner element information
	}

	default String origin() {
		return getResolvedAsDefault() ? "[default]" : getOwnerModel().getName();
	}
	
}
