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
package com.braintribe.model.workbench;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.session.GmSession;

@SelectiveInformation("${displayName} Perspective")
public interface WorkbenchPerspective extends GenericEntity {

	EntityType<WorkbenchPerspective> T = EntityTypes.T(WorkbenchPerspective.class);

	@Mandatory
	String getName();
	void setName(String value);

	@Mandatory
	LocalizedString getDisplayName();
	void setDisplayName(LocalizedString value);

	List<Folder> getFolders();
	void setFolders(List<Folder> value);
	
	default WorkbenchPerspective workbenchPerspective(String name) {
		return initWorkbenchPerspective(name, name);
	}
	
	default WorkbenchPerspective initWorkbenchPerspective(String name, String displayName) {
		GmSession session = session();
		if (session != null)
			setDisplayName(session.create(LocalizedString.T).putDefault(displayName));
		else
			setDisplayName(LocalizedString.create(displayName));
			
		setName(name);
		return this;
	}
	
	default WorkbenchPerspective initWorkbenchPerspective(String name, LocalizedString displayName) {
		setDisplayName(displayName);
		setName(name);
		return this;
	}
	
	static WorkbenchPerspective create(String name, String displayName) {
		return T.create().initWorkbenchPerspective(name, displayName);
	}
	
	static WorkbenchPerspective create(String name) {
		return T.create().initWorkbenchPerspective(name, name);
	}
	
	static WorkbenchPerspective create(String name, LocalizedString displayName) {
		return T.create().initWorkbenchPerspective(name, displayName);
	}
}
