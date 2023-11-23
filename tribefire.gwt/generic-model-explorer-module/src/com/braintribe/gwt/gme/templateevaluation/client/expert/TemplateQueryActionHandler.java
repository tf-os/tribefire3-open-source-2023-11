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
package com.braintribe.gwt.gme.templateevaluation.client.expert;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.google.gwt.user.client.ui.Widget;

public class TemplateQueryActionHandler extends TemplateBasedActionHandler<TemplateQueryAction> {
	
	//private static final Property templatePrototypeProperty = Template.T.getProperty("prototype");
	//private static final Property templateScriptProperty = Template.T.getProperty("script");
	
	private ExplorerConstellation explorerConstellation;
	
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	@Override
	public Future<Object> prepareTemplateEvaluation(TemplateEvaluationContext templateEvaluationContext, Object parentPanel) throws TemplateEvaluationException {
		//TemplateEvaluation templateEvaluation = templateEvaluationContext.getTemplateEvaluation();
		//Object evaluatedObject = templateEvaluation.evaluateTemplate(getCloneToPeristenceSession(), true);
		// This is needed to pass all the informations of the persisted template but instead of prototype and script
		// use the already evaluated Query (still having variables preserved)
		//Template templateCopy = createTemplateCopy(evaluatedObject, templateEvaluation.getTemplate());
		Template templateCopy = createTemplateCopy(templateEvaluationContext);
		return new Future<Object>(templateCopy);
	}
	
	@Override
	protected void handleEvaluateTemplate(WorkbenchActionContext<TemplateQueryAction> workbenchActionContext, TemplateEvaluationContext templateEvaluationContext) {
		if (explorerConstellation == null) {
			super.handleEvaluateTemplate(workbenchActionContext, templateEvaluationContext);
			return;
		}
		
		Template template = templateEvaluationContext.getTemplate();
		TemplateQueryAction templateQueryAction = workbenchActionContext.getWorkbenchAction();
		boolean useNewNormalizedView = templateQueryAction.getExecutionType() != null;
		if (!useNewNormalizedView) {
			super.handleEvaluateTemplate(workbenchActionContext, templateEvaluationContext);
			return;
		}

		TemplateQueryOpener opener = new TemplateQueryOpener(template, workbenchActionContext, this);
		explorerConstellation.handleServiceRequestPanel(templateEvaluationContext, templateQueryAction, getName(workbenchActionContext), opener);
	}
	
	@Override
	public Future<Boolean> handleEvaluatedTemplate(Object evaluatedObject, WorkbenchActionContext<TemplateQueryAction> workbenchActionContext)
			throws TemplateEvaluationException {
		Future<Boolean> result = new Future<>();
		
		String name = getName(workbenchActionContext);
		if (wasHandledInSameTab(evaluatedObject, workbenchActionContext, name, result))
			return result;		
		
		EntityType<?> entityType = ((GenericEntity) evaluatedObject).entityType();
		
		SelectionConstellation selectionConstellation = workbenchActionContext.getPanel() instanceof SelectionConstellation
				? (SelectionConstellation) workbenchActionContext.getPanel() : null;
		if (explorerConstellation == null && selectionConstellation == null) {
			result.onSuccess(true);
			return result;
		}
		
		ProfilingHandle ph = Profiling.start(getClass(), "Handling evaluated template", false, true);
		
		try {
			String useCase = selectionConstellation != null ? selectionConstellation.getUseCase() : explorerConstellation.getUseCase();
			
			String description = GMEMetadataUtil.getEntityNameMDOrShortName(entityType, null, useCase);
			final ProfilingHandle ph1 = Profiling.start(getClass(), "Creating new tab element", true, true);
			ProfilingHandle phBrowsing = Profiling.start(getClass(), "Creating browsing constellation", false, true);
			Supplier<BrowsingConstellation> browsingConstellationSupplier;
			PersistenceGmSession workbenchSession = (explorerConstellation == null) ? null : explorerConstellation.getHomeConstellation().getWorkbenchSession();
			if (selectionConstellation != null) 
				browsingConstellationSupplier = selectionConstellation.provideSelectionBrowsingConstellation(name, (GenericEntity) evaluatedObject, false);
			else 
				browsingConstellationSupplier = explorerConstellation.provideBrowsingConstellation(name, (GenericEntity) evaluatedObject, workbenchActionContext);
			
			phBrowsing.stop();
			final ProfilingHandle phVerticalTabElement = Profiling.start(getClass(), "Creating VerticalTabElement", true, true);
			
			if (selectionConstellation != null) {
				VerticalTabElement verticalTabElement = selectionConstellation.maybeCreateVerticalTabElement(workbenchActionContext, name, name,
						description, GMEIconUtil.getSmallIcon(workbenchActionContext, workbenchSession), browsingConstellationSupplier, false, true, false);
				ph1.stop();
				handleVerticalTabElement(verticalTabElement, workbenchActionContext);
				phVerticalTabElement.stop();
			} else {
				Boolean contextAdd = false;
				if (!workbenchActionContext.isHandleInNewTab()) {
					BrowsingConstellation browsingConstellation = explorerConstellation.getCurrentBrowsingConstellation();
					if (browsingConstellation != null) {
						explorerConstellation.maybeCreateQueryTetherBarElement(name, (GenericEntity) evaluatedObject, workbenchActionContext, browsingConstellation);
						contextAdd = true;
					}
				}
				
				if (!contextAdd) {
					explorerConstellation
							.maybeCreateVerticalTabElement(workbenchActionContext, name, description, browsingConstellationSupplier,
									GMEIconUtil.getSmallIcon(workbenchActionContext, workbenchSession), evaluatedObject, false)
							.andThen(verticalTabElement -> {
								ph1.stop();
								handleVerticalTabElement(verticalTabElement, workbenchActionContext);
								phVerticalTabElement.stop();
							}).onError(e -> {
								ph1.stop();
								phVerticalTabElement.stop();
							});
				}
			}
			
			result.onSuccess(true);
		} catch (RuntimeException ex) {
			result.onFailure(ex);
		}
		
		ph.stop();
		return result;
	}
	
