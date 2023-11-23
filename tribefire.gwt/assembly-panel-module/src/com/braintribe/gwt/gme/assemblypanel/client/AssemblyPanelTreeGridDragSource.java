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

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListTreeModelInterface;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyAndValueTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ValueTreeModel;
import com.braintribe.gwt.gmview.client.GmeEntityDragAndDropSource;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.TreeGridDragSource;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 * {@link AssemblyPanel}'s implementation for DnD.
 * @author michel.docouto
 *
 */
public class AssemblyPanelTreeGridDragSource extends TreeGridDragSource<AbstractGenericTreeModel> implements GmeEntityDragAndDropSource {
	
	private AssemblyPanel assemblyPanel;
	private boolean listReorderAvailable;
	private List<TemplateBasedAction> actions;

	public AssemblyPanelTreeGridDragSource(AssemblyPanel assemblyPanel) {
		super(assemblyPanel.getTreeGrid());
		this.assemblyPanel = assemblyPanel;
	}
	
	/**
	 * Returns whether a list reorder is available.
	 */
	protected boolean isListReorderAvailable() {
		return listReorderAvailable;
	}
	
	/**
	 * Returns the list of available actions
	 */
	@Override
	public List<TemplateBasedAction> getTemplateActions() {
		return actions;
	}
	
	@Override
	public Widget getView() {
		return assemblyPanel;
	}
	
	@Override
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext() {
		return assemblyPanel.getDragAndDropWorkbenchActionContext();
	}
	
	@Override
	protected void onDragStart(DndDragStartEvent event) {
		listReorderAvailable = false;
		Element startTarget = event.getDragStartEvent().getStartElement().<Element> cast();
		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		com.sencha.gxt.widget.core.client.tree.Tree.TreeNode<AbstractGenericTreeModel> start = treeGrid.findNode(startTarget);
		if (start == null || !treeGrid.getTreeView().isSelectableTarget(startTarget)) {
			event.setCancelled(true);
			return;
		}
		
		List<AbstractGenericTreeModel> draggedModels = treeGrid.getSelectionModel().getSelectedItems();
		
		if (draggedModels.size() == 0 || (GXT.isTouch() && !draggedModels.contains(start.getModel()))) {
			event.setCancelled(true);
			return;
		}
		
		super.onDragStart(event);
		listReorderAvailable = !event.isCancelled();
		
		//Now making sure that the dragged element is also selected
		if (draggedModels.size() == 1 && !assemblyPanel.ignoreSelection)
			assemblyPanel.editorTreeGrid.selectionModel.handleSelectionChanged(draggedModels, false);
		
		if (assemblyPanel.gmeDragAndDropSupport != null)
			actions = assemblyPanel.gmeDragAndDropSupport.getTemplateActionsSupplier().get();
		if (actions != null && !actions.isEmpty()) {
			event.setData(draggedModels.stream().map(m -> treeGrid.getTreeStore().getSubTree(m)).collect(Collectors.toList()));
			event.setCancelled(false);
			
			if (getStatusText() == null)
				event.getStatusProxy().update(SafeHtmlUtils.fromString(LocalizedText.INSTANCE.itemSelected(draggedModels.size())));
			else
				event.getStatusProxy().update(SafeHtmlUtils.fromString(Format.substitute(getStatusText(), draggedModels.size())));
			
			if (listReorderAvailable)
				listReorderAvailable = checkListReorderAvailability(draggedModels);
			
			return;
		}
		
		if (event.isCancelled())
			return;

		//If reached here, only dnd for reordering may be available
		listReorderAvailable = false;
		AbstractGenericTreeModel delegateModel = draggedModels.get(0).getDelegate();
		List<AbstractGenericTreeModel> rootItems = assemblyPanel.editorTreeGrid.getTreeStore().getRootItems();
		if (draggedModels.size() > 1) {
			for (AbstractGenericTreeModel model : draggedModels) {
				if (model.getDelegate().getClass() != delegateModel.getClass()) {
					event.setCancelled(true);
					return;
				}
				
				if (rootItems.contains(model) && assemblyPanel.rootModelPath == null) {
					event.setCancelled(true);
					return;
				}
			}
		} else if (rootItems.contains(draggedModels.get(0)) && assemblyPanel.rootModelPath == null) {
			event.setCancelled(true);
			return;
		}
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (!(entityTreeModel != null || delegateModel instanceof ValueTreeModel
				|| delegateModel instanceof ListTreeModelInterface || delegateModel instanceof SetTreeModel || delegateModel instanceof MapTreeModel
				|| delegateModel instanceof MapKeyAndValueTreeModel)) {
			event.setCancelled(true);
			return;
		}
		
		listReorderAvailable = true;
	}
	
	private boolean checkListReorderAvailability(List<AbstractGenericTreeModel> draggedModels) {
		List<AbstractGenericTreeModel> rootItems = assemblyPanel.editorTreeGrid.getTreeStore().getRootItems();
		
		AbstractGenericTreeModel delegateModel = draggedModels.get(0).getDelegate();
		if (draggedModels.size() > 1) {
			for (AbstractGenericTreeModel model : draggedModels) {
				if (model.getDelegate().getClass() != delegateModel.getClass())
					return false;
				
				if (rootItems.contains(model) && assemblyPanel.rootModelPath == null)
					return false;
			}
		} else if (rootItems.contains(draggedModels.get(0)) && assemblyPanel.rootModelPath == null)
			return false;
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (!(entityTreeModel != null || delegateModel instanceof ValueTreeModel
				|| delegateModel instanceof ListTreeModelInterface || delegateModel instanceof SetTreeModel || delegateModel instanceof MapTreeModel
				|| delegateModel instanceof MapKeyAndValueTreeModel)) {
			return false;
		}
		
		return true;
	}

}
