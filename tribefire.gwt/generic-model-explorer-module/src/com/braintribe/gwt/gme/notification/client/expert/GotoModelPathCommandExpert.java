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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.EntryPointPathElement;
import com.braintribe.model.generic.path.ListItemPathElement;
import com.braintribe.model.generic.path.MapKeyPathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.path.SetItemPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.path.GmListItemPathElement;
import com.braintribe.model.path.GmMapValuePathElement;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmModelPathElementType;
import com.braintribe.model.path.GmPropertyRelatedPathElement;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.uicommand.GotoModelPath;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.Scheduler;

public class GotoModelPathCommandExpert implements CommandExpert<GotoModelPath> {

	private ExplorerConstellation explorerConstellation;
	private Map<EntityType<?>, Consumer<ModelPath>> specialTypeExperts;

	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures additional experts for handling some types.
	 */
	@Configurable
	public void setSpecialTypeExperts(Map<EntityType<?>, Consumer<ModelPath>> specialTypeExperts) {
		this.specialTypeExperts = specialTypeExperts;
	}

	@Override
	public void handleCommand(GotoModelPath command) {		
		Future<ModelPath> future = convertModelPath(command.getPath());
		future.andThen(modelPath -> {
			if (specialTypeExperts != null) {
				Consumer<ModelPath> consumer = specialTypeExperts.get(modelPath.last().getType());
				if (consumer != null) {
					consumer.accept(modelPath);
					return;
				}
			}

			int selectedIndex = -1;
			ModelPathElement selectedElement = null;
			if (command.getSelectedElement() != null)
				selectedIndex = command.getPath().getElements().indexOf(command.getSelectedElement());

			if (selectedIndex >= 0)
				selectedElement = modelPath.get(selectedIndex);

			List<ModelPathElement> listOpenWithElements = null;
			if (command.getOpenWithActionElements() != null && !command.getOpenWithActionElements().isEmpty()) {
				listOpenWithElements = new ArrayList<>();
				for (GmModelPathElement gmElement : command.getOpenWithActionElements()) {
					int index = command.getPath().getElements().indexOf(gmElement);
					listOpenWithElements.add(modelPath.get(index));
				}
			}

			explorerConstellation.onOpenModelPath(modelPath, listOpenWithElements, selectedElement, command.getShowFullModelPath(),
					command.getAddToCurrentView());
		}).onError(e -> ErrorDialog.show("Error while preparing the ModelPath for the GotoModelPath.", e));
	}

	//TODO: we must rewrite this so we cover that all path is adopted.
	private Future<ModelPath> convertModelPath (GmModelPath gmPath) {
		if (gmPath.getElements().size() == 1) {
			GmModelPathElement gmElement = gmPath.getElements().get(0);
			if ((gmElement.elementType() == GmModelPathElementType.Root || gmElement.elementType() == GmModelPathElementType.EntryPoint)
					&& gmElement.getValue() instanceof GenericEntity) {
				return handleSingleElement(gmElement);
			}
		}
		
		ModelPath path = transformPathElements(gmPath);
		
		Future<ModelPath> future = new Future<>();
		handleDeferredReturn(path, future);
		
		return future;
	}

	private ModelPath transformPathElements(GmModelPath gmPath) {
		ModelPath path = new ModelPath();
		for (GmModelPathElement gmElement : gmPath.getElements()) {
			String signature = gmElement.getTypeSignature();
			GenericModelType type = GMF.getTypeReflection().getType(signature);
			Object value = gmElement.getValue();
			GenericEntity entity = null;
			Property property = null;
			if (gmElement instanceof GmPropertyRelatedPathElement) {
				GmPropertyRelatedPathElement gmPropertyRelatedElement = (GmPropertyRelatedPathElement) gmElement;
				entity = gmPropertyRelatedElement.getEntity();
				property = entity.entityType().getProperty(gmPropertyRelatedElement.getProperty());
			}
			ModelPathElement element = null;
			
			switch (gmElement.elementType()) {
				case Root:
					element = new RootPathElement(type, value);
					break;
				case EntryPoint:
					element = new EntryPointPathElement(type, value);
					break;
				case SetItem:
					element = new SetItemPathElement(entity,property,type,value);
					break;
				case ListItem:
					GmListItemPathElement gmListItemElement = (GmListItemPathElement) gmElement;
					element = new ListItemPathElement(entity,property,gmListItemElement.getIndex(),type,value);
					break;
				case MapValue:
					GmMapValuePathElement gmMapKeyElement = (GmMapValuePathElement) gmElement;
					GenericModelType keyType = GMF.getTypeReflection().getType(gmMapKeyElement.getTypeSignature());
					element = new MapKeyPathElement(entity, property, keyType, gmMapKeyElement.getKey(), type, value);
					break;
				case Property:
					element = new PropertyPathElement(entity,property,value); 
					break;
				case MapKey:
					// GmMapKeyPathElement seems missing key and keyType info. Thus ignore that for now.
				default:
					break;
			}
			path.add(element);
		}
		
		return path;
	}
	
	private Future<ModelPath> handleSingleElement(GmModelPathElement gmElement) {
		Future<ModelPath> future = new Future<>();
		adoptEntity((GenericEntity) gmElement.getValue()) //
				.andThen(adoptedEntity -> {
					ModelPathElement element;
					if (gmElement.elementType() == GmModelPathElementType.Root)
						element = new RootPathElement(adoptedEntity.type(), adoptedEntity);
					else
						element = new EntryPointPathElement(adoptedEntity.type(), adoptedEntity);

					ModelPath modelPath = new ModelPath();
					modelPath.add(element);
					future.onSuccess(modelPath);
				}).onError(future::onFailure);
		
		return future;
	}

	private void handleDeferredReturn(ModelPath modelPath, Future<ModelPath> future) {
		Scheduler.get().scheduleDeferred(() -> future.onSuccess(modelPath));
	}
	
	private Future<GenericEntity> adoptEntity(GenericEntity entity) {
		Future<GenericEntity> future = new Future<>();
		
		explorerConstellation.getGmSession().merge().suspendHistory(true).adoptUnexposed(true).doFor(entity,
				AsyncCallback.of(future::onSuccess, future::onFailure));
		return future;
	}

}
