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
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.instruction.AddFolderToPerspective;

/**
 * Adds a given folder to the perspective identified by given perspectiveName. 
 */
public class AddFolderToPerspectiveExpert implements WorkbenchInstructionExpert<AddFolderToPerspective> {

	@Override
	public void process(AddFolderToPerspective instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
	
		// Search for the target folder based on given path.
		WorkbenchPerspective perspective = getPerspective(context.getSession(), instruction.getPerspectiveName());
		
		if (shouldAddFolder(perspective, instruction)) {
			
			Folder folderToAdd = null;
			if (instruction.getUseExistingFolder()) {
				folderToAdd = context.findFolderByPath(instruction.getFolderToAdd().getName());
			}

			if (folderToAdd == null) {
				// We synchronize the new folder to ensure it's session based and 
				// assume a single folder returned by the synchronization. 
				folderToAdd = 
						BasicEntitySynchronization
						.newInstance(false)
						.session(context.getSession())
						.addEntity(instruction.getFolderToAdd())
						.addIdentityManager(new ResourceIdentityManager())
						.addDefaultIdentityManagers()
						.synchronize()
						.unique();
			}
			
			// Finally adding the synchronized folder to the sub folders of target folder.
			perspective.getFolders().add(folderToAdd);
		}
		
		
	}

	private WorkbenchPerspective getPerspective(PersistenceGmSession session, String perspectiveName) {
		EntityQuery query = EntityQueryBuilder.from(WorkbenchPerspective.T).where().property("name").eq(perspectiveName).done();
		WorkbenchPerspective perspective = session.queryCache().entities(query).first();
		if (perspective == null) {
			perspective = session.query().entities(query).first();
		}
		return perspective;
	}

	/**
	 * If the target folder already contains a folder with same name of folderToAdd and
	 * we shouldn't override existing folders we can skip this instruction. 
	 */
	private boolean shouldAddFolder(WorkbenchPerspective perspective, AddFolderToPerspective instruction) {
		if (perspective == null) return false;
		if (instruction.getOverrideExisting()) return true; // Add folder anyways.

		String folderToAddName = instruction.getFolderToAdd().getName();
		for (Folder subFolder : perspective.getFolders()) {
			if (subFolder.getName().equals(folderToAddName)) {
				return false;
			}
		}
		// No existing subFolder found. Add folder
		return true;
	}

}
