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
package com.braintribe.gwt.gme.assemblypanel.client.model;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil;
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class ListTreeModel extends CollectionTreeModel implements ListTreeModelInterface {
	
	private GenericModelType elementType;
	private Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory;
	
	public ListTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory) {
		collectionType = objectAndType.getType();
		elementType = collectionType.getCollectionElementType();
		this.modelFactory = modelFactory;
		setModelObject(objectAndType.getObject(), objectAndType.getDepth());
	}
	
	@Override
	public void setModelObject(Object modelObject, int depth) {
		super.setModelObject(modelObject, depth);
		this.clear();
		if (depth < ModelFactory.MAX_DEPTH || modelObject == null) {
			notCompleted = false;
			if (modelObject != null) {
				List<Object> list = (List<Object>) modelObject;
				insertEntries(list, depth, this, modelFactory, elementType);
			}
		} else
			notCompleted = true;
	}
	
	protected static void insertEntries(List<Object> list, int depth, CollectionTreeModel collectionModel,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, GenericModelType defaultElementType) {
		int i = 0;
		for (Object value : list) {
			GenericModelType type = getCollectionELementType(defaultElementType, value);
			ListEntryTreeModel listEntryTreeModel = prepareListEntryTreeModel(i, value, type, modelFactory, depth + 1);
			if (listEntryTreeModel != null)
				collectionModel.add(listEntryTreeModel);
			i++;
		}
	}
	
	@Override
	public void insertNewItems(List<Pair<Object, Object>> itemsToInsert, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel, boolean addToRootInCaseParentNotInTree) {
		int itemsToInsertCount = itemsToInsert.size();
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		boolean select = --itemsToInsertCount == 0 && isCollectionCurrentlySelected(parentModel, triggerModel);
		for (Pair<Object, Object> entry : itemsToInsert) {
			int index = resolveIndex(entry.getFirst(), treeGrid);
			if (index == -1)
				index = 0;
			Object value = entry.getSecond();
			GenericModelType type = getCollectionELementType(elementType, value);
			ListEntryTreeModel listEntryTreeModel = prepareListEntryTreeModel(index, value, type, modelFactory, 0);
			if (listEntryTreeModel == null) {
				itemsToInsertCount--;
				continue;
			}
			insert(listEntryTreeModel, index);
			//selecting and expanding only if the parentModel is selected. The reason is
			//Imagine we have a node in 2 places in the tree. And then we are adding a child to this node. This make sure we only select/expand one of them,
			//which is the one that we are currently working with
			AssemblyUtil.insertToTreeStore(assemblyPanel, parentModel, listEntryTreeModel, index, select, addToRootInCaseParentNotInTree);
		}
	}
	
	/*
	 * Prepares entries and inserts them into the treeGrid.
	 *
	public static void insertNewItems(GenericModelType collectionElementType, List<Pair<Object, Object>> itemsToInsert,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, AssemblyPanel assemblyPanel) {
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		
		for (Pair<Object, Object> entry : itemsToInsert) {
			int index = resolveIndex(entry.getFirst(), treeGrid);
			if (index == -1)
				index = 0;
			Object value = entry.getSecond();
			GenericModelType type = getCollectionELementType(collectionElementType, value);
			ListEntryTreeModel listEntryTreeModel = prepareListEntryTreeModel(index, value, type, modelFactory, 0);
			if (listEntryTreeModel != null)
				AssemblyUtil.insertToTreeStore(assemblyPanel, null, listEntryTreeModel, index, false, true);
		}
	}*/
	
	@Override
	public void removeItems(Set<Object> itemsKeyToRemove, AbstractGenericTreeModel parentModel, TreeGrid<AbstractGenericTreeModel> treeGrid,
			AbstractGenericTreeModel triggerModel) {
		if (!notCompleted) {
			for (Object itemToRemove : itemsKeyToRemove) {
				int index = resolveIndex(itemToRemove, treeGrid);
				if (index != -1) {
					remove(index);
					AssemblyUtil.removeFromTreeStore(treeGrid, parentModel, index, true);
				}
			}
		}
	}
	
	private static int resolveIndex(Object value, TreeGrid<AbstractGenericTreeModel> treeGrid) {
		int index;
		if (value instanceof Integer)
			index = (Integer) value;
		else
			index = resolveRootIndex(value, treeGrid);
		
		return index;
	}
	
	private static int resolveRootIndex(Object value, TreeGrid<AbstractGenericTreeModel> treeGrid) {
		int index = 0;
		for (AbstractGenericTreeModel model : treeGrid.getTreeStore().getRootItems()) {
			if (model.refersTo(value))
				return index;
			index++;
		}
		return -1;
	}
	
	/**
	 * Removes the entries from the treeGrid root.
	 */
	public static void removeItems(Set<Object> itemsKeyToRemove, TreeGrid<AbstractGenericTreeModel> treeGrid) {
		for (Object itemToRemove : itemsKeyToRemove) {
			int index = resolveIndex(itemToRemove, treeGrid);
			if (index != -1)
				AssemblyUtil.removeFromTreeStore(treeGrid, null, index, true);
		}
	}
	
	public static boolean isCollectionCurrentlySelected(AbstractGenericTreeModel parentModel, AbstractGenericTreeModel triggerModel) {
		return triggerModel != null && (parentModel == triggerModel || parentModel.getDelegate() == triggerModel.getParent());
	}
	
	@Override
	public void replaceItems(List<Pair<Object, Object>> itemsToReplace, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel) {
		int itemsToReplaceCount = itemsToReplace.size();
		for (Pair<Object, Object> entry : itemsToReplace) {
			Object itemToReplace = entry.getSecond();
			int index = getIndexOfValue(parentModel, itemToReplace); //TODO: shouldn't I use the index in the itemsToReplace instead of getIndexOfValue?
			GenericModelType type = getCollectionELementType(elementType, itemToReplace);
			ListEntryTreeModel listEntryTreeModel = prepareListEntryTreeModel(index, itemToReplace, type, modelFactory, 0);
			remove(index);
			insert(listEntryTreeModel, index);
			AssemblyUtil.replaceInTreeStore(assemblyPanel, parentModel, null, listEntryTreeModel, index, --itemsToReplaceCount == 0);
		}
	}
	
	@Override
	public void clearItems() {
		//Nothing to do?
	}
	
	@Override
	public AbstractGenericTreeModel getDelegate() {
		return this;
	}
	
	@Override
	public <X extends GenericModelType> X getElementType() {
		return (X) elementType;
	}

	public static ListEntryTreeModel prepareListEntryTreeModel(int index, Object object, GenericModelType elementType,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, int depth) {
		if (!isEntryVisible(object))
			return null;
		
		ListOrSetEntry listEntry = new ListOrSetEntry(index, object);
		ObjectAndType listEntryObjectAndType = new ObjectAndType();
		listEntryObjectAndType.setObject(listEntry);
		listEntryObjectAndType.setType(elementType);
		listEntryObjectAndType.setDepth(depth);
		ListEntryTreeModel listEntryTreeModel = new ListEntryTreeModel(listEntryObjectAndType, modelFactory);
		return listEntryTreeModel;
	}

	private int getIndexOfValue(AbstractGenericTreeModel parentModel, Object value) {
		List<?> list = (List<?>) parentModel.getModelObject();
		return list.indexOf(value);
	}
	
	private static boolean isEntryVisible(Object object) {
		if (!(object instanceof GenericEntity))
			return true;
		
		GenericEntity entity = (GenericEntity) object;
		return GmSessions.getMetaData(entity).entity(entity).is(Visible.T);
	}

}
