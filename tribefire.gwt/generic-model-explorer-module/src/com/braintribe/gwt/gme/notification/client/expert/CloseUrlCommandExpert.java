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
package com.braintribe.gwt.gme.notification.client.expert;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.uicommand.CloseUrl;

/**
 * Expert which closes a previously opened URL which is opened INLINE.
 * @author michel.docouto
 *
 */
public class CloseUrlCommandExpert implements CommandExpert<CloseUrl> {
	
	private ExplorerConstellation explorerConstellation;
	
	/**
	 * Configures the required {@link ExplorerConstellation} where the URL will be closed.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}

	@Override
	public void handleCommand(CloseUrl command) {
		String name = command.getName();
		if (name != null && !name.startsWith("$"))
			name = "$" + name;
		
		String tabNameToBeClosed = name;
		
		explorerConstellation.getVerticalTabPanel().getTabElements().stream()
				.filter(el -> el.getName() != null && el.getName().equals(tabNameToBeClosed)).findAny().ifPresent(el -> {
					explorerConstellation.getVerticalTabPanel().removeVerticalTabElement(el);
				});
	}

}
