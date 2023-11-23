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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class ProviderEditor extends PropertyEditorSupport implements BeanFactoryAware {
	private BeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@SuppressWarnings("unchecked")
	public String getAsText() {
		BeanFactoryProvider<Object> provider = (BeanFactoryProvider<Object>)getValue();
		return provider != null? provider.getBeanName(): "";
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null || text.trim().length() == 0) {
			setValue(null);
		} else {
			if (beanFactory.containsBean(text)) {
				BeanFactoryProvider<Object> provider = new BeanFactoryProvider<Object>();
				provider.setBeanFactory(beanFactory);
				provider.setBeanName(text);
				setValue(provider);
			} else
				throw new IllegalArgumentException("no bean with id " + text
						+ " found");
		}
	}
}
