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
package com.braintribe.model.uitheme;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.style.Color;
import com.braintribe.model.style.Font;

/**
 * 
 *         UiTheme Template with basic variables to configure Colors and Fonts in Tribefire menus, grids, dialogs,..
 */
public interface UiTheme extends GenericEntity {

	EntityType<UiTheme> T = EntityTypes.T(UiTheme.class);

	Color getHooverColor();
	void setHooverColor(Color hooverColor);

	Color getSelectColor();
	void setSelectColor(Color selectInactiveColor);

	Color getSelectInactiveColor();
	void setSelectInactiveColor(Color selectInactiveColor);

	Font getCaptionFont();
	void setCaptionFont(Font captionFont);

	Font getTetherFont();
	void setTetherFont(Font tetherFont);

	Font getTabFont();
	void setTabFont(Font tabFont);

	Font getBasicFont();
	void setBasicFont(Font basicFont);

	Font getMenuFont();
	void setMenuFont(Font menuFont);

	Font getHeaderFont();
	void setHeaderFont(Font headerFont);

}
