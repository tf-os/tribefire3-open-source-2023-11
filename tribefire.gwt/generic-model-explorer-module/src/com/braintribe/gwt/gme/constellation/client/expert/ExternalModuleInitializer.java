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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.MultiLoader;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.TransientGmSession;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.logging.client.Profiling;
import com.braintribe.gwt.logging.client.ProfilingHandle;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;

/**
 * This expert will initialized the sessions.
 * @author michel.docouto
 *
 */
public class ExternalModuleInitializer implements InitializableBean {
	private static final GenericModelTypeReflection typeReflection =  GMF.getTypeReflection();
	private static Logger logger = new Logger(ExternalModuleInitializer.class);
	
	private String accessId;
	private Function<String, Future<ModelEnvironment>> modelEnvironmentProvider;
	private boolean useItwAsync = false;
	private TransientGmSession transientSession;
	private ModelEnvironmentDrivenGmSession gmSession;
	private ModelEnvironmentDrivenGmSession workbenchSession;
	private ModelEnvironmentDrivenSessionUpdater modelEnvironmentDrivenSessionUpdater;
	
	/**
	 * Configures the provider used for loading the model environment.
	 */
	@Required
	public void setModelEnvironmentProvider(Function<String, Future<ModelEnvironment>> modelEnvironmentProvider) {
		this.modelEnvironmentProvider = modelEnvironmentProvider;
	}
	
