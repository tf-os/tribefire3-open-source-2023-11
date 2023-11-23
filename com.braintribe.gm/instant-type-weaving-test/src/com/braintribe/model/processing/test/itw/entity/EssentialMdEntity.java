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
package com.braintribe.model.processing.test.itw.entity;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface EssentialMdEntity extends GenericEntity {

	EntityType<EssentialMdEntity> T = EntityTypes.T(EssentialMdEntity.class);

	String regularProperty = "regularProperty";
	String confidential = "confidential";

	String getRegularProperty();
	void setRegularProperty(String regularProperty);

	@Confidential
	String getConfidential();
	void setConfidential(String confidential);

	/**
	 * This property is declared confidential in the {@link EssentialMdSiblingEntity} to test the multiple inheritance case. The sub-type
	 * {@link EssentialMdSubEntity} must have both "confidential" and "siblingConfidential" marked as confidential.
	 */
	String getSiblingConfidential();
	void setSiblingConfidential(String siblingConfidential);

}
