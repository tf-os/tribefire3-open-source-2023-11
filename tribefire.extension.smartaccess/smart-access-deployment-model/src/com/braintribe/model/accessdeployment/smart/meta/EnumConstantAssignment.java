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
package com.braintribe.model.accessdeployment.smart.meta;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.EnumConstantMetaData;

/**
 * Mapping which is configured for a {@link GmEnumConstant} of a smart enum, defining what given constant is mapped to
 * in the delegate.
 * 
 * IMPORTANT NOTE: In case we also have a {@link SmartConversion} defined for an enum property, only the conversion is
 * considered (so the constant assignment is ignored). So you could say this {@link EnumConstantAssignment} is just a
 * convenient way to configure all the conversions (for all the enum-related properties) at the same time. This means a
 * conversion for an enum property must always say how the original enum is converted, without having to consider what
 * is mapped to by default via this meta-data.
 * 
 * @see ConversionEnumConstantAssignment
 * @see IdentityEnumConstantAssignment
 * @see QualifiedEnumConstantAssignment
 */
@Abstract
public interface EnumConstantAssignment extends EnumConstantMetaData {

	EntityType<EnumConstantAssignment> T = EntityTypes.T(EnumConstantAssignment.class);

}
