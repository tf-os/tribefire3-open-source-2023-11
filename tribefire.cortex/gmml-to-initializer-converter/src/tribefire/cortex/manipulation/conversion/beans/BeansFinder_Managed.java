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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Root beans are {@link NewBean}s which have dependencies on all the {@link NewBean}s in out pool. Only these need to be stated in the "initialize"
 * method.
 */
public class BeansFinder_Managed {

	public static void markManagedBeans(Set<NewBean> rootBeans, BeanRegistry beanRegistry) {
		new BeansFinder_Managed(rootBeans, beanRegistry).findEm();
	}

	private final Set<NewBean> rootBeans;
	private final BeanRegistry beanRegistry;

	private final Map<NewBean, Integer> beanToDependersCount = newMap();

	private BeansFinder_Managed(Set<NewBean> rootBeans, BeanRegistry beanRegistry) {
		this.rootBeans = rootBeans;
		this.beanRegistry = beanRegistry;
	}

	private void findEm() {
		indexDeps(rootBeans);
		index(beanRegistry.existingBeans);
		index(beanRegistry.newBeans);

		beanToDependersCount.entrySet().stream() //
				.filter(e -> e.getValue() > 1) //
				.map(e -> e.getKey()) //
				.forEach(nb -> nb.isManaged = true);
	}

	private void index(Collection<? extends EntityBean<?>> beans) {
		for (EntityBean<?> bean : beans)
			indexDeps(bean.deps);
	}

	private void indexDeps(Collection<? extends EntityBean<?>> deps) {
		for (EntityBean<?> dep : deps)
			countDepOn(dep);
	}

	private void countDepOn(EntityBean<?> dep) {
		if (dep instanceof ExistingBean)
			return;

		Integer i = beanToDependersCount.get(dep);
		i = i == null ? 1 : i + 1;
		beanToDependersCount.put((NewBean) dep, i);
	}

}
