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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractBeanProvider<T> implements Supplier<T> {
	private List<Supplier<?>> attachedProviders = null;
	private List<Supplier<?>> dependsOnProviders = null;

	public abstract T create() throws Exception;

	protected void attach(Supplier<?> provider) {
		if (attachedProviders == null) {
			attachedProviders = new ArrayList<>();
		}
		attachedProviders.add(provider);
	}

	protected void attachTo(Supplier<?> provider) {
		AbstractBeanProvider<?> abstractBeanProvider = (AbstractBeanProvider<?>) provider;
		abstractBeanProvider.attach(this);
	}

	protected void dependsOn(Supplier<?> provider) {
		if (dependsOnProviders == null) {
			dependsOnProviders = new ArrayList<>();
		}
		dependsOnProviders.add(provider);
	}

	protected void ensurePreconditions() throws RuntimeException {
		if (dependsOnProviders != null) {
			for (Supplier<?> provider : dependsOnProviders) {
				provider.get();
			}
		}
	}

	protected void ensureAttachmentsInstantiated() throws RuntimeException {
		if (attachedProviders != null) {
			for (Supplier<?> provider : attachedProviders) {
				provider.get();
			}
		}
	}
}
