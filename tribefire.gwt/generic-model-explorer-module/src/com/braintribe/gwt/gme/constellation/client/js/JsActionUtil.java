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
package com.braintribe.gwt.gme.constellation.client.js;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.action.client.ActionOrGroup;
import com.braintribe.gwt.action.client.ActionOrGroup.PropertyListener;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.reflection.EntityType;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Utility class with methods related to actions, to be used in the JS side, via JsInterop.
 * @author michel.docouto
 *
 */
public class JsActionUtil {
	
	/**
	 * Clones the given externalActionProviderConfiguration, from another GWT space, to this one.
	 */
	public static ActionProviderConfiguration prepareActionProviderConfiguration(GmContentView view, Object externalActionProviderConfiguration) {
		if (externalActionProviderConfiguration == null)
			return null;
		
		ActionProviderConfiguration configuration = new ActionProviderConfiguration();
		configuration.setGmContentView(view);
		
		Object externalActions = getExternalActions(externalActionProviderConfiguration);
		List<Pair<ActionTypeAndName, ModelAction>> list = null;
		if (externalActions != null) {
			List<Pair<ActionTypeAndName, ModelAction>> theList = new ArrayList<>();
			for (int i = 0; i < getSize(externalActions); i++) {
				Object pair = getAtIndex(externalActions, i);
				Object externalActionTypeAndName = getPairFirst(pair);
				Object externalModelAction = getPairSecond(pair);
				String actionName = getActionName(externalActionTypeAndName);
				String actionTypeSignature = getActionTypeSignature(externalActionTypeAndName);
				EntityType<? extends ActionFolderContent> actionType = null;
				if (actionTypeSignature != null)
					actionType = GMF.getTypeReflection().getEntityType(actionTypeSignature);
				ActionTypeAndName actionTypeAndName = new ActionTypeAndName(actionType, actionName);
				ModelAction modelAction = JsActionUtil.prepareModelAction(externalModelAction);
				
				Pair<ActionTypeAndName, ModelAction> newPair = new Pair<>(actionTypeAndName, modelAction);
				theList.add(newPair);
			}
			
			list = theList;
		}
		
		configuration.addExternalActions(list);
		return configuration;
	}
	
	private static ModelAction prepareModelAction(Object externalModelAction) {
		ModelAction modelAction = new ModelAction() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				callPerform(externalModelAction);
			}
			
			@Override
			protected void updateVisibility() {
				callUpdateVisibility(externalModelAction);
			}
			
			@Override
			public void updateState(List<List<ModelPath>> modelPaths) {
				callUpdateState(externalModelAction, modelPaths);
			}
			
			@Override
			public void configureGmContentView(GmContentView gmContentView) {
				callConfigureGmContentView(externalModelAction, gmContentView);
			}
			
			@Override
			public void addPropertyListener(PropertyListener listener) {
				callAddPropertyListener(externalModelAction, listener);
			}
			
			@Override
			public void removePropertyListener(PropertyListener listener) {
				callRemovePropertyListener(externalModelAction, listener);
			}
			
			@Override
			public Object put(String property, Object value) {
				return callPut(externalModelAction, property, value);
			}
			
			@Override
			public Object put(String property, Object value, boolean forceChange) {
				return callPut(externalModelAction, property, value, forceChange);
			}
			
			@Override
			public <T> T get(String property) {
				T value = callGet(externalModelAction, property);
				if (!ModelAction.PROPERTY_POSITION.equals(property))
					return value;
				
				if (value == null)
					return null;
				
				for (ModelActionPosition position : ModelActionPosition.values()) {
					if (checkPosition(value, position.toString()))
						return (T) position;
				}
				
				List<Object> positions = new ArrayList<>();
				for (int i = 0; i < getSize(value); i++) {
					Object externalPosition = getAtIndex(value, i);
					for (ModelActionPosition position : ModelActionPosition.values()) {
						if (checkPosition(externalPosition, position.toString()))
							positions.add(position);
					}
				}
				return (T) positions;
			}
			
