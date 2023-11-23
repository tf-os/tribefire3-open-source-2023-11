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
package com.braintribe.model.workbench.instruction;

import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.style.Color;
import com.braintribe.model.uitheme.UiTheme;


public interface UpdateUiStyle extends WorkbenchInstruction {
	
	
	EntityType<UpdateUiStyle> T = EntityTypes.T(UpdateUiStyle.class);

	
	void setEnsureWorkbenchConfiguration(boolean ensureWorkbenchConfiguration);
	@Initializer("true")
	boolean getEnsureWorkbenchConfiguration();
	
	void setOverrideExisting(boolean overrideExisting);
	@Initializer("true")
	boolean getOverrideExisting();
	
	void setStylesheet(Resource stylesheet);
	Resource getStylesheet();
	
	void setColorsToEnsure(Set<Color> colorsToEnsure);
	Set<Color> getColorsToEnsure();
	
	void setUiThemesToEnsure(Set<UiTheme> uiThemesToEnsure);
	Set<UiTheme> getUiThemesToEnsure();
	
}
