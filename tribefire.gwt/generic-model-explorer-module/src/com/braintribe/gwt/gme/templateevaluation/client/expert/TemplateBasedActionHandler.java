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
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.AbstractQueryActionHandler;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateEvaluationCancelledException;
import com.braintribe.gwt.gme.templateevaluation.client.TemplateGIMADialog;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluation;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.template.preprocessing.TemplatePreprocessing;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class TemplateBasedActionHandler<T extends TemplateBasedAction> extends AbstractQueryActionHandler<T> {
	
	private static final Logger logger = new Logger(TemplateBasedActionHandler.class);
	
	private Supplier<? extends TemplateGIMADialog> templateGIMADialogProvider;
	private Supplier<String> userNameProvider = () -> null;
	private TemplateGIMADialog gimaDialog;
	
	@Configurable
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	@Required
	public void setTemplateGIMADialogProvider(Supplier<? extends TemplateGIMADialog> templateGIMADialogProvider) {
		this.templateGIMADialogProvider = templateGIMADialogProvider;
	}
	
	public abstract Future<Boolean> handleEvaluatedTemplate(Object evaluatedObject, WorkbenchActionContext<T> workbenchActionContext)
			throws TemplateEvaluationException;
	
	public abstract Future<Boolean> checkIfPerformPossible(WorkbenchActionContext<T> workbenchActionContext);
	
	public abstract boolean getCloneToPersistenceSession();
	
	public abstract boolean getUseEvaluation();
	
	/**
	 * This method should be overridden by implementations which want to handle heading. 
	 * @param template - Used for the Heading.
	 */
	public String getHeading(Template template) {
		return null;
	}
	
	/**
	 * This method should be overridden by implementations which want to change buttons. 
	 */
	public String getApplyText() {
		return null;
	}
	
	/**
	 * This method should be overridden by implementations which want to change buttons. 
	 */
	public String getApplyDescriptionText() {
		return null;
	}
	
	/**
	 * This method should be overridden by implementations which want to change buttons. 
	 */
	public String getCancelDescriptionText() {
		return null;
	}

	@Override
	public void perform(final WorkbenchActionContext<T> workbenchActionContext) {
		checkIfPerformPossible(workbenchActionContext) //
				.andThen(result -> {
					if (result)
						handlePerform(workbenchActionContext);
				}) //
				.onError(error -> {
					ErrorDialog.show("Error while checking if perform is possible", error);
					error.printStackTrace();
				});
	}
	
	private void handlePerform(final WorkbenchActionContext<T> workbenchActionContext) {
		getTemplateEvaluationContext(workbenchActionContext).get( //
				templateEvaluationContext -> handleEvaluateTemplate(workbenchActionContext, templateEvaluationContext), //
				e ->  {
					ErrorDialog.show("Error while preparing template evaluation", e);
					e.printStackTrace();
				});
	}

	protected void handleEvaluateTemplate(final WorkbenchActionContext<T> workbenchActionContext, TemplateEvaluationContext templateEvaluationContext) {
		boolean useForm = templateEvaluationContext.getUseFormular();

		// if (!useForm)
		// GlobalState.mask(LocalizedText.INSTANCE.performingAction());

		if (logger.isDebugEnabled()) {
			List<ModelPath> modelPaths = workbenchActionContext.getModelPaths();
			ModelPath rootModelPath = workbenchActionContext.getRootModelPath();

			logger.debug("Following model-paths provided in action context. RootModelPath: " + (rootModelPath == null ? "false" : "true")
					+ ", SelectedModelPaths: " + (modelPaths != null ? modelPaths.size() : "0"));
		}

		try {
			prepareTemplateEvaluation(templateEvaluationContext, workbenchActionContext.getPanel())
					.get(getPrepareTemplateEvaluationCallback(workbenchActionContext, useForm));

		} catch (TemplateEvaluationException e) {
			ErrorDialog.show("Error while preparing template evaluation", e);
			e.printStackTrace();
			// if (!useForm)
			// GlobalState.unmask();
		}
	}
	
	/** Prepares a new {@link TemplateEvaluationContext} based on the given parameters. */
	protected Future<TemplateEvaluationContext> getTemplateEvaluationContext(WorkbenchActionContext<T> ctx) {
		return Future.fromSupplier(() -> templPreproc(ctx, userNameProvider)) //
				.andThenMapAsync(TemplatePreprocessing::run) //
				.andThenMap(templPreproc -> {
					TemplateBasedAction action = ctx.getWorkbenchAction();
					Template template = action.getTemplate();
					boolean useForm = action.getForceFormular() || (ctx.isUseForm() && !templPreproc.getValuesSatisfiedFromModelPath());

					TemplateEvaluation te = new TemplateEvaluation();
					te.setTemplate(template);
					te.setVariableValues(templPreproc.getVariableValues());
					te.setTargetSession((PersistenceGmSession) ctx.getGmSession());
					te.setModelPaths(ctx.getModelPaths());
					te.setRootModelPath(ctx.getRootModelPath());
					te.setUserNameProvider(userNameProvider);

					TemplateEvaluationContext result = new TemplateEvaluationContext();
					result.setUseFormular(useForm);
					result.setTemplatePreprocessing(templPreproc);
					result.setTemplateEvaluation(te);
					result.setTemplate(template);
					result.setCloneToPersistenceSession(getCloneToPersistenceSession());

					return result;
				});
	}

	private static TemplatePreprocessing templPreproc(WorkbenchActionContext<? extends TemplateBasedAction> ctx, Supplier<String> userNameSupplier) {
		TemplatePreprocessing result = new TemplatePreprocessing();
		result.setModelPaths(ctx.getModelPaths());
		result.setRootModelPath(ctx.getRootModelPath());
		result.setTemplate(ctx.getWorkbenchAction().getTemplate());
		result.setUserNameProvider(userNameSupplier);
		result.setSession((PersistenceGmSession) ctx.getGmSession());
		result.setConfiguredVariableValues(ctx.getValues());

		return result;
	}

	private AsyncCallback<Object> getPrepareTemplateEvaluationCallback(WorkbenchActionContext<T> workbenchActionContext, boolean useForm) {
		return AsyncCallbacks.of(result -> {
			if (result == null)
				return;
			
			try {
				handleEvaluatedTemplate(result, workbenchActionContext) //
						.andThen(booleanResult -> {
							// if (!useForm)
							// GlobalState.unmask();

							if (useForm && gimaDialog != null) {
								if (booleanResult)
									gimaDialog.hide(false);
								else {
									if (!gimaDialog.isVisible())
										gimaDialog.show();
									gimaDialog.getEvaluatedPrototype().get(getPrepareTemplateEvaluationCallback(workbenchActionContext, useForm));
								}
							}
						}).onError(e -> {
							GlobalState.showError("Error while performing action", e);
							if (useForm && gimaDialog != null && !gimaDialog.isVisible())
								gimaDialog.show();
							// if (!useForm)
							// GlobalState.unmask();
						});
			} catch (TemplateEvaluationException e) {
				e.printStackTrace();
				ErrorDialog.show("Error while prepareTemplateEvaluation", e);
			}
		}, e -> {
			if (e instanceof TemplateEvaluationCancelledException)
				return;
			
			e.printStackTrace();
			GlobalState.showError("Error while preparing the template evaluation.", e);
//			if (!useForm)
//				GlobalState.unmask();
		});
	}
	
	public Future<Object> prepareTemplateEvaluation(TemplateEvaluationContext templateEvaluationContext, Object parentPanel) throws TemplateEvaluationException {
		if (!templateEvaluationContext.getUseFormular()) {
			Future<Object> result = templateEvaluationContext.evaluateTemplate();
			gimaDialog = null;
			return result;
		}
		
		gimaDialog = null;
		if (parentPanel instanceof TemplateGIMADialog)
			gimaDialog = (TemplateGIMADialog) parentPanel;

		String heading = getHeading(templateEvaluationContext.getTemplate());
		if (gimaDialog == null) {
			gimaDialog = templateGIMADialogProvider.get();
			if (heading != null)
				gimaDialog.setHeading(heading);
		}
		
		boolean showDialog = gimaDialog.setTemplateEvaluationContext(templateEvaluationContext, heading);
		if (showDialog) {
			
			gimaDialog.setPosition(-1000, -1000);
			gimaDialog.show();
			
			//RVE - use timer instead of Scheduler.get().scheduleDeferred - in case of PP with expanding rows need more time
			new Timer() {
				@Override
				public void run() {
					gimaDialog.updateHeight();
					gimaDialog.center();
				}
			}.schedule(50);
			
			gimaDialog.setButtonsText(getApplyText(), getApplyDescriptionText(), getCancelDescriptionText());
		}
		
		return gimaDialog.getEvaluatedPrototype();
		
		/*TemplateGIMADialog templateGIMADialog = templateGIMADialogProvider.get();
		boolean showDialog = templateGIMADialog.setTemplateEvaluationContext(templateEvaluationContext);
		if (showDialog) {
			String heading = getHeading(templateEvaluationContext.getTemplate());
			if (heading != null)
				templateGIMADialog.setHeading(heading);
			templateGIMADialog.setButtonsText(getApplyText(), getApplyDescriptionText(), getCancelDescriptionText());
			templateGIMADialog.show();
			templateGIMADialog.center();
		}
		
		return templateGIMADialog.getEvaluatedPrototype();*/
	}

}
