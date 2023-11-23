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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;


public class EarlyReferenceFixBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements BeanFactoryAware {

	private BeanFactory beanFactory;
	
	private static Map<BeanFactory, Map<String, Object>> earlyReferences = new HashMap<BeanFactory, Map<String,Object>>();

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public static Map<String, Object> getEarlyReferences(BeanFactory beanFactory) {
		Map<String, Object> refs = earlyReferences.get(beanFactory);
		if (refs == null) {
			refs = new HashMap<String, Object>();
			earlyReferences.put(beanFactory, refs);
		}
		return refs;
	}
	
	public static Object getEarlyReference(BeanFactory beanFactory, String beanName) {
		return getEarlyReferences(beanFactory).get(beanName);
	}
	
	protected Map<String, Object> getEarlyReferences() {
		return getEarlyReferences(beanFactory);
	}
	
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName)
			throws BeansException {
		getEarlyReferences().put(beanName, bean);
		return super.postProcessAfterInstantiation(bean, beanName);
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		getEarlyReferences().remove(beanName);
		return super.postProcessAfterInitialization(bean, beanName);
	}
}
