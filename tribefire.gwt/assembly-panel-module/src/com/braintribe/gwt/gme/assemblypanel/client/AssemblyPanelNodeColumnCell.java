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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import com.braintribe.gwt.gme.assemblypanel.client.AssemblyUtil.ValueDescriptionBean;
import com.braintribe.gwt.gme.assemblypanel.client.model.AbstractGenericTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.EntityTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.ListEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyAndValueTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.MapKeyOrValueEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntry;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryModelInterface;
import com.braintribe.gwt.gme.assemblypanel.client.model.PropertyEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.model.SetEntryTreeModel;
import com.braintribe.gwt.gme.assemblypanel.client.resources.AssemblyPanelResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.display.Emphasized;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeNode;

public class AssemblyPanelNodeColumnCell extends AbstractCell<AbstractGenericTreeModel> {
	
	// TODO document
	public static final String PROPERTY_EMPHASIS_PROPERTY = "property";
	public static final String PROPERTY_EMPHASIS_CHILDREN = "children";
	
	protected static final String LINK_CLASS = AssemblyPanelResources.INSTANCE.css().linkStyle();
	
	private static final String SELECTION_CHECKED_CLASS = "assemblyTreeSelectionCheckedClass";
	private static final String SELECTION_UNCHECKED_CLASS = "assemblyTreeSelectionUncheckedClass";
	private static final String selectionRadioCheckedImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.radioChecked()).getHTML()
			.replace("style='", "class='" + SELECTION_CHECKED_CLASS + "' style='cursor:pointer; padding-top: 3px; margin-right: 3px;");
	private static final String selectionRadioUncheckedImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.radio()).getHTML()
			.replace("style='", "class='" + SELECTION_UNCHECKED_CLASS + "' style='cursor:pointer; padding-top: 3px; margin-right: 3px;");
	private static final String selectionCheckedImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.checked()).getHTML()
			.replace("style='", "class='" + SELECTION_CHECKED_CLASS + "' style='cursor:pointer; margin-right: 3px;");
	private static final String selectionUncheckedImageString = AbstractImagePrototype.create(AssemblyPanelResources.INSTANCE.unchecked()).getHTML()
			.replace("style='", "class='" + SELECTION_UNCHECKED_CLASS + "' style='cursor:pointer; margin-right: 3px;");
	
	private AssemblyPanel assemblyPanel;
	
	public AssemblyPanelNodeColumnCell(AssemblyPanel assemblyPanel) {
		this.assemblyPanel = assemblyPanel;
	}

	@Override
	public void render(Context context, AbstractGenericTreeModel model, SafeHtmlBuilder sb) {
		ValueDescriptionBean bean = model.getLabel();
		if (bean == null) {
			bean = assemblyPanel.assemblyUtil.prepareLabel(model, assemblyPanel.isTopLevelMap(), assemblyPanel.showNodeTextAsTooltip,
	    		(assemblyPanel.prepareToolBarActions || assemblyPanel.showContextMenu) && assemblyPanel.showLinkStyle);
			model.setLabel(bean);
		}
	    String text = bean.getValue();
	    
	    //Maybe we won't need this special thing anymore
	    String specialStyle = model instanceof EntityTreeModel ? (String) ((EntityTreeModel) model).get(EntityTreeModel.SPECIAL_STYLE_PROPERTY) : null;
	    String emphasisStyle = prepareEmphasisStyle(model);
	    if (emphasisStyle != null) {
		    if (specialStyle != null)
		    	specialStyle += " " + emphasisStyle;
			else
				specialStyle = emphasisStyle;
	    }
	    
	    if (assemblyPanel.nodeRenderingStyleProvider != null) {
	    	String nodeRenderingStyle = null;
			nodeRenderingStyle = assemblyPanel.nodeRenderingStyleProvider.apply(model);
	    	if (nodeRenderingStyle != null) {
		    	if (specialStyle == null)
		    		specialStyle = nodeRenderingStyle;
		    	else
		    		specialStyle += " " + nodeRenderingStyle;
	    	}
	    }
	    
	    if ((assemblyPanel.prepareToolBarActions || assemblyPanel.showContextMenu) && assemblyPanel.showLinkStyle && !text.contains(LINK_CLASS)) {
	    	if (specialStyle == null)
	    		specialStyle = LINK_CLASS;
	    	else
	    		specialStyle += " " + LINK_CLASS;
	    }
	    
	    if (specialStyle != null) {
	    	String classes = specialStyle + " ";
	    	text = "<span class='" + classes + "'>" + text + "</span>";
	    }
	    
	    String checkedImageString = selectionRadioCheckedImageString;
	    String uncheckedImageString = selectionRadioUncheckedImageString;
	    if (assemblyPanel.maxSelectionCount > 1) {
	    	checkedImageString = selectionCheckedImageString;
	    	uncheckedImageString = selectionUncheckedImageString;
	    }
	    
		String checkBoxImage = assemblyPanel.isModelCheckable(model)
				? (assemblyPanel.editorTreeGrid.selectionModel.checkedModels.contains(model) ? checkedImageString : uncheckedImageString)
				: null;
	    text = prepareValueRendererString(model, text, bean.getDescription(), checkBoxImage);
	    sb.appendHtmlConstant(text);
	    
	    Scheduler.get().scheduleDeferred(() -> {
			TreeNode<AbstractGenericTreeModel> node = AssemblyUtil.findNode(assemblyPanel.editorTreeGrid, model);
			if (node != null) {
				Element jointElement = assemblyPanel.editorTreeGrid.getTreeView().getJointElement(node);
				if (jointElement != null)
					jointElement.getStyle().setVisibility(Visibility.HIDDEN);
			}
		});
	}
	
	private String prepareEmphasisStyle(AbstractGenericTreeModel model) {
		boolean applyEmphasisStyle = false;
		ModelMdResolver modelMdResolver = assemblyPanel.gmSession.getModelAccessory().getMetaData().useCase(assemblyPanel.useCase);
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		if (model instanceof ListEntryTreeModel || model instanceof MapEntryTreeModel || model instanceof MapKeyAndValueTreeModel || model instanceof MapKeyOrValueEntryTreeModel
				|| model instanceof SetEntryTreeModel) {
			EntityTreeModel entityTreeModel = AssemblyUtil.getParentEntityTreeModel(model);
			if (entityTreeModel != null) {
				String propertyName;
				if (model instanceof MapKeyOrValueEntryTreeModel)
					propertyName = delegateModel.getParent().getParent().getPropertyName();
				else
					propertyName = delegateModel.getParent().getPropertyName();
				applyEmphasisStyle = getEmphasysStyle(modelMdResolver, entityTreeModel, propertyName, PROPERTY_EMPHASIS_CHILDREN);
			}
		} else if (model instanceof PropertyEntryTreeModel) {
			PropertyEntry pEntry = ((PropertyEntryTreeModel) model).getPropertyEntry();
			applyEmphasisStyle = getEmphasysStyle(modelMdResolver, pEntry.getEntity(), pEntry.getEntityType(), pEntry.getPropertyName(), PROPERTY_EMPHASIS_PROPERTY);
		}
		
		EntityTreeModel entityTreeModel = delegateModel.getEntityTreeModel();
		if (!applyEmphasisStyle && entityTreeModel != null) {
			EntityMdResolver entityContextBuilder;
			if (entityTreeModel.getModelObject() instanceof GenericEntity) {
				GenericEntity entity = (GenericEntity) entityTreeModel.getModelObject();
				entityContextBuilder = getMetaData(entity).useCase(assemblyPanel.useCase).entity(entity);
			} else
				entityContextBuilder = modelMdResolver.lenient(true).entityType(entityTreeModel.getElementType());
			applyEmphasisStyle = entityContextBuilder.is(Emphasized.T);
		}
		
		return applyEmphasisStyle ? AssemblyPanelResources.INSTANCE.css().emphasisStyle() : null;
	}
	
	private boolean getEmphasysStyle(ModelMdResolver modelMdResolver, EntityTreeModel entityTreeModel, String propertyName, String useCase) {
		GenericEntity entity = null;
		if (entityTreeModel.getModelObject() instanceof GenericEntity)
			entity = (GenericEntity) entityTreeModel.getModelObject();
		return getEmphasysStyle(modelMdResolver, entity, entityTreeModel.getElementType(), propertyName, useCase);
	}
	
	private boolean getEmphasysStyle(ModelMdResolver modelMdResolver, GenericEntity entity, EntityType<?> entityType, String propertyName, String useCase) {
		EntityMdResolver entityContextBuilder;
		if (entity != null) {
			entityContextBuilder = getMetaData(entity).entity(entity);
		} else
			entityContextBuilder = modelMdResolver.entityType(entityType);

		return entityContextBuilder.property(propertyName).useCase(useCase).is(Emphasized.T);
	}
	
	private String prepareValueRendererString(AbstractGenericTreeModel model, String valueString, String description, String checkBoxImage) {
		String valueIcon = null;
		boolean absent = false;
		AbstractGenericTreeModel delegateModel = model.getDelegate();
		if (model instanceof PropertyEntryModelInterface || delegateModel instanceof PropertyEntryModelInterface) {
			PropertyEntry propertyEntry = model instanceof PropertyEntryModelInterface ? ((PropertyEntryModelInterface) model).getPropertyEntry() :
					((PropertyEntryModelInterface) delegateModel).getPropertyEntry();
			if (propertyEntry.isAbsent() && assemblyPanel.isAllowExpandNodes()) //Only showing the absent marker if the node may be expanded.
				absent = true;
			else if (model.refersTo(null) && delegateModel.getModelObject() instanceof String && ((String) delegateModel.getModelObject()).isEmpty())
				valueIcon = AssemblyPanelTreeGridView.emptyStringImageString;
		} else if (model instanceof MapKeyOrValueEntryTreeModel && valueString.isEmpty())
			valueIcon = AssemblyPanelTreeGridView.emptyStringImageString;
		
		StringBuilder builder = new StringBuilder();
		builder.append("<table class='").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append(" ")
				.append(AssemblyPanelResources.INSTANCE.css().tableForTreeWithFixedLayout());
		builder.append("' border='0' cellpadding='2' cellspacing='0'>\n");
		builder.append("   <tr class='").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append("'>\n");
		builder.append("      <td class='gxtReset ").append(AssemblyPanelResources.INSTANCE.css().inheritFont()).append(" ")
				.append(AssemblyPanelResources.INSTANCE.css().textOverflowNoWrap());
		builder.append("' width='100%'");
		if (!assemblyPanel.disableNodesTooltip && ((description != null && !description.isEmpty()) || absent)) {
			builder.append(" qtip='");
			builder.append(absent ? LocalizedText.INSTANCE.absent() : SafeHtmlUtils.htmlEscape(description));
			builder.append("'");
		}
		builder.append(">");
		if (checkBoxImage != null)
			builder.append(checkBoxImage);
		builder.append(valueString);
		if (absent)
			builder.append("&nbsp;");
		if (valueIcon != null)
			builder.append(valueIcon);
		builder.append("</td>\n");
		builder.append("   </tr>\n</table>");
		return builder.toString();
	}

}
