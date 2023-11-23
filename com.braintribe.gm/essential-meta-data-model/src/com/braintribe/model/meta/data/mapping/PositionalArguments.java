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
package com.braintribe.model.meta.data.mapping;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;

/**
 * <p>PositionalArguments determines the order of positioned arguments in an API call - think of command-line, REST and DSLs. 
 * A positioned argument maps to a property name and therefore this is an alternative way to address that property.
 * 
 * <p>Normal APIs work only with positional arguments which only look nice for a small set of arguments and gets annoying when
 * a lot of argument recombinations are of interest. 
 * 
 * <p>Denotation-driven APIs are recommended and well supported by GenericModels. 
 * They primarily work with named arguments (properties) to make them defaultable, addressable and distinguishable in any order. 
 * Still very important usecases would be sexier when using positional arguments and that can be achieved with this metadata. 
 * 
 * @author Dirk Scheffler
 */
public interface PositionalArguments extends EntityTypeMetaData, ModelSkeletonCompatible {

	EntityType<PositionalArguments> T = EntityTypes.T(PositionalArguments.class);
	
	List<String> getProperties();
	void setProperties(List<String> properties);
}
