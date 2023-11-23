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
package com.braintribe.gwt.gme.notification.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gme.notification.client.adapter.ManipulationAdapter;
import com.braintribe.gwt.gme.notification.client.resources.NotificationBarStyle;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.notification.client.resources.NotificationTemplates;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.command.Command;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.notification.ClearNotification;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.InternalCommand;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationRegistry;
import com.braintribe.model.notification.NotificationRegistryEntry;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Render the new "global state" bar mostly on the top center.
 * 
 */
public class NotificationBar {

	private static final String HINT_ACTION_CLASS_NAME = "hintAction";
	private static final String ERROR_ACTION_CLASS_NAME = "errorAction";
	private static final String WARNING_ACTION_CLASS_NAME = "warningAction";
	private static final String SUCCESS_ACTION_CLASS_NAME = "successAction";
	private static final Logger logger = new Logger(NotificationBar.class);
	
	private static final String HINT_ACTION_DETAILS = "Details";
	private static final String HINT_ACTION_CLEAR = "Clear";
	private static final String HINT_ACTION_COMMAND = "Command";
	private static final int MAX_NUMBER_CHARS = 42;

	private static final ImageResource ICON_ACTION_CLEAR = GmViewActionResources.INSTANCE.close();

	private final ClickHandler clickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			EventTarget et = event.getNativeEvent().getEventTarget();
			if (!Element.is(et))
				return;
			
			Element e = et.cast();
			
