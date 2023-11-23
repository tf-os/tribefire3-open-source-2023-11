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
package com.braintribe.gwt.customizationui.client;

import com.braintribe.gwt.ioc.client.DisposableBean;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.form.TextField;

public class UserNameTextField extends TextField implements DisposableBean {
	
	private HandlerRegistration attachHandlerRegistration;
	
	public UserNameTextField() {
		setWidth(175);
		attachHandlerRegistration = this.addAttachHandler(event -> {
			if (event.isAttached())
				Scheduler.get().scheduleDeferred(UserNameTextField.this::focus);
		});
	}
	
	@Override
	public void disposeBean() {
		attachHandlerRegistration.removeHandler();
	}
}