	/**
	 * Configures the session that will be configured with the transient model, if available.
	 */
	@Required
	public void setTransientSession(TransientGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required {@link ModelEnvironmentDrivenGmSession}.
	 */
	@Required
	public void setPersistenceSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the session to be used within the Workbench.
	 */
	@Required
	public void setWorkbenchSession(ModelEnvironmentDrivenGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	/**
	 * Configures the initial accessId.
	 */
	@Configurable
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	/**
	 * Configures whether we should use ITW asynchronously. Defaults to false.
	 */
	@Configurable
	public void setUseItwAsync(boolean useItwAsync) {
		this.useItwAsync = useItwAsync;
	}
	
	/**
	 * Configures the {@link ModelEnvironmentDrivenSessionUpdater} used for configuring external sessions with ModelEnvironment once the
	 * {@link ModelEnvironment} is changed.
	 */
	@Configurable
	public void setModelEnvironmentDrivenSessionUpdater(ModelEnvironmentDrivenSessionUpdater modelEnvironmentDrivenSessionUpdater) {
		this.modelEnvironmentDrivenSessionUpdater = modelEnvironmentDrivenSessionUpdater;
	}
	
	@Override
	public void intializeBean() throws Exception {
		if (accessId != null)
			initializeSession();
		else
			modelEnvironmentProvider.apply(accessId); //Needed for triggering the bootstrapping load
	}
	
	private void initializeSession() {
		/*if (accessId != null) {
			if (uiThemeLoader != null)
				uiThemeLoader.loadUiThemeCss(accessId, applicationId);
			if (favIconLoader != null)
				favIconLoader.loadFavIcon(accessId, applicationId);			
			if (titleLoader != null)
				titleLoader.loadTitle(accessId, applicationId);
			if (jsScriptLoader != null)
				jsScriptLoader.loadScript();								
		}*/
		
		ProfilingHandle ph = Profiling.start(ExternalModuleInitializer.class, "Getting Model Environment (async)", true);
		
		modelEnvironmentProvider.apply(accessId) //
				.andThen(modelEnvironment -> {
					ph.stop();
					ExternalModuleInitializer.this.accessId = modelEnvironment.getDataAccessId();
					ProfilingHandle ensuringPH = Profiling.start(ExternalModuleInitializer.class, "Ensuring Model Types (async)", true, true);
					ensureModelTypes(modelEnvironment) //
							.andThen(v -> {
								ensuringPH.stop();
								handleSessionInitialized(ph, modelEnvironment);
							}).onError(e -> {
								ensuringPH.stop();
								//initializationProfiling.stop();
								//MaskController.maskScreenOpaque = false;
								//progressUnmask();
								ErrorDialog.show(LocalizedText.INSTANCE.errorEnsuringModelTypes(), e);
								e.printStackTrace();
							});
				}).onError(e -> {
					ph.stop();
					//initializationProfiling.stop();
					//MaskController.maskScreenOpaque = false;
					//progressUnmask();
					ErrorDialog.show(LocalizedText.INSTANCE.errorGettingModelEnvironment(), e, true);
					e.printStackTrace();
				});
	}
	
	private Future<Void> ensureModelTypes(final ModelEnvironment modelEnvironment) {
		GmMetaModel serviceModel = modelEnvironment.getServiceModel();
		if (useItwAsync) {
			final Future<Void> future = new Future<>();
			MultiLoader itwMultiLoader = new MultiLoader();
			if (modelEnvironment.getDataModel() != null)
				itwMultiLoader.add("dataModel", ensureModel(modelEnvironment.getDataModel()));
			if (modelEnvironment.getWorkbenchModel() != null)
				itwMultiLoader.add("workbenchModel", ensureModel(modelEnvironment.getWorkbenchModel()));
			if (serviceModel != null)
				itwMultiLoader.add("transientModel", ensureModel(serviceModel));

			itwMultiLoader.load(AsyncCallbacks.of( //
					result -> {
						prepareTransientSession(serviceModel);
						future.onSuccess(null);
					}, future::onFailure));
			
			return future;
		}
		
		try {
			if (modelEnvironment.getDataModel() != null) {
				ProfilingHandle ph = Profiling.start(ExternalModuleInitializer.class, "Ensuring Data Model", false);
				typeReflection.deploy(modelEnvironment.getDataModel());
				ph.stop();
			}
			
			if (modelEnvironment.getWorkbenchModel() != null) {
				ProfilingHandle ph = Profiling.start(ExternalModuleInitializer.class, "Ensuring Workbench Model", false);
				typeReflection.deploy(modelEnvironment.getWorkbenchModel());
				ph.stop();
			}
			
			if (serviceModel == null)
				prepareTransientSession(null);
			else {
				ProfilingHandle ph = Profiling.start(ExternalModuleInitializer.class, "Ensuring Service Model", false);
				typeReflection.deploy(serviceModel);
				ph.stop();
				prepareTransientSession(serviceModel);
			}
		} catch (GmfException ex) {
			logger.error("Error while ensuring model types.", ex);
			ex.printStackTrace();
		}
		
		return new Future<Void>(null);
	}
	
	private Future<Void> ensureModel(GmMetaModel model) {
		Future<Void> future = new Future<>();
		typeReflection.deploy(model, future);
		return future;
	}
	
	private void prepareTransientSession(GmMetaModel transientModel) {
		Transaction transaction = transientSession.getTransaction();
		List<Manipulation> manipulations = transaction.getManipulationsDone();
		if (manipulations != null && !manipulations.isEmpty())
			transaction.undo(manipulations.size());
		
		transientSession.cleanup();
		GmMetaModel currentTransientGmMetaModel = transientSession.getTransientGmMetaModel();
		if (currentTransientGmMetaModel != transientModel)
			transientSession.configureGmMetaModel(transientModel);
	}
	
	private void handleSessionInitialized(final ProfilingHandle ph, final ModelEnvironment modelEnvironment) {
		ProfilingHandle ph1 = Profiling.start(ExternalModuleInitializer.class, "Configuring Model Environment within the session (async)", true,
				true);
		
		gmSession.configureModelEnvironment(modelEnvironment, com.braintribe.processing.async.api.AsyncCallback.of( //
				v -> {
					/*if (!isModelVisible()) {
						MessageBox messageBox = new AlertMessageBox(LocalizedText.INSTANCE.information(), LocalizedText.INSTANCE.notAllowedAccess());
						messageBox.addDialogHideHandler(event -> Window.Location.replace(loginServletUrl));
						messageBox.show();
						return;
					}*/

					//if (appendAccessToTitle)
						//appendAccessToTitle(accessId);
					ph1.stop();
					prepareWorkbenchSession(modelEnvironment);
					if (modelEnvironmentDrivenSessionUpdater != null)
						modelEnvironmentDrivenSessionUpdater.updateModelEnvironment(modelEnvironment);
					//handleModelEnvironmentSet();
				}, e -> {
					//initializationProfiling.stop();
					ph.stop();
					ph1.stop();
					//MaskController.maskScreenOpaque = false;
					//progressUnmask();
					ErrorDialog.show("Error while configuring the ModelEnvironment within the session.", e);
					e.printStackTrace();
				}));
	}
	
	private void prepareWorkbenchSession(ModelEnvironment sessionModelEnvironment) {
		if (sessionModelEnvironment == null)
			return;
		
		ModelEnvironment modelEnvironment = ModelEnvironment.T.create();
		modelEnvironment.setDataModel(sessionModelEnvironment.getWorkbenchModel());
		modelEnvironment.setDataAccessId(sessionModelEnvironment.getWorkbenchModelAccessId());
		modelEnvironment.setMetaModelAccessId(sessionModelEnvironment.getMetaModelAccessId());
		
		workbenchSession.configureModelEnvironment(modelEnvironment);
	}

}
