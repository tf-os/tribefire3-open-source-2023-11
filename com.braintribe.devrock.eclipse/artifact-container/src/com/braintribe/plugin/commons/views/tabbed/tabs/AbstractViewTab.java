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
package com.braintribe.plugin.commons.views.tabbed.tabs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.plugin.commons.views.TabItemImageListener;
import com.braintribe.plugin.commons.views.listener.ViewNotificationListener;

public abstract class AbstractViewTab implements ViewNotificationListener {

	protected TabItemImageListener tabImageListener;
	protected Display display;
	protected boolean active;
	protected boolean visible;
	protected AbstractViewTab instance;

	@Configurable @Required
	public void setTabImageListener(TabItemImageListener tabImageListener) {
		this.tabImageListener = tabImageListener;
	}

	public AbstractViewTab(Display display) {
		this.display = display;
		instance = this;
	}

	@Override
	public void acknowledgeVisibility(String key) {
		visible = true;
	}

	@Override
	public void acknowledgeInvisibility(String key) {
		visible = false;
	}
	
	public void acknowledgeActivation(){
		active = true;
	}
	public void acknowledgeDeactivation(){
		active = false;
	}
	
	public void dispose(){		
	}
		
	public abstract Composite createControl( Composite parent);
	
	

}
