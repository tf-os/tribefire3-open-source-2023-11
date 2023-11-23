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

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;


public class AttachBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
	private BeanFactory beanFactory;
	private Map<String, List<String>> attachments = null; 
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public boolean postProcessAfterInstantiation(Object bean, String beanName)
			throws BeansException {
		return true;
	}


	public PropertyValues postProcessPropertyValues(PropertyValues pvs,
			PropertyDescriptor[] pds, Object bean, String beanName)
			throws BeansException {
		return pvs;
	}
	
	@SuppressWarnings("unchecked")
	public Object postProcessBeforeInstantiation(Class beanClass,
			String beanName) throws BeansException {
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getAttachments() {
		if (attachments == null) {
			attachments = new HashMap<String, List<String>>();
			
			DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)beanFactory;
			Map<String, Attach> attachInstances = defaultListableBeanFactory.getBeansOfType(Attach.class, false, false);
			
			for (Attach attach: attachInstances.values()) {
				String masterBeanName = attach.getMasterBeanName();
				
				List<String> beanAttachments = attachments.get(masterBeanName);
				if (beanAttachments == null) {
					beanAttachments = new ArrayList<String>();
					attachments.put(masterBeanName, beanAttachments);
				}
				
				beanAttachments.add(attach.getBeanName());
			}
		}

		return attachments;
	}
	
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		
		Map<String, List<String>> attachments = getAttachments();
		
		List<String> attachtedBeanNames = attachments.get(beanName);
		
		if (attachtedBeanNames != null) {
			for (String attachedBeanName: attachtedBeanNames) {
				beanFactory.getBean(attachedBeanName);
			}
		}
		
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}
	

}
