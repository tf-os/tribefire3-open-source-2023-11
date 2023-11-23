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
package com.braintribe.gwt.gmview.ddsarequest.client.confirmation;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.extensiondeployment.meta.ConfirmationMouseClick;
import com.braintribe.model.extensiondeployment.meta.DynamicConfirmation;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.data.constraint.Confirmation;
import com.braintribe.model.meta.data.constraint.StaticConfirmation;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.uiservice.ConfirmationData;
import com.braintribe.model.uiservice.GetConfirmationData;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;

/**
 * Expert which handles the {@link Confirmation} metadata.
 * @author michel.docouto
 *
 */
public class ConfirmationExpert {
	
	private static Supplier<? extends Function<StaticConfirmation, Future<Boolean>>> staticConfirmationDialogSupplier;
	private static Supplier<? extends Function<DynamicConfirmationData, Future<Boolean>>> dynamicConfirmationDialogSupplier;
	
	public static void setStaticConfirmationDialogSupplier(Supplier<? extends Function<StaticConfirmation, Future<Boolean>>> supplier) {
		staticConfirmationDialogSupplier = supplier;
	}
	
	public static void setDynamicConfirmationDialogSupplier(Supplier<? extends Function<DynamicConfirmationData, Future<Boolean>>> supplier) {
		dynamicConfirmationDialogSupplier = supplier;
	}
	
	/**
	 * Checks whether the given data for {@link ServiceRequest} execution can be run.
	 */
	public static Future<Boolean> checkConfirmation(RequestExecutionData data) {
		ServiceRequest serviceRequest = data.serviceRequest;
		
		PersistenceGmSession theSession = data.transientSession;
		if (serviceRequest.session() instanceof PersistenceGmSession)
			theSession = (PersistenceGmSession) serviceRequest.session();
		
		boolean useDataSessionForIcon = false;
		
		ModelAccessory modelAccessory = theSession.getModelAccessory();
		Confirmation confirmation = null;
		if (modelAccessory != null) {
			confirmation = modelAccessory.getMetaData().entity(serviceRequest).meta(Confirmation.T).exclusive();
			useDataSessionForIcon = true;
		}
		if (confirmation == null)
			confirmation = getConfirmationFromTemplate(data.template);

		if (confirmation instanceof StaticConfirmation)
			return handleStaticConfirmation((StaticConfirmation) confirmation, data, useDataSessionForIcon);
		else if (confirmation instanceof DynamicConfirmation)
			return handleDynamicConfirmation((DynamicConfirmation) confirmation, data, useDataSessionForIcon);

		return new Future<>(true);
	}
	
	private static Future<Boolean> handleStaticConfirmation(StaticConfirmation confirmation, RequestExecutionData data, boolean useDataSessionForIcon) {
		if (staticConfirmationDialogSupplier != null) {
			Function<StaticConfirmation, Future<Boolean>> functionConfirmation = staticConfirmationDialogSupplier.get();		
			if (functionConfirmation instanceof ResourceSessionConfig)
				if (useDataSessionForIcon && data != null && data.dataSession != null && data.dataSession.getModelAccessory() != null && data.dataSession.getModelAccessory().getModelSession() != null)
					((ResourceSessionConfig) functionConfirmation).setResourceSession(data.dataSession.getModelAccessory().getModelSession());						
			return functionConfirmation.apply(confirmation);
		}
		
		String okDisplay = null;
		LocalizedString ok = confirmation.getOKDisplay();
		if (ok != null)
			okDisplay = I18nTools.getLocalized(ok);
		
		String cancelDisplay = null;
		LocalizedString cancel = confirmation.getCancelDisplay();
		if (cancel != null)
			cancelDisplay = I18nTools.getLocalized(cancel);
		
		ImageResource imageResource = null;
		if (confirmation.getIcon() != null) {
			if (useDataSessionForIcon && data != null && data.dataSession != null && data.dataSession.getModelAccessory() != null && data.dataSession.getModelAccessory().getModelSession() != null)
				imageResource = GMEIconUtil.transform(GMEIconUtil.getMediumImageFromIcon(confirmation.getIcon()), data.dataSession.getModelAccessory().getModelSession());
			else	
				imageResource = GMEIconUtil.transform(GMEIconUtil.getMediumImageFromIcon(confirmation.getIcon()));
		}
		
		return prepareMessageBox(I18nTools.getLocalized(confirmation.getMessage()), okDisplay, cancelDisplay, imageResource,
				ConfirmationMouseClick.none);
	}
	
