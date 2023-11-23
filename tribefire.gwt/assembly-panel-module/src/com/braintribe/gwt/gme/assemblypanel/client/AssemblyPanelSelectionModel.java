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
package com.braintribe.gwt.gme.assemblypanel.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.CondensedEntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedSelectionModel;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.WindowManager;
import com.sencha.gxt.widget.core.client.event.RowMouseDownEvent;

public class AssemblyPanelSelectionModel extends FixedSelectionModel<AbstractGenericTreeModel> {
	
	private AssemblyPanel assemblyPanel;
	protected AbstractGenericTreeModel selectedModel;
	protected List<AbstractGenericTreeModel> checkedModels = new ArrayList<>();
	private int indexSelectionPending = -1;
	private HandlerRegistration addAttachHandler;
	
	public AssemblyPanelSelectionModel(AssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
		
		addSelectionChangedHandler(event -> {
			if (!assemblyPanel.ignoreSelection)
				Scheduler.get().scheduleDeferred(() -> handleSelectionChanged(event.getSelection(), false));
		});
		
		if (assemblyPanel.useCheckBoxColumn)
			setSelectionMode(SelectionMode.SINGLE);
		
		addAttachHandler = assemblyPanel.addAttachHandler(event -> {
			Scheduler.get().scheduleDeferred(() -> {
				if (addAttachHandler != null)
					addAttachHandler.removeHandler();
				if (event.isAttached()) {
					if (indexSelectionPending == -1)
						return;
					
					select(indexSelectionPending, false);
					indexSelectionPending = -1;
				}
			});
		});
	}
	
	@Override
	protected void onKeyPress(NativeEvent ne) {
		if (ne.getKeyCode() == KeyCodes.KEY_SPACE) //Ignores space handling
			return;
		
		super.onKeyPress(ne);
	}
	
	protected void performSelect(int index, boolean keepExisting) {
		if (!assemblyPanel.isRendered()) {
			indexSelectionPending = index;
			return;
		}
		
		ListStore<AbstractGenericTreeModel> store = assemblyPanel.editorTreeGrid.getStore();
		List<AbstractGenericTreeModel> all = store.getAll();
		if (all.size() == 1 && (assemblyPanel.columnData == null
				|| (!assemblyPanel.columnData.getPreventSingleEntryExpand() && assemblyPanel.columnData.getDisplayNode()))) {
			assemblyPanel.editorTreeGrid.setExpanded(store.get(0), true);
			index = 0;
		}

		if (all.isEmpty())
			fireGmSelectionListeners();
		else if (index < all.size())
			select(index, keepExisting);
		
		//Do not focus the grid in case we have some active window
		if (WindowManager.get().getActive() == null)
			assemblyPanel.editorTreeGrid.focus();
	}
	
	/**
	 * Clears the checked items.
	 * @param silent - true for not firing the CheckBoxSelectionChanged event.
	 */
	protected void clearCheckedItems(boolean silent) {
		int checkedItemsSize = checkedModels.size();
		for (int i = 0; i < checkedItemsSize; i++) {
			AbstractGenericTreeModel model = checkedModels.remove(0);
			AssemblyUtil.refreshRow(assemblyPanel.editorTreeGrid, model);
		}
		
		if (!silent)
			fireCheckBoxSelectionChanged();
	}
	
	protected ModelPath getFirstCheckedItem() {
		if (!checkedModels.isEmpty()) {
			if (!isAmbiguous(checkedModels.get(0)))
				return AssemblyUtil.getModelPath(checkedModels.get(0), assemblyPanel.rootModelPath);
			
			//For now, getting ONLY the entity of the ambiguous entry
			List<ModelPath> modelPaths = AssemblyUtil.getAmbiguousModelPath(checkedModels.get(0), assemblyPanel.rootModelPath);
			for (ModelPath modelPath : modelPaths) {
				if (modelPath.last().getValue() instanceof GenericEntity)
					return modelPath;
			}
		}

		return null;
	}
	
