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
package com.braintribe.model.processing.meta.oracle.flat;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.List;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmEntityTypeOverride;

/**
 * Represents a property declared for a given {@link GmEntityType}, with all the {@link GmPropertyInfo}s from that type (i.e. considers the
 * {@link GmEntityType} itself and all the corresponding {@link GmEntityTypeOverride}, but does not include any information from super-types).
 * 
 * There are two cases for a {@link FlatProperty}, hence we also have two constructors.
 * 
 * If the property is declared/overridden on given entity level, we create a regular {@link FlatProperty} with corresponding list of
 * {@link GmPropertyInfo}s.
 * 
 * If, however, this property is only inherited, and is not overridden on this level, we keep this list of {@link GmPropertyInfo}s empty. In such
 * case, we use a constructor taking the {@linkplain FlatProperty} from a super-type, and we set the <tt>infos</tt> to an empty list. This handling
 * exists to reduce the memory usage.
 * 
 * @see FlatPropertiesFactory#ensureSuperProperty
 * 
 * @author peter.gazdik
 */
public class FlatProperty {

	public final GmProperty gmProperty;
	public final List<GmPropertyInfo> infos;

	public FlatProperty(GmProperty gmProperty) {
		this.gmProperty = gmProperty;
		this.infos = newList();
	}

	public FlatProperty(FlatProperty flatSuperProperty) {
		this.gmProperty = flatSuperProperty.gmProperty;
		this.infos = Collections.emptyList();
	}

}
