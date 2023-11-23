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
package com.braintribe.model.meta.data.display;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.UniversalMetaData;
import com.braintribe.model.meta.data.prompt.Outline;

/**
 * Specifies, if present, that a property label in a view for properties should be hidden from the view.
 * For example, in the case of the <code>PropertyPanel</code>, this metadata will hide the property name column
 * and in case the property is also marked {@link Outline}, it will hide the whole line, only displaying the outlined part.
 *
 */
public interface HideLabel extends UniversalMetaData, ExplicitPredicate {
	
	EntityType<HideLabel> T = EntityTypes.T(HideLabel.class);

}
