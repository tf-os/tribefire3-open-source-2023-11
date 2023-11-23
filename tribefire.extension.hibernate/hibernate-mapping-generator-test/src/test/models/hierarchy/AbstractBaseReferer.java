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
package test.models.hierarchy;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmEntityTypeMapBuilder;

/**
 * The hierarchy with {@link Base}, {@link Left} and {@link Right} should collapse into a single table, even if this type is not mapped, as long as
 * it's sub-type {@link BaseReferer} is.
 * <p>
 * There was a bug that this would not be the case, because this {@link #getBase()} base property would not be considered when electing top level
 * entities in {@link HbmEntityTypeMapBuilder#categorizeEntities()}.
 */
@Abstract
public interface AbstractBaseReferer extends StandardIdentifiable {

	EntityType<AbstractBaseReferer> T = EntityTypes.T(AbstractBaseReferer.class);

	Base getBase();
	void setBase(Base base);

}
