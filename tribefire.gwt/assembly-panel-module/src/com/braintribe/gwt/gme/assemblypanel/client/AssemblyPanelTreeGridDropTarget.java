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

import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListTreeModelInterface;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmeDragAndDropView;
import com.braintribe.gwt.gmview.client.GmeEntityDragAndDropSource;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DndDragCancelEvent;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.TreeGridDropTarget;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

/**
 * {@link AssemblyPanel}'s implementation for DnD.
 * 
 * @author michel.docouto
 *
 */
public class AssemblyPanelTreeGridDropTarget extends TreeGridDropTarget<AbstractGenericTreeModel> {

	private final AssemblyPanel assemblyPanel;
	private boolean performingReordering;
	private List<TemplateBasedAction> actions;
	private GenericEntity previousDefaultValue;
	private GmSession previousSession;
	private Element lastElement;

	public AssemblyPanelTreeGridDropTarget(AssemblyPanel assemblyPanel) {
		super(assemblyPanel.getTreeGrid());
		this.assemblyPanel = assemblyPanel;
		
		setAllowSelfAsSource(true);
		setAllowDropOnLeaf(false);
		setFeedback(Feedback.INSERT);
		setOperation(Operation.COPY);
		setAutoExpand(false);
	}
	
	@Override
	protected void handleAppend(DndDragMoveEvent event, TreeNode<AbstractGenericTreeModel> item) {
		cancelTarget();
		super.handleAppend(event, item);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void handleInsertDrop(DndDropEvent event, TreeNode<AbstractGenericTreeModel> item, int before) {
		cancelTarget();
		AbstractGenericTreeModel dropModel = item.getModel();
		
		if (performingReordering) {
			List<AbstractGenericTreeModel> draggedItems = (List) prepareDropData(event.getData(), true);
			if (draggedItems.isEmpty())
				return;
			
			ListEntryTreeModel listEntryModel = (ListEntryTreeModel) dropModel;
			
			ListEntryTreeModel draggedItem = (ListEntryTreeModel) draggedItems.get(0);
			int draggedIndex = draggedItem.getListEntryIndex();
			int dropIndex = listEntryModel.getListEntryIndex();
			if (before == 0 && (dropIndex > draggedIndex))
				dropIndex--;
			else if (before == 1 && (dropIndex < draggedIndex))
				dropIndex++;
			
			if ((dropIndex > draggedIndex) && draggedItems.size() > 1)
				dropIndex -= draggedItems.size() - 1;
			
			if (draggedIndex != dropIndex)
				assemblyPanel.manipulationHandler.replaceInList(dropIndex, draggedItems);
			
			return;
		}
		
		if (!(event.getSource() instanceof GmeEntityDragAndDropSource))
			return;

		GenericEntity dropEntity = dropModel.getModelObject();
		GmeEntityDragAndDropSource dragAndDropSource = (GmeEntityDragAndDropSource) event.getSource();
		WorkbenchActionContext<TemplateBasedAction> workbenchActionContext = dragAndDropSource.getDragAndDropWorkbenchActionContext();
		Map<TemplateBasedAction, Variable> availableActionsMap = GmeEntityDragAndDropSource.handleDrop(dropEntity, actions, workbenchActionContext);
		
		Future<TemplateBasedAction> chosenActionFuture;
		if (availableActionsMap.size() > 1 && assemblyPanel.workbenchActionSelectionHandler != null)
			chosenActionFuture = chooseAvailableAction(availableActionsMap.keySet());
		else
			chosenActionFuture = new Future<>(first(availableActionsMap.keySet()));
		
		chosenActionFuture.andThen(action -> {
			if (action == null)
				return;
			
			Variable actionVariable = availableActionsMap.get(action);
			workbenchActionContext.setWorkbenchAction(action);
			prepareDefaultValue(actionVariable, dropEntity);
			
			ModelAction modelAction = assemblyPanel.workbenchActionHandlerRegistry.apply(workbenchActionContext);
			if (modelAction != null) {
				GmContentView view = workbenchActionContext.getPanel() instanceof GmContentView ? ((GmContentView) workbenchActionContext.getPanel()) : null;
				modelAction.configureGmContentView(view);
				modelAction.perform(null);
				
				if (assemblyPanel != dragAndDropSource.getView())
					dragAndDropSource.markParentAsReloadPending(assemblyPanel);
			}
			
			actionVariable.setDefaultValue(previousDefaultValue);
			if (previousSession != null)
				actionVariable.attach(previousSession);
		});
	}
	
	private Future<TemplateBasedAction> chooseAvailableAction(Set<TemplateBasedAction> actions) {
		Future<TemplateBasedAction> future = new Future<>();
		assemblyPanel.workbenchActionSelectionHandler.handleActionSelection(new ArrayList<>(actions), future);
		return future;
	}

	@Override
	protected void showFeedback(DndDragMoveEvent event) {
		performingReordering = false;
		super.showFeedback(event);

		if (!event.getStatusProxy().getStatus()) {
			cancelTarget();
			return;
		}
		
		if (!(event.getDragSource() instanceof GmeEntityDragAndDropSource)) {
			cancelTarget();
			return;
		}

		TreeGrid<AbstractGenericTreeModel> treeGrid = assemblyPanel.getTreeGrid();
		TreeNode<AbstractGenericTreeModel> node = treeGrid.findNode(event.getDragMoveEvent().getNativeEvent().getEventTarget().<Element> cast());
		if (node == null) {
			event.getStatusProxy().setStatus(false);
			cancelTarget();
			return;
		}

		AbstractGenericTreeModel dropModel = node.getModel().getDelegate();
		
		if (event.getDragSource() == assemblyPanel.treeGridDragSource && assemblyPanel.treeGridDragSource.isListReorderAvailable()) {
			List<?> draggedItems = prepareDropData(event.getData(), true);
			if (!(draggedItems.get(0) instanceof AbstractGenericTreeModel)) {
				event.getStatusProxy().setStatus(false);
				cancelTarget();
				return;
			}
			
			AbstractGenericTreeModel draggedItem = ((AbstractGenericTreeModel) draggedItems.get(0)).getDelegate();
			
			// Must reorder only within the same parent
			if (dropModel.getParent() != null && dropModel.getParent() == draggedItem.getParent()
					&& (draggedItem.getParent()).getDelegate() instanceof ListTreeModelInterface) {
				if (!treeGrid.getTreeStore().getRootItems().contains(draggedItem) || assemblyPanel.rootModelPath != null) {
					event.getStatusProxy().setStatus(true);
					performingReordering = true;
					return;
				}
			}
		}
		
		
		GmeEntityDragAndDropSource dragSource = (GmeEntityDragAndDropSource) event.getDragSource();
		actions = dragSource.getTemplateActions();
		if (GmeEntityDragAndDropSource.isAnyActionValid(actions, dropModel.getModelObject(), dragSource)) {
			event.getStatusProxy().setStatus(true);
		} else {
			event.getStatusProxy().setStatus(false);
			cancelTarget();
		}
	}

	@Override
	protected List<Object> prepareDropData(Object data, boolean convertTreeStoreModel) {
		if (!(data instanceof List))
			return null;

		if (convertTreeStoreModel) {
			List<?> list = (List<?>) data;
			return list.stream().filter(e -> e instanceof com.sencha.gxt.data.shared.TreeStore.TreeNode)
					.map(e -> ((com.sencha.gxt.data.shared.TreeStore.TreeNode<?>) e).getData()).collect(Collectors.toList());
		}

		return (List<Object>) data;
	}
	
	//Overriding this because the handleInsert method is private... That method was the only thing which has changed...
	@Override
	protected void handleInsert(DndDragMoveEvent event, TreeNode<AbstractGenericTreeModel> item) {
		if (performingReordering) {
			super.handleInsert(event, item);
			return;
		}
		
		int height = getWidget().getView().getRow(item.getModel()).getOffsetHeight();
		int mid = height / 2;
		int top = getWidget().getView().getRow(item.getModel()).getAbsoluteTop();
		mid += top;
		int y = event.getDragMoveEvent().getNativeEvent().<XEvent> cast().getXY().getY();
		boolean before = y < mid;

		if ((!getWidget().isLeaf(item.getModel()) || isAllowDropOnLeaf()) && (feedback == Feedback.BOTH || feedback == Feedback.APPEND)
				&& ((before && y > top + 4) || (!before && y < top + height - 4))) {
			handleAppend(event, item);
			return;
		}

		// clear any active append item
		if (activeItem != null && activeItem != item)
			clearStyle(activeItem);

		appendItem = null;

		status = before ? 0 : 1;

		if (activeItem != null)
			clearStyle(activeItem);

		activeItem = item;

		TreeStore<AbstractGenericTreeModel> store = getWidget().getTreeStore();

		int idx = -1;

		AbstractGenericTreeModel p = store.getParent(activeItem.getModel());
		if (p != null)
			idx = store.getChildren(p).indexOf(activeItem.getModel());
		else
			idx = store.getRootItems().indexOf(activeItem.getModel());

		ImageResource status = resources.dropInsert();
		if (before && idx == 0)
			status = resources.dropInsertAbove();
		else if (idx > 1 && !before && p != null && idx == store.getChildCount(p) - 1)
			status = resources.dropInsertBelow();

		event.getStatusProxy().setStatus(true, status);

		showInsert(getWidget().getView().getRow(item.getModel()));
	}
	
	private void showInsert(Element row) {
		cancelTarget();
		row.addClassName(GmeDragAndDropView.GME_ELEMENT_DROP_TARGET);
		lastElement = row;
	}
	
	@Override
	protected void clearStyle(TreeNode<AbstractGenericTreeModel> node) {
		Element element = getWidget().getView().getRow(node.getModel());
		if (element == null)
			return;
		super.clearStyle(node);
		
		element.removeClassName(GmeDragAndDropView.GME_ELEMENT_DROP_TARGET);
		if (element == lastElement)
			lastElement = null;
	}
	
	@Override
	protected void onDragCancelled(DndDragCancelEvent event) {
		super.onDragCancelled(event);
		cancelTarget();
	}
	
	private void cancelTarget() {
		if (lastElement != null) {
			lastElement.removeClassName(GmeDragAndDropView.GME_ELEMENT_DROP_TARGET);
			lastElement = null;
		}
	}

	private void prepareDefaultValue(Variable variable, GenericEntity entity) {
		previousDefaultValue = (GenericEntity) variable.getDefaultValue();
		previousSession = variable.detach();
		variable.setDefaultValue(entity);
	}

}
