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
package com.braintribe.model.processing.notification.api.builder.impl;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.command.Command;
import com.braintribe.model.command.CompoundCommand;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmPropertyPathElement;
import com.braintribe.model.path.GmRootPathElement;
import com.braintribe.model.processing.notification.api.builder.CommandBuilder;
import com.braintribe.model.processing.notification.api.builder.MessageBuilder;
import com.braintribe.model.processing.notification.api.builder.ModelPathBuilder;
import com.braintribe.model.processing.notification.api.builder.NotificationBuilder;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.notification.api.builder.PatternFormatter;
import com.braintribe.model.processing.notification.api.builder.UrlBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.Query;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.uicommand.ApplyManipulation;
import com.braintribe.model.uicommand.DownloadResource;
import com.braintribe.model.uicommand.GotoModelPath;
import com.braintribe.model.uicommand.GotoUrl;
import com.braintribe.model.uicommand.PrintResource;
import com.braintribe.model.uicommand.Refresh;
import com.braintribe.model.uicommand.RefreshPreview;
import com.braintribe.model.uicommand.Reload;
import com.braintribe.model.uicommand.ReloadView;
import com.braintribe.model.uicommand.RunQuery;
import com.braintribe.model.uicommand.RunQueryString;
import com.braintribe.model.uicommand.RunWorkbenchAction;
import com.braintribe.utils.template.Template;

public class BasicNotificationBuilder implements com.braintribe.model.processing.notification.api.builder.NotificationBuilder {

	private Consumer<Notification> notificationReceiver;
	private Function<EntityType<? extends GenericEntity>, GenericEntity> entityFactory;

	private String message;
	private String detailMessage;
	private Level level = Level.INFO;
	private Set<String> context;
	private boolean confirmationRequired = false;
	private List<Command> commands = new ArrayList<>();
	private NotificationsBuilder notifications;

	public BasicNotificationBuilder(NotificationsBuilder notifications, Function<EntityType<? extends GenericEntity>, GenericEntity> entityFactory,
			Consumer<Notification> notificationReceiver) {
		this.entityFactory = entityFactory;
		this.notificationReceiver = notificationReceiver;
		this.notifications = notifications;
	}

	@Override
	public MessageBuilder message() {
		return new BasicMessageBuilder(this);
	}

	@Override
	public CommandBuilder command() {
		return new BasicCommandBuilder(this);
	}

	/* Internal builder classes */

	public class BasicMessageBuilder implements MessageBuilder {

		private NotificationBuilder notificationBuilder;

		public BasicMessageBuilder(NotificationBuilder notificationBuilder) {
			this.notificationBuilder = notificationBuilder;
		}

		@Override
		public MessageBuilder info() {
			level = Level.INFO;
			return this;
		}
		@Override
		public MessageBuilder warn() {
			level = Level.WARNING;
			return this;
		}
		@Override
		public MessageBuilder error() {
			level = Level.ERROR;
			return this;
		}
		@Override
		public MessageBuilder success() {
			level = Level.SUCCESS;
			return this;
		}

		@Override
		public MessageBuilder level(Level param) {
			level = param;
			return this;
		}
		
		@Override
		public MessageBuilder message(String m) {
			message = m;
			return this;
		}

		@Override
		public MessageBuilder details(Throwable t) {
			details(BuilderTools.createDetailedMessage(t));
			return this;
		}

		@Override
		public MessageBuilder details(String details) {
			detailMessage = details;
			return this;
		}