	private boolean wasHandledInSameTab(Object evaluatedObject, WorkbenchActionContext<TemplateQueryAction> workbenchActionContext, String name,
			Future<Boolean> result) {
		if (workbenchActionContext.isHandleInNewTab())
			return false;
		
		BrowsingConstellation browsingConstellation = getParentBrowsingConstellation(workbenchActionContext.getPanel());
		if (browsingConstellation == null)
			return false;
		
		explorerConstellation.maybeCreateQueryTetherBarElement(name, (GenericEntity) evaluatedObject, workbenchActionContext, browsingConstellation);
		
		result.onSuccess(true);
		return true;
	}
	
	private BrowsingConstellation getParentBrowsingConstellation(Object panel) {
		if (panel instanceof BrowsingConstellation)
			return (BrowsingConstellation) panel;
		
		if (panel instanceof Widget)
			return getParentBrowsingConstellation(((Widget) panel).getParent());
		
		return null;
	}

	@Override
	public Future<Boolean> checkIfPerformPossible(WorkbenchActionContext<TemplateQueryAction> workbenchActionContext) {
		return new Future<>(true);
	}
	
	@Override
	public boolean getCloneToPersistenceSession() {
		return false;
	}
	
	@Override
	public boolean getUseEvaluation() {
		return true;
	}
	
	/*
	 * Returns a detached (GMF.create) copy of the given template without the original prototype and script attached.
	 * Instead as a prototype the evaluatedObject will be set to the copy. 
	 *
	private Template createTemplateCopy(Object evaluatedObject, Template persistedTemplate) {
		Template copy = persistedTemplate.clone(new StandardCloningContext() {
			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				return property != templatePrototypeProperty && property != templateScriptProperty;
			}
		});
		copy.setPrototype(evaluatedObject);
		return copy;
	}*/
	
	private Template createTemplateCopy(final TemplateEvaluationContext templateEvaluationContext) {
		EntityType<Template> templateType = Template.T;
		StandardCloningContext cloningContext = new StandardCloningContext() {
			@Override
			public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
				if (!(clonedValue instanceof Variable))
					return super.postProcessCloneValue(propertyType, clonedValue);
				
				Variable variable = (Variable) clonedValue;
				Map<String, Object> values = templateEvaluationContext.getTemplateEvaluation().getVariableValues();
				if (values != null && !values.isEmpty() && values.containsKey(variable.getName())) {
					variable.setDefaultValue(values.get(variable.getName()));
					return variable;
				}
				
				return super.postProcessCloneValue(propertyType, clonedValue);
			}
		};
		
		Template template = (Template) templateType.clone(cloningContext, templateEvaluationContext.getTemplate(), StrategyOnCriterionMatch.reference);
		if (template instanceof EnhancedEntity)
			((EnhancedEntity) template).detach();
		
		return template;		
	}
	
	public static class TemplateQueryOpener {
		private Object evaluatedObject;
		private WorkbenchActionContext<TemplateQueryAction> workbenchActionContext;
		private TemplateQueryActionHandler handler;
		
		public TemplateQueryOpener(Object evaluatedObject, WorkbenchActionContext<TemplateQueryAction> workbenchActionContext, TemplateQueryActionHandler handler) {
			this.evaluatedObject = evaluatedObject;
			this.workbenchActionContext = workbenchActionContext;
			this.handler = handler;
		}
		
		public void openTemplateQuery() {
			handler.handleEvaluatedTemplate(evaluatedObject, workbenchActionContext);
		}
	}

}
