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
package com.braintribe.gwt.gmview.action.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.InstantiatedEntityListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelEnvironmentSetListener;
import com.braintribe.gwt.gmview.codec.client.KeyConfigurationRendererCodec;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.utils.client.RootKeyNavExpert;
import com.braintribe.gwt.utils.client.RootKeyNavExpert.RootKeyNavListener;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.workbench.InstantiationAction;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;

/**
 * This action is responsible for instantiating a new entity.
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class InstantiateEntityAction extends AbstractInstantiateEntityAction
		implements RootKeyNavListener, GmSessionHandler, ActionWithMenu, ModelEnvironmentSetListener, KnownGlobalAction {
	public static EntityType<GenericEntity> DEFAULT_ENTITY_TYPE = GenericEntity.T;
	private static final String KNOWN_NAME = "new";
	
	private boolean showAllInstance = false;
	private boolean showTransientInstance = false;
	private boolean showInstancesAtMenu;
	private int setShowAtMenuMaxLimit;
	private Menu menu = new Menu();
	private List<ActionMenuChangeListener> menuChangeListeners = new ArrayList<>();
	private Supplier<InstantiateTransientEntityAction> instantiateTransientEntityActionProvider = null;
	private InstantiateTransientEntityAction instantiateTransientEntityAction = null;
	private Supplier<InstantiateEntityAction> defaultInstantiateEntityActionProvider = null;
	private InstantiateEntityAction defaultInstantiateEntityAction = null;
	private boolean isDefaultInstantiateEntityAction = false;
	private boolean isConfiguredByActionFolderContent = false;
	private ImageResource itemDefaultIcon = null;
	private boolean showMenuOnClick = false;
	private List<InstantiationAction> actionsWithKeyConfiguration = new ArrayList<>();
	private boolean useShortcuts = false;
	private boolean disableAllInstances = false;
	
	public InstantiateEntityAction() {
		setName(LocalizedText.INSTANCE.newEntity());
	}
	
	/**
	 * Configures the useCase to be used within this action.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		if (instantiateTransientEntityAction != null)
			instantiateTransientEntityAction.configureUseCase(useCase);
		if (defaultInstantiateEntityAction != null)
			defaultInstantiateEntityAction.configureUseCase(useCase);
	}	
	
	/**
	 * Configures an {@link InstantiatedEntityListener} to be called when instantiating.
	 * Notice that this is used only if there is no {@link GmContentView} configured via {@link #configureGmContentView(com.braintribe.gwt.gmview.client.GmContentView)}.
	 */
	@Override
	public void configureInstantiatedEntityListener(InstantiatedEntityListener instantiatedEntityListener) {
		this.instantiatedEntityListener = instantiatedEntityListener;
		if (instantiateTransientEntityAction != null)
			instantiateTransientEntityAction.configureInstantiatedEntityListener(instantiatedEntityListener);
		if (defaultInstantiateEntityAction != null)
			defaultInstantiateEntityAction.configureInstantiatedEntityListener(instantiatedEntityListener);
	}	
	
	public InstantiateTransientEntityAction getInstantiateTransientEntityAction() {
		if (isDefaultInstantiateEntityAction)
			return null;
		
		if (instantiateTransientEntityAction == null) { 
			instantiateTransientEntityAction = instantiateTransientEntityActionProvider.get();
			instantiateTransientEntityAction.configureUseCase(useCase);
			instantiateTransientEntityAction.configureInstantiatedEntityListener(instantiatedEntityListener);
			if (itemDefaultIcon != null) {
				instantiateTransientEntityAction.setIcon(itemDefaultIcon);
				instantiateTransientEntityAction.setHoverIcon(itemDefaultIcon);
			}			
		}
		
		return instantiateTransientEntityAction;
	}

	public InstantiateEntityAction getDefaultInstantiateEntityAction() {
		if (isDefaultInstantiateEntityAction)
			return this;

		if (defaultInstantiateEntityAction == null) { 
			defaultInstantiateEntityAction = defaultInstantiateEntityActionProvider.get();
			defaultInstantiateEntityAction.configureUseCase(useCase);
			defaultInstantiateEntityAction.configureInstantiatedEntityListener(instantiatedEntityListener);
			defaultInstantiateEntityAction.configureGmSession(gmSession);
			defaultInstantiateEntityAction.configureEntityType(DEFAULT_ENTITY_TYPE);
			defaultInstantiateEntityAction.setName(defaultInstantiateEntityAction.getName() + "...");
			if (itemDefaultIcon != null) {
				defaultInstantiateEntityAction.setIcon(itemDefaultIcon);
				defaultInstantiateEntityAction.setHoverIcon(itemDefaultIcon);
			}			
		}
		
		return defaultInstantiateEntityAction;
	}
	
	public void setInstantiateTransientEntityActionProvider(Supplier<InstantiateTransientEntityAction> instantiateTransientEntityActionProvider) {
		this.instantiateTransientEntityActionProvider = instantiateTransientEntityActionProvider;
	}
		
	/**
	 * Configures the Default {@link EntityType} that will be instantiated.
	 */
	public void configureDefaultEntityType() {
		configureEntityType(DEFAULT_ENTITY_TYPE);
	}	
	
	/**
	 * Configures the {@link EntityType} that will be instantiated.
	 */
	@Override
	public void configureEntityType(EntityType<?> entityType) {
		this.entityType = entityType;
		//boolean hidden = entityType == null || !isInstantiable(entityType);
		//setHidden(hidden);
		
		updateEnable();		
		updateVisibility();
		//if (!hidden && getGmSession().getModelAccessory().getModel() != null) {
		if (getGmSession().getModelAccessory().getModel() != null) {
			String entityName = GMEMetadataUtil.getEntityNameMDOrShortName(entityType,
					getGmSession().getModelAccessory().getMetaData().lenient(true), gmContentView != null ? gmContentView.getUseCase() : useCase);
			String actionName = LocalizedText.INSTANCE.createEntity(entityName);
			if (displayEntityNameInAction)
				setName(actionName);
			setTooltip(actionName);
		}
		
		prepareMenu();
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		
		if (defaultInstantiateEntityAction != null)
			defaultInstantiateEntityAction.configureGmSession(gmSession);
	}
	
	@Override
	public void configureGmContentView(GmContentView gmContentView) {
		super.configureGmContentView(gmContentView);
		//if (gmContentView == null || gmContentView.getGmSession() != gmSession)
		//	gmSession = null;
	}
	
	@Override
	protected void updateVisibility() {		
		if (defaultInstantiateEntityAction != null)
			defaultInstantiateEntityAction.updateVisibility();
		
		if (instantiateTransientEntityAction != null)
			instantiateTransientEntityAction.updateState(null);
		
		if (entityType == null) {
			setHidden(true, true);
			return;
		}
		
		PersistenceGmSession theSession = getGmSession();
		if (theSession == null || theSession instanceof TransientPersistenceGmSession) {//We should use the InstantiateTransientEntityAction in those cases
			setHidden(true, true);
			return;
		}
		
		if (isInstantiable(entityType, theSession )) {
			setHidden(false);
			return;
		}
		
		instantiationActionsProvider.apply(entityType) //
				.andThen(result -> {
					if (!result.isEmpty()) {
						setHidden(false);
					} else {
						setHidden(true, true);
					}
				}).onError(e -> setHidden(true, true));
	}
			
	//@Override
	//protected boolean isInstantiable(EntityType<?> entityType) {
	protected boolean isInstantiable(EntityType<?> entityType, PersistenceGmSession theSession) {
		if (theSession.getModelAccessory().getModel() != null) {
			EntityMdResolver entityMetaDataContextBuilder = theSession.getModelAccessory().getMetaData().lenient(true).entityType(entityType)
					.useCase(gmContentView != null ? gmContentView.getUseCase() : useCase);
			if (!entityMetaDataContextBuilder.is(Instantiable.T))				
				return false;
		}
		
		return true;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		if (gmSession == null && gmContentView != null) {
			PersistenceGmSession theSession = gmContentView.getGmSession();
			if (theSession != null) {
				theSession.listeners().add(InstantiateEntityAction.this);
				return theSession;
			}
		}
		
		return gmSession;
	}
	
	@Override
	public String getKnownName() {
		return KNOWN_NAME;
	}
		
	public void setShowInstancesAtMenu(boolean show) {
		this.showInstancesAtMenu = show;
	}
	
	public void setShowAtMenuMaxLimit(int limit) {
		this.setShowAtMenuMaxLimit = limit;
	}
	
	public void setShowAllInstance(boolean show) {
		this.showAllInstance = show;
	}
	
	public void setShowTransientInstance(boolean show) {
		this.showTransientInstance = show;		
	}

	private void prepareMenu() {
		showMenuOnClick = false;
		
		if (isDefaultInstantiateEntityAction)
			return;
		
		boolean changed = false;
		if (menu.getWidgetCount() > 0) {
			menu.clear();
			changed = true;
		}
				
		if (showAllInstance || !isConfiguredByActionFolderContent) {
			getDefaultInstantiateEntityAction();
			MenuItem menuItem = new MenuItem();
			if (itemDefaultIcon != null)
				menuItem.setIcon(itemDefaultIcon);				
			else	
				menuItem.setIcon(this.getIcon());
			defaultInstantiateEntityAction.updateState(null);		
			MenuItemActionAdapter.linkActionToMenuItem(true, defaultInstantiateEntityAction, menuItem);	
			menu.add(menuItem);
			changed = true;
		}
		if (showTransientInstance || !isConfiguredByActionFolderContent) {
			getInstantiateTransientEntityAction();
			MenuItem menuItem = new MenuItem();
			if (itemDefaultIcon != null)
				menuItem.setIcon(itemDefaultIcon);				
			else	
				menuItem.setIcon(this.getIcon());
			instantiateTransientEntityAction.updateState(null);		
			MenuItemActionAdapter.linkActionToMenuItem(true, instantiateTransientEntityAction, menuItem);				
			menu.add(menuItem);			
			changed = true;
		}
		
		if (!showInstancesAtMenu || !isConfiguredByActionFolderContent) {
			if (changed) 
				fireMenuChangeListener();
			return;
		}		
		
		actionsWithKeyConfiguration.clear();
		
		instantiationActionsProvider.apply(entityType) //
				.andThen(instantiationActions -> {
					if (instantiationActions.size() <= 1 || instantiationActions.size() > setShowAtMenuMaxLimit)
						return;
					
					if (menu.getWidgetCount() > 0) { // add separator
						SeparatorMenuItem separatorItem = new SeparatorMenuItem();
						menu.add(separatorItem);
					}

					for (InstantiationAction instantationAction : instantiationActions) {
						ModelAction action = new ModelAction() {							
							@Override
							public void perform(TriggerInfo triggerInfo) {
								instantiationActionHandler.handleInstantiationAction(instantationAction, null);								
							}
							
							@Override
							protected void updateVisibility() {
								setHidden(false);								
							}
						};
						
						MenuItem menuItem = new MenuItem();
						Icon icon = instantationAction.getIcon();
						if (icon != null) {
							ImageResource resource = GMEIconUtil.transform(GMEIconUtil.getLargestImageFromIcon(icon));
							if (resource != null) {
								//menuItem.setIcon(resource);
								action.setIcon(resource);
								action.setHoverIcon(resource);
							}
						}
						LocalizedString displayName = instantationAction.getDisplayName();
						String name = displayName != null ? I18nTools.getLocalized(displayName) : null;
						//menuItem.setText(name);
						action.setName(name);
						
						if (instantationAction.getKeyConfiguration() != null) {
						    String shortcut = KeyConfigurationRendererCodec.encodeKeyConfiguration(instantationAction.getKeyConfiguration());
							action.put("keyConfiguration", shortcut);
							actionsWithKeyConfiguration.add(instantationAction);
						}
						MenuItemActionAdapter.linkActionToMenuItem(false, action, menuItem);
						
						//menuItem.addSelectionHandler(event -> instantiationActionHandler.handleInstantiationAction(instantationAction, null));
						menu.add(menuItem);
					}
					showMenuOnClick = true;
					fireMenuChangeListener();
				}).onError(e -> {
					ErrorDialog.show(LocalizedText.INSTANCE.errorInstantiatingEntity(), e);
					e.printStackTrace();
				});
	}
	
	@Override
	public Menu getActionMenu() {
		return menu;
	}

	@Override
	public void addMenuChangeListener(ActionMenuChangeListener listener) {
		menuChangeListeners.add(listener);		
	}

	@Override
	public void removeMenuChangeListener(ActionMenuChangeListener listener) {
		menuChangeListeners.remove(listener);				
	}

	@Override
	public void fireMenuChangeListener() {
		menuChangeListeners.forEach(l -> l.onMenuChange(this));
	}

	public boolean isDefaultInstantiateEntityAction() {
		return isDefaultInstantiateEntityAction;
	}

	public void setIsDefaultInstantiateEntityAction(boolean isDefaultInstantiateEntityAction) {
		this.isDefaultInstantiateEntityAction = isDefaultInstantiateEntityAction;
	}

	public void setDefaultInstantiateEntityActionProvider(Supplier<InstantiateEntityAction> defaultInstantiateEntityActionProvider) {
		this.defaultInstantiateEntityActionProvider = defaultInstantiateEntityActionProvider;
	}

	public boolean isConfiguredByActionFolderContent() {
		return isConfiguredByActionFolderContent;
	}

	public void setConfiguredByActionFolderContent(boolean isConfiguredByActionFolderContent) {
		this.isConfiguredByActionFolderContent = isConfiguredByActionFolderContent;
		updateEnable();
		//prepareMenu();
	}

	public ImageResource getItemDefaultIcon() {
		return itemDefaultIcon;
	}

	public void setItemDefaultIcon(ImageResource itemDefaultIcon) {
		this.itemDefaultIcon = itemDefaultIcon;
		if (itemDefaultIcon == null)
			return;
		
		if (instantiateTransientEntityAction != null) {
			instantiateTransientEntityAction.setIcon(itemDefaultIcon);
			instantiateTransientEntityAction.setHoverIcon(itemDefaultIcon);
		}
		
		if (defaultInstantiateEntityAction != null) {
			defaultInstantiateEntityAction.setIcon(itemDefaultIcon);
			defaultInstantiateEntityAction.setHoverIcon(itemDefaultIcon);
		}
	}

	@Override
	public void onModelEnvironmentSet() {
		prepareMenu();
	}

	@Override
	public boolean showMenuOnClick() {
		return showMenuOnClick;
	}

	@Override
	public void onRootKeyPress(NativeEvent evt) {
		if (actionsWithKeyConfiguration == null || gmContentView == null)
			return;
		
		// If the target is an input or textArea or the current view is an IgnoreKeyConfigurationDialog, then we do not
		// handle the key press.
		EventTarget eventTarget = evt.getEventTarget();
		if (Element.is(eventTarget)) {
			Element element = eventTarget.cast();
			String tagName = element.getTagName();
			if ("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName))
				return;
			
			String contentEditable = element.getAttribute("contenteditable");
			if (contentEditable != null && "true".equalsIgnoreCase(contentEditable.toLowerCase()))
				return;
			
			EventListener eventListener = DOM.getEventListener(element);
			if (eventListener instanceof IgnoreKeyConfigurationDialog)
				return;
		}
		
		for (InstantiationAction action : actionsWithKeyConfiguration) {
			KeyConfiguration keyConfiguration = action.getKeyConfiguration();
			if (evt.getKeyCode() != keyConfiguration.getKeyCode() || evt.getAltKey() != keyConfiguration.getAlt()
					|| evt.getShiftKey() != keyConfiguration.getShift() || evt.getCtrlKey() != keyConfiguration.getCtrl()
					|| evt.getMetaKey() != keyConfiguration.getMeta())
				continue;
						
			Scheduler.get().scheduleDeferred(() -> {
				instantiationActionHandler.handleInstantiationAction(action, null);
			});
			
			evt.stopPropagation();
			evt.preventDefault();
			return;
		}
	}

	public boolean isUseShortcuts() {
		return useShortcuts;
	}

	public void setUseShortcuts(boolean useShortcuts) {
		this.useShortcuts = useShortcuts;		
		if (useShortcuts)
			RootKeyNavExpert.addRootKeyNavListener(this);
		else
			RootKeyNavExpert.removeRootKeyNavListener(this);
	}

	private void updateEnable() {
		if ((entityType == null || entityType.equals(DEFAULT_ENTITY_TYPE)) && isConfiguredByActionFolderContent && !isDefaultInstantiateEntityAction && disableAllInstances) {		
			setEnabled(false);
			return;
		} else {
			setEnabled(true);			
		}		
	}

	public boolean isDisableAllInstances() {
		return disableAllInstances;
	}

	public void setDisableAllInstances(boolean disableAllInstances) {
		this.disableAllInstances = disableAllInstances;
	}
}
