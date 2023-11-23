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
package com.braintribe.model.processing.workbench;

import java.util.List;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.instruction.WorkbenchInstruction;

public interface WorkbenchInstructionContext {
	
	/**
	 * Returns the {@link PersistenceGmSession}
	 */
	PersistenceGmSession getSession();
	
	/**
	 * Searches for a folder under given path. 
	 * Returns null if no folder could be found.
	 */
	Folder findFolderByPath(String path) throws WorkbenchInstructionProcessorException;

	/**
	 * Searches for a folder under given path.
	 * Throws {@link FolderNotFoundException} in case no folder could be found. 
	 */
	Folder getFolderByPath(String path) throws WorkbenchInstructionProcessorException, FolderNotFoundException;

	/**
	 * Returns the subFolder of the given parentFolder with specified folderName.
	 * This method returns null if no according subFolder could be found. 
	 */
	Folder findSubFolder(String folderName, Folder parentFolder) throws WorkbenchInstructionProcessorException;
	
	/**
	 * Returns a root folder identified by name. A root folder is defined by having no parent set.
	 * This method returns null if no according folder could be found.
	 */
	Folder findRootFolder(String folderName) throws WorkbenchInstructionProcessorException;
	
	/**
	 * Returns a list of elements of given path.
	 */
	List<String> getPathElements(String path);
	
	/**
	 * Returns the registered expert for the given instruction.
	 */
	WorkbenchInstructionExpert<WorkbenchInstruction> getExpertForInstruction(WorkbenchInstruction instruction) throws WorkbenchInstructionProcessorException;
	
	
}
