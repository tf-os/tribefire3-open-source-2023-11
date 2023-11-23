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
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.gme.constellation.client.MasterDetailConstellation;
import com.braintribe.gwt.gme.notification.client.adapter.ManipulationAdapter;
import com.braintribe.gwt.gme.notification.client.resources.LocalizedText;
import com.braintribe.gwt.gme.notification.client.resources.NotificationResources;
import com.braintribe.gwt.gme.notification.client.resources.NotificationTemplates;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabActionMenu;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.NotificationViewHandler;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.CheckComboBoxCell;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationRegistry;
import com.braintribe.model.notification.NotificationRegistryEntry;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.resources.CommonStyles;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.event.StoreAddEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent;
import com.sencha.gxt.data.shared.event.StoreClearEvent.StoreClearHandler;
import com.sencha.gxt.data.shared.event.StoreAddEvent.StoreAddHandler;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Constellation for the history of notifications (see {@link NotificationView})
 * 
 */
public class NotificationConstellation extends ContentPanel
		implements NotificationViewHandler, GmViewActionProvider, GmSelectionSupport, DisposableBean, NotificationListener {
	private static final int ACTION_BAR_SIZE = 250;

	private Supplier<MasterDetailConstellation> masterDetailConstellationProvider;
	private MasterDetailConstellation masterDetailConstellation;
	private HTML emptyPanel;
	private ManagedGmSession notificationSession;
	private List<NotificationConstellationListener> notificationConstellationListeners;
	private TextField filterTextField;
	private SimpleComboBox<String> filterComboBox;
	//private SimpleComboBox<String> filterContextComboBox;
	private ComboBox<ContextValueModel> filterContextComboBox;
	private ListStore<ContextValueModel> contextStore;
	
	private Label filterContextLabel;
	private Timer keyPressTimer;
	private VerticalTabActionMenu verticalTabActionBar;
	private boolean verticalTabActionBarVisible = false;
	private List<Action> listAction;
	private FlowPanel topPanel;
	private final BorderLayoutData westData = new BorderLayoutData(0);
	private List<String> selectedContextList = new ArrayList<>();
	private ContextValueProperties contextProps = GWT.create(ContextValueProperties.class);

	private final ManipulationListener manipulationListener = new ManipulationAdapter() {
		{
			addListener(null, null, (OnCollectionAdd<NotificationRegistry, NotificationRegistryEntry>) (entity,
					propertyName, itemsToAdd) -> {
				MasterDetailConstellation masterDetailConstellation = getMasterDetailConstellation();
				GenericModelTypeReflection gmTypeReflection = GMF.getTypeReflection();
				CollectionType contentType = gmTypeReflection.getCollectionType(List.class,
						new GenericModelType[] { NotificationRegistryEntry.T });
				ModelPath modelPath = new ModelPath();
				modelPath.add(new RootPathElement(contentType, new ArrayList<>(itemsToAdd.values())));
				if (masterDetailConstellation.getContentPath() == null)
					masterDetailConstellation.setContent(modelPath);
				else
					masterDetailConstellation.addContent(modelPath);
				setWidget(masterDetailConstellation.getContentPath() == null ? getEmptyPanel()
						: masterDetailConstellation);

				if (masterDetailConstellation.getCurrentMasterView() instanceof NotificationView)
					((NotificationView) masterDetailConstellation.getCurrentMasterView()).addNotificationListener(NotificationConstellation.this);
				
				fireNotificationReceived();
			});
		}

		private void fireNotificationReceived() {
			if (notificationConstellationListeners == null)
				return;
			
			notificationConstellationListeners.forEach(listener -> listener.onNotificationReceived());
		}
	};

	private final AsyncCallback<NotificationRegistry> addManipulationCallback = AsyncCallback
			.of(singleton -> notificationSession.listeners().entityProperty(singleton, NotificationRegistry.entries)
					.add(manipulationListener), e -> ErrorDialog.show("Fatal error in NotificationConstellation!", e));

	private PersistenceGmSession dataSession;

	public NotificationConstellation() {
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
		setWidget(getEmptyPanel());
	}

	@Override
	public void setWidget(Widget w) {
		if (widget != w) {
			super.setWidget(w);
			doLayout();
		}
	}

	private void setNotificationSession(ManagedGmSession notificationSession) {
		this.notificationSession = notificationSession;
		if (notificationSession != null)
			notificationSession.query().entity(NotificationRegistry.T, NotificationRegistry.INSTANCE)
					.require(addManipulationCallback);
	}

	@Required
	public void setMasterDetailConstellationProvider(
			Supplier<MasterDetailConstellation> masterDetailConstellationProvider) {
		this.masterDetailConstellationProvider = masterDetailConstellationProvider;
	}

	@Required
	public void setGmSession(ManagedGmSession gmSession) {
		setNotificationSession(gmSession);
	}
	
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	@Required
	public void setVerticalTabActionBar(VerticalTabActionMenu verticalTabActionBar) {
		this.verticalTabActionBar = verticalTabActionBar;
	}
	
	public void setVisibleVerticalTabActionBar(boolean visible) {
		this.verticalTabActionBarVisible = visible;
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
		}
	}
	
	public void setConstellationDefaultModelActions(List<Action> list) {
		this.listAction = list;
	}	
	
	public void addNotificationConstellationListener(NotificationConstellationListener listener) {
		if (listener != null) {
			if (notificationConstellationListeners == null)
				notificationConstellationListeners = new ArrayList<>();
			notificationConstellationListeners.add(listener);
		}
	}
	
	public void removeNotificationConstellationListener(NotificationConstellationListener listener) {
		if (notificationConstellationListeners != null && listener != null) {
			notificationConstellationListeners.remove(listener);
			if (notificationConstellationListeners.isEmpty())
				notificationConstellationListeners = null;
		}
	}

	private HTML getEmptyPanel() {
		if (emptyPanel == null)
			emptyPanel = new HTML(NotificationTemplates.INSTANCE.renderEmpty(LocalizedText.INSTANCE));
		return emptyPanel;
	}
	
	private MasterDetailConstellation getMasterDetailConstellation() {
		if (masterDetailConstellation != null)
			return masterDetailConstellation;
		
		masterDetailConstellation = masterDetailConstellationProvider.get();
		masterDetailConstellation.configureGmSession(dataSession);
		
		topPanel = new FlowPanel();		
		topPanel.setStyleName("notificationConstellationTopPanel");
		
		SimpleContainer container = new SimpleContainer();
		container.setStyleName("notificationConstellationSimplePanel");
		topPanel.add(container);
		
		FlowPanel filterPanel = new FlowPanel();
		filterPanel.setStyleName("notificationConstellationFilterPanel");
		
		filterPanel.add(prepareClearButton());
		filterPanel.add(prepareLevelPanel());		
		filterPanel.add(prepareFilterTextField());
		filterPanel.add(prepareContextPanel());		
		
		container.add(filterPanel);
		topPanel.add(prepareVerticalTabActionBar());
		
		
		masterDetailConstellation.setNorthWidget(topPanel, new BorderLayoutData(32));
		
		return masterDetailConstellation;
	}

	private VerticalTabActionMenu prepareVerticalTabActionBar() {
		if (verticalTabActionBar != null) {
			verticalTabActionBar.setShowDynamicTabElements(verticalTabActionBarVisible);
			verticalTabActionBar.setUseContentMenuAction(verticalTabActionBarVisible);
			verticalTabActionBar.setStaticActionGroup(listAction);
			
			verticalTabActionBar.addStyleName("browsingConstellationActionBar");
			verticalTabActionBar.addStyleName("notificationConstellationActionBar");

			/*
			*/
			verticalTabActionBar.removeStyleName(CommonStyles.get().positionable());
			
			verticalTabActionBar.addVerticalTabListener(new VerticalTabListener() {
				@Override
				public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement,
						VerticalTabElement verticalTabElement) {
					if (verticalTabElement == null)
						return;
									
					Object object = verticalTabElement.getModelObject();
					Widget widget = verticalTabElement.getWidget();
										
					if (widget instanceof Menu) {
						int leftMenu = ((Menu) widget).getData("left");
						int topMenu = ((Menu) widget).getData("top");
						((Menu) widget).showAt(leftMenu, topMenu);
						return;
					} else if (widget instanceof MenuItem) {
						if (object == null)
							object = ((MenuItem) widget).getData("action");
					}
					
					if (!(object instanceof Action))
						return;
										
					Element element = verticalTabElement.getElement();
					TriggerInfo triggerInfo = new TriggerInfo();
					triggerInfo.put(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT, element);
					((Action) object).perform(triggerInfo);
					updateElement((Action) object);
					verticalTabActionBar.refresh();
				}
				
				@Override
				public void onVerticalTabElementAddedOrRemoved(int elements, boolean added,
						List<VerticalTabElement> verticalTabElements) {
					// NOP
				}
				
				@Override
				public void onHeightChanged(int newHeight) {
					//NOP					
				}
			});
			
		}
		return verticalTabActionBar;
	}
	
	public interface NotificationConstellationListener {
		public void onNotificationReceived();
	}
	
	@Override
	public ActionProviderConfiguration getActions() {
		return masterDetailConstellation.getActions();
	}
	
	@Override
	public boolean isFilterExternalActions() {
		return masterDetailConstellation.isFilterExternalActions();
	}
	
	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		masterDetailConstellation.setActionManager(actionManager);
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (notificationConstellationListeners != null) {
			notificationConstellationListeners.clear();
			notificationConstellationListeners = null;
		}
	}
	
	private void filterNotifications(String text, Level level, List<String> listContext) {
		if (masterDetailConstellation.getCurrentMasterView() instanceof NotificationView)
			((NotificationView) masterDetailConstellation.getCurrentMasterView()).filterNotifications(text, level, listContext);
	}
	
	private TextField prepareFilterTextField() {
		filterTextField = new TextField();
		filterTextField.setEmptyText(LocalizedText.INSTANCE.typeForFilter());
		filterTextField.setWidth(300);
		filterTextField.addStyleName("notificationConstellationElement");
		
		filterTextField.addKeyDownHandler(event -> getKeyPressTimer().schedule(100));
		
		return filterTextField;
	}
	
	private TextButton prepareClearButton() {
		TextButton clearButton = new TextButton(LocalizedText.INSTANCE.clear());
		clearButton.addStyleName("notificationConstellationElement");
		clearButton.addSelectHandler(event -> {
			if (masterDetailConstellation.getCurrentMasterView() instanceof NotificationView)
				((NotificationView) masterDetailConstellation.getCurrentMasterView()).clearNotifications();
		});
		
		return clearButton;
	}
	
	private FlowPanel prepareLevelPanel() {
		FlowPanel panel = new FlowPanel();
		panel.setStyleName("notificationConstellationFlowPanel");
		panel.addStyleName("notificationConstellationElement");
		panel.add(prepareLevelLabel());
		panel.add(prepareFilterComboBox());
		return panel;
	}
	
	private FlowPanel prepareContextPanel() {
		FlowPanel panel = new FlowPanel();
		panel.setStyleName("notificationConstellationFlowPanel");
		panel.addStyleName("notificationConstellationElement");
		panel.add(prepareContextLabel());
		panel.add(prepareFilterContextComboBox());
		return panel;
	}
	
	private Label prepareLevelLabel() {
		return new Label(LocalizedText.INSTANCE.level());
	}
	
	private Label prepareContextLabel() {
		filterContextLabel = new Label(LocalizedText.INSTANCE.context());
		return filterContextLabel;
	}
	
	private SimpleComboBox<String> prepareFilterComboBox() {
		filterComboBox = new SimpleComboBox<>(item -> item == null ? LocalizedText.INSTANCE.all() : item);
		filterComboBox.addStyleName("notificationConstellationComboBox");
		filterComboBox.setEditable(false);
		filterComboBox.setTriggerAction(TriggerAction.ALL);
		filterComboBox.setForceSelection(true);
		filterComboBox.add(LocalizedText.INSTANCE.all());
		for (Level level : Level.values())
			filterComboBox.add(level.name());
		filterComboBox.setValue(LocalizedText.INSTANCE.all());
		filterComboBox.addSelectionHandler(event -> filterNotifications(filterTextField.getSelectedText(),
				getLevelValue(filterComboBox.getCurrentValue()), getContextList()));

		return filterComboBox;
	}

	private ComboBox<?> prepareFilterContextComboBox() {
		/*
		filterContextComboBox = new SimpleComboBox<>(item -> item == null ? LocalizedText.INSTANCE.all() : item);
		filterContextComboBox.addStyleName("notificationConstellationComboBox");
		filterContextComboBox.setEditable(false);
		filterContextComboBox.setTriggerAction(TriggerAction.ALL);
		filterContextComboBox.setForceSelection(true);
		filterContextComboBox.add(LocalizedText.INSTANCE.all());
		filterContextComboBox.setValue(LocalizedText.INSTANCE.all());
		filterContextComboBox.addSelectionHandler(event -> filterNotifications(filterTextField.getValue(),
				getLevelValue(filterComboBox.getCurrentValue()), getContextList()));
				*/

		if (contextStore == null)
			contextStore = new ListStore<ContextValueModel>(contextProps.name());
		contextStore.clear();
		
		CheckComboBoxCell<ContextValueModel> comboBoxCell = new CheckComboBoxCell<ContextValueModel>(contextStore, getLabelProvider(),
	              new AbstractSafeHtmlRenderer<ContextValueModel>() {
	                final ComboBoxImageTemplates comboBoxTemplates = GWT.create(ComboBoxImageTemplates.class);
	                @Override
	                public SafeHtml render(ContextValueModel item) {
	                	SafeUri imageUri;
	                	if (item.getSelected())
	                		imageUri = NotificationResources.INSTANCE.checked().getSafeUri();
	                	else	
	                		imageUri = NotificationResources.INSTANCE.unchecked().getSafeUri();
	                	return comboBoxTemplates.image(imageUri, item.getName());
	                }
	              });
		filterContextComboBox = new ComboBox<ContextValueModel>(comboBoxCell);
		filterContextComboBox.getListView().getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		
		filterContextComboBox.setTypeAhead(true);
		filterContextComboBox.setTriggerAction(TriggerAction.ALL);
		
		filterContextComboBox.addStyleName("notificationConstellationComboBox");
		filterContextComboBox.setForceSelection(false);
		filterContextComboBox.setEditable(false);
		filterContextComboBox.setMaxHeight(150);
		filterContextComboBox.setMinListWidth(70);
		filterContextComboBox.setEmptyText(LocalizedText.INSTANCE.all());
		filterContextComboBox.getStore().addStoreAddHandler(new StoreAddHandler<ContextValueModel>() {			
			@Override
			public void onAdd(StoreAddEvent<ContextValueModel> event) {
				filterContextComboBox.setEditable(filterContextComboBox.getStore().getAll().size() > 0);					
			}
		});
		filterContextComboBox.getStore().addStoreClearHandler(new StoreClearHandler<ContextValueModel>() {			
			@Override
			public void onClear(StoreClearEvent<ContextValueModel> event) {
				filterContextComboBox.setEditable(false);				
			}
		});
		filterContextComboBox.getListView().addStyleName("notificationConstellationComboBoxList");
		if (filterContextComboBox.getListView().getParent() != null) {
			filterContextComboBox.getListView().getParent().setStyleName("notificationConstellationComboBoxListParent");
		}
		
		//ContextValueModel allValueModel = new ContextValueModel(true, LocalizedText.INSTANCE.all());
		
		//filterContextComboBox.getStore().add(allValueModel);
		//filterContextComboBox.setValue(allValueModel);
		filterContextComboBox.addSelectionHandler(event -> fireContextSelection(event));
		
		return filterContextComboBox;
	}	
	
	private void fireContextSelection(SelectionEvent<ContextValueModel> event) {	
		if (event.getSelectedItem() != null) {
			event.getSelectedItem().setSelected(!event.getSelectedItem().getSelected());
		}
		filterContextComboBox.getListView().refresh();
		
		filterNotifications(filterTextField.getSelectedText(), getLevelValue(filterComboBox.getCurrentValue()), getContextList());
	}
	
	
	private LabelProvider<? super ContextValueModel> getLabelProvider(){
		LabelProvider<? super ContextValueModel> labelProvider = new LabelProvider<ContextValueModel>() {
			@Override
			public String getLabel(ContextValueModel item) {
				return item.getName();
			}
		};
		return labelProvider;
	}	
	
	private Level getLevelValue(String stringValue) {
		Level level = null;
		if (stringValue == null || LocalizedText.INSTANCE.all().toUpperCase().equals(stringValue.toUpperCase()))
			return level;
		
		try {
			level = Level.valueOf(stringValue.toUpperCase());			
		} catch(Exception e) {
			level = null;	
		}
		
		return level;
	}
	
	private Timer getKeyPressTimer() {
		if (keyPressTimer == null) {
			keyPressTimer = new Timer() {
				@Override
				public void run() {
					filterNotifications(filterTextField.getCurrentValue(), getLevelValue(filterComboBox.getSelectedText()), getContextList());
				}
			};
		}
		
		return keyPressTimer;
	}
	
	protected List<String> getContextList() {
		selectedContextList.clear();
		
		//String stringValue = filterContextComboBox.getCurrentValue();

		for (ContextValueModel contextValueModel:  filterContextComboBox.getStore().getAll()) {
			if (contextValueModel == null)
				continue;
			
			
			if (contextValueModel.getSelected() && !LocalizedText.INSTANCE.all().equals(contextValueModel.getName()))
				selectedContextList.add(contextValueModel.getName());
		}
		
		return selectedContextList;
	}

	private void updateElement(Action action) {
		if (action == null)
			return;
		
		VerticalTabElement element = null;
		if (verticalTabActionBar != null) {
			element = verticalTabActionBar.getVerticalTabElementByModelObject(action);
			if (element == null)
				return;
			
			verticalTabActionBar.updateElement(element);
		}
	}

	public void updateVerticalTabActionBar(GmContentView contentView) {
		GmContentView viewToUse = contentView;
		if (contentView == null)
			viewToUse = masterDetailConstellation.getCurrentMasterView();
		
		if ((viewToUse == null) || !(viewToUse instanceof NotificationView))
			return;
		
		if (verticalTabActionBar != null && verticalTabActionBarVisible) {
			westData.setSize(ACTION_BAR_SIZE);
			viewToUse.addSelectionListener(verticalTabActionBar);
			verticalTabActionBar.configureGmConentView(viewToUse);
			doLayout();
		} else {
			westData.setSize(0);
		}
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		masterDetailConstellation.addSelectionListener(sl);		
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		masterDetailConstellation.removeSelectionListener(sl);		
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return masterDetailConstellation.getFirstSelectedItem();
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return masterDetailConstellation.getCurrentSelection();
	}

	@Override
	public boolean isSelected(Object element) {
		return masterDetailConstellation.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		masterDetailConstellation.select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return masterDetailConstellation.getCurrentMasterView().getView();
	}

	@Override
	public void onAddNotification(Notification notification) {
		if (notification == null || notification.getContext() == null || notification.getContext().isEmpty())
			return;
		
		boolean newAdd = false;
		for (String contextLine : notification.getContext()) {
			if (contextLine == null || contextLine.isEmpty())
				continue;

			boolean find = false;						
			for (ContextValueModel valueModel : filterContextComboBox.getStore().getAll()) {
				if (valueModel.getValue().trim().equals(contextLine.trim())) {
					find = true;
					break;
				}
			}
			
			if (!find) {
				newAdd = true;
				filterContextComboBox.getStore().add(new ContextValueModel(false, contextLine));
			}
		}
		
		if (newAdd)
			filterContextComboBox.getListView().refresh();
	}

	@Override
	public void onRemoveNotification(Notification notification) {
		// RVE - actually empty
		//need keep state that on remove some other notifications still have same context
	}

	@Override
	public void onClearNotifications() {
		selectedContextList.clear();
		filterContextComboBox.getStore().clear();
		filterContextComboBox.clear();
		//ContextValueModel allValueModel = new ContextValueModel(true, LocalizedText.INSTANCE.all());
		//filterContextComboBox.getStore().add(allValueModel);
		//filterContextComboBox.setValue(allValueModel);

		//filterContextComboBox.add(LocalizedText.INSTANCE.all());
		//filterContextComboBox.setValue(LocalizedText.INSTANCE.all());
	}
	
	
	interface ComboBoxImageTemplates extends XTemplates {
	    @XTemplate("<img width=\"16\" height=\"16\" src=\"{imageUri}\"><span style=\"padding:0px 2px;display:table-cell;vertical-align:middle\">{name}</span>")
	    SafeHtml image(SafeUri imageUri, String name);
	}	
	
	interface ContextValueProperties extends PropertyAccess<ContextValueModel> {
	    ModelKeyProvider<ContextValueModel> name();

	    LabelProvider<ContextValueModel> value();
	}	
}
