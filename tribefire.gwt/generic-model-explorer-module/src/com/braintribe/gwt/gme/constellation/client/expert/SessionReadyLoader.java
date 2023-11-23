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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.List;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Expert used for configuring multiple loaders.
 * @author michel.docouto
 *
 */
public class SessionReadyLoader implements Loader<Void> {
	
	private List<? extends Loader<Void>> loaders;
	private int loadersLoaded;
	
	/**
	 * Configures the list of loaders.
	 */
	@Required
	public void setLoaders(List<? extends Loader<Void>> loaders) {
		this.loaders = loaders;
	}

	@Override
	public void load(final AsyncCallback<Void> asyncCallback) {
		loadersLoaded = 0;
		for (Loader<Void> loader : loaders) {
			loader.load(AsyncCallbacks.of( //
					result -> {
						loadersLoaded++;
						if (loadersLoaded == loaders.size())
							asyncCallback.onSuccess(result);
					}, asyncCallback::onFailure));
		}
	}

}
