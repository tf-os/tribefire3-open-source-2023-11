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

public class SetTreeModel extends CollectionTreeModel {
	
	private GenericModelType elementType;
	private Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory;
	private boolean isRoot;
	protected Integer maxSize;
	
	public SetTreeModel(ObjectAndType objectAndType, Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory) {
		collectionType = objectAndType.getType();
		elementType = collectionType.getCollectionElementType();
		this.modelFactory = modelFactory;
		this.isRoot = objectAndType.isRoot();
		setMaxSize(objectAndType.getMaxSize());
		setModelObject(objectAndType.getObject(), objectAndType.getDepth());
	}
	
	@Override
	public void setModelObject(Object modelObject, int depth) {
		super.setModelObject(modelObject, depth);
		updateNodesOnly((Set<?>) modelObject, depth);
	}
	
	/**
	 * In some cases, for sets, we must update the nodes to show just a sub set of the entries in the actual set (modelObject).
	 * For example, when handling the results of a PropertyQuery.
	 */
	public void updateNodesOnly(Set<?> set) {
		this.clear();
		if (set != null)
			insertEntries(set, 0);
	}
	
	public boolean isRoot() {
		return isRoot;
	}
	
	private void updateNodesOnly(Set<?> set, int depth) {
		this.clear();
		if (depth < ModelFactory.MAX_DEPTH || set == null) {
			notCompleted = false;
			if (set != null)
				insertEntries(set, depth);
		} else
			notCompleted = true;
	}
	
	private void insertEntries(Set<?> set, int depth) {
		int i = 0;
		int maxSize = getMaxSize();
		for (Object value : set) {
			GenericModelType type = getCollectionELementType(elementType, value);
			SetEntryTreeModel setEntryTreeModel = prepareSetEntryTreeModel(i, value, type, modelFactory, depth + 1);
			if (setEntryTreeModel == null)
				continue;
			
			add(setEntryTreeModel);
			i++;
			
			if (!isRoot && i == maxSize)
				break;
		}
	}
	
	@Override
	public void insertNewItems(List<Pair<Object, Object>> itemsToInsert, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel, boolean addToRootInCaseParentNotInTree) {
		int childCount = this.getChildCount();
		int itemsToInsertCount = itemsToInsert.size();
		boolean parentIsTrigger = parentModel == triggerModel;
		for (Pair<Object, Object> entry : itemsToInsert) {
			Object itemToInsert = entry.getSecond();
			if (!containsElement(itemToInsert)) {
				GenericModelType type = getCollectionELementType(elementType, itemToInsert);
				SetEntryTreeModel setEntryTreeModel = prepareSetEntryTreeModel(childCount++, itemToInsert, type, modelFactory, 0);
				if (setEntryTreeModel == null)
					continue;
				add(setEntryTreeModel);
				//selecting and expanding only if the parentModel is selected. The reason is
				//Imagine we have a node in 2 places in the tree. And then we are adding a child to this node. This make sure we only select/expand one of them,
				//which is the one that we are currently working with
				AssemblyUtil.insertToTreeStore(assemblyPanel, parentModel, setEntryTreeModel, -1, --itemsToInsertCount == 0 && parentIsTrigger,
						addToRootInCaseParentNotInTree);
			}
		}
	}
	
	protected boolean containsElement(Object value) {
		if (getChildren() != null)
			return getChildren().stream().anyMatch(child -> child.refersTo(value));
		
		return false;
	}
	
	/*
	 * Prepares entries and inserts them into the treeGrid.
	 *
	public static void insertNewItems(GenericModelType collectionElementType, List<Pair<Object, Object>> itemsToInsert,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, AssemblyPanel assemblyPanel, int index) {
		for (Pair<Object, Object> entry : itemsToInsert) {
			Object itemToInsert = entry.getSecond();
			GenericModelType type = getCollectionELementType(collectionElementType, itemToInsert);
			SetEntryTreeModel setEntryTreeModel = prepareSetEntryTreeModel(index++, itemToInsert, type, modelFactory, 0);
			if (setEntryTreeModel != null)
				AssemblyUtil.insertToTreeStore(assemblyPanel, null, setEntryTreeModel, -1, false, true);
		}
	}*/
	
