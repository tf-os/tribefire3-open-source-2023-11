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
package com.braintribe.gwt.ioc.gme.client.expert;

import java.util.function.Supplier;

import com.braintribe.gwt.gme.constellation.client.SaveAction;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Required;


/**
 * This controller is responsible for warning the user that a manipulation was not already saved, if any, before
 * performing a logout.
 * 
 * @author michel.docouto
 * 
 */
public class LogoutController implements Supplier<String> {

	private Supplier<SaveAction> saveActionProvider;
	
	@Required
	public void setSaveActionProvider(Supplier<SaveAction> saveActionProvider) {
		this.saveActionProvider = saveActionProvider;
	}
	
	@Override
	public String get() throws RuntimeException {
		if(saveActionProvider != null) {
			SaveAction saveAction = saveActionProvider.get();
			if (saveAction.getEnabled())
				return LocalizedText.INSTANCE.signoutConfirmation();
		}		
		return null;
	}

}
