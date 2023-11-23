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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.gwt.gme.workbench.client.resources.WorkbenchResources;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.WorkbenchTreeAppearance;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceManipulationListenerRegistry;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.dnd.core.client.TreeDropTarget;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;
import com.sencha.gxt.widget.core.client.tree.TreeStyle;
import com.sencha.gxt.widget.core.client.tree.TreeView;

public class WorkbenchTree extends SimpleContainer implements ManipulationListener {
	
	private static int lastId = 0;
	
	private Tree<BaseNode, String> tree;
	private Element previousJointEl;
	private Workbench workbench;
	private List<Folder> foldersWithManipulationListeners = new ArrayList<>();
	private boolean expandAfterDnD;
	
	protected class KeyProvider implements ModelKeyProvider<BaseNode> {
		@Override
		public String getKey(BaseNode item) {
			return "m-" + item.getId().toString();
		}
	}
	
	public WorkbenchTree(Workbench workbench) {
		this.workbench = workbench;
		TreeStore<BaseNode> store = new TreeStore<>(new KeyProvider());
		
		tree = new Tree<BaseNode, String>(store, new ValueProvider<BaseNode, String>() {
			@Override
			public String getValue(BaseNode object) {
				return object.getName();
			}

			@Override
			public void setValue(BaseNode object, String value) {
				//NOP
			}

			@Override
			public String getPath() {
				return "name";
			}
		}, GWT.<TreeAppearance> create(WorkbenchTreeAppearance.class)) {
			@Override
			protected ImageResource calculateIconStyle(BaseNode model) {
				if (getIconProvider() != null) {
					ImageResource iconStyle = getIconProvider().getIcon(model);
					if (iconStyle != null)
						return iconStyle;
				}
				
				TreeStyle ts = getStyle();
				if (isLeaf(model))
					return ts.getLeafIcon();
				
				if (isExpanded(model))
					return ts.getNodeOpenIcon();
				
				return ts.getNodeCloseIcon();
			}
		};
		
		tree.setCell(new AbstractCell<String>() {
			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				String specialStyle = null;
				
				BaseNode node = tree.getStore().findModelWithKey(context.getKey().toString());
			    Folder folder = node.getFolder();
				if (folder.getContent() != null)
					specialStyle = WorkbenchResources.INSTANCE.css().link() + " gmeWorkbenchRow";
				
			    if (specialStyle != null) {
			    	String classes = specialStyle + " ";
			    	value = "<span class='" + classes + "'>" + value + "</span>";
			    }
			    
			    if (value != null)
			    	sb.appendHtmlConstant(value);
			    
			    Scheduler.get().scheduleDeferred(() -> {
			    	TreeNode<BaseNode> treeNode = tree.findNode(node);
			    	if (treeNode == null)
			    		return;
			    	
			    	Element jointElement = tree.getView().getJointElement(treeNode);
			    	if (jointElement == previousJointEl)
			    		jointElement.getStyle().setVisibility(Visibility.VISIBLE);
			    	else if (jointElement != null)
						jointElement.getStyle().setVisibility(Visibility.HIDDEN);
				});
			}
		});
		
		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		tree.getSelectionModel().addSelectionChangedHandler(event -> {
			List<BaseNode> selection = event.getSelection();
			if (selection.isEmpty())
				return;
			
			workbench.fireFolderSelected(selection.get(0).getFolder(), null);
			Scheduler.get().scheduleDeferred(tree.getSelectionModel()::deselectAll);
		});
		
		tree.setIconProvider(model -> {
			Folder folder = model.getFolder();
			if (folder == null || folder.getIcon() == null)
				return null;
			
			Resource resource = GMEIconUtil.getLargestImageFromIcon(folder.getIcon());
			if (resource == null)
				return null;
			
			return new GmImageResource(resource, workbench.getWorkbenchSession().resources().url(resource).asString()) {
				@Override
				public int getHeight(){
					return 16;
				}
				
				@Override
				public int getWidth(){
					return 16;
				}
			};
		});
		
		tree.setView(new TreeView<BaseNode>() {
			@Override
			public void onSelectChange(BaseNode model, boolean select) {
				if (select) {
					BaseNode p = tree.getStore().getParent(model);
					if (p != null)
						tree.setExpanded(tree.getStore().getParent(model), true);
				}
				TreeNode<BaseNode> node = findNode(model);
				if (node != null)
					moveFocus(tree, node.getElement());
			}
		});
		
