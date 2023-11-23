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

import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.generic.synchronize.BasicEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.experts.ResourceIdentityManager;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.workbench.instruction.AddFolder;

/**
 * Adds a given folder to the subFolder list of target folder identified by given path. 
 */
public class AddFolderExpert implements WorkbenchInstructionExpert<AddFolder> {

	@Override
	public void process(AddFolder instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
	
		// Search for the target folder based on given path.
		Folder folder = context.getFolderByPath(instruction.getPath());
		
		if (shouldAddFolder(folder, instruction)) {
			
			// We synchronize the new folder to ensure it's session based and 
			// assume a single folder returned by the synchronization. 
			Folder synchronizedFolderToAdd = 
					BasicEntitySynchronization
					.newInstance(false)
					.session(context.getSession())
					.addEntity(instruction.getFolderToAdd())
					.addIdentityManager(new ResourceIdentityManager())
					.addDefaultIdentityManagers()
					.synchronize()
					.unique();
			
			// Finally adding the synchronized folder to the sub folders of target folder.
			folder.getSubFolders().add(synchronizedFolderToAdd);
		}
		
		
	}

	/**
	 * If the target folder already contains a folder with same name of folderToAdd and
	 * we shouldn't override existing folders we can skip this instruction. 
	 */
	private boolean shouldAddFolder(Folder folder, AddFolder instruction) {
		if (instruction.getOverrideExisting()) return true; // Add folder anyways.

		String folderToAddName = instruction.getFolderToAdd().getName();
		for (Folder subFolder : folder.getSubFolders()) {
			if (subFolder.getName().equals(folderToAddName)) {
				return false;
			}
		}
		// No existing subFolder found. Add folder
		return true;
	}

}