			@Override
			public void setId(String id) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_ID, id);
			}
			
			@Override
			public String getId() {
				return callGetId(externalModelAction);
			}
			
			@Override
			public void setName(String name) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_NAME, name);
			}
			
			@Override
			public String getName() {
				return callGet(externalModelAction, ActionOrGroup.PROPERTY_NAME);
			}
			
			@Override
			public void setTooltip(String tooltip) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_TOOLTIP, tooltip);
			}
			
			@Override
			public String getTooltip() {
				return callGet(externalModelAction, ActionOrGroup.PROPERTY_TOOLTIP);
			}
			
			@Override
			public void setEnabled(boolean enabled) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_ENABLED, enabled);
			}
			
			@Override
			public void setEnabled(boolean enabled, boolean forceChangeEvent) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_ENABLED, enabled, forceChangeEvent);
			}
			
			@Override
			public boolean getEnabled() {
				Boolean enabled = callGet(externalModelAction, ActionOrGroup.PROPERTY_ENABLED);
				return enabled != null? enabled: true;
			}
			
			@Override
			public void setHidden(boolean hidden) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_HIDDEN, hidden);
			}
			
			@Override
			public void setHidden(boolean hidden, boolean forceChangeEvent) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_HIDDEN, hidden, forceChangeEvent);
			}
			
			@Override
			public boolean getHidden() {
				Boolean hidden = callGet(externalModelAction, ActionOrGroup.PROPERTY_HIDDEN);
				return hidden != null? hidden: true;
			}
			
			@Override
			public void setToggled(boolean toggled) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_TOGGLED, toggled);
			}
			
			@Override
			public boolean getToggled() {
				Boolean toggled = callGet(externalModelAction, ActionOrGroup.PROPERTY_TOGGLED);
				return toggled != null? toggled: true;
			}
			
			@Override
			public void setIcon(ImageResource icon) {
				setIconAsString(icon.getSafeUri().asString(), icon.getName(), icon.getWidth(), icon.getHeight(), false);
			}
			
			@Override
			public void setIconAsString(String iconUrl, String iconName, int width, int height, boolean hoverIcon) {
				callSetIconAsString(externalModelAction, iconUrl, iconName, width, height, hoverIcon);
			}
			
			@Override
			public ImageResource getIcon() {
				return getExternalIcon(false);
			}
			
			@Override
			public void setHoverIcon(ImageResource icon) {
				setIconAsString(icon.getSafeUri().asString(), icon.getName(), icon.getWidth(), icon.getHeight(), true);
			}
			
			@Override
			public ImageResource getHoverIcon() {
				return getExternalIcon(true);
			}
			
			@Override
			public void setStyleName(String styleName) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_STYLENAME, styleName);
			}
			
			@Override
			public String getStyleName() {
				return callGet(externalModelAction, ActionOrGroup.PROPERTY_STYLENAME);
			}
			
			@Override
			public void setRoles(Set<String> roles) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_ROLES, roles);
			}
			
			@Override
			public Set<String> getRoles() {
				return callGet(externalModelAction, ActionOrGroup.PROPERTY_ROLES);
			}
			
			@Override
			public void setOperations(Set<String> operations) {
				callPut(externalModelAction, ActionOrGroup.PROPERTY_OPERATIONS, operations);
			}
			
			@Override
			public Set<String> getOperations() {
				return callGet(externalModelAction, ActionOrGroup.PROPERTY_OPERATIONS);
			}
			
			private ImageResource getExternalIcon(boolean hoverIcon) {
				String iconUrl = callGetIconUrl(externalModelAction, hoverIcon);
				if (iconUrl == null)
					return null;
				
				String iconName = callGetIconName(externalModelAction, hoverIcon);
				int iconWidth = callGetIconWidth(externalModelAction, hoverIcon);
				int iconHeight = callGetIconHeight(externalModelAction, hoverIcon);
				
				return new ImageResourcePrototype(iconName, UriUtils.fromTrustedString(iconUrl), 0, 0, iconWidth, iconHeight, false, false);
			}
		};
		
		return modelAction;
	}
	
	private static native List<Object> getExternalActions(Object externalActionProviderConfiguration) /*-{
		return externalActionProviderConfiguration.getExternalActions();
	}-*/;
	
	private static native int getSize(Object list) /*-{
		return list.size();
	}-*/;
	
	private static native Object getAtIndex(Object list, int index) /*-{
		return list.getAtIndex(index);
	}-*/;
	
	private static native Object getPairFirst(Object pair) /*-{
		return pair.first;
	}-*/;
	
	private static native Object getPairSecond(Object pair) /*-{
		return pair.second;
	}-*/;
	
	private static native String getActionName(Object actionTypeAndName) /*-{
		return actionTypeAndName.getActionName();
	}-*/;
	
	private static native String getActionTypeSignature(Object actionTypeAndName) /*-{
		var type = actionTypeAndName.getDenotationType();
		if (type)
			return type.getTypeSignature();
		
		return null;
	}-*/;
	
	private static native void callPerform(Object action) /*-{
		action.perform(null);
	}-*/;
	
	private static native void callUpdateVisibility(Object action) /*-{
		action.updateVisibility();
	}-*/;
	
	private static native void callUpdateState(Object action, List<List<ModelPath>> modelPaths) /*-{
		action.updateState(modelPaths);
	}-*/;
	
	private static native void callConfigureGmContentView(Object action, GmContentView view) /*-{
		action.configureGmContentView(view);
	}-*/;
	
	private static native void callAddPropertyListener(Object action, PropertyListener listener) /*-{
		action.addPropertyListener(listener);
	}-*/;
	
	private static native void callRemovePropertyListener(Object action, PropertyListener listener) /*-{
		action.removePropertyListener(listener);
	}-*/;
	
	private static native Object callPut(Object action, String property, Object value) /*-{
		return action.put(property, value);
	}-*/;
	
	private static native Object callPut(Object action, String property, Object value, boolean forceChange) /*-{
		return action.putWithForceChange(property, value, forceChange);
	}-*/;
	
	private static native String callGetId(Object action) /*-{
		return action.getId();
	}-*/;
	
	private static native <T> T callGet(Object action, String property) /*-{
		return action.get(property);
	}-*/;
	
	private static native void callSetIconAsString(Object action, String iconUrl, String iconName, int width, int height, boolean hoverIcon) /*-{
		action.setIconAsString(iconUrl, iconName, width, height, hoverIcon);
	}-*/;
	
	private static native String callGetIconUrl(Object action, boolean hoverIcon) /*-{
		return action.getIconUrl(hoverIcon);
	}-*/;
	
	private static native String callGetIconName(Object action, boolean hoverIcon) /*-{
		return action.getIconName(hoverIcon);
	}-*/;
	
	private static native int callGetIconWidth(Object action, boolean hoverIcon) /*-{
		return action.getIconWidth(hoverIcon);
	}-*/;
	
	private static native int callGetIconHeight(Object action, boolean hoverIcon) /*-{
		return action.getIconHeight(hoverIcon);
	}-*/;
	
	private static native boolean checkPosition(Object value, String position) /*-{
		return value == position;
	}-*/;

}
