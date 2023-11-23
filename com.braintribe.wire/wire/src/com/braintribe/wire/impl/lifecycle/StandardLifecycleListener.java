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
package com.braintribe.wire.impl.lifecycle;

import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.impl.util.Exceptions;

public class StandardLifecycleListener implements LifecycleListener {

	public static final StandardLifecycleListener INSTANCE = new StandardLifecycleListener();
	
	@Override
	public void onPostConstruct(InstanceHolder beanHolder, Object bean) {
		if (bean instanceof InitializationAware) {
			((InitializationAware)bean).postConstruct();
		}
	}

	@Override
	public void onPreDestroy(InstanceHolder beanHolder, Object bean) {
		if (bean instanceof AutoCloseable) {
			try {
				((AutoCloseable)bean).close();
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while calling AutoClosable.close", IllegalStateException::new);
			}
		} else if (bean instanceof DestructionAware) {
			((DestructionAware)bean).preDestroy();
		}
	}

}
