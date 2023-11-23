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
package com.braintribe.gwt.gmview.ddsarequest.client.message;

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentSupplier;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.ddsarequest.client.LocalizedText;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.template.client.TemplateException;
import com.braintribe.model.extensiondeployment.RequestProcessing;
import com.braintribe.model.extensiondeployment.meta.DynamicConfirmation;
import com.braintribe.model.extensiondeployment.meta.DynamicMessage;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.meta.data.constraint.Message;
import com.braintribe.model.meta.data.constraint.StaticMessage;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.uiservice.ConfirmationData;
import com.braintribe.model.uiservice.GetMessageData;
import com.braintribe.model.uiservice.MessageData;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Expert which handles the {@link Message} metadata.
 * @author michel.docouto
 *
 */
public class MessageExpert {
	protected static final Logger logger = new Logger(MessageExpert.class);
	
	private static Supplier<? extends MessageDialog> messageDialogSupplier;
	private static MessageDialog messageDialog;
	private static boolean usingMessageDialog;
	private static String defaultMessage = LocalizedText.INSTANCE.executingServiceRequest();
	
	public static void setMessageDialogSupplier(Supplier<? extends MessageDialog> supplier) {
		messageDialogSupplier = supplier;
	}
	
	/**
	 * Shows the given message.
	 */
	public static void showMessage(String message) {
		if (messageDialog == null)
			messageDialog = messageDialogSupplier.get();
		
		messageDialog.setIcon(null);
		messageDialog.setMessage(message);
		messageDialog.show();
	}
	
	/**
	 * Shows a message while executing the request.
	 */
	public static void showMessage(RequestExecutionData data) {
		ServiceRequest serviceRequest = data.serviceRequest;
		
		PersistenceGmSession theSession = data.transientSession;
		if (serviceRequest.session() instanceof PersistenceGmSession)
			theSession = (PersistenceGmSession) serviceRequest.session();
		
		ModelAccessory modelAccessory = theSession.getModelAccessory();
		Message message = null;
		if (modelAccessory != null)
			message = modelAccessory.getMetaData().entity(serviceRequest).meta(Message.T).exclusive();
		if (message == null)
			message = getMessageFromTemplate(data.template);
		
		Resource resource = null;
		if (message != null && message.getIcon() != null)
			resource = GMEIconUtil.getLargestImageFromIcon(((StaticMessage) message).getIcon());
		
		String theMessage = null;
		if (message instanceof DynamicMessage)
			handleDynamicMessage((DynamicMessage) message, data);
		else if (message instanceof StaticMessage) {
			theMessage = I18nTools.getLocalized(((StaticMessage) message).getMessage());
			theMessage = resolveTemplateMessage(data.navigationListener, theMessage);
		}
		
		if (theMessage == null)
			theMessage = defaultMessage;
		
		if (resource == null) {
			usingMessageDialog = false;
			GlobalState.mask(theMessage);
			return;
		}
		
		usingMessageDialog = true;
		
		if (messageDialog == null)
			messageDialog = messageDialogSupplier.get();
		
		messageDialog.setIcon(resource);
		messageDialog.setMessage(theMessage);
		messageDialog.show();
	}
	
	public static void hideMessage() {
		if (usingMessageDialog && messageDialog != null)
			messageDialog.hide();
		else
			GlobalState.unmask();
	}
	
	private static void handleDynamicMessage(DynamicMessage message, RequestExecutionData data) {
		RequestProcessing requestProcessing = message.getRequestProcessing();
		
		GetMessageData request = GetMessageData.T.create();
		request.setDomainId(GMEUtil.getDomainId(requestProcessing, data.dataSession));
		request.setServiceId(GMEUtil.getServiceId(requestProcessing));
		request.setSnapshot(GMEUtil.makeShallowScalarCopy(data.serviceRequest));
		
		RequestExecutionData red = new RequestExecutionData(request, data.dataSession, data.transientSession, null, data.transientSessionProvider,
				data.notificationFactorySupplier);
		
		Future<MessageData> requestFuture = DdsaRequestExecution.executeRequest(red);
		requestFuture //
				.andThen(result -> {
					if (result == null)
						return;

					String theMessage = result.getMessage();
					theMessage = resolveTemplateMessage(data.navigationListener, theMessage);
					messageDialog.setMessage(theMessage);
					if (result.getIcon() != null)
						messageDialog.setIcon(result.getIcon());
				}).onError(e -> logger.error("Error while getting the message data", e));
	}
	
	private static Message getMessageFromTemplate(Template template) {
		if (template == null)
			return null;
		
		return GMEMetadataUtil.getTemplateMetaData(template, Message.T, null);
	}
	
	private static String resolveTemplateMessage(ModelPathNavigationListener navigationListener, String theMessage) {
		ModelPath modelPath = null;
		if (navigationListener instanceof GmContentSupplier)
			modelPath = ((GmContentSupplier) navigationListener).getContent();
		else if (navigationListener instanceof GmContentView)
			modelPath = ((GmContentView) navigationListener).getFirstSelectedItem();
		
		if (modelPath == null)
			return theMessage;
		
		Object value = modelPath.last().getValue();
		if (!(value instanceof GenericEntity))
			return theMessage;
		
		GenericEntity selectedEntity = (GenericEntity) value;
		EntityMdResolver entityMdResolver = getMetaData(selectedEntity).entity(selectedEntity);
		try {
			theMessage = SelectiveInformationResolver.resolveTemplateString(theMessage, selectedEntity, entityMdResolver);
		} catch (TemplateException e) {
			logger.error("Error while resolving the message template.", e);
			e.printStackTrace();
		}
		
		return theMessage;
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
