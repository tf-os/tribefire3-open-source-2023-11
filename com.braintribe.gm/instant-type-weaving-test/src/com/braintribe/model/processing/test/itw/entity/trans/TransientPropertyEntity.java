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
package com.braintribe.model.processing.test.itw.entity.trans;

import java.sql.Timestamp;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.test.jta.model.errors.NonGmEnum;

/**
 * @author peter.gazdik
 */
public interface TransientPropertyEntity extends GenericEntity {

	EntityType<TransientPropertyEntity> T = EntityTypes.T(TransientPropertyEntity.class);

	int NUMBER_OF_PROPS = 5;
	
	@Transient
	String getName();
	void setName(String name);

	@Transient
	Object getObject();
	void setObject(Object object);

	@Transient
	Timestamp getTimestamp();
	void setTimestamp(Timestamp timestamp);

	@Transient
	NonGmEnum getNonGmEnum();
	void setNonGmEnum(NonGmEnum nonGmEnum);

	@Transient
	TransientPropertyEntity getSelf();
	void setSelf(TransientPropertyEntity self);

}
