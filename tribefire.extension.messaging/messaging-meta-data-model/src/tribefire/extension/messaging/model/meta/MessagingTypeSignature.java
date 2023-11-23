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
package tribefire.extension.messaging.model.meta;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * Contains information required to inform Messaging aspect whether the id(s) should be loaded from 'request' or
 * 'expert's response'
 */
public interface MessagingTypeSignature extends EntityTypeMetaData {
	EntityType<MessagingTypeSignature> T = EntityTypes.T(MessagingTypeSignature.class);

	/* Type of object containing identification to load the object state using Property from MessagingPropertyMd
	 * (Request/Response) */
	@Mandatory
	RelatedObjectType getIdObjectType();
	void setIdObjectType(RelatedObjectType idObjectType);
}