	protected List<ModelPath> getCurrentCheckedItems() {
		List<ModelPath> checkedItems = new ArrayList<>();
		for (AbstractGenericTreeModel checkedModel : checkedModels) {
			if (isAmbiguous(checkedModel)) {
				//For now, getting ONLY the entity of the ambiguous entry
				List<ModelPath> modelPaths = AssemblyUtil.getAmbiguousModelPath(checkedModel, assemblyPanel.rootModelPath);
				modelPaths.stream().filter(p -> p.last().getValue() instanceof GenericEntity).findFirst().ifPresent(p -> checkedItems.add(p));
			} else
				checkedItems.add(AssemblyUtil.getModelPath(checkedModel, assemblyPanel.rootModelPath));
		}

		return checkedItems;
	}
	
	protected boolean isChecked(Object element) {
		return checkedModels.stream().anyMatch(m -> m.refersTo(element));
	}
	
	protected boolean uncheckAll() {
		if (!checkedModels.isEmpty()) {
			clearCheckedItems(false);
			return true;
		}

		return false;
	}
	
	@Override
	protected void onRowMouseDown(RowMouseDownEvent event) {
		super.onRowMouseDown(event);
		
		String cls = ((Element) event.getEvent().getEventTarget().cast()).getClassName();
		if (!AssemblyPanelResources.INSTANCE.css().moreItemsInSetStyle().equals(cls))
			return;
		
		Scheduler.get().scheduleDeferred(() -> {
			if (assemblyPanel.navigationEnabled) {
				ModelAction action = assemblyPanel.actionManager.getWorkWithEntityAction(assemblyPanel);
				if (action != null) {
					action.updateState(Collections.singletonList(Collections.singletonList(assemblyPanel.getFirstSelectedItem())));
					action.perform(null);
				}
			}
		});
	}
	
	protected void handleSelectionChanged(List<AbstractGenericTreeModel> selections, boolean forceSelection) {
		AbstractGenericTreeModel lastSelectedModel = null;
		
		if (!selections.isEmpty())
			lastSelectedModel = selections.get(selections.size() - 1);
		
		if (lastSelectedModel != selectedModel || forceSelection)
			changeSelectedModel(lastSelectedModel);
		
		//selection with checkbox
		if (assemblyPanel.useCheckBoxColumn) {
			//if (lastSelectedModel != null && isModelCheckable(lastSelectedModel))
			//	handleRadioBoxChange(lastSelectedModel);
			//else
			//	handleRadioBoxChange(null);
			
			//keep checked list same as selected list and clear checkbox on unselected items
			int i = 0;
			AbstractGenericTreeModel checked;
			while (i < checkedModels.size()) {
				checked = checkedModels.get(i);
			    if (selections.indexOf(checked) == -1) {
			    	checkedModels.remove(i);
			    	AssemblyUtil.refreshRow(assemblyPanel.editorTreeGrid, checked);
			    } else
				    i++;
			}

			selections.stream().filter(m -> m != null && checkedModels.indexOf(m) == -1).forEach(selected -> {
				changeSelectedModel(selected);
			    checkedModels.add(selected);
				AssemblyUtil.refreshNode(assemblyPanel.editorTreeGrid, selected);
				AssemblyUtil.refreshRow(assemblyPanel.editorTreeGrid, selected);
			});
			
			fireCheckBoxSelectionChanged();
		}
		
		fireGmSelectionListeners();
	}
	
	private void changeSelectedModel(final AbstractGenericTreeModel selectedItem) {
		this.selectedModel = selectedItem;
		assemblyPanel.loadAbsentOrIncompleteModel(selectedModel);
	}
	
	protected void fireCheckBoxSelectionChanged() {
		if (assemblyPanel.gmCheckListeners != null)
			assemblyPanel.gmCheckListeners.forEach(listener -> listener.onCheckChanged(assemblyPanel));
	}
	
	protected void fireGmSelectionListeners() {
		if (assemblyPanel.gmSelectionListeners != null)
			assemblyPanel.gmSelectionListeners.forEach(listener -> listener.onSelectionChanged(assemblyPanel));
	}
	
	private boolean isAmbiguous(AbstractGenericTreeModel model) {
		return model.getDelegate() instanceof CondensedEntityTreeModel
				&& ((CondensedEntityTreeModel) model.getDelegate()).getEntityTreeModel().getModelObject() != null;
	}

}
