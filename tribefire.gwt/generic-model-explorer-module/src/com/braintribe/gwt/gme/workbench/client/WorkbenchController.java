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
package com.braintribe.gwt.gme.workbench.client;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.processing.async.api.AsyncCallback;

public class WorkbenchController {
	
	private Workbench workbench;
	//private Comparator<Folder> foldersComparator;
	//private PersistenceGmSession gmSession;
	
	@Required
	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
		/*workbench.getAssemblyPanel().getTreeGrid().getTreeStore().setStoreSorter(new StoreSorter<AbstractGenericTreeModel>() {
			@Override
			public int compare(Store<AbstractGenericTreeModel> store, AbstractGenericTreeModel m1, AbstractGenericTreeModel m2, String property) {
				return getFoldersComparator().compare((Folder) m1.getModelObject(), (Folder) m2.getModelObject());
			}
		});*/
		
		workbench.addWorkbenchListener(new WorkbenchListenerAdapter() {
			@Override
			public void onModelEnvironmentChanged(ModelEnvironment modelEnvironment) {
				updateModelEnvironment(modelEnvironment);
			}
		});
	}
	
	/*@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}*/
	
	private void updateModelEnvironment(ModelEnvironment sessionModelEnvironment) {
		if (sessionModelEnvironment == null)
			return;
		
		ModelEnvironment modelEnvironment = ModelEnvironment.T.create();
		modelEnvironment.setDataModel(sessionModelEnvironment.getWorkbenchModel());
		modelEnvironment.setDataAccessId(sessionModelEnvironment.getWorkbenchModelAccessId());
		modelEnvironment.setMetaModelAccessId(sessionModelEnvironment.getMetaModelAccessId());
		
		workbench.getWorkbenchSession().configureModelEnvironment(modelEnvironment,
				AsyncCallback.ofConsumer((Void) -> workbench.prepareFolders(true)));
	}
	
	/*private Comparator<Folder> getFoldersComparator() {
		if (foldersComparator == null) {
			foldersComparator = new Comparator<Folder>() {
				@Override
				public int compare(Folder folder0, Folder folder1) {
					EntityType<?> entityType0 = null;
					EntityType<?> entityType1 = null;
					if (folder0.getContent() instanceof SimpleQueryAction) {
						entityType0 = GMF.getTypeReflection().getEntityType(((SimpleQueryAction) folder0.getContent()).getTypeSignature());
					}
					if (folder1.getContent() instanceof SimpleQueryAction) {
						entityType1 = GMF.getTypeReflection().getEntityType(((SimpleQueryAction) folder1.getContent()).getTypeSignature());
					}
					
					CascadingMetaDataResolver metaDataResolver = gmSession.getModelAccessory().getCascadingMetaDataResolver();
					
					int priorityComparison = 0;
					if (entityType0 != null && entityType1 != null) {
						EntityPriority entityPriority = metaDataResolver.getMetaData().entityType(entityType0).useCase(workbench.getAssemblyPanel().getUseCase()).meta(EntityPriority.T).exclusive();
						Double priority0 = entityPriority == null ? null : entityPriority.getPriority();
						if (priority0 == null)
							priority0 = metaDataResolver.getPriorityReverse() ? Double.MAX_VALUE : Double.NEGATIVE_INFINITY;
						
						entityPriority = metaDataResolver.getMetaData().entityType(entityType1).useCase(workbench.getAssemblyPanel().getUseCase()).meta(EntityPriority.T).exclusive();
						Double priority1 = entityPriority == null ? null : entityPriority.getPriority();
						if (priority1 == null)
							priority1 = metaDataResolver.getPriorityReverse() ? Double.MAX_VALUE : Double.NEGATIVE_INFINITY;
						
						if (metaDataResolver.getPriorityReverse()) {
							priorityComparison = priority0.compareTo(priority1);
						} else {
							priorityComparison = priority1.compareTo(priority0);
						}
					}
					if (priorityComparison == 0) {
						String o0Name = getName(folder0);
						String o1Name = getName(folder1);
						return o0Name.compareToIgnoreCase(o1Name);
					}
					return priorityComparison;
				}
			};
		}
		
		return foldersComparator;
	}
	
	private String getName(Folder folder) {
		String name;
		if (folder.getDisplayName() != null)
			name = I18nTools.getLocalized(folder.getDisplayName());
		else
			name = folder.getName();
		if (name == null)
			name = "";
		return name;
	}*/
	
}
