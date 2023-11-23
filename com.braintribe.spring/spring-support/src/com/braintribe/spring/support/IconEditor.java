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

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class IconEditor extends PropertyEditorSupport implements
		ApplicationContextAware {
	private ApplicationContext applicationContext;
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public String getAsText() {
		return "";
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null || text.trim().length() == 0) {
			setValue(null);
		} else {
			try {
				Resource resource = applicationContext.getResource(text);
				URL url = resource.getURL();
				ImageIcon icon = new ImageIcon(url);
				setValue(icon);
			} catch (IOException e) {
				throw new IllegalArgumentException("error while building url from: " + text, e);
			}
		}
	}
}
