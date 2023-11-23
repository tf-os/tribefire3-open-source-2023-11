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
import com.braintribe.gwt.gme.assemblypanel.client.model.factory.ModelFactory;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;

import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class MapTreeModel extends CollectionTreeModel {
	
	private GenericModelType[] parameterization;
	private Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory;
	
	public MapTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory) {
		collectionType = objectAndType.getType();
		parameterization = collectionType.getParameterization();
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
				Map<Object, Object> map = (Map<Object, Object>) modelObject;
				insertEntries(map, depth);
			}
		} else
			notCompleted = true;
	}
	
	private void insertEntries(Map<Object, Object> map, int depth) {
		int counter = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			GenericModelType[] types = getActualType(parameterization[0], entry.getKey(), parameterization[1], entry.getValue());
			add(prepareMapKeyAndValueTreeModel(entry.getKey(), types[0], entry.getValue(), types[1], modelFactory, counter++, depth + 1));
		}
	}
	
	private GenericModelType[] getActualType(GenericModelType keyType, Object key, GenericModelType valueType, Object value) {
		return new GenericModelType[]{getActualType(keyType, key), getActualType(valueType, value)};
	}
	
	private static GenericModelType getActualType(GenericModelType defaultType, Object object) {
		if (object == null)
			return defaultType;
		return GMF.getTypeReflection().getType(object);
	}
	
	@Override
	public void insertNewItems(List<Pair<Object, Object>> itemsToInsert, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel, boolean addToRootInCaseParentNotInTree) {
		int counter = this.getChildCount();
		int itemsToInsertCount = itemsToInsert.size();
		for (Pair<Object, Object> entry : itemsToInsert) {
			Object key = entry.getFirst();
			Object value = entry.getSecond();
			GenericModelType keyType = getActualType(parameterization[0], key);
			GenericModelType valueType = getActualType(parameterization[1], value);
			GenericModelType[] mapTypes = {keyType, valueType};
			AbstractGenericTreeModel mapEntry;
			/*if (isKeyDetailElementType() || isValueDetailElementType()) {
				mapEntry = prepareMapEntryTreeModel(key, value, mapTypes, modelFactory, isKeyDetailElementType, 0);
			} else {*/
			mapEntry = prepareMapKeyAndValueTreeModel(key, mapTypes[0], value, mapTypes[1], modelFactory, counter++, 0);
			//}
			add(mapEntry);
			if (triggerModel == null) {
				triggerModel = parentModel;
			}
			//selecting and expanding only if the parentModel is selected. The reason is
			//Imagine we have a node in 2 places in the tree. And then we are adding a child to this node. This make sure we only select/expand one of them,
			//which is the one that we are currently working with
			AssemblyUtil.insertToTreeStore(assemblyPanel, parentModel, mapEntry, -1, --itemsToInsertCount == 0 && parentModel == triggerModel, addToRootInCaseParentNotInTree);
		}
	}
	
	/*
	 * Prepares entries and inserts them into the treeGrid.
	 *
	public static void insertNewItems(GenericModelType[] parameterization, Map<Object, Object> itemsToInsert,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, AssemblyPanel assemblyPanel, int index) {
		for (Map.Entry<Object, Object> entry : itemsToInsert.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			GenericModelType keyType = getActualType(parameterization[0], key);
			GenericModelType valueType = getActualType(parameterization[1], value);
			GenericModelType[] mapTypes = {keyType, valueType};
			AbstractGenericTreeModel mapEntry = prepareMapKeyAndValueTreeModel(key, mapTypes[0], value, mapTypes[1], modelFactory, index++, 0);
			AssemblyUtil.insertToTreeStore(assemblyPanel, null, mapEntry, -1, false, true);
		}
	}*/
	
	@Override
	public void removeItems(Set<Object> itemsKeyToRemove, AbstractGenericTreeModel parentModel, TreeGrid<AbstractGenericTreeModel> treeGrid,
			AbstractGenericTreeModel triggerModel) {
		for (Object itemToRemove : itemsKeyToRemove) {
			Object key = itemToRemove;
			int index = getModelIndexWithKey(key, getChildren());
			if (index != -1) {
				remove(index);
				/*if (!isKeyDetailElementType() && !isValueDetailElementType()) {
					GMEditorUtil.removeFromTreeStore(treeGrid, parentModel, index, true);
				} else {*/
				AssemblyUtil.removeFromTreeStore(treeGrid, parentModel, index, false);
				//}
			}
		}
	}
	
	/**
	 * Removes the entries from the treeGrid root.
	 */
	public static void removeItems(Set<Object> itemsKeyToRemove, TreeGrid<AbstractGenericTreeModel> treeGrid) {
		for (Object itemToRemove : itemsKeyToRemove) {
			int index = getModelIndexWithKey(itemToRemove, treeGrid.getTreeStore().getRootItems());
			AssemblyUtil.removeFromTreeStore(treeGrid, null, index, false);
		}
	}
	
	@Override
	public void replaceItems(List<Pair<Object, Object>> itemsToReplace, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel) {
		int itemsToReplaceCount = itemsToReplace.size();
		for (Pair<Object, Object> entry : itemsToReplace) {
			Object key = entry.getFirst();
			Object value = entry.getSecond();
			GenericModelType keyType = getActualType(parameterization[0], key);
			GenericModelType valueType = getActualType(parameterization[1], value);
			GenericModelType[] mapTypes = {keyType, valueType};
			int index = getModelIndexWithKey(key, getChildren());
			if (index != -1) {
				remove(index);
				AbstractGenericTreeModel mapEntry;
				/*if (isKeyDetailElementType() || isValueDetailElementType()) {
					mapEntry = prepareMapEntryTreeModel(key, value, mapTypes, modelFactory, isKeyDetailElementType, 0);
				} else {*/
				mapEntry = prepareMapKeyAndValueTreeModel(key, mapTypes[0], value, mapTypes[1], modelFactory, index, 0);
				//}
				insert(mapEntry, index);
				AssemblyUtil.replaceInTreeStore(assemblyPanel, parentModel, null, mapEntry, index, --itemsToReplaceCount == 0);
			}
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
		return (X) parameterization[1];
	}
	
	public <X extends GenericModelType> X getKeyElementType() {
		return (X) parameterization[0];
	}
	
	/*public static MapEntryTreeModel prepareMapEntryTreeModel(Map.Entry<?, ?> entry, GenericModelType[] elementTypes,
			IndexedProvider<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, boolean isKeyDetailElementType,
			int depth) {
		return prepareMapEntryTreeModel(entry.getKey(), entry.getValue(), elementTypes, modelFactory, isKeyDetailElementType, depth);
	}
	
	private static MapEntryTreeModel prepareMapEntryTreeModel(Object entryKey, Object entryValue, GenericModelType[] elementTypes,
			IndexedProvider<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, boolean isKeyDetailElementType,
			int depth) {
		ObjectAndType mapEntryObjectAndType = new ObjectAndType();
		Object key;
		Object object;
		String keyString;
		GenericModelType keyElementType;
		if (isKeyDetailElementType) {
			mapEntryObjectAndType.setType(elementTypes[1]);
			key = entryKey;
			object = entryValue;
			keyString = elementTypes[0].getSelectiveInformation(entryKey);
			keyElementType = elementTypes[0];
		} else {
			mapEntryObjectAndType.setType(elementTypes[0]);
			key = entryValue;
			object = entryKey;
			keyString = elementTypes[1].getSelectiveInformation(entryValue);
			keyElementType = elementTypes[1];
		}
		MapEntry mapEntry = new MapEntry(key, object, keyString, keyElementType, isKeyDetailElementType);
		mapEntryObjectAndType.setDepth(depth);
		
		mapEntryObjectAndType.setObject(mapEntry);
		MapEntryTreeModel mapEntryTreeModel = new MapEntryTreeModel(mapEntryObjectAndType, modelFactory);
		return mapEntryTreeModel;
	}*/
	
	public static MapEntryTreeModel prepareMapEntryTreeModel(MapEntry mapEntry, GenericModelType[] elementTypes,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, boolean isKeyDetailElementType, int depth) {
		ObjectAndType mapEntryObjectAndType = new ObjectAndType();
		if (isKeyDetailElementType) {
			mapEntryObjectAndType.setType(elementTypes[1]);
		} else {
			mapEntryObjectAndType.setType(elementTypes[0]);
		}
		mapEntryObjectAndType.setDepth(depth);
		
		mapEntryObjectAndType.setObject(mapEntry);
		MapEntryTreeModel mapEntryTreeModel = new MapEntryTreeModel(mapEntryObjectAndType, modelFactory);
		return mapEntryTreeModel;
	}
	
	public static MapKeyAndValueTreeModel prepareMapKeyAndValueTreeModel(Object key, GenericModelType keyType, Object value,
			GenericModelType valueType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, int entryNumber, int depth) {
		ObjectAndType keyObjectAndType = new ObjectAndType();
		keyObjectAndType.setObject(key);
		keyObjectAndType.setType(keyType);
		keyObjectAndType.setDepth(depth);
		MapKeyOrValueEntryTreeModel mapKeyEntryTreeModel = new MapKeyOrValueEntryTreeModel(keyObjectAndType, modelFactory, true);
		
		ObjectAndType valueObjectAndType = new ObjectAndType();
		valueObjectAndType.setDepth(depth);
		valueObjectAndType.setObject(value);
		valueObjectAndType.setType(valueType);
		MapKeyOrValueEntryTreeModel mapValueEntryTreeModel = new MapKeyOrValueEntryTreeModel(valueObjectAndType, modelFactory, false);
		
		return new MapKeyAndValueTreeModel(mapKeyEntryTreeModel, mapValueEntryTreeModel, entryNumber);
	}
	
	private static int getModelIndexWithKey(Object key, List<AbstractGenericTreeModel> children) {
		if (children == null)
			return -1;
		
		int i = 0;
		for (AbstractGenericTreeModel model : children) {
			if (model instanceof MapEntryTreeModel) {
				MapEntryTreeModel mapModel = (MapEntryTreeModel) model;
				MapEntry mapEntry = mapModel.getMapEntry();
				if (mapEntry.getRepresentsKey()) {
					if (mapEntry.getKey() == key) {
						return i;
					}
				} else {
					if (mapEntry.getObject() == key) {
						return i;
					}
				}
			} else if (model instanceof MapKeyAndValueTreeModel) {
				MapKeyAndValueTreeModel mapModel = (MapKeyAndValueTreeModel) model;
				if (mapModel.getMapKeyEntryTreeModel().getModelObject() == key) {
					return i;
				}
			}
			i++;
		}
		
		return --i;
	}

}
