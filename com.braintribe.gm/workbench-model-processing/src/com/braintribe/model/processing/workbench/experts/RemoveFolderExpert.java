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
package com.braintribe.model.processing.workbench.experts;

import java.util.Iterator;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.workbench.instruction.RemoveFolder;


/**
 * Identifies a folder by specified path and removes a subFolder of this folder identified by
 * name specified in folderToRemove. 
 */
public class RemoveFolderExpert implements WorkbenchInstructionExpert<RemoveFolder> {
	
	@Override
	public void process(RemoveFolder instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
		
		// Search for the target folder based on given path.
		Folder folder = context.getFolderByPath(instruction.getPath());
		
		// Iterate through subFolders and remove subFolders with specified name.
		Iterator<Folder> subFolderIterator = folder.getSubFolders().iterator();
		while (subFolderIterator.hasNext()) {
			Folder subFolder = subFolderIterator.next();
			if (subFolder.getName().equals(instruction.getFolderToRemove())) {
				subFolderIterator.remove();
			}
		}
		
	}

}
