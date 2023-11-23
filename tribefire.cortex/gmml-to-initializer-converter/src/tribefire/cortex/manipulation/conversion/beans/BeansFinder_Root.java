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
package tribefire.cortex.manipulation.conversion.beans;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;

import java.util.Set;

/**
 * Root beans are {@link NewBean}s which have dependencies on all the {@link NewBean}s in out pool. Only these need to be stated in the "initialize"
 * method.
 */
public class BeansFinder_Root {

	public static Set<NewBean> findRootBeans(BeanRegistry beanRegistry) {
		return new BeansFinder_Root(beanRegistry).findEm();
	}

	private final BeanRegistry beanRegistry;

	private final Set<NewBean> roots = newLinkedSet();
	private final Set<EntityBean<?>> handled = newLinkedSet();

	private BeansFinder_Root(BeanRegistry beanRegistry) {
		this.beanRegistry = beanRegistry;
	}

	private Set<NewBean> findEm() {
		for (NewBean newBean : beanRegistry.newBeans) {
			if (!handled.add(newBean))
				continue;

			removeAllDepsOf(newBean);
			roots.add(newBean);
		}

		return roots;
	}

	private void removeAllDepsOf(EntityBean<?> bean) {
		for (EntityBean<?> dep : bean.deps) {
			roots.remove(dep);

			if (handled.add(dep))
				removeAllDepsOf(dep);
		}
	}
}