		tree.addDomHandler(getMouseOverHandler(), MouseOverEvent.getType());
		tree.setTrackMouseOver(false);
		
		add(tree);
		
		configureDragAndDrop();
	}
	
	public Tree<BaseNode, String> getTree() {
		return tree;
	}
	
	/**
	 * Adds to the tree, including subNodes.
	 * @param index - if -1 is set, it is added to the end
	 */
	protected void addToTreeWithChildren(BaseNode parent, BaseNode node, int index) {
		TreeStore<BaseNode> treeStore = tree.getStore();
		if (parent != null) {
			if (index == -1)
				treeStore.add(parent, node);
			else
				treeStore.insert(parent, index, node);
		} else {
			if (index == -1)
				treeStore.add(node);
			else
				treeStore.insert(index, node);
		}
		
		addChildrenToTree(treeStore, node);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		switch (manipulation.manipulationType()) {
			case CHANGE_VALUE:
				BaseNode parentNode = getParentManipulationNode(manipulation);
				if (parentNode == null)
					return;
				
				parentNode.setName(I18nTools.getLocalized(parentNode.getFolder().getDisplayName()));
				tree.getStore().update(parentNode);
				break;
			case ADD:
				AddManipulation addManipulation = (AddManipulation) manipulation;
				parentNode = getParentManipulationNode(manipulation);
				if (parentNode == null)
					return;
				
				Folder addedFolder = (Folder) addManipulation.getItemsToAdd().values().stream().findAny().get();
				addManipulationListeners(addedFolder);
				int index = (Integer) addManipulation.getItemsToAdd().keySet().stream().findAny().get();
				BaseNode addedNode = prepareTreeNode(addedFolder);
				addToTreeWithChildren(parentNode, addedNode, index);
				if (expandAfterDnD) {
					tree.setExpanded(addedNode, true);
					expandAfterDnD = false;
				}
				break;
			case REMOVE:
				RemoveManipulation removeManipulation = (RemoveManipulation) manipulation;
				Folder removedFolder = (Folder) removeManipulation.getItemsToRemove().values().stream().findAny().get();
				BaseNode removedNode = getNode(removedFolder);
				if (removedNode != null) {
					expandAfterDnD = tree.isExpanded(removedNode);
					tree.getStore().remove(removedNode);
				}
				break;
			case CLEAR_COLLECTION:
				parentNode = getParentManipulationNode(manipulation);
				if (parentNode == null)
					return;
				
				tree.getStore().removeChildren(parentNode);
				break;
			default:
				break;
		}
	}
	
	protected void addManipulationListeners(Folder folder) {
		boolean added = foldersWithManipulationListeners.add(folder);
		if (added) {
			workbench.getWorkbenchSession().listeners().entity(folder).add(this);
			
			folder.getSubFolders().forEach(subFolder -> addManipulationListeners(subFolder));
		}
	}
	
	protected void removeManipulationListeners() {
		PersistenceManipulationListenerRegistry listeners = workbench.getWorkbenchSession().listeners();
		foldersWithManipulationListeners.forEach(folder -> listeners.entity(folder).remove(this));
		foldersWithManipulationListeners.clear();
	}
	
	protected static BaseNode prepareTreeNode(Folder folder) {
		BaseNode node = new BaseNode(lastId++, I18nTools.getLocalized(folder.getDisplayName()), folder);
		folder.getSubFolders().forEach(subFolder -> node.addChild(prepareTreeNode(subFolder)));
		
		return node;
	}
	
	private void addChildrenToTree(TreeStore<BaseNode> treeStore, BaseNode parent) {
		List<BaseNode> children = parent.getChildren();
		if (children != null) {
			treeStore.add(parent, children);
			children.forEach(child -> addChildrenToTree(treeStore, child));
		}
	}
	
	private MouseOverHandler getMouseOverHandler() {
		return event -> {
			NativeEvent nativeEvent = event.getNativeEvent();
			try {
				if (!Element.is(nativeEvent.getEventTarget()))
					return;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			TreeNode<BaseNode> node = tree.findNode((Element) nativeEvent.getEventTarget().cast());
			if (node == null)
				return;
			
			handleExpandIconVisibility(node);
		};
	}
	
	private void handleExpandIconVisibility(TreeNode<BaseNode> node) {
		Element jointElement = tree.getView().getJointElement(node);
		if (previousJointEl != null)
			previousJointEl.getStyle().setVisibility(Visibility.HIDDEN);
		jointElement.getStyle().setVisibility(Visibility.VISIBLE);
		previousJointEl = jointElement;
	}
	
	@SuppressWarnings("unused")
	private void configureDragAndDrop() {
		new TreeDragSource<BaseNode>(tree) {
			@Override
			protected void onDragStart(DndDragStartEvent e) {
				super.onDragStart(e);
				
				if (e.isCancelled())
					return;
			}
		};
		
		TreeDropTarget<BaseNode> dropTarget = new TreeDropTarget<BaseNode>(tree) {
			@SuppressWarnings("rawtypes")
			@Override
			protected void handleInsertDrop(DndDropEvent event, TreeNode<BaseNode> dropItem, int before) {
				List<BaseNode> draggedItems = (List) prepareDropData(event.getData(), true);
				if (draggedItems.isEmpty())
					return;
				
				BaseNode draggedItem = draggedItems.get(0);
				Folder draggedFolder = draggedItem.getFolder();
				int draggedIndex = draggedFolder.getParent().getSubFolders().indexOf(draggedFolder);
				
				Folder dropFolder = dropItem.getModel().getFolder();
				int dropIndex = dropFolder.getParent().getSubFolders().indexOf(dropFolder);
				
				if (before == 0 && (dropIndex > draggedIndex))
					dropIndex--;
				else if (before == 1 && (dropIndex < draggedIndex))
					dropIndex++;
				
				if (draggedIndex != dropIndex) {
					NestedTransaction transaction = workbench.getWorkbenchSession().getTransaction().beginNestedTransaction();
					draggedFolder.getParent().getSubFolders().remove(draggedFolder);
					dropFolder.getParent().getSubFolders().add(dropIndex, draggedFolder);
					transaction.commit();
				}
			}
			
			@Override
			protected void showFeedback(DndDragMoveEvent event) {
				super.showFeedback(event);
				
				if (!event.getStatusProxy().getStatus())
					return;
				
				TreeNode<BaseNode> dropNode = tree.findNode((Element) event.getDragMoveEvent().getNativeEvent().getEventTarget().cast());
				if (dropNode == null) {
					event.getStatusProxy().setStatus(false);
					return;
				}
				
				List<?> draggedItems = prepareDropData(event.getData(), true);
				Object draggedItem = draggedItems.isEmpty() ? null : draggedItems.get(0);
				if (!(draggedItem instanceof BaseNode)) {
					event.getStatusProxy().setStatus(false);
					return;
				}
				
				Folder draggedFolder = ((BaseNode) draggedItem).getFolder();
				
				//Must reorder only within the same parent
				if (dropNode.getModel().getFolder().getParent() != draggedFolder.getParent()) {
					event.getStatusProxy().setStatus(false);
					return;
				}
				
				event.getStatusProxy().setStatus(true);
			}
			
			@Override
			protected List<Object> prepareDropData(Object data, boolean convertTreeStoreModel) {
				if (convertTreeStoreModel) {
					List<?> list = (List<?>) data;
					List<Object> modelList = list.stream().filter(element -> element instanceof com.sencha.gxt.data.shared.TreeStore.TreeNode)
							.map(element -> ((com.sencha.gxt.data.shared.TreeStore.TreeNode<?>) element).getData()).collect(Collectors.toList());
					
					return modelList;
				}
				
				return (List<Object>) data;
			}
		};
		
		dropTarget.setAllowSelfAsSource(true);
		dropTarget.setAllowDropOnLeaf(false);
		dropTarget.setFeedback(Feedback.INSERT);
		dropTarget.setOperation(Operation.COPY);
		dropTarget.setAutoExpand(false);
	}
	
	private BaseNode getParentManipulationNode(Manipulation manipulation) {
		if (!(manipulation instanceof PropertyManipulation))
			return null;
		
		LocalEntityProperty owner = (LocalEntityProperty) ((PropertyManipulation) manipulation).getOwner();
		Folder parentFolder = (Folder) owner.getEntity();
		return getNode(parentFolder);
	}
	
	private BaseNode getNode(Folder folder) {
		return tree.getStore().getAll().stream().filter(node -> node.getFolder() == folder).findFirst().orElse(null);
	}
	
	private static native void moveFocus(Tree<?,?> tree, Element element) /*-{
		tree.@com.sencha.gxt.widget.core.client.tree.Tree::moveFocus(Lcom/google/gwt/dom/client/Element;)(element);
	}-*/;
	
}
