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
package tribefire.cortex.manipulation.conversion.code;

import java.util.function.Predicate;

import com.braintribe.model.generic.manipulation.Manipulation;

import tribefire.cortex.manipulation.conversion.beans.EntityBean;

/**
 * @author peter.gazdik
 */
public class CodeWriterParams {

	public JscPool jscPool = new JscPool();

	public String initializerPackage;
	public String spacePrefix;
	public Manipulation manipulation;
	public Predicate<EntityBean<?>> allowedRootTypeFilter;
}
