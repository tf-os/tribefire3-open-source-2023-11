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
package com.braintribe.gwt.ioc.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.braintribe.provider.BeanLifeCycleExpert;

/**
 * The LifeCycleManager holds a set of objects that are stored to be released
 * if some scope calls {@link #disposeAllBeans()}
 * 
 * @author Dirk
 *
 */
public class LifeCycleManager {
	public static BeanLifeCycleExpert beanLifeCycleExpert = new IocBeanLifeCycleExpert();
	
	private Set<Object> objects;
	
	/**
	 * Intializes a given object with the {@link #beanLifeCycleExpert} and stores
	 * it for later disposing
	 * @return the intialized object
	 */
	public <T> T initBean(T object) {
		if (objects == null) objects = new HashSet<Object>();
		try {
			beanLifeCycleExpert.intializeBean(object);
			objects.add(object);
			return object;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("error while intializing a bean", e);
		}
	}
	
	/**
	 * Disposes the given object with the {@link #beanLifeCycleExpert} if the object
	 * is known to this LifeCycleManager and removes it from known objects.
	 * @return true if the object was known and could be disposed otherwise false
	 */
	public boolean disposeBean(Object object) {
		if (objects != null && objects.contains(object)) {
			try {
				beanLifeCycleExpert.disposeBean(object);
				objects.remove(object);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error while disposing a bean", e);
			}
		}
		else return false;
	}
	
	/**
	 * disposes and removes all known beans
	 */
	public void disposeAllBeans() {
		if (objects != null) {
			try {
				Iterator<Object> it = objects.iterator();
				while (it.hasNext()) {
					Object object = it.next();
					beanLifeCycleExpert.disposeBean(object);
					it.remove();
				}
				objects.clear();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error while disposing a bean", e);
			}
		}
	}
}
