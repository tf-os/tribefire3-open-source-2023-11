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
package com.braintribe.gwt.gmview.client;

import com.braintribe.gwt.ioc.client.DisposableBean;
import com.sencha.gxt.widget.core.client.ContentPanel;

/**
 * {@link ContentPanel} implementation which is also a {@link DisposableBean}.
 * If the child widget from {@link #getWidget()} is also a {@link DisposableBean}, then it will get disposed.
 * 
 * @author michel.docouto
 *
 */
public class DisposableContentPanel extends ContentPanel implements DisposableBean {

	@Override
	public void disposeBean() throws Exception {
		if (getWidget() instanceof DisposableBean)
			((DisposableBean) getWidget()).disposeBean();
	}

}
