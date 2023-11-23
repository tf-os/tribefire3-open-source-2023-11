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
package com.braintribe.model.csa;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link SmoodInitializer} which represents a GMML file based persistence initializer.
 * <p>
 * The implementation loads and applies manipulation files. The actual files are always called "model.man" and
 * "data.man" and are found in a sub-folder with the name of this initializer (e.g. if the initailizer's name is
 * "custom", there will two files "custom/model.man" and "custom/data.man")
 * 
 * @author peter.gazdik
 */
public interface ManInitializer extends SmoodInitializer {

	EntityType<ManInitializer> T = EntityTypes.T(ManInitializer.class);

	/** @deprecated not needed (9.Feb.2018) */
	@Deprecated
	String getFolderName();
	@Deprecated
	void setFolderName(String FolderName);

	@Override
	default void normalize() {
		setFolderName(null);
	}
}
