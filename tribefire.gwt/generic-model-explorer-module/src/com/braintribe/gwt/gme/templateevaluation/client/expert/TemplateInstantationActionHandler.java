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

import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.TransientGmSession;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.InstantiationData;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.workbench.TemplateInstantiationAction;

@SuppressWarnings("unusable-by-js")
public class TemplateInstantationActionHandler extends TemplateBasedActionHandler<TemplateInstantiationAction> {
	
	private InstantiatedEntityListener listener;
	private TransientGmSession transientGmSession;
	private WorkbenchActionContext<TemplateInstantiationAction> workbenchActionContext;
	
	@Required
	public void setTransientGmSession(TransientGmSession transientGmSession) {
		this.transientGmSession = transientGmSession;
	}
	
	public void setListener(InstantiatedEntityListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected Future<TemplateEvaluationContext> getTemplateEvaluationContext(WorkbenchActionContext<TemplateInstantiationAction> workbenchActionContext) {
		boolean isTransient = workbenchActionContext.getWorkbenchAction().getTransient();
		if (!isTransient)
			return super.getTemplateEvaluationContext(workbenchActionContext);
		
		this.workbenchActionContext = workbenchActionContext;
		
		return super.getTemplateEvaluationContext(new TransientWorkbenchActionContext());
	}
	
	@Override
	public Future<Boolean> handleEvaluatedTemplate(Object evaluatedObject, WorkbenchActionContext<TemplateInstantiationAction> workbenchActionContext)
			throws TemplateEvaluationException {
		if (evaluatedObject == null)
			return new Future<>(true);
		
		Object value = evaluatedObject;
		GenericModelType type = GMF.getTypeReflection().getType(value);
		RootPathElement rootPathElement = new RootPathElement(type, value);
		if (listener != null) {
			boolean isTransient = workbenchActionContext.getWorkbenchAction().getTransient();
			listener.onEntityInstantiated(new InstantiationData(rootPathElement, !isTransient, true, null, false, isTransient));
		}
//			if (parentPanel instanceof ExplorerConstellation)
//				((ExplorerConstellation) parentPanel).onEntityInstantiated(rootPathElement);
//			else if (parentPanel instanceof SelectionConstellation && value instanceof GenericEntity)
//				((SelectionConstellation) parentPanel).fireEntitySelected((GenericEntity) value);
		return new Future<>(true);
	}
	
	@Override
	public Future<Boolean> checkIfPerformPossible(WorkbenchActionContext<TemplateInstantiationAction> workbenchActionContext) {
		return new Future<>(true);
	}
	
	@Override
	public boolean getCloneToPersistenceSession() {
		return true;
	}
	
	@Override
	public boolean getUseEvaluation() {
		return true;
	}

	@SuppressWarnings("unusable-by-js")
	private class TransientWorkbenchActionContext implements WorkbenchActionContext<TemplateInstantiationAction> {
		@Override
		public TemplateInstantiationAction getWorkbenchAction() {
			return workbenchActionContext.getWorkbenchAction();
		}
		
		@Override
		public Object getPanel() {
			return workbenchActionContext.getPanel();
		}
		
		@Override
		public List<ModelPath> getModelPaths() {
			return workbenchActionContext.getModelPaths();
		}
		
		@Override
		public GmSession getGmSession() {
			return transientGmSession;
		}
		
		@Override
		public Folder getFolder() {
			return workbenchActionContext.getFolder();
		}
	}

}
