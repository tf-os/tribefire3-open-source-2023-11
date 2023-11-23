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
package com.braintribe.gwt.gme.constellation.client.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.GmCondensationView;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.meta.data.prompt.Condensed;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree.CheckState;

public class CondensateEntityAction extends Action {
	
	private final Map<EntityType<?>, List<Menu>> entitiesMenus;
	private EntityType<?> currentEntityType;
	private List<Condensed> currentEntityCondensations;
	private List<Widget> menuComponents;
	private final GmCondensationView condensationView;
	private final UncondenseLocalAction uncondenseLocalAction;
	private Action afterSelectionAction;
	
	public CondensateEntityAction(GmCondensationView condensationView, UncondenseLocalAction uncondenseLocalAction) {
		this.condensationView = condensationView;
		this.uncondenseLocalAction = uncondenseLocalAction;
		setHidden(true);
		setName(LocalizedText.INSTANCE.condensation());
		setIcon(ConstellationResources.INSTANCE.condensedGlobal());
		entitiesMenus = new HashMap<>();
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		//This will simply show the menu...
	}
	
	/**
	 * Configures the components to be set with the condensation menu. This component can be either a Button or a MenuItem.
	 */
	public void addComponentToSetMenu(Widget component) {
		if (menuComponents == null)
			menuComponents = new ArrayList<>();
		if (!menuComponents.contains(component))
			menuComponents.add(component);
	}
	
	/**
	 * Updates the visibility based on the given entityType.
	 */
	public void updateVisibility(EntityType<?> entityType) {
		currentEntityType = entityType;
		if (currentEntityType == null) {
			setHidden(true);
			return;
		}
		
		currentEntityCondensations = GMEMetadataUtil.getEntityCondensations(null, currentEntityType, condensationView
				.getGmSession()
				.getModelAccessory()
				.getMetaData(),
				condensationView.getUseCase());
		
		if (currentEntityCondensations == null || currentEntityCondensations.isEmpty()) {
			setHidden(true);
			return;
		}
		
		if (!isSomeEntityCondensationForced(currentEntityCondensations)) {
			setHidden(false);
			if (menuComponents != null) {
				int counter = 0;
				for (Widget component : menuComponents) {
					if (component instanceof TextButton)
						((TextButton) component).setMenu(prepareMenu(counter));
					else if (component instanceof MenuItem)
						((MenuItem) component).setSubMenu(prepareMenu(counter));
					counter++;
				}
			}
		}
	}
	
	/**
	 * Configures the action to be called after the a condensation selection is made.
	 */
	public void configureAfterSelectionAction(Action afterSelectionAction) {
		this.afterSelectionAction = afterSelectionAction;
	}
	