		@Override
		public NotificationBuilder info(String message) {
			info();
			message(message);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder info(String message, Throwable t) {
			info(message);
			details(t);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder info(String message, String details) {
			info(message);
			details(details);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder confirmInfo(String message) {
			confirmationRequired();
			return info(message);
		}

		@Override
		public NotificationBuilder confirmInfo(String message, String details) {
			confirmationRequired();
			return info(message, details);
		}

		@Override
		public NotificationBuilder confirmInfo(String message, Throwable t) {
			confirmationRequired();
			return info(message, t);
		}

		@Override
		public NotificationBuilder warn(String message) {
			warn();
			message(message);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder warn(String message, Throwable t) {
			warn(message);
			details(t);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder warn(String message, String details) {
			warn(message);
			details(details);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder confirmWarn(String message) {
			confirmationRequired();
			return warn(message);
		}

		@Override
		public NotificationBuilder confirmWarn(String message, String details) {
			confirmationRequired();
			return warn(message, details);
		}

		@Override
		public NotificationBuilder confirmWarn(String message, Throwable t) {
			confirmationRequired();
			return warn(message, t);
		}

		@Override
		public NotificationBuilder error(String message) {
			error();
			message(message);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder error(String message, Throwable t) {
			error(message);
			details(t);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder error(String message, String details) {
			error(message);
			details(details);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder confirmError(String message) {
			confirmationRequired();
			return error(message);
		}

		@Override
		public NotificationBuilder confirmError(String message, String details) {
			confirmationRequired();
			return error(message, details);
		}

		@Override
		public NotificationBuilder confirmError(String message, Throwable t) {
			confirmationRequired();
			return error(message, t);
		}

		@Override
		public NotificationBuilder success(String message) {
			success();
			message(message);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder success(String message, Throwable t) {
			success(message);
			details(t);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder success(String message, String details) {
			success(message);
			details(details);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder confirmSuccess(String message) {
			confirmationRequired();
			return success(message);
		}

		@Override
		public NotificationBuilder confirmSuccess(String message, String details) {
			confirmationRequired();
			return success(message, details);
		}

		@Override
		public NotificationBuilder confirmSuccess(String message, Throwable t) {
			confirmationRequired();
			return success(message, t);
		}

		@Override
		public MessageBuilder confirmationRequired() {
			confirmationRequired = true;
			return this;
		}

		@Override
		public PatternFormatter pattern(String messagePattern) {
			return new BasicPatternFormatter(messagePattern, this);
		}

		@Override
		public NotificationBuilder close() {
			return notificationBuilder;
		}

	}

	public class BasicPatternFormatter implements PatternFormatter {

		private MessageBuilder messageBuilder;
		private String messagePattern;

		private Map<String, Object> variables = new HashMap<>();

		public BasicPatternFormatter(String messagePattern, MessageBuilder messageBuilder) {
			this.messagePattern = messagePattern;
			this.messageBuilder = messageBuilder;
		}

		@Override
		public PatternFormatter var(String key, Object value) {
			this.variables.put(key, value);
			return this;
		}

		@Override
		public MessageBuilder format() {
			String formattedMessage = Template.merge(this.messagePattern, this.variables);
			this.messageBuilder.message(formattedMessage);
			return this.messageBuilder;
		}
	}

	public class BasicCommandBuilder implements CommandBuilder {
		private NotificationBuilder notificationBuilder;
		private Consumer<Command> commandReceiver;

		public BasicCommandBuilder(NotificationBuilder notificationBuilder) {
			this.notificationBuilder = notificationBuilder;
			this.commandReceiver = commands::add;
		}

		@Override
		public ModelPathBuilder gotoModelPath(final String name) {

			return new BasicModelPathBuilder(notificationBuilder, new Consumer<GotoModelPathInfo>() {
				@Override
				public void accept(GotoModelPathInfo modelPathInfo) {
					GotoModelPath gotoModelPath = BuilderTools.createEntity(GotoModelPath.T, entityFactory);
					gotoModelPath.setPath(modelPathInfo.modelPath());
					gotoModelPath.setName(name);
					gotoModelPath.setOpenWithActionElements(modelPathInfo.openWithActionElements());
					gotoModelPath.setSelectedElement(modelPathInfo.selectedElement());
					commandReceiver.accept(gotoModelPath);
				}
			});
		}

		@Override
		public ModelPathBuilder gotoModelPath(final String name, boolean addToCurrentView, boolean showFullModelPath) {

			return new BasicModelPathBuilder(notificationBuilder, new Consumer<GotoModelPathInfo>() {
				@Override
				public void accept(GotoModelPathInfo modelPathInfo) throws RuntimeException {
					GotoModelPath gotoModelPath = BuilderTools.createEntity(GotoModelPath.T, entityFactory);
					gotoModelPath.setPath(modelPathInfo.modelPath());
					gotoModelPath.setName(name);
					// Adding new goto model path options
					gotoModelPath.setAddToCurrentView(addToCurrentView);
					gotoModelPath.setShowFullModelPath(showFullModelPath);
					gotoModelPath.setOpenWithActionElements(modelPathInfo.openWithActionElements());
					gotoModelPath.setSelectedElement(modelPathInfo.selectedElement());
					commandReceiver.accept(gotoModelPath);
				}
			});
		}

		@Override
		public UrlBuilder gotoUrl(final String name) {
			return new BasicUrlBuilder(notificationBuilder, new Consumer<GotoUrl>() {
				@Override
				public void accept(GotoUrl gotoUrl) throws RuntimeException {
					gotoUrl.setName(name);
					commandReceiver.accept(gotoUrl);
				}
			});
		}
		@Override
		public NotificationBuilder refresh(String name) {
			Refresh command = BuilderTools.createEntity(Refresh.T, entityFactory);
			command.setName(name);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder refresh(String name, PersistentEntityReference reference) {
			Refresh command = BuilderTools.createEntity(Refresh.T, entityFactory);
			command.setName(name);
			command.setReferenceToRefresh(reference);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder refresh(String name, GenericEntity entity) {
			return refresh(name, entity.reference());
		}

		@Override
		public NotificationBuilder refresh(String name, EntityType<? extends GenericEntity> type, Object id) {
			PersistentEntityReference reference = PersistentEntityReference.T.create();
			reference.setTypeSignature(type.getTypeSignature());
			reference.setRefId(id);
			return refresh(name, reference);
		}

		@Override
		public NotificationBuilder reload(String name) {
			Reload command = BuilderTools.createEntity(Reload.T, entityFactory);
			command.setName(name);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}
		@Override
		public NotificationBuilder reloadView(String name) {
			ReloadView command = BuilderTools.createEntity(ReloadView.T, entityFactory);
			command.setName(name);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}
		
		@Override
		public NotificationBuilder reloadAllViews(String name) {
			ReloadView command = BuilderTools.createEntity(ReloadView.T, entityFactory);
			command.setName(name);
			command.setReloadAll(true);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder refreshPreview(String name, GenericEntity entity) {
			return refreshPreview(name, entity.entityType().getTypeSignature(), entity.getId());
		}

		@Override
		public NotificationBuilder refreshPreview(String name, EntityType<? extends GenericEntity> type, Object id) {
			return refreshPreview(name, type.getTypeSignature(), id);
		}

		@Override
		public NotificationBuilder refreshPreview(String name, String typeSignature, Object id) {
			RefreshPreview refreshCommand = BuilderTools.createEntity(RefreshPreview.T, entityFactory);
			refreshCommand.setName(name);
			refreshCommand.setTypeSignature(typeSignature);
			refreshCommand.setEntityId(id);
			this.commandReceiver.accept(refreshCommand);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder downloadResource(String name, Resource resource) {
			return downloadResource(name, Collections.singletonList(resource));
		}

		@Override
		public NotificationBuilder downloadResource(String name, Resource... resources) {
			return downloadResource(name, Arrays.asList(resources));
		}

		@Override
		public NotificationBuilder downloadResource(String name, List<Resource> resources) {
			DownloadResource command = BuilderTools.createEntity(DownloadResource.T, entityFactory);
			command.setName(name);
			command.setResources(resources);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder printResource(String name, Resource resource) {
			return printResource(name, Collections.singletonList(resource));
		}

		@Override
		public NotificationBuilder printResource(String name, Resource... resources) {
			return printResource(name, Arrays.asList(resources));
		}

		@Override
		public NotificationBuilder printResource(String name, List<Resource> resources) {
			PrintResource command = BuilderTools.createEntity(PrintResource.T, entityFactory);
			command.setName(name);
			command.setResources(resources);
			this.commandReceiver.accept(command);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder applyManipulation(String name, Manipulation manipulation) {
			ApplyManipulation applyManipulation = BuilderTools.createEntity(ApplyManipulation.T, entityFactory);
			applyManipulation.setName(name);
			applyManipulation.setManipulation(manipulation);
			this.commandReceiver.accept(applyManipulation);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder applyManipulations(String name, List<Manipulation> manipulation) {
			return applyManipulation(name, asManipulation(manipulation));
		}
		@Override
		public <T> NotificationBuilder applyManipulation(String name, Supplier<ManagedGmSession> trackingSessionSupplier, T trackingSubject,
				Consumer<T> tracker) {
			return applyManipulation(name, trackingSessionSupplier, trackingSubject, tracker, null);
		}
		@Override
		public <T> NotificationBuilder applyManipulation(String name, Supplier<ManagedGmSession> trackingSessionSupplier, T trackingSubject,
				Consumer<T> tracker, Function<Manipulation, Manipulation> trackedManipulationAdapter) {
			ManagedGmSession trackingSession = trackingSessionSupplier.get();
			//@formatter:off
			T managedTrackingSubject = 
					trackingSession
						.merge()
						.suspendHistory(true)
						.doFor(trackingSubject);
			//@formatter:on
			List<Manipulation> trackedManipulations = new ArrayList<>();

			//@formatter:off
			trackingSession
				.listeners()
				.add(trackedManipulations::add);
			//@formatter:on

			tracker.accept(managedTrackingSubject);
			Manipulation trackedManipulation = asManipulation(trackedManipulations);
			if (trackedManipulationAdapter != null) {
				trackedManipulation = trackedManipulationAdapter.apply(trackedManipulation);
			}
			return applyManipulation(name, trackedManipulation);
		}

		@Override
		public NotificationBuilder runQuery(String name, Query query) {
			RunQuery runQuery = BuilderTools.createEntity(RunQuery.T, entityFactory);
			runQuery.setName(name);
			runQuery.setQuery(query);
			this.commandReceiver.accept(runQuery);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder runQuery(String name, String queryString) {
			RunQueryString runQueryString = BuilderTools.createEntity(RunQueryString.T, entityFactory);
			runQueryString.setName(name);
			runQueryString.setQuery(queryString);
			this.commandReceiver.accept(runQueryString);
			return notificationBuilder;
		}

		@Override
		public NotificationBuilder runWorkbenchAction(String name, String workbenchActionFolderId) {
			return runWorkbenchAction(name, workbenchActionFolderId, null);
		}

		@Override
		public NotificationBuilder runWorkbenchAction(String name, String workbenchActionFolderId, Map<String, Object> variableValues) {
			RunWorkbenchAction runWorkbenchAction = BuilderTools.createEntity(RunWorkbenchAction.T, entityFactory);
			runWorkbenchAction.setName(name);
			runWorkbenchAction.setWorkbenchFolderId(workbenchActionFolderId);
			if (variableValues != null) {
				runWorkbenchAction.setVariables(variableValues);
			}
			this.commandReceiver.accept(runWorkbenchAction);
			return notificationBuilder;
		}
		
	}

	public class BasicUrlBuilder implements UrlBuilder {
		private NotificationBuilder notificationBuilder;
		private Consumer<GotoUrl> urlReceiver;

		private String target;
		private String url;
		private boolean useImage;

		public BasicUrlBuilder(NotificationBuilder notificationBuilder, Consumer<GotoUrl> urlReceiver) {
			this.notificationBuilder = notificationBuilder;
			this.urlReceiver = urlReceiver;
		}

		@Override
		public UrlBuilder target(String target) {
			this.target = target;
			return this;
		}
		@Override
		public UrlBuilder url(String url) {
			this.url = url;
			return this;
		}
		@Override
		public UrlBuilder useImage() {
			this.useImage = true;
			return this;
		}

		@Override
		public NotificationBuilder close() {
			GotoUrl gotoUrl = BuilderTools.createEntity(GotoUrl.T, entityFactory);
			gotoUrl.setTarget(target);
			gotoUrl.setUrl(url);
			gotoUrl.setUseImage(useImage);
			BuilderTools.receive(gotoUrl, urlReceiver);
			return notificationBuilder;
		}

	}

	private interface GotoModelPathInfo {

		GmModelPath modelPath();
		GmModelPathElement selectedElement();
		Set<GmModelPathElement> openWithActionElements();

	}

	public class BasicModelPathBuilder implements ModelPathBuilder {
		private NotificationBuilder notificationBuilder;
		private List<GmModelPathElement> elements = new ArrayList<GmModelPathElement>();
		private Consumer<GotoModelPathInfo> modelPathInfoReceiver;
		private GmModelPathElement selectedElement;
		private Set<GmModelPathElement> openWithActionElements = new HashSet<>();

		public BasicModelPathBuilder(NotificationBuilder notificationBuilder, Consumer<GotoModelPathInfo> modelPathInfoReceiver) {
			this.notificationBuilder = notificationBuilder;
			this.modelPathInfoReceiver = modelPathInfoReceiver;
		}

		@Override
		public ModelPathBuilder addElement(GenericEntity entity, boolean isSelected, boolean openWithAction) {
			GmRootPathElement element = BuilderTools.createEntity(GmRootPathElement.T, entityFactory);
			element.setTypeSignature(entity.entityType().getTypeSignature());
			element.setValue(entity);
			return addElement(element, isSelected, openWithAction);
		}

		@Override
		public ModelPathBuilder addElement(GenericEntity entity, String property, boolean isSelected, boolean openWithAction) {
			GmPropertyPathElement element = BuilderTools.createEntity(GmPropertyPathElement.T, entityFactory);
			EntityType<GenericEntity> entityType = entity.entityType();
			element.setTypeSignature(entityType.getTypeSignature());
			element.setValue(entityType.getProperty(property).get(entity));
			element.setEntity(entity);
			element.setProperty(property);
			return addElement(element, isSelected, openWithAction);
		}

		@Override
		public ModelPathBuilder addElement(String typeSignature, Object value, boolean isSelected, boolean openWithAction) {
			GmRootPathElement element = BuilderTools.createEntity(GmRootPathElement.T, entityFactory);
			element.setTypeSignature(typeSignature);
			element.setValue(value);
			return addElement(element, isSelected, openWithAction);
		}

		@Override
		public ModelPathBuilder addElement(GmModelPathElement element, boolean isSelected, boolean openWithAction) {
			elements.add(element);
			if (isSelected) {
				this.selectedElement = element;
			}
			if (openWithAction) {
				this.openWithActionElements.add(element);
			}
			return this;
		}

		@Override
		public ModelPathBuilder addElement(GmModelPathElement element) {
			return addElement(element, false, false);
		}
		@Override
		public ModelPathBuilder addElement(String typeSignature, Object value) {
			return addElement(typeSignature, value, false, false);
		}
		@Override
		public ModelPathBuilder addElement(GenericEntity entity) {
			return addElement(entity, false, false);
		}
		@Override
		public ModelPathBuilder addElement(GenericEntity entity, String property) {
			return addElement(entity, property, false, false);
		}

		public GmModelPathElement getSelectedElement() {
			return selectedElement;
		}

		public Set<GmModelPathElement> getOpenWithActionElements() {
			return openWithActionElements;
		}

		@Override
		public NotificationBuilder close() {
			GmModelPath modelPath = BuilderTools.createEntity(GmModelPath.T, entityFactory);
			modelPath.getElements().addAll(elements);

			BuilderTools.receive(new GotoModelPathInfo() {

				@Override
				public GmModelPathElement selectedElement() {
					return selectedElement;
				}

				@Override
				public Set<GmModelPathElement> openWithActionElements() {
					return openWithActionElements;
				}

				@Override
				public GmModelPath modelPath() {
					return modelPath;
				}
			}, modelPathInfoReceiver);
			return notificationBuilder;
		}

	}

	@Override
	public NotificationBuilder context(Set<String> context) {
		this.context = context;
		return this; 
	}

	@Override
	public NotificationBuilder context(String... context) {
		Set<String> set = new HashSet<>();
		Collections.addAll(set, context);		
		return context(set);
	}
	
	@Override
	public NotificationsBuilder close() {

		Notification notification = null;
		Command command = getCommand();
		
		if (message != null && command != null) {
			MessageWithCommand messageWithCommand = BuilderTools.createEntity(MessageWithCommand.T, entityFactory);
			setMessage(messageWithCommand);
			messageWithCommand.setCommand(command);
			notification = messageWithCommand;
		} else if (message != null) {
			MessageNotification messageNotification = BuilderTools.createEntity(MessageNotification.T, entityFactory);
			setMessage(messageNotification);
			notification = messageNotification;
		} else if (command != null) {
			CommandNotification commandNotification = BuilderTools.createEntity(CommandNotification.T, entityFactory);
			commandNotification.setCommand(command);
			notification = commandNotification;
		}

		if (notification != null) {
			setContext(notification);
			BuilderTools.receive(notification, notificationReceiver);
		}

		return notifications;
	}

	private Command getCommand() {
		switch (commands.size()) {
		case 0: return null;
		case 1: return commands.get(0);
		default:
				CompoundCommand cc = CompoundCommand.T.create();
				cc.setCommands(commands);
				return cc;
		}
	}

	private void setMessage(MessageNotification messageNotification) {
		messageNotification.setLevel(level);
		messageNotification.setMessage(message);
		messageNotification.setConfirmationRequired(confirmationRequired);
		if (detailMessage != null) {
			messageNotification.setDetails(detailMessage);
		}
	}
	
	private void setContext(Notification notification) {
		notification.setContext(context);
	}

	/**
	 * Creates a {@link Manipulation} instance based on the passed list of {@link Manipulation}. <br/>
	 * If the list contains one element the Manipulation itself will be returned. <br/>
	 * If the list contains no element null will be returned. <br/>
	 * If more then one elements are in the list a {@link CompoundManipulation} containing the list will be returned.
	 */
	private static Manipulation asManipulation(List<? extends Manipulation> list) {
		switch (list.size()) {
			case 0:
				return null;
			case 1:
				return list.get(0);
			default:
				return compound(list);
		}
	}
}
