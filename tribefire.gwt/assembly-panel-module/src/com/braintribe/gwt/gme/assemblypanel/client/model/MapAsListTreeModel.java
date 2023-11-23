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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyPanel;
import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;

import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class MapAsListTreeModel extends CollectionTreeModel implements ListTreeModelInterface {
	
	private CollectionType mapCollectionType;
	private GenericModelType elementType;
	private Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory;
	private Map<Integer, Object> map;
	private List<Object> list;
	
	public MapAsListTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory) {
		mapCollectionType = objectAndType.getType();
		collectionType = prepareListCollectionType();
		elementType = collectionType.getCollectionElementType();
		this.modelFactory = modelFactory;
		setModelObject(objectAndType.getObject(), objectAndType.getDepth());
	}
	
	@Override
	public void setModelObject(Object modelObject, int depth) {
		if (modelObject instanceof Map)
			list = prepareListFromModel((Map<Integer, Object>) modelObject);
		else
			list = (List<Object>) modelObject;
		super.setModelObject(list, depth);
		
		this.clear();
		if (modelObject != null)
			insertEntries(list, depth);
	}
	
	@Override
	public void insertNewItems(List<Pair<Object, Object>> itemsToInsert, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel, boolean addToRootInCaseParentNotInTree) {
		int itemsToInsertCount = itemsToInsert.size();
		GenericModelType type = elementType;
		boolean selectCheck = triggerModel != null && (parentModel == triggerModel || parentModel.getDelegate() == triggerModel.getParent());
		for (Pair<Object, Object> entry : itemsToInsert) {
			int index = (Integer) entry.getFirst();
			Object value = entry.getSecond();
			list.add(index, value);
			if (type.isEntity() && value != null)
				type = ((GenericEntity) value).entityType();
			ListEntryTreeModel listEntryTreeModel = ListTreeModel.prepareListEntryTreeModel(index, value, type, modelFactory, 0);
			insert(listEntryTreeModel, index);
			//selecting and expanding only if the parentModel is selected. The reason is
			//Imagine we have a node in 2 places in the tree. And then we are adding a child to this node. This make sure we only select/expand one of them,
			//which is the one that we are currently working with
			boolean select = --itemsToInsertCount == 0 && selectCheck;
			AssemblyUtil.insertToTreeStore(assemblyPanel, parentModel, listEntryTreeModel, index, select, addToRootInCaseParentNotInTree);
		}
	}
	
	@Override
	public void removeItems(Set<Object> itemsKeyToRemove, AbstractGenericTreeModel parentModel, TreeGrid<AbstractGenericTreeModel> treeGrid,
			AbstractGenericTreeModel triggerModel) {
		for (Object itemToRemove : itemsKeyToRemove) {
			int index = (Integer) itemToRemove;
			list.remove(index);
			remove(index);
			AssemblyUtil.removeFromTreeStore(treeGrid, parentModel, index, true);
		}
	}
	
	@Override
	public void replaceItems(List<Pair<Object, Object>> itemsToReplace, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel) {
		int itemsToReplaceCount = itemsToReplace.size();
		for (Pair<Object, Object> entry : itemsToReplace) {
			Object itemToReplace = entry.getSecond();
			int index = getIndexOfValue(parentModel, itemToReplace); //TODO: shouldn't I use the index in the itemsToReplace instead of getIndexOfValue?
			GenericModelType type = elementType;
			list.set(index, itemToReplace);
			if (type.isEntity() && itemToReplace != null)
				type = ((GenericEntity) itemToReplace).entityType();
			ListEntryTreeModel listEntryTreeModel = ListTreeModel.prepareListEntryTreeModel(index, itemToReplace, type, modelFactory, 0);
			remove(index);
			insert(listEntryTreeModel, index);
			AssemblyUtil.replaceInTreeStore(assemblyPanel, parentModel, null, listEntryTreeModel, index, --itemsToReplaceCount == 0);
		}
	}

	@Override
	public void clearItems() {
		if (list != null)
			list.clear();
	}
	
	@Override
	public boolean refersTo(Object object) {
		modelObject = map;
		if (super.refersTo(object)) {
			modelObject = list;
			return true;
		}
		modelObject = list;
		return super.refersTo(object);
	}

	@Override
	public AbstractGenericTreeModel getDelegate() {
		return this;
	}

	@Override
	public <X extends GenericModelType> X getElementType() {
		return (X) elementType;
	}
	
	public CollectionType getMapCollectionType() {
		return mapCollectionType;
	}

	private CollectionType prepareListCollectionType() {
		return GMF.getTypeReflection().getListType(mapCollectionType.getParameterization()[1]);
	}
	
	private List<Object> prepareListFromModel(Map<Integer, Object> map) {
		this.map = map;
		List<Object> list = null;
		if (map != null) {
			list = (List<Object>) collectionType.createPlain();
			for (int i = 0; i < map.size(); i++)
				list.add(null);
			for (Map.Entry<Integer, ?> entry : map.entrySet())
				list.set(entry.getKey(), entry.getValue());
		}
		
		return list;
	}
	
	private void insertEntries(List<Object> list, int depth) {
		ListTreeModel.insertEntries(list, depth, this, modelFactory, elementType);
	}
	
	private int getIndexOfValue(AbstractGenericTreeModel parentModel, Object value) {
		List<?> list = (List<?>) parentModel.getModelObject();
		return list.indexOf(value);
	}

}