	/**
	 * Called before the menu is supposed to be shown.
	 */
	public void onBeforeShowMenu(Menu menu) {
		if (uncondenseLocalAction == null)
			return;
		
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget widget = menu.getWidget(i);
			if (widget instanceof SeparatorMenuItem) {
				SeparatorMenuItem separator = (SeparatorMenuItem) widget;
				boolean hidden = isComponentHidden(separator);
				if (uncondenseLocalAction.getHidden() && !hidden)
					separator.setVisible(false);
				else if (!uncondenseLocalAction.getHidden() && hidden)
					separator.setVisible(true);
				break;
			}
		}
	}
	
	private static boolean isSomeEntityCondensationForced(Collection<Condensed> entityCondensations) {
		return entityCondensations.stream().filter(condensation -> condensation.getCondensationMode().equals(CondensationMode.forced)).count() > 0;
	}
	
	private Menu prepareMenu(int index) {
		List<Menu> menus = entitiesMenus.get(currentEntityType);
		String currentCondensedProperty = condensationView.getCurrentCondensedProperty(currentEntityType);
		if (menus == null) {
			menus = new ArrayList<>();
			for (int i = 0; i < menuComponents.size(); i++)
				menus.add(prepareMenu(currentCondensedProperty));
			
			entitiesMenus.put(currentEntityType, menus);
			return menus.get(index);
		}
		
		if (menus.size() <= index) {
			menus.add(prepareMenu(currentCondensedProperty));
			return menus.get(index);
		}
		
		Menu menu = menus.get(index);
		String displayPropertyName = null;
		if (currentCondensedProperty != null) {
			displayPropertyName = GMEMetadataUtil.getPropertyDisplay(currentCondensedProperty, condensationView.getGmSession().getModelAccessory().getMetaData()
					.entityType(currentEntityType).property(currentCondensedProperty).useCase(condensationView.getUseCase()));
		}
		
		for (int i = 0; i < menu.getWidgetCount(); i++) {
			Widget widget = menu.getWidget(i);
			if (widget instanceof CheckMenuItem) {
				CheckMenuItem checkMenuItem = (CheckMenuItem) widget;
				if (checkMenuItem.getText().equals(LocalizedText.INSTANCE.uncondense()))
					checkMenuItem.setChecked(currentCondensedProperty == null, true);
				else
					checkMenuItem.setChecked(checkMenuItem.getText().equals(LocalizedText.INSTANCE.condenseBy(displayPropertyName)), true);
			}
		}
		return menus.get(index);
	}
	
	private Menu prepareMenu(String currentCondensedProperty) {
		final Menu menu = new Menu();
		menu.setMinWidth(180);
		CheckMenuItem item = new CheckMenuItem(LocalizedText.INSTANCE.uncondense());
		item.setItemId(item.getText());
		item.setGroup("condensationOptions");
		item.setChecked(currentCondensedProperty == null);
		//item.setIcon(ConstellationResources.INSTANCE.uncondensed());
		addCheckChangeListener(item, null, null);
		menu.add(item);
		for (Condensed entityCondensation : currentEntityCondensations) {
			String propertyName = entityCondensation.getProperty().getName();
			String displayPropertyName = GMEMetadataUtil.getPropertyDisplay(propertyName,
					condensationView.getGmSession().getModelAccessory().getMetaData().entityType(currentEntityType).property(propertyName).useCase(condensationView.getUseCase()));
			item = new CheckMenuItem(LocalizedText.INSTANCE.condenseBy(displayPropertyName)); 
			item.setGroup("condensationOptions");
			item.setChecked(propertyName.equals(currentCondensedProperty));
			//item.setIcon(ConstellationResources.INSTANCE.condensedGlobal());
			addCheckChangeListener(item, propertyName, entityCondensation.getCondensationMode());
			menu.add(item);
			item.setItemId(item.getText());
		}
		
		if (uncondenseLocalAction != null) {
			final SeparatorMenuItem separator = new SeparatorMenuItem();
			menu.add(separator);
			separator.setVisible(false);
			MenuItem uncondenseMenuItem = new MenuItem();
			MenuItemActionAdapter.linkActionToMenuItem(uncondenseLocalAction, uncondenseMenuItem);
			menu.add(uncondenseMenuItem);
			menu.addBeforeShowHandler(event -> {
				boolean hidden = isComponentHidden(separator);
				if (uncondenseLocalAction.getHidden() && !hidden)
					separator.setVisible(false);
				else if (!uncondenseLocalAction.getHidden() && hidden)
					separator.setVisible(true);
			});
		}
		
		return menu;
	}
	
	private void addCheckChangeListener(CheckMenuItem item, final String propertyName, final CondensationMode condensationMode) {
		item.addCheckChangeHandler(event -> {
			if (event.getChecked().equals(CheckState.CHECKED))
				condensationView.condense(propertyName, condensationMode, currentEntityType);
			if (event.getSource() instanceof Menu) {
				getMenuList((Menu) event.getSource()).stream().filter(menu -> event.getSource() != menu).forEach(menu -> {
					Widget component = menu.getItemByItemId(event.getItem().getItemId());
					if (component instanceof CheckMenuItem)
						((CheckMenuItem) component).setChecked(event.getChecked().equals(CheckState.CHECKED), true);
				});
			}
			
			if (afterSelectionAction != null)
				afterSelectionAction.perform(null);
		});
	}
	
	private List<Menu> getMenuList(Menu menu) {
		return entitiesMenus.values().stream().filter(list -> list.contains(menu)).findFirst().orElse(null);
	}
	
	private native boolean isComponentHidden(Component component) /*-{
		return component.@com.sencha.gxt.widget.core.client.Component::hidden;
	}-*/;

}
