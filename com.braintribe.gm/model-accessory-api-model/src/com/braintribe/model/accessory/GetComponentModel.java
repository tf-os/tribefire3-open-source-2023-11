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
package com.braintribe.model.accessory;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.components.ModelExtension;

/**
 * Resolves a model of a deployable component, identified by it's {@link #getExternalId() externalId}.
 * 
 * @see GetAccessModel
 * @see GetServiceModel
 * 
 * @author peter.gazdik
 */
@Abstract
public interface GetComponentModel extends ModelRetrievingRequest {

	EntityType<GetComponentModel> T = EntityTypes.T(GetComponentModel.class);

	/** ExternalId of the deployable for which we are getting the model. */
	@Mandatory
	String getExternalId();
	void setExternalId(String externalId);

	/**
	 * If <tt>true</tt>, the extended model will be returned, i.e. one obtained after {@link ModelExtension} meta-data has been applied on top of the
	 * model configured on the deployable.
	 */
	boolean getExtended();
	void setExtended(boolean extended);

}
