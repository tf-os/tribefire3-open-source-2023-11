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

import java.util.Collections;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.notification.ClearNotification;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationBarEventSource;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.Component;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(namespace = InteropConstants.JS_UTIL_NAMESPACE)
public class GlobalState {

	public static final String MASK_DETAILS = "Mask.Details";
	private static Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private static String detailsText;
	private static Component maskComponent;
	private static Notification maskNotification;
	private static boolean maskingWithoutMessage;

	@Required
	@JsIgnore
	public static void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		GlobalState.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configures the component which will be masked when mask is in place.
	 * If Nothing is set, we assume there is also no global notification in place, thus masking displays the message itself.
	 */
	@Configurable
	@JsIgnore
	public static void setMaskComponent(Component maskComponent) {
		GlobalState.maskComponent = maskComponent;
	}

	@Configurable
	@JsIgnore
	public static void setDetailsText(String detailsText) {
		GlobalState.detailsText = detailsText;
	}
	
	public static void showError(String errorMessage) {
		broadcast(prepareMessageWithCommand(errorMessage, null, null));
	}

	@JsIgnore
	public static void showError(String message, final Throwable caught) {
		if (ErrorDialog.handleIfExceptionFilter(caught))
			return;
		
		if (notificationFactorySupplier == null)
			return;
		
		StringBuilder errorMessageBuilder = new StringBuilder();
		errorMessageBuilder.append(message);
		
		/*
		 * SPR: Requested to be removed by RKO for AD-2535
		String lastCauseMessage = ExceptionUtil.getLastMessage(caught);
		if (lastCauseMessage != null)
			errorMessageBuilder.append('\n').append(lastCauseMessage);
		*/
		
		broadcast(prepareMessageWithCommand(errorMessageBuilder.toString(), null, caught));
	}
	