			if (e.getClassName().endsWith(HINT_ACTION_CLASS_NAME) || e.getClassName().endsWith(ERROR_ACTION_CLASS_NAME)
					|| e.getClassName().endsWith(WARNING_ACTION_CLASS_NAME) || e.getClassName().endsWith(SUCCESS_ACTION_CLASS_NAME)
					|| e.getClassName().endsWith(NotificationResources.INSTANCE.notificationViewStyles().command())) {
				if (HINT_ACTION_DETAILS.equals(e.getId()) && currentTopMessage instanceof CommandNotification) {
					if (gmCommandRegistry == null)
						gmCommandRegistry = gmCommandRegistrySupplier.get();
					CommandNotification cm = ((CommandNotification) currentTopMessage);
					Command command = cm.getCommand();
					CommandExpert<Command> ce = gmCommandRegistry.findExpert(CommandExpert.class).<CommandExpert<Command>>forInstance(command);
					if (ce != null)
						ce.handleCommand(command);
					else
						logger.info("No expert found for: " + command);
				} else if (HINT_ACTION_CLEAR.equals(e.getId())) {
					updateWasReadAt();
					update(null);
				} else if (HINT_ACTION_COMMAND.equals(e.getId()) && currentTopMessage instanceof MessageWithCommand) {
					if (gmCommandRegistry == null)
						gmCommandRegistry = gmCommandRegistrySupplier.get();
					MessageWithCommand mwc = (MessageWithCommand) currentTopMessage;
					Command command = mwc.getCommand();
					CommandExpert<Command> ce = gmCommandRegistry.findExpert(CommandExpert.class).<CommandExpert<Command>>forInstance(command);
					if (ce != null)
						ce.handleCommand(command);
					else
						logger.info("No expert found for: " + command);
				}
			} else if (showNotificationsActionSupplier != null) {
				if (showNotificationsAction == null)
					showNotificationsAction = showNotificationsActionSupplier.get();
				showNotificationsAction.perform(null);
			}
		}

		private void updateWasReadAt() {
			if (currentRegistryEntry != null)
				currentRegistryEntry.setWasReadAt(new Date());
		}
	};

	private final Timer clearTimer = new Timer() {
		@Override
		public void run() {
			update(null);
		}
	};

	private final ManipulationListener manipulationListener = new ManipulationAdapter() {
		{
			addListener(NotificationRegistry.class, NotificationRegistry.entries, (OnCollectionAdd<NotificationRegistry, NotificationRegistryEntry>) (entity, propertyName, itemsToAdd) -> {
				for (NotificationRegistryEntry entry : itemsToAdd.values())
					handleRegistryEntry(entity.getEntries(), entry);
			});
			addListener(NotificationRegistry.class, NotificationRegistry.entries, (OnCollectionRemove<NotificationRegistry, NotificationRegistryEntry>) (entity, propertyName, itemsToRemove) -> {
				if (currentRegistryEntry != null && itemsToRemove.values().contains(currentRegistryEntry))
					update(null);
			});
			addListener(NotificationRegistryEntry.class, NotificationRegistryEntry.wasReadAt, (OnPropertyChange<NotificationRegistryEntry, Date>) (entity, propertyName, newValue) -> {
				if (currentRegistryEntry == entity && newValue != null)
					update(null);
			});
		}
	};

	private ManagedGmSession gmSession;
	private GmExpertRegistry gmCommandRegistry;
	private Supplier<? extends GmExpertRegistry> gmCommandRegistrySupplier;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private Supplier<? extends Action> showNotificationsActionSupplier;
	private Action showNotificationsAction;
	private Widget globalStateSlot;
	private HandlerRegistration handlerRegistration;
	private NotificationRegistryEntry currentRegistryEntry;
	private MessageNotification currentTopMessage;
	private int hintTimeOut = 5000;
	private MessageNotification errorMessageToRestore;

	@Required
	public void setGlobalStateLabel(Label globalStateLabel) {
		assert globalStateSlot == null && globalStateLabel != null;
		if (handlerRegistration != null)
			handlerRegistration.removeHandler();
		globalStateSlot = globalStateLabel;
		handlerRegistration = globalStateLabel.addClickHandler(clickHandler);
		if (globalStateLabel.isAttached())
			AttachEvent.fire(globalStateLabel, true);
	}

	@Required
	public void setGmSession(final ManagedGmSession gmSession) {
		this.gmSession = gmSession;
		gmSession.query().entity(NotificationRegistry.T, NotificationRegistry.INSTANCE)
				.require(AsyncCallback.of(
						singleton -> gmSession.listeners().entityProperty(singleton, NotificationRegistry.entries).add(manipulationListener),
						e -> ErrorDialog.show("Fatal error in NotificationBar!", e)));
	}

	@Required
	public void setCommandRegistry(Supplier<? extends GmExpertRegistry> gmCommandRegistrySupplier) {
		this.gmCommandRegistrySupplier = gmCommandRegistrySupplier;
	}

	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}

	@Required
	public void setShowNotificationsAction(Supplier<? extends Action> showNotificationsActionSupplier) {
		this.showNotificationsActionSupplier = showNotificationsActionSupplier;
	}

	public void setHintTimeOut(int hintTimeOut) {
		this.hintTimeOut = hintTimeOut;
	}

	private void handleRegistryEntry(List<NotificationRegistryEntry> entries, NotificationRegistryEntry entry) {
		MessageNotification lastTopMessageNotification = null;
		List<Notification> notifications = entry.getNotifications();
		if (notifications.isEmpty())
			return;
		
		for (Notification notification : notifications) {
			if (notification instanceof ClearNotification) {
				Notification notificationToClear = ((ClearNotification) notification).getNotificationToClear();
				if (notificationToClear == null || notificationToClear.equals(currentTopMessage))
					update(null); // clear the bar
				// remove this entry
				if (entries.remove(entry) == false) {
					// set as was-read for automatic cleanup
					entry.setWasReadAt(entry.getReceivedAt());
				}
				
				continue;
			}
			
			if (!(notification instanceof MessageNotification))
				continue;
			
			MessageNotification mn = (MessageNotification) notification;
			if (!mn.getConfirmationRequired()
					&& (lastTopMessageNotification == null || lastTopMessageNotification.getLevel().ordinal() > mn.getLevel().ordinal()))
				lastTopMessageNotification = mn;
		}
		
		if (lastTopMessageNotification != null) {
			update(lastTopMessageNotification);
			currentRegistryEntry = entry;
			gmSession.listeners().entity(entry).add(manipulationListener);
		}
	}
	
	public void showNotification(MessageNotification mn) {
		update(mn);
	}

	protected void update(MessageNotification mn) {
		clearTimer.cancel();
		if (currentRegistryEntry != null) {
			gmSession.listeners().entity(currentRegistryEntry).remove(manipulationListener);
			currentRegistryEntry = null;
		}
		
		//Since we are now using the NotificationBar to place masks, further messages may appear after an error.
		//When that happens, we mark the error message to be shown again once the mask message is cleared.
		if (currentTopMessage != null && Level.ERROR.equals(currentTopMessage.getLevel()) && mn != null)
			errorMessageToRestore = currentTopMessage;
		
		currentTopMessage = mn;
		Level level = mn == null ? null : mn.getLevel();
		List<NotificationAction> actions = new ArrayList<>();
		if (level != null) {
			if (mn instanceof MessageWithCommand && ((MessageWithCommand) mn).getExecuteManually()) {					
				actions.add(new NotificationAction(HINT_ACTION_COMMAND, NotificationResources.INSTANCE.notificationViewStyles().command(), null,
						((MessageWithCommand) mn).getCommand().getName()));				
			} else if (mn instanceof CommandNotification) {
				CommandNotification cm = ((CommandNotification) mn);
				Command command = cm.getCommand();
				if (command instanceof InternalCommand)
					addActions(actions, notificationFactorySupplier.get().getTransientObject((InternalCommand) command));
			}
			
			if (!mn.getManualClose()) {
				actions.add(new NotificationAction(HINT_ACTION_CLEAR, null, ICON_ACTION_CLEAR, ""));
				if (level == Level.INFO || level == Level.SUCCESS)
					clearTimer.schedule(hintTimeOut);
			}
		}

		String message = formatMessage(mn);
		SafeHtml safeMessage = message == null ? null : SafeHtmlUtils.fromSafeConstant(message);
		NotificationBarStyle style = NotificationResources.LEVEL_STYLES.get(level);	
		String textStyleClass= "";
		if (style != null) {
			if (mn.getTextBold())
				textStyleClass = textStyleClass + " " + style.boldText();
			if (mn.getTextItalic())
				textStyleClass = textStyleClass + " " + style.italicText();
			if (mn.getTextStrikeout())
				textStyleClass = textStyleClass + " " + style.strikeoutText();
			if (mn.getTextUnderline())
				textStyleClass = textStyleClass + " " + style.underlineText();
		}
		
		globalStateSlot.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderBar(style, textStyleClass, safeMessage, actions));
		
		if (mn == null && errorMessageToRestore != null) {
			update(errorMessageToRestore);
			errorMessageToRestore = null;
		}
	}

	private String formatMessage(MessageNotification mn) {
		if (mn == null)
			return null;
		
		String message = mn.getMessage();
		if (message == null)
			return null;
		
		message = SafeHtmlUtils.htmlEscape(message);
		message = message.replace("\n", "<br>");
		String[] split = message.split(" ");
		StringBuilder builder = new StringBuilder();
		for (String part : split) {
			if (part.length() > MAX_NUMBER_CHARS) {
				builder.append(part.substring(0, MAX_NUMBER_CHARS - 5));
				builder.append("(...)");
			} else
				builder.append(part);
			builder.append(' ');
		}
		
		String result = builder.toString();
		//mn.setMessage(result);
		return result;
	}

	private void addActions(List<NotificationAction> actions, Object transientObject) {
		if (transientObject instanceof Action) {
			Action action = (Action) transientObject;
			String name = action.getName();
			actions.add(new NotificationAction(name, null, null, name));
		}
	}

}
