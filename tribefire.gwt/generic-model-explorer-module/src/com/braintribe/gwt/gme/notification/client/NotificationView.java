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

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.notification.client.adapter.ManipulationAdapter;
import com.braintribe.gwt.gme.notification.client.resources.NotificationBarStyle;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.notification.client.resources.NotificationTemplates;
import com.braintribe.gwt.gme.notification.client.resources.NotificationViewStyle;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmViewport;
import com.braintribe.gwt.gmview.client.GmViewportListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.NotificationViewHandler;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedListViewDefaultResources;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.command.Command;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.notification.NotificationRegistryEntry;
import com.braintribe.model.processing.core.expert.api.GmExpertBuilder;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.notification.api.NotificationEventSourceExpert;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent;
import com.sencha.gxt.data.shared.event.StoreRemoveEvent.StoreRemoveHandler;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 * Master view to display the history of the received notifications.
 * 
 */
public class NotificationView extends ListView<NotificationViewModel, NotificationViewModel>
		implements NotificationViewHandler, InitializableBean, GmListView, GmViewport, GmActionSupport, GmViewActionProvider {

	private static final long CLEANER_TIMEOUT = 1000 * 60 * 10; // 10 minutes
	private static final int CLEANER_INTERVAL = (int) (CLEANER_TIMEOUT / 60);
	private static final Logger logger = new Logger(NotificationView.class);

	protected static final NotificationViewStyle STYLES = NotificationResources.INSTANCE.notificationViewStyles();
	protected static final boolean INJECTED = STYLES.ensureInjected();
	
	private static final String ITEM_SEL_SELECTOR = "div." + STYLES.itemSel();
	private static final KeyProvider KEY_PROVIDER = new KeyProvider();
	private static final Appearance APPEARANCE = new Appearance();

	private Supplier<? extends GmExpertRegistry> commandRegistrySupplier;
	private List<GmViewportListener> gmViewportListeners;
	private final Handler handler = new Handler();
	private final ManipulationListener manipulationAdapter = new ManipulationListener();
	private final Scheduler.RepeatingCommand cmdWindowChanged = () -> {
		if (gmViewportListeners != null)
			gmViewportListeners.forEach(listener -> listener.onWindowChanged(NotificationView.this));
		
		return false; // one-shot = no repeating
	};
	private final Scheduler.RepeatingCommand cmdCleaning = () -> {
		Date cleanDate = new Date(System.currentTimeMillis() - CLEANER_TIMEOUT);
		ListStore<NotificationViewModel> store = getStore();
		for (int index = store.size(); index-- > 0;) {
			NotificationViewModel model = store.get(index);
			NotificationRegistryEntry entry = model.getEntry();
			Date wasReadAt = entry.getWasReadAt();
			if (wasReadAt != null && wasReadAt.compareTo(cleanDate) <= 0)
				store.remove(model);
		}
		return true; // repeating
	};

	private List<Pair<ActionTypeAndName, ModelAction>> externalActions = null;
	private List<GmSelectionListener> gmSelectionListeners;
	private List<NotificationListener> notificationListeners = new ArrayList<>();
	private GmExpertBuilder<NotificationEventSourceExpert<NotificationEventSource>> gmEventSourceExpert;
	private GmExpertBuilder<CommandExpert<Command>> gmCommandExpert;
	private PersistenceGmSession gmSession;
	private ModelPath rootModelPath;
	private NotificationRegistryEntry selectedRegEntry;
	private GenericEntity selectedEntity;
	private ActionProviderConfiguration actionProviderConfiguration;
	private TextButton removeButton;
	private TextButton clearButton;
	private NotificationStoreFilter storeFilter;
	private NotificationViewModel selectedModel;
	private GmContentViewActionManager actionManager;
	private RemoveNotificationAction removeNotificationAction;
	private ClearNotificationsAction clearNotificationsAction;

	// Constructor

	public NotificationView() {
		super(new ListStore<NotificationViewModel>(KEY_PROVIDER), KEY_PROVIDER, APPEARANCE);
		setStyleName(STYLES.parent());
		setCell(new Cell());
		setBorders(false);
		setTrackMouseOver(false);
		setSelectionModel(new NotificationListViewSelectionModel<NotificationViewModel>() {
			{
				setSelectionMode(SelectionMode.SINGLE);
				addBeforeSelectionHandler(NotificationView.this.handler);
			}
		});
		getStore().addSortInfo(new StoreSortInfo<NotificationViewModel>(new ReceivedAtValueProvider(), SortDir.DESC));
		getStore().addStoreAddHandler(handler);
		getStore().addStoreRemoveHandler(handler);
		addHandler(handler, ClickEvent.getType());
		Scheduler.get().scheduleFixedDelay(cmdCleaning, CLEANER_INTERVAL);
	}

	// Helpers

	private XElement getSelected() {
		return getElement().selectNode(ITEM_SEL_SELECTOR);
	}

	private void setSelected(XElement target, GenericEntity entity) {
		XElement selected = getSelected();
		if (selected != null)
			getAppearance().onSelect(selected, false);
		if (target != null)
			getAppearance().onSelect(target, true);
		selectedEntity = entity;
		if (selectedEntity != null)
			fireGmSelectionListeners();
		
		if (removeButton != null)
			removeButton.setVisible(selectedEntity instanceof NotificationRegistryEntry);
	}

	private List<GmSelectionListener> getSelectionListeners() {
		if (gmSelectionListeners == null)
			gmSelectionListeners = new ArrayList<>();
		return gmSelectionListeners;
	}

	private List<GmViewportListener> getViewportListeners() {
		if (gmViewportListeners == null)
			gmViewportListeners = new ArrayList<>();
		return gmViewportListeners;
	}

	private void setContent(ModelPath modelPath, boolean initialData) {
		if (initialData) {
			this.rootModelPath = modelPath;
			getStore().clear();
		}
		
		if (modelPath == null)
			return;
		
		List<NotificationRegistryEntry> entries = modelPath.last().getValue();
		List<MessageNotification> confirmationRequiredMessages = new ArrayList<>();
		for (NotificationRegistryEntry entry : entries) {
			NotificationViewModel model = new NotificationViewModel(entry);
			if (!model.getMessages().isEmpty())
				getStore().add(model);
			
			for (Notification notification : entry.getNotifications()) {
				if (notification instanceof CommandNotification) {
					if (notification instanceof MessageWithCommand && ((MessageWithCommand) notification).getConfirmationRequired()) {
						confirmationRequiredMessages.add((MessageWithCommand) notification);
						continue;
					}
					
					if (!((CommandNotification) notification).getExecuteManually())
						executeCommand((CommandNotification) notification);
				} else if (notification instanceof MessageNotification && ((MessageNotification) notification).getConfirmationRequired())
					confirmationRequiredMessages.add((MessageNotification) notification);
			}
		}
		
		confirmationRequiredMessages.forEach(this::showConfirmationDialog);
	}
	
	private void showConfirmationDialog(MessageNotification notification) {
		ConfirmationDialog dialog = showConfirmationDialog(notification.getLevel(), notification.getMessage());
		dialog.setCancelButtonVisible(notification instanceof CommandNotification);
		dialog.getConfirmation().andThen(result -> {
			if (result && notification instanceof CommandNotification)
				executeCommand((CommandNotification) notification);
		});
	}
	
	/**
	 * Prepares a {@link ConfirmationDialog} with the message and level.
	 * @param level - if null, then INFO is used.
	 */
	public static ConfirmationDialog showConfirmationDialog(Level level, String message) {
		Level theLevel;
		if (level == null)
			theLevel = Level.INFO;
		else
			theLevel = level;
		
		return new ConfirmationDialog() {
			{
				Label header = new Label();
				NotificationBarStyle headerStyle = NotificationResources.LEVEL_STYLES_BIG.get(theLevel);
				header.getElement().setInnerSafeHtml(NotificationTemplates.INSTANCE.renderBar(headerStyle, "", SafeHtmlUtils.fromSafeConstant(theLevel.name()), null));
				
				TextArea textArea = new TextArea();
				configureTextAreaLayoutAndMessage(textArea, message);
				
				mainPanel.add(header);
				mainPanel.add(textArea);
			}
		};
	}
	
	public void removeNotification() {
		if (selectedModel != null) {			
			getStore().remove(selectedModel);			
		}
		fireGmSelectionListeners();
	}

	public void clearNotifications() {
		ListStore<NotificationViewModel> store = getStore();
		for (NotificationViewModel model : store.getAll())
			removeModel(model.getEntry());
		
		store.clear();
		if (clearButton != null)
			clearButton.setVisible(false);
		
		fireGmSelectionListeners();
		fireClearNotificationListeners();
	}
		
	protected void filterNotifications(String text, Level level, List<String> contextList) {
		ListStore<NotificationViewModel> store = getStore();
		
		if (storeFilter == null) {
			store.setEnableFilters(true);
			store.addFilter(getStoreFilter(text, level, contextList));
		} else {
			storeFilter.setText(text);
			storeFilter.setLevel(level);
			storeFilter.setContext(contextList);
			
			applyFilters(store);
		}
	}

	private NotificationStoreFilter getStoreFilter(String text, Level level, List<String> contextList) {
		if (storeFilter == null)
			storeFilter = new NotificationStoreFilter();
		
		storeFilter.setText(text);
		storeFilter.setLevel(level);
		storeFilter.setContext(contextList);
		return storeFilter;
	}

	private void fireGmSelectionListeners() {
		if (gmSelectionListeners != null)
			gmSelectionListeners.forEach(listener -> listener.onSelectionChanged(this));
	}

	private void fireAddNotificationListeners(Notification notification) {
		if (notificationListeners != null)
			notificationListeners.forEach(listener -> listener.onAddNotification(notification));
	}

	private void fireRemoveNotificationListeners(Notification notification) {
		if (notificationListeners != null)
			notificationListeners.forEach(listener -> listener.onRemoveNotification(notification));
	}
	
	private void fireClearNotificationListeners() {
		if (notificationListeners != null)
			notificationListeners.forEach(listener -> listener.onClearNotifications());
	}	
	
	private void fireWindowChanged() {
		Scheduler.get().scheduleFixedDelay(cmdWindowChanged, 200);
	}
	
	private TextButton prepareRemoveButton() {
		removeButton = new TextButton();
		removeButton.setIcon(ConstellationResources.INSTANCE.removeBig());
		removeButton.setText(LocalizedText.INSTANCE.removeNotification());
		removeButton.setWidth(70);
		removeButton.setIconAlign(IconAlign.TOP);
		removeButton.setScale(ButtonScale.LARGE);
		removeButton.setVisible(false);
		removeButton.addSelectHandler(event -> removeNotification());
		return removeButton;
	}
	
	private TextButton prepareClearButton() {
		clearButton = new TextButton();
		clearButton.setIcon(ConstellationResources.INSTANCE.clearBig());
		clearButton.setText(LocalizedText.INSTANCE.clearNotifications());
		clearButton.setWidth(70);
		clearButton.setIconAlign(IconAlign.TOP);
		clearButton.setScale(ButtonScale.LARGE);
		clearButton.setVisible(getStore().size() > 0);
		clearButton.addSelectHandler(event -> clearNotifications());
		return clearButton;
	}

	// members of GmContentView

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (this.externalActions == null) {
			this.externalActions = new ArrayList<>();
			if (removeNotificationAction != null)
				this.externalActions.add(new Pair<>(new ActionTypeAndName("removeNotification"), removeNotificationAction));
			if (clearNotificationsAction != null)
				this.externalActions.add(new Pair<>(new ActionTypeAndName("clearNotification"), clearNotificationsAction));
		}
		
		if (externalActions != null)
			this.externalActions.addAll(0, externalActions);
		
		if (this.externalActions != null) {
			if (actionProviderConfiguration != null)
				actionProviderConfiguration.addExternalActions(this.externalActions);
			if (actionManager != null) //Already initialized
				actionManager.addExternalActions(this, this.externalActions);
		}
	}

	@Override
	public void setContent(ModelPath modelPath) {
		setContent(modelPath, true);
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		//throw new RuntimeException();
		List<ModelPath> modelPaths = null;
		if (selectedEntity != null) {
			modelPaths = new ArrayList<>();
			ModelPath modelPath = new ModelPath();
			modelPath.add(new RootPathElement(selectedEntity.entityType(), selectedEntity));
			modelPaths.add(modelPath);			
		} 
		return modelPaths;
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}

	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public void addSelectionListener(GmSelectionListener listener) {
		if (listener != null)
			getSelectionListeners().add(listener);
	}

	@Override
	public boolean isSelected(Object element) {
		//NotificationView.throw new RuntimeException();
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		// NOP
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		//NOP
		//throw new RuntimeException();
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		if (selectedEntity == null)
			return null;
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(selectedEntity.entityType(), selectedEntity));
		return modelPath;
	}

	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		getSelectionListeners().remove(sl);
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}

	@Override
	public GmContentViewActionManager getGmContentViewActionManager() {
		return actionManager;
	}
		
	@Override
	public ModelPath getContentPath() {
		return rootModelPath;
	}

	// members of GmListView

	@Override
	public void configureTypeForCheck(GenericModelType typeForCheck) {
		// NOP
	}

	@Override
	public void addContent(ModelPath modelPath) {
		setContent(modelPath, false);
	}

	@Override
	public List<ModelPath> getAddedModelPaths() {
		//throw new RuntimeException();
		return null;
	}

	// members of GmViewport

	@Override
	public void addGmViewportListener(GmViewportListener listener) {
		if (listener != null)
			getViewportListeners().add(listener);
	}

	@Override
	public void removeGmViewportListener(GmViewportListener vl) {
		getViewportListeners().remove(vl);
	}

	@Override
	public boolean isWindowOverlappingFillingSensorArea() {
		//throw new RuntimeException();
		return false;
	}

	// public members

	@Required
	public void setCommandRegistry(Supplier<? extends GmExpertRegistry> commandRegistrySupplier) {
		this.commandRegistrySupplier = commandRegistrySupplier;
	}
	
	// members of Container

	@Override
	protected void onAfterFirstAttach() {
		super.onAfterFirstAttach();
		fireWindowChanged();
	}

	@Override
	protected void onUpdate(NotificationViewModel model, int index) {
		super.onUpdate(model, index);
		XElement subItem = getElement(index);
		if (subItem != null && subItem.hasClassName(STYLES.itemSel()))
			getAppearance().onSelect(getElement(index), true);
		else if (subItem != null) {
			subItem = subItem.selectNode(ITEM_SEL_SELECTOR);
			if (subItem != null && subItem.hasClassName(STYLES.itemSel())) {
				String selector = "." + subItem.getClassName().split(" ")[0];
				subItem = getElement(index).selectNode(selector);
				if (subItem != null)
					getAppearance().onSelect(subItem, true);
			}
		}
	}

	// internal members

	private void addModel(NotificationRegistryEntry entry) {
		gmSession.listeners().entity(entry).add(manipulationAdapter);
	}

	private void removeModel(NotificationRegistryEntry entry) {
		gmSession.listeners().entity(entry).add(manipulationAdapter);
		if (selectedRegEntry == entry) {
			selectedRegEntry = null;
			selectedModel = null;
			setSelected(null, null);
			fireGmSelectionListeners();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void executeCommand(CommandNotification n) {
		if (gmCommandExpert == null)
			gmCommandExpert = (GmExpertBuilder) commandRegistrySupplier.get().findExpert(CommandExpert.class);
		
		Command command = n.getCommand();
		CommandExpert<Command> commandExpert = gmCommandExpert.forInstance(command);
		if (commandExpert != null)
			commandExpert.handleCommand(command);
		else
			logger.info("No expert found for: " + command);
	}
	
	// Members of GmViewActionProvider
	
	@Override
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
		actionProviderConfiguration = new ActionProviderConfiguration();
		actionProviderConfiguration.setGmContentView(this);

		/*
		List<Pair<String, ? extends Widget>> externalButtons = new ArrayList<>();
		externalButtons.add(new Pair<>("removeNotification", prepareRemoveButton()));
		externalButtons.add(new Pair<>("clearNotification", prepareClearButton()));
		
		actionProviderConfiguration.setExternalButtons(externalButtons);
		*/
							
		actionProviderConfiguration.addExternalActions(externalActions);		
		
		return actionProviderConfiguration;
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return false;
	}
	
	public void setRemoveNotificationAction(RemoveNotificationAction removeNotificationAction) {
		this.removeNotificationAction = removeNotificationAction;
		this.removeNotificationAction.configureGmContentView(this);
	}

	public void setClearNotificationsAction(ClearNotificationsAction clearNotificationsAction) {
		this.clearNotificationsAction = clearNotificationsAction;
		this.clearNotificationsAction.configureGmContentView(this);
	}
	
	// internal classes

	private static class KeyProvider extends IdentityValueProvider<NotificationViewModel> implements ModelKeyProvider<NotificationViewModel> {
		@Override
		public String getKey(NotificationViewModel model) {
			return String.valueOf(model.getEntry().<Object>getId());
		}
	}

	private static class ReceivedAtValueProvider implements ValueProvider<NotificationViewModel, Date> {

		@Override
		public Date getValue(NotificationViewModel model) {
			return model.getEntry().getReceivedAt();
		}

		@Override
		public void setValue(NotificationViewModel modal, Date value) {
			// NOP
		}

		@Override
		public String getPath() {
			return "";
		}

	}

	private static class Appearance extends ListViewCustomAppearance<NotificationViewModel> {

		public Appearance() {
			super("div." + STYLES.group(), null, STYLES.itemSel());
		}

		@Override
		public Element findCellParent(XElement item) {
			return super.findCellParent(item);
		}

		@Override
		public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
			builder.append(content);
		}
		
		@Override
		public void onSelect(XElement item, boolean select) {
			super.onSelect(item, select);
			item.setClassName(ExtendedListViewDefaultResources.GME_LIST_VIEW_SEL, select);
		}
	}

	private class Cell extends AbstractCell<NotificationViewModel> {
		@SuppressWarnings("rawtypes")
		@Override
		public void render(Context context, NotificationViewModel model, SafeHtmlBuilder sb) {
			if (gmEventSourceExpert == null)
				gmEventSourceExpert = (GmExpertBuilder) commandRegistrySupplier.get().findExpert(NotificationEventSourceExpert.class);
			
			sb.append(NotificationTemplates.INSTANCE.renderView(STYLES, model, gmEventSourceExpert.forInstance(model.getEntry().getEventSource())));
		}
	}

	private class Handler implements StoreAddHandler<NotificationViewModel>, StoreRemoveHandler<NotificationViewModel>, ClickHandler,
			BeforeSelectionHandler<NotificationViewModel> {

		private XElement findParentByStyle(XElement target, String name) {
			return target.findParent("div." + name, 10);
		}

		@Override
		public void onAdd(StoreAddEvent<NotificationViewModel> event) {
			for (NotificationViewModel model : event.getItems()) {
				addModel(model.getEntry());
				model.getMessages().forEach(messageModel -> fireAddNotificationListeners(messageModel.getNotification()));
			}
			
			if (clearButton != null)
				clearButton.setVisible(true);
		}

		@Override
		public void onRemove(StoreRemoveEvent<NotificationViewModel> event) {
			removeModel(event.getItem().getEntry());
			NotificationViewModel model = event.getItem(); 
			model.getMessages().forEach(messageModel -> fireRemoveNotificationListeners(messageModel.getNotification()));
			
			if (getStore().size() == 0 && clearButton != null)
				clearButton.setVisible(false);
		}
		
		@Override
		public void onClick(ClickEvent ce) {
			XElement target = ce.getNativeEvent().<XEvent> cast().getEventTargetEl();
			XElement group = findElement(target).<XElement> cast();
			NotificationViewModel model = getStore().get(indexOf(group));
			if (model == null)
				return;
			
			selectedModel = model;
			selectedRegEntry = model.getEntry();
			XElement e = null;
			if ((e = findParentByStyle(target, STYLES.ruler())) != null) {
				model.toogleExpanded();
				refreshNode(getStore().indexOf(model));
				return;
			}
			
			if ((e = findParentByStyle(target, STYLES.command())) != null) {
				String idString = e.getPropertyString("id");
				if (idString == null || idString.isEmpty())
					idString = e.getParentElement().getPropertyString("id");
				if (idString != null && !idString.isEmpty()) {
					Object id;
					try {
						id = Long.parseLong(idString);
					} catch (NumberFormatException ex) {
						id = idString;
					}
					for (Notification n : model.getEntry().getNotifications()) {
						if (n.getId().equals(id))
							executeCommand((CommandNotification) n);
					}
				}
				
				return;
			}
			
			if ((e = findParentByStyle(target, STYLES.source())) != null) {
				setSelected(e, model.getEntry().getEventSource());
				return;
			}
			
			if ((e = findParentByStyle(target, STYLES.item())) == null) {
				setSelected(group, selectedRegEntry);
				return;
			}
			
			String idString = e.getPropertyString("id");
			if (idString == null || idString.isEmpty())
				return;
			
			Object id;
			try {
				id = Long.parseLong(idString);
			} catch (NumberFormatException ex) {
				id = idString;
			}
			for (Notification n : model.getEntry().getNotifications()) {
				if (n.getId().equals(id))
					setSelected(e, n);
			}
		}

		@Override
		public void onBeforeSelection(final BeforeSelectionEvent<NotificationViewModel> event) {
			Scheduler.get().scheduleFixedDelay(() -> {
				NotificationRegistryEntry regEntry = event.getItem().getEntry();
				if (regEntry != null && regEntry.getWasReadAt() == null)
					regEntry.setWasReadAt(new Date());
				return false; // one-shot = no repeating
			}, 200);
			event.cancel();
		}

	}

	private class ManipulationListener extends ManipulationAdapter implements ManipulationAdapter.OnPropertyChange<NotificationRegistryEntry, Object> {

		public ManipulationListener() {
			addListener(NotificationRegistryEntry.class, null, this);
		}

		@Override
		public void onChangeValue(NotificationRegistryEntry entity, String propertyName, Object newValue) {
			refreshNode(getStore().indexOf(getStore().findModelWithKey(String.valueOf(entity.getId()))));
		}

	}
	
	private class NotificationStoreFilter implements StoreFilter<NotificationViewModel> {
		
		private String text;
		private Level level;
		private List<String> contextList;

		@Override
		public boolean select(Store<NotificationViewModel> store, NotificationViewModel parent, NotificationViewModel item) {
			if (level == null && text == null && (contextList == null || contextList.isEmpty()))
				return true;
			
			boolean found = false;
			for (MessageModel message : item.getMessages()) {
				if (level != null && !level.equals(message.getLevel()))
					continue;
				
				if (text != null && !message.getMessage().toLowerCase().contains(text))
					continue;
				
				if (contextList != null && !contextList.isEmpty()) {
				    if (message.getContext() == null || message.getContext().isEmpty())	
				    	continue;
				    
				    for (String contextLine : message.getContext()) {
				    	if (contextList.contains(contextLine)) {
				    		found = true;
				    		break;				    
				    	}
				    }
				    if (!found)
				    	continue;
				}
				
				found = true;
				break;
			}
			
			return found;
		}
		
		public void setText(String text) {
			if (text != null) {
				text = text.trim().toLowerCase();
				if (text.isEmpty())
					text = null;
			}
			
			this.text = text;
		}
		
		public void setLevel(Level level) {
			this.level = level;
		}
		
		public void setContext(List<String> contextList) {
			this.contextList = contextList;
		}
	}
	
	private static native void applyFilters(Store<?> store) /*-{
		store.@com.sencha.gxt.data.shared.Store::applyFilters()();
	}-*/;

	@Override
	public void intializeBean() throws Exception {
		if (actionManager != null)
			actionManager.connect(this);		
	}
	
	public void addNotificationListener(NotificationListener listener) {
		if (listener != null && !notificationListeners.contains(listener))
			notificationListeners.add(listener);
	}

	public void removeNotificationListener(NotificationListener listener) {
		if (listener != null)
			notificationListeners.remove(listener);
	}
	
}