	@JsIgnore
	public static MessageWithCommand prepareMessageWithCommand(final String errorMessage, final String details, final Throwable caught) {
		Action action = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				if (caught != null)
					ErrorDialog.show(errorMessage, caught, true);
				else
					ErrorDialog.show(errorMessage, details);
			}
		};
		action.setName(detailsText == null ? "Details" : detailsText);
		
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		MessageWithCommand mwc = notificationFactory.createNotification(MessageWithCommand.T, Level.ERROR, errorMessage);
		mwc.setDetails(caught != null ? caught.toString() : details);
		mwc.setCommand(notificationFactory.createTransientCommand(action.getName(), action));
		mwc.setExecuteManually(true);
		
		return mwc;
	}
	
	
	public static void showWarning(String message) {
		showWarning(message, (Action[]) null);
	}
	
	@JsIgnore
	public static void showWarning(String message, Action... actions) {
		showWarning(message, true, actions);
	}

	@JsIgnore
	public static void showWarning(String message, boolean closable, Action... actions) {
		showWarning(message, closable, false, false, false, false, actions);      		
	}	
	
	@JsIgnore
	public static void showWarning(String message, boolean closable, boolean useBold, boolean useItalic, boolean useUnderline, boolean useStrikeout, Action... actions) {
		if (notificationFactorySupplier == null)
			return;
				
		Notification n;
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		if (actions != null && actions.length > 0) {
			MessageWithCommand mwc = notificationFactory.createNotification(MessageWithCommand.T, Level.WARNING, message);
			mwc.setCommand(notificationFactory.createTransientCommand(actions[0].getName(), actions[0]));
			mwc.setExecuteManually(true);
			mwc.setManualClose(!closable);
			mwc.setTextBold(useBold);
			mwc.setTextItalic(useItalic);
			mwc.setTextStrikeout(useStrikeout);
			mwc.setTextUnderline(useUnderline);
			
			n = mwc;
		} else {
			MessageNotification mn = notificationFactory != null ? notificationFactory.createNotification(MessageNotification.T, Level.WARNING, message) : null;
			if (mn != null) {
				mn.setManualClose(!closable);
				mn.setTextBold(useBold);
				mn.setTextItalic(useItalic);
				mn.setTextStrikeout(useStrikeout);
				mn.setTextUnderline(useUnderline);
}
			n = mn;
		}
			
		if (n != null)
			broadcast(n);
	}

	public static void showSuccess(String message) {
		showSuccess(message, (Action[]) null);
	}
	
	@JsIgnore
	public static Notification showSuccess(String message, Action...actions) {
		return showSuccess(message, false, actions);
	}

	@JsIgnore
	public static Notification showSuccess(String message, boolean manualClose, Action...actions) {
	  	return showNotification(Level.SUCCESS, message, null, manualClose, actions);
	}
	
	public static void showInfo(String message) {
		showInfo(message, (Action[]) null);
	}

	@JsIgnore
	public static Notification showInfo(String message, Action...actions) {
		return showInfo(message, false, actions);
	}
	
	@JsIgnore
	public static Notification showInfo(String message, boolean manualClose, Action...actions) {
	  	return showNotification(Level.INFO, message, null, manualClose, actions);
	}
	
	@JsIgnore
	public static Notification showInfo(String message, String details, boolean manualClose, Action...actions) {
	  	return showNotification(Level.INFO, message, details, manualClose, actions);
	}
		
	@JsIgnore
	private static Notification showNotification(Level level, String message, String details, boolean manualClose, Action...actions) {
		if (notificationFactorySupplier == null)
			return null;
		
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		MessageNotification n;
		
		if (actions == null || actions.length == 0) {
			MessageNotification messageNotification = notificationFactory.createNotification(MessageNotification.T, level, message);
			messageNotification.setManualClose(manualClose);
			n = messageNotification;
		} else {
			MessageWithCommand mwc = notificationFactory.createNotification(MessageWithCommand.T, level, message);
			mwc.setCommand(notificationFactory.createTransientCommand(actions[0].getName(), actions[0]));
			mwc.setExecuteManually(true);
			mwc.setManualClose(manualClose);
			n = mwc;
		}
		
		if (details != null)
			n.setDetails(details);
		
		broadcast(n);
		return n;
	}
	
	/**
	 * Masks the UI by masking the whole UI.
	 */
	public static void mask() {
		mask(null);
	}
	
	/**
	 * Masks the UI by placing the message in the notification and masking the UI.
	 */
	@JsMethod (name="maskWithMessage")
	public static void mask(String message) {
		if (maskNotification != null) {
			clearState(maskNotification);
			maskNotification = null;
		}
		
		if (message != null) {
			if (maskComponent != null) {
				maskComponent.mask();
				maskNotification = showInfo(message, MASK_DETAILS, true, (Action[]) null);
				maskingWithoutMessage = false;
			} else {
				XElement.as(RootPanel.get().getElement()).mask(message);
				maskingWithoutMessage = true;
			}
		} else {
			XElement.as(RootPanel.get().getElement()).mask(null);
			maskingWithoutMessage = true;
		}
	}
		
	/**
	 * Unmasks the UI by removing the notification and unmasking the UI.
	 */
	public static void unmask() {
		if (maskingWithoutMessage) {
			XElement.as(RootPanel.get().getElement()).unmask();
			return;
		}
		
		maskComponent.unmask();
		if (maskNotification != null) {
			clearState(maskNotification);
			maskNotification = null;
		}
	}

	public static void clearState() {
		clearState(null);
	}
	
	private static void clearState(Notification notificationToClear) {
		if (notificationFactorySupplier != null) {
			ClearNotification clearNotification = notificationFactorySupplier.get().createNotification(ClearNotification.T);
			if (notificationToClear != null)
				clearNotification.setNotificationToClear(notificationToClear);
			broadcast(clearNotification);
		}
	}

	public static void showProcess(String message) {
		if (notificationFactorySupplier == null)
			return;
		
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		Notification notification = notificationFactory.createNotification(MessageNotification.T, Level.INFO, message);
		NotificationEventSource eventSource = notificationFactory.createEventSource(NotificationBarEventSource.T);
		notificationFactory.broadcast(Collections.singletonList(notification), eventSource);
	}

	private static void broadcast(Notification notification) {
		if (notificationFactorySupplier == null)
			return;
		
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		NotificationEventSource eventSource = notificationFactory.createEventSource(NotificationEventSource.T);
		notificationFactory.broadcast(Collections.singletonList(notification), eventSource);
	}

}
