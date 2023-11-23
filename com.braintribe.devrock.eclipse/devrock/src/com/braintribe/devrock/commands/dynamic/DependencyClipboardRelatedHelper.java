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
package com.braintribe.devrock.commands.dynamic;

import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;

public abstract class DependencyClipboardRelatedHelper implements StorageLockerSlots{
	
	public static final String STORAGE_SLOT_COPY_MODE = SLOT_CLIP_COPY_MODE;
	public static final String STORAGE_SLOT_PASTE_MODE = SLOT_CLIP_PASTE_MODE;

	public static String getAppropriateActionLabelRepresentation( VersionModificationAction action) {
		switch (action) {
		case rangified:
			return "and rangify ...";
		case referenced:
			return "and replace ...";
		case untouched:
		default:
			return "";
		}
	}
	public static String getAppropriateActionTooltipRepresentation( VersionModificationAction action) {
		switch (action) {
		case rangified:
			return "and rangify the version";
		case referenced:
			return "and replace the version with a variable";
		case untouched:
		default:
			return "and keep the version as is";
		}
	}
	
	
}
