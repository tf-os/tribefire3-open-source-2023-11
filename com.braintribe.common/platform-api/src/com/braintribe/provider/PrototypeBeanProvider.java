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
package com.braintribe.provider;

public abstract class PrototypeBeanProvider<T> extends AbstractBeanProvider<T> {
	private static BeanLifeCycleExpert defaultBeanLifeCycleExpert = null;
	private BeanLifeCycleExpert beanLifeCycleExpert;

	private boolean isAbstract = false;

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public static void setDefaultBeanLifeCycleExpert(BeanLifeCycleExpert defaultLifeCycleExpert) {
		defaultBeanLifeCycleExpert = defaultLifeCycleExpert;
	}

	public PrototypeBeanProvider() {
		this(defaultBeanLifeCycleExpert);
	}

	public PrototypeBeanProvider(BeanLifeCycleExpert beanLifeCycleExpert) {
		this.beanLifeCycleExpert = beanLifeCycleExpert;
	}

	@Override
	public T get() throws RuntimeException {
		T instance = null;
		try {
			ensurePreconditions();
			instance = create();
			if (!isAbstract() && beanLifeCycleExpert != null) {
				beanLifeCycleExpert.intializeBean(instance);
			}
			ensureAttachmentsInstantiated();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return instance;
	}
}
