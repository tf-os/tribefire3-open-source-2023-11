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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.weaving.ProtoGmEntityType;

/**
 * @author peter.gazdik
 */
public interface ItwTypeReflection extends GenericModelTypeReflection {

	<T extends GenericModelType> T getDeployedType(String typeSignature);

	EnumType deployEnumType(Class<? extends Enum<?>> enumClass);

	void deployEntityType(EntityType<?> entityType);

	/**
	 * Returns a ProtoGmEntityType created with "ProtoAnalysis" of corresponding {@link Class} object, or <tt>null</tt>, if no class is found via
	 * Class.forName(...)
	 * <p>
	 * This is used in ITW to make sure a type with given signature is always woven based on the class on classpath, if available, rather than using
	 * given {@link GmEntityType} instance, which (e.g. when coming from a remote system) might not correspond to the one on classpath.
	 */
	ProtoGmEntityType findProtoGmEntityType(String typeSignature);

}
