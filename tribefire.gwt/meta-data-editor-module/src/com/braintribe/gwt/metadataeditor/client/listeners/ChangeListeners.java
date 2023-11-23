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
package com.braintribe.gwt.metadataeditor.client.listeners;

import com.braintribe.gwt.gmview.client.GmCheckListener;
import com.braintribe.gwt.gmview.client.GmCheckSupport;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

public class ChangeListeners extends AbstractListeners<GmCheckListener> implements ChangeHandler {

	private final GmCheckSupport gmCheckSupport;

	public ChangeListeners(GmCheckSupport gmCheckSupport) {
		super();
		this.gmCheckSupport = gmCheckSupport;
	}

	@Override
	public void onChange(ChangeEvent event) {
		fireListeners();
	}

	@Override
	protected void execute(GmCheckListener listener) {
		listener.onCheckChanged(this.gmCheckSupport);
	}

}
