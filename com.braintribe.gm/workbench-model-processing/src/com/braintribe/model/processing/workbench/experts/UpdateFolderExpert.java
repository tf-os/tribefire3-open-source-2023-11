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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.processing.generic.synchronize.BasicEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.experts.ResourceIdentityManager;
import com.braintribe.model.processing.workbench.WorkbenchInstructionContext;
import com.braintribe.model.processing.workbench.WorkbenchInstructionExpert;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessorException;
import com.braintribe.model.workbench.instruction.UpdateFolder;

/**
 * Updates a specified property of the folder identified by specified path with
 * a configured newValue. 
 */
public class UpdateFolderExpert implements WorkbenchInstructionExpert<UpdateFolder> {

	private static final Logger logger = Logger.getLogger(UpdateFolderExpert.class);
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private static final EntityType<GenericEntity> folderType = typeReflection.getEntityType(Folder.class);

	@Override
	public void process(UpdateFolder instruction, WorkbenchInstructionContext context) throws WorkbenchInstructionProcessorException {
	
		// Search for the target folder based on given path.
		Folder folder = context.getFolderByPath(instruction.getPath());
		
		// Search for the specified property.
		Property property = folderType.findProperty(instruction.getProperty());
		if (property != null) {
			
			// We found a property. Now check whether we should really set the new value.
			// If property is null or overrideExising is specified we shoult set the new value.
			if (instruction.getOverrideExisting() || property.get(folder) == null) {
				
				// Synchronize (if necessary) the new value into session.
				Object synchronizedNewValue = synchronizeIfNecessary(instruction.getNewValue(), context);
				
				// Set the new value on the property.
				property.set(folder, synchronizedNewValue);
			}
			
		} else {
			logger.warn("Property: "+instruction.getProperty()+" not found on folder.");
		}
	}

	/**
	 *  The given newValue will be synchronized into session in case it's an entity or a collection
	 *  of entities (list or set). Otherwise the passed newValue is returned.
	 *  
	 *  TODO: Support of Map keys and values. If one of the are entities they should be synchronized as well.  
	 */
	@SuppressWarnings({ "incomplete-switch", "unchecked" })
	private Object synchronizeIfNecessary(Object newValue, WorkbenchInstructionContext context) {
		GenericModelType type = typeReflection.getType(newValue);

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false)
					.session(context.getSession())
					.addIdentityManager(new ResourceIdentityManager())
					.addDefaultIdentityManagers();
		
		switch (type.getTypeCode()) {
		case entityType:
			return synchronization
					.addEntity((GenericEntity) newValue)
					.synchronize()
					.unique();
		case setType:
			CollectionType setType = (CollectionType) type;
			if (setType.getCollectionElementType().getTypeCode() == TypeCode.entityType) {
				return new HashSet<GenericEntity>(
						synchronization
						.addEntities((Set<GenericEntity>) newValue)
						.synchronize()
						.list());
			}
			break;
		case listType:
			CollectionType listType = (CollectionType) type;
			if (listType.getCollectionElementType().getTypeCode() == TypeCode.entityType) {
				return synchronization
						.addEntities((List<GenericEntity>) newValue)
						.synchronize()
						.list();
			}
			break;
		}
				
		return newValue;
		

	}

}
