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
package com.braintribe.wire.impl.scope.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.braintribe.wire.impl.scope.AbstractWireScope;

public abstract class AbstractSingletonScope extends AbstractWireScope {

	private static final Logger logger = Logger.getLogger(AbstractSingletonScope.class.getName());

	protected List<AbstractSingletonInstanceHolder> holders = new ArrayList<>();

	@Override
	public void close() throws Exception {
		for (ListIterator<AbstractSingletonInstanceHolder> it = holders.listIterator(holders.size()); it.hasPrevious();) {
			AbstractSingletonInstanceHolder holder = it.previous();
			try {
				logger.finest(() -> "Destroying bean " + holder.space().getClass().getName() + ":" + holder.name());
				holder.onDestroy();
			} catch (Exception e) {
				reportError(holder, e);
			} catch (IllegalAccessError e) {
				// This might occur during shutdown
				reportError(holder, e);
			} catch (Error e) {
				reportError(holder, e);
				throw e;
			}

		}
		logger.finest(() -> "Finished closing of scope: " + this.getClass().getName());
	}

	private void reportError(AbstractSingletonInstanceHolder holder, Throwable e) {
		logger.log(Level.SEVERE, "Exception while destroying bean " + holder.space().getClass().getName() + ":" + holder.name(), e);
	}

	public void appendBean(AbstractSingletonInstanceHolder singletonBeanHolder) {
		holders.add(singletonBeanHolder);
	}
}
