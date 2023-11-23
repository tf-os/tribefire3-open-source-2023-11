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
package com.braintribe.spring.support.factory;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;


public class SpringSwitch<T> implements FactoryBean<T>, BeanFactoryAware {
	
	private Class<T> type;
	private BeanFactory beanFactory;
	private List<SpringSwitchRule> rules;
	private String selectedBeanId;
	private boolean singleton = false;

	public SpringSwitch() {
	}
	
	public SpringSwitch(Class<T> type) {
		this.type = type;
	}
	
	public String getSelectedBeanId() {
		if (selectedBeanId == null) {
			for (SpringSwitchRule rule: rules) {
				try {
					if (Boolean.TRUE.equals(rule.getSelector().get())) {
						selectedBeanId = rule.getBeanId();
						break;
					}
				} catch (RuntimeException e) {
					throw new RuntimeException("error while checking rules for spring switch", e);
				}
			}
		}

		return selectedBeanId;
	}
	
	@Configurable
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	@Configurable @Required
	public void setRules(List<SpringSwitchRule> rules) {
		this.rules = rules;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		String selectedBeanId = getSelectedBeanId();
		if (selectedBeanId == null)
			throw new RuntimeException("no beanId selected");
		
		return (T)beanFactory.getBean(selectedBeanId);
	}

	@Override
	public Class<?> getObjectType() {
		return type;
	}

	@Override
	public boolean isSingleton() {
		return singleton;
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
}