	@Override
	public void removeItems(Set<Object> itemsKeyToRemove, AbstractGenericTreeModel parentModel, TreeGrid<AbstractGenericTreeModel> treeGrid,
			AbstractGenericTreeModel triggerModel) {
		for (Object itemToRemove : itemsKeyToRemove) {
			int index = getValueModelIndex(itemToRemove, getChildren());
			if (index != -1) {
				remove(index);
				AssemblyUtil.removeFromTreeStore(treeGrid, parentModel, index, true);
			}
		}
	}
	
	/**
	 * Removes the entries from the treeGrid root.
	 */
	public static void removeItems(Set<Object> itemsKeyToRemove, TreeGrid<AbstractGenericTreeModel> treeGrid) {
		for (Object itemToRemove : itemsKeyToRemove) {
			int index = getValueModelIndex(itemToRemove, treeGrid.getTreeStore().getRootItems());
			if (index != -1)
				AssemblyUtil.removeFromTreeStore(treeGrid, null, index, true);
		}
	}
	
	@Override
	public void replaceItems(List<Pair<Object, Object>> itemsToReplace, AbstractGenericTreeModel parentModel, AssemblyPanel assemblyPanel,
			AbstractGenericTreeModel triggerModel) {
		int childCount = this.getChildCount() - 1;
		int itemsToReplaceCount = itemsToReplace.size();
		for (Pair<Object, Object> entry : itemsToReplace) {
			Object oldValue = entry.getFirst();
			Object newValue = entry.getSecond();
			GenericModelType type = getCollectionELementType(elementType, newValue);
			SetEntryTreeModel setEntryTreeModel = prepareSetEntryTreeModel(childCount, newValue, type, modelFactory, 0);
			AbstractGenericTreeModel oldModel = getValueModel(oldValue);
			if (oldModel != null) {
				remove(oldModel);
				add(setEntryTreeModel);
				AssemblyUtil.replaceInTreeStore(assemblyPanel, parentModel, oldModel, setEntryTreeModel, -1, --itemsToReplaceCount == 0);
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
		return (X) elementType;
	}
	
	public static SetEntryTreeModel prepareSetEntryTreeModel(int index, Object object, GenericModelType elementType,
			Function<ObjectAndType, ? extends AbstractGenericTreeModel> modelFactory, int depth) {
		if (!isEntryVisible(object))
			return null;
		
		ListOrSetEntry setEntry = new ListOrSetEntry(index, object);
		ObjectAndType setEntryObjectAndType = new ObjectAndType();
		setEntryObjectAndType.setObject(setEntry);
		setEntryObjectAndType.setType(elementType);
		setEntryObjectAndType.setDepth(depth);
		SetEntryTreeModel setEntryTreeModel = new SetEntryTreeModel(setEntryObjectAndType, modelFactory);
		return setEntryTreeModel;
	}
	
	private AbstractGenericTreeModel getValueModel(Object value) {
		List<AbstractGenericTreeModel> children = getChildren();
		if (children != null)
			return children.stream().filter(m -> m.refersTo(value)).findFirst().orElse(null);
		
		return null;
	}
	
	private static int getValueModelIndex(Object value, List<AbstractGenericTreeModel> children) {
		if (children != null) {
			int i = 0;
			for (AbstractGenericTreeModel model : children) {
				if (model.refersTo(value))
					return i;
				i++;
			}
		}
		
		return -1;
	}
	
	private static boolean isEntryVisible(Object object) {
		if (!(object instanceof GenericEntity))
			return true;
		
		GenericEntity entity = (GenericEntity) object;
		return GmSessions.getMetaData(entity).entity(entity).is(Visible.T);
	}
	
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	
	public int getMaxSize() {
		return maxSize == null ? -1 : maxSize;
	}

}
