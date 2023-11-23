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
package com.braintribe.model.processing.query.smart.test2.shared.model.shared;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartGenericEntity;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;

/**
 * Default partition -> {@link SmartMappingSetup#accessIdB}
 */
public interface SharedFile extends SharedEntity, SmartGenericEntity {

	EntityType<SharedFile> T = EntityTypes.T(SharedFile.class);

	String name = "name";
	String parent = "parent";
	String fileDescriptor = "fileDescriptor";
	String childSet = "childSet";
	String childList = "childList";
	String childMap = "childMap";

	String getName();
	void setName(String name);

	SharedFile getParent();
	void setParent(SharedFile parent);

	SharedFileDescriptor getFileDescriptor();
	void setFileDescriptor(SharedFileDescriptor fileDescriptor);

	Set<SharedFile> getChildSet();
	void setChildSet(Set<SharedFile> childSet);

	List<SharedFile> getChildList();
	void setChildList(List<SharedFile> childList);

	Map<SharedFile, SharedFile> getChildMap();
	void setChildMap(Map<SharedFile, SharedFile> childMap);

}
