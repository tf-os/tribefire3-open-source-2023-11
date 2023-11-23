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
package tribefire.extension.js.model.deployment;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.resource.Icon;

/**
 * This meta data links an entity type to a {@link JsUxComponent JS UX component} in which the entity type should be viewed. 
 *
 */
public interface ViewWithJsUxComponent extends EntityTypeMetaData {

	EntityType<ViewWithJsUxComponent> T = EntityTypes.T(ViewWithJsUxComponent.class);
	
	/**
	 * The component is referenced via its global id
	 */
	JsUxComponent getComponent();
	void setComponent(JsUxComponent component);
	
	boolean getListView();
	void setListView(boolean listView);
	
	Icon getIcon();
	void setIcon(Icon icon);

	LocalizedString getDisplayName();
	void setDisplayName(LocalizedString displayName);
	
	boolean getHideDetails();
	void setHideDetails(boolean hideDetails);
	
	public boolean getReadOnly();
	public void setReadOnly(boolean readOnly);
	
}
