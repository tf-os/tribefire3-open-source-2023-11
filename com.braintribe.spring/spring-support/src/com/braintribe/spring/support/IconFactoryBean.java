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
package com.braintribe.spring.support;

import java.net.URL;

import javax.swing.ImageIcon;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class IconFactoryBean implements FactoryBean {
	private URL url;
	
	@Required
	public void setUrl(URL url) {
		this.url = url;
	}

	public Object getObject() throws Exception {
		return new ImageIcon(url);
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return ImageIcon.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
