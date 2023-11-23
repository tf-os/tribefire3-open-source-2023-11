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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.braintribe.cfg.Configurable;

public class DeferredSpringProxy<T> implements FactoryBean<T>, BeanFactoryAware, InvocationHandler {

	private boolean singleton = true;
	private String beanId;
	private BeanFactory beanFactory;
	private Class<T> interfaceClass;
	private T proxy;
	private T delegate;
	
	@Required
	@Configurable
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	@Configurable
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	@Required
	@Configurable
	public void setInterfaceClass(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized T getObject() throws Exception {
		if (proxy == null) {
			proxy = (T)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{interfaceClass}, this);
		}
		return proxy;
	}
	
	@SuppressWarnings("unchecked")
	protected T getDelegate() {
		if (delegate == null) {
			delegate = (T) beanFactory.getBean(beanId);
		}
		
		return delegate;
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	@Override
	public boolean isSingleton() {
		return singleton;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(getDelegate(), args);
	}
}
