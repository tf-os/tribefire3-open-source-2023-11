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
package com.braintribe.gwt.action.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.shared.UriUtils;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType (namespace = "$tf.view")
public abstract class ActionOrGroup extends ActionPropertyHolder implements KnownProperties, DisposableBean {
	
	@JsMethod
	public abstract void perform(TriggerInfo triggerInfo);
	
	public interface PropertyListener {
		public void propertyChanged(ActionOrGroup source, String property);
	}
	
	private List<PropertyListener> listeners = new ArrayList<PropertyListener>();
	
	public void addPropertyListener(PropertyListener listener) {
		listeners.add(listener);
	}
	
	public void removePropertyListener(PropertyListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent(String property) {
		for (PropertyListener listener: listeners) {
			listener.propertyChanged(this, property);
		}
	}
	
	@Override
	public Object put(String property, Object value) {
		return put(property, value, false);
	}
	
	@JsMethod(name="putWithForceChange")
	public Object put(String property, Object value, boolean forceChange) {
		Object oldValue = super.put(property, value);

		if (forceChange || (value != oldValue && (value == null || oldValue == null || !value.equals(oldValue))))
			fireChangeEvent(property);

		return oldValue;
	}
	
	@Configurable
	public void setId(String id) {
		put(PROPERTY_ID, id);
	}
	
	public String getId() {
		String id = get(PROPERTY_ID);
		if (id == null) {
			id = getClass().getName();
			ImageResource imageResource = getIcon();
			if (imageResource != null) {
				id += "#" + imageResource.getName();
			}
		}
		
		return id;
	}
	
	@Configurable
	public void setName(String name) {
		put(PROPERTY_NAME, name);
	}
	
	public String getName() {
		return get(PROPERTY_NAME);
	}
	
	@Configurable
	public void setTooltip(String tooltip) {
		put(PROPERTY_TOOLTIP, tooltip);
	}
	
	public String getTooltip() {
		return get(PROPERTY_TOOLTIP);
	}
	
	@Configurable
	public void setEnabled(boolean enabled) {
		put(PROPERTY_ENABLED, enabled);
	}
	
	@Configurable
	@JsMethod(name="setEnabledWithForceChange")
	public void setEnabled(boolean enabled, boolean forceChangeEvent) {
		put(PROPERTY_ENABLED, enabled,forceChangeEvent);
	}
	
	public boolean getEnabled() {
		Boolean enabled = get(PROPERTY_ENABLED);
		return enabled != null? enabled: true;
	}
	
	/**
	 * True to hide the action. False to show it.
	 */
	@Configurable
	public void setHidden(boolean hidden) {
		setHidden(hidden, false);
	}
	
	/**
	 * True to hide the action. False to show it.
	 */
	@Configurable
	@JsMethod(name="setHiddenWithForceChange")
	public void setHidden(boolean hidden, boolean forceChangeEvent) {
		put(PROPERTY_HIDDEN, hidden, forceChangeEvent);
	}
	
	public boolean getHidden() {
		Boolean hidden = get(PROPERTY_HIDDEN);
		return hidden != null ? hidden : false;
	}
	
	@Configurable
	public void setToggled(boolean toggled) {
		put(PROPERTY_TOGGLED, toggled);
	}
	
	public boolean getToggled() {
		Boolean toggled = get(PROPERTY_TOGGLED);
		return toggled != null? toggled: false;
	}
	
	@Configurable
	public void setIcon(ImageResource icon) {
		put(PROPERTY_ICON, icon);
	}
	
	@Configurable
	public void setIconAsString(String iconUrl, String iconName, int width, int height, boolean hoverIcon) {
		if (hoverIcon)
			put(PROPERTY_HOVERICON, new ImageResourcePrototype(iconName, UriUtils.fromTrustedString(iconUrl), 0, 0, width, height, false, false));
		else
			put(PROPERTY_ICON, new ImageResourcePrototype(iconName, UriUtils.fromTrustedString(iconUrl), 0, 0, width, height, false, false));
	}
	
	public ImageResource getIcon() {
		return get(PROPERTY_ICON);
	}
	
	/**
	 * Returns the icon URL.
	 */
	public String getIconUrl(boolean hover) {
		ImageResource icon = hover ? getHoverIcon() : getIcon();
		if (icon == null)
			return null;
		
		return icon.getSafeUri().asString();
	}
	
	/**
	 * Returns the icon name.
	 */
	public String getIconName(boolean hover) {
		ImageResource icon = hover ? getHoverIcon() : getIcon();
		if (icon == null)
			return null;
		
		return icon.getName();
	}
	
	/**
	 * Returns the icon width.
	 */
	public int getIconWidth(boolean hover) {
		ImageResource icon = hover ? getHoverIcon() : getIcon();
		if (icon == null)
			return 0;
		
		return icon.getWidth();
	}
	
	/**
	 * Returns the icon height.
	 */
	public int getIconHeight(boolean hover) {
		ImageResource icon = hover ? getHoverIcon() : getIcon();
		if (icon == null)
			return 0;
		
		return icon.getHeight();
	}
	
	@Configurable
	public void setHoverIcon(ImageResource icon) {
		put(PROPERTY_HOVERICON, icon);
	}
	
	@Configurable
	public void setHoverIconAsString(String iconUrl, String iconName, int width, int height) {
		put(PROPERTY_HOVERICON, new ImageResourcePrototype(iconName, UriUtils.fromTrustedString(iconUrl), 0, 0, width, height, false, false));
	}
	
	public ImageResource getHoverIcon() {
		return get(PROPERTY_HOVERICON);
	}
	
	@Configurable
	public void setStyleName(String styleName) {
		put(PROPERTY_STYLENAME, styleName);
	}
	
	public String getStyleName() {
		return get(PROPERTY_STYLENAME);
	}
	
	@Configurable
	public void setRoles(Set<String> roles) {
		put(PROPERTY_ROLES, roles);
	}
	
	public Set<String> getRoles() {
		return get(PROPERTY_ROLES);
	}
	
	@Configurable
	public void setOperations(Set<String> operations) {
		put(PROPERTY_OPERATIONS, operations);
	}
	
	public Set<String> getOperations() {
		return get(PROPERTY_OPERATIONS);
	}
	
	@Override
	public void disposeBean() throws Exception {
		listeners.clear();
	}

}
