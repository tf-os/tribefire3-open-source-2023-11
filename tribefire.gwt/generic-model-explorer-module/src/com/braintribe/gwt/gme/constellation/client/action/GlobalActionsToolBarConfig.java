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
package com.braintribe.gwt.gme.constellation.client.action;

import com.braintribe.gwt.gme.constellation.client.LocalizedText;

public enum GlobalActionsToolBarConfig {

	ACTION_HOME("$new", LocalizedText.INSTANCE.newEntry(), "", "action"),
	ACTION_DUAL_SECTION("$dualSectionButtons", "Dual Section Buttons", "", "action"),
	ACTION_UPLOAD("$upload", LocalizedText.INSTANCE.upload(), "$dualSectionButtons", "action"),
	ACTION_UNDO("$undo", LocalizedText.INSTANCE.undo(), "", "action"),
	ACTION_REDO("$redo", LocalizedText.INSTANCE.redo(), "", "action"),
	ACTION_COMMIT("$commit", LocalizedText.INSTANCE.commit(), "", "action"),
	;
	
	GlobalActionsToolBarConfig(String name, String displayName, String parentFolder, String kind) {
		this.name = name;
		this.displayName = displayName;
		this.parentFolder = parentFolder;
		this.kind = kind;
	}
	
	private String name;
	private String displayName;
	private String parentFolder;
	private String kind;
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getParentFolder() {
		return parentFolder;
	}

	public String getKind() {
		return kind;
	}
}
