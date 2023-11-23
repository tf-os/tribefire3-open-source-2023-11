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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.springframework.beans.factory.FactoryBean;

import com.braintribe.cfg.Required;

public class JndiVariableValue<T> implements FactoryBean<T> {

	protected Class<T> clazz = null;
	protected String jndiPath = null;
	
	@SuppressWarnings("unchecked")
	public JndiVariableValue() {
		this.clazz = (Class<T>) String.class;
	}

	public JndiVariableValue(Class<T> cls) {
		this.clazz = cls;
	}
	
	@Override
	public T getObject() throws Exception {
		Context ctx = new InitialContext();
		Context envCtx = (Context) ctx.lookup("java:comp/env");
		@SuppressWarnings("unchecked")
		T value = (T) envCtx.lookup(this.jndiPath);
		return value;
	}

	@Override
	public Class<?> getObjectType() {
		return this.clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Required
	public void setJndiPath(String jndiPath) {
		this.jndiPath = jndiPath;
	}

}
