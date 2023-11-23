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
package com.braintribe.devrock.eclipse.model.identification;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface SelectableEnhancedCompiledArtifactIdentification extends EnhancedCompiledArtifactIdentification {
	EntityType<SelectableEnhancedCompiledArtifactIdentification> T = EntityTypes.T(SelectableEnhancedCompiledArtifactIdentification.class);

	String existInWorkspace = "existInWorkspace";
	String existInCurrentWorkingSet = "existInCurrentWorkingSet";
	String canBeImportedWithCurrentChoices = "canBeImportedWithCurrentChoices";
	
	
	boolean getExistsInWorkspace();
	void setExistsInWorkspace(boolean value);

	boolean getExistsInCurrentWorkingSet();
	void setExistsInCurrentWorkingSet(boolean value);
	
	boolean getCanBeImportedWithCurrentChoices();
	void setCanBeImportedWithCurrentChoices(boolean value);



	static SelectableEnhancedCompiledArtifactIdentification from( EnhancedCompiledArtifactIdentification ecai) {
		SelectableEnhancedCompiledArtifactIdentification secai = SelectableEnhancedCompiledArtifactIdentification.T.create();
		secai.setGroupId( ecai.getGroupId());
		secai.setArtifactId( ecai.getArtifactId());
		secai.setVersion( ecai.getVersion());
		secai.setArchetype( ecai.getArchetype());
		secai.setOrigin( ecai.getOrigin());
		
		return secai;
	}
}
