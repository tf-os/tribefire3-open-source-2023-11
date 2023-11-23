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
package com.braintribe.gwt.gmview.client;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.gmview.action.client.ActionPerformanceContext;
import com.braintribe.gwt.gmview.action.client.ActionPerformanceListener;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for all {@link GenericEntity} DnD sources.
 * @author michel.couto
 *
 */
public interface GmeEntityDragAndDropSource {
	
	public List<TemplateBasedAction> getTemplateActions();
	public WorkbenchActionContext<TemplateBasedAction> getDragAndDropWorkbenchActionContext();
	public Widget getView();
	
	public default void markParentAsReloadPending(Widget widget) {
		ActionPerformanceListener listener = getActionPerformanceListener(widget);
		if (listener == null)
			return;
		
		ActionPerformanceContext context = new ActionPerformanceContext();
		context.setParentWidget(getView());
		listener.onAfterPerformAction(context);
	}
	
	public default ActionPerformanceListener getActionPerformanceListener(Widget widget) {
		if (widget == null)
			return null;
		
		if (widget instanceof ActionPerformanceListener)
			return (ActionPerformanceListener) widget;
		
		return getActionPerformanceListener(widget.getParent());
	}
	
	/** Checks if any of the given actions is valid for the given context. */
	public static boolean isAnyActionValid(List<TemplateBasedAction> actions, Object drop, GmeEntityDragAndDropSource source) {
		if (!isEmpty(actions) && drop instanceof GenericEntity) {
			WorkbenchActionContext<TemplateBasedAction> workbenchActionContext = source.getDragAndDropWorkbenchActionContext();
			GenericEntity _drop = (GenericEntity) drop;
			for (TemplateBasedAction templateAction : actions) {
				workbenchActionContext.setWorkbenchAction(templateAction);

				List<Variable> visibleVariables = TemplateSupport.getVisibleVariables(templateAction.getTemplate());
				Variable actionVariable = getValidVariable(visibleVariables, _drop);
				if (actionVariable != null)
					return true;
			}
		}

		return false;
	}

	/**
	 * Handles the drop event.
	 */
	public static Map<TemplateBasedAction, Variable> handleDrop(GenericEntity dropEntity, List<TemplateBasedAction> actions,
			WorkbenchActionContext<TemplateBasedAction> workbenchActionContext) {

		Map<TemplateBasedAction, Variable> availableActionsMap = new LinkedHashMap<>();
		if (actions == null || workbenchActionContext == null)
			return availableActionsMap;

		for (TemplateBasedAction templateAction : actions) {
			workbenchActionContext.setWorkbenchAction(templateAction);

			List<Variable> visibleVariables = TemplateSupport.getVisibleVariables(templateAction.getTemplate());

			Variable actionVariable = getValidVariable(visibleVariables, dropEntity);
			if (actionVariable != null)
				availableActionsMap.put(templateAction, actionVariable);
		}

		return availableActionsMap;
	}

	public static Variable getValidVariable(List<Variable> variables, GenericEntity entity) {
		for (Variable v : variables) {
			GenericModelType varType = GMF.getTypeReflection().findType(v.getTypeSignature());
			if (varType != null && varType.isEntity() && varType.isValueAssignable(entity))
				return v;
		}
		
		return null;
	}
	
}