	private static Future<Boolean> prepareMessageBox(String message, String okDisplay, String cancelDisplay, ImageResource imageResource,
			ConfirmationMouseClick mouseClick) {
		Future<Boolean> future = new Future<>();
		
		ConfirmationMessageBox confirmationMessageBox = new ConfirmationMessageBox(message, okDisplay, cancelDisplay, mouseClick);
		if (imageResource != null)
			confirmationMessageBox.setIcon(imageResource);

		confirmationMessageBox.setButtonAlign(BoxLayoutPack.END);
		confirmationMessageBox.setPredefinedButtons(PredefinedButton.CANCEL, PredefinedButton.OK);
		confirmationMessageBox.addDialogHideHandler(event -> future.onSuccess(event.getHideButton().equals(PredefinedButton.OK)));
		
		confirmationMessageBox.show();

		return future;
	}
	
	private static Future<Boolean> handleDynamicConfirmation(DynamicConfirmation confirmation, RequestExecutionData data, boolean useDataSessionForIcon) {
		Future<Boolean> future = new Future<>();
		RequestProcessing requestProcessing = confirmation.getRequestProcessing();
		
		GetConfirmationData request = GetConfirmationData.T.create();
		request.setDomainId(GMEUtil.getDomainId(requestProcessing, data.dataSession));
		request.setServiceId(GMEUtil.getServiceId(requestProcessing));
		request.setSnapshot(GMEUtil.makeShallowScalarCopy(data.serviceRequest));
		
		RequestExecutionData red = new RequestExecutionData(request, data.dataSession, data.transientSession, null, data.transientSessionProvider,
				data.notificationFactorySupplier);
		
		Future<ConfirmationData> requestFuture = DdsaRequestExecution.executeRequest(red);
		requestFuture.andThen(result -> {
			if (dynamicConfirmationDialogSupplier != null) {
				Function<DynamicConfirmationData, Future<Boolean>> functionConfirmation = dynamicConfirmationDialogSupplier.get();				
				if (functionConfirmation instanceof ResourceSessionConfig)
					if (useDataSessionForIcon && data.dataSession != null && data.dataSession.getModelAccessory() != null && data.dataSession.getModelAccessory().getModelSession() != null)
						((ResourceSessionConfig) functionConfirmation).setResourceSession(data.dataSession.getModelAccessory().getModelSession());
				functionConfirmation.apply(new DynamicConfirmationData(confirmation, result)).get(future);
				return;
			}
			
			String okDisplay = null;
			LocalizedString ok = confirmation.getOKDisplay();
			if (ok != null)
				okDisplay = I18nTools.getLocalized(ok);
			
			String cancelDisplay = null;
			LocalizedString cancel = confirmation.getCancelDisplay();
			if (cancel != null)
				cancelDisplay = I18nTools.getLocalized(cancel);
			
			ImageResource imageResource = null;
			Resource icon = result.getIcon();
			if (icon == null)
				icon = GMEIconUtil.getLargestImageFromIcon(confirmation.getIcon());
			if (icon != null) {
				if (useDataSessionForIcon && data.dataSession != null && data.dataSession.getModelAccessory() != null && data.dataSession.getModelAccessory().getModelSession() != null)
					imageResource = GMEIconUtil.transform(icon, data.dataSession.getModelAccessory().getModelSession());
				else
					imageResource = GMEIconUtil.transform(icon);
			}
			
			prepareMessageBox(result.getMessage(), okDisplay, cancelDisplay, imageResource, confirmation.getMouseClick()).andThen(future::onSuccess);
		}).onError(future::onFailure);
		
		return future;
	}
	
	private static Confirmation getConfirmationFromTemplate(Template template) {
		if (template == null)
			return null;
		
		return GMEMetadataUtil.getTemplateMetaData(template, Confirmation.T, null);
	}
	
	public static class DynamicConfirmationData {
		public DynamicConfirmation dynamicConfirmation;
		public ConfirmationData confirmationData;
		
		public DynamicConfirmationData(DynamicConfirmation dynamicConfirmation, ConfirmationData confirmationData) {
			this.dynamicConfirmation = dynamicConfirmation;
			this.confirmationData = confirmationData;
		}
	}

}
