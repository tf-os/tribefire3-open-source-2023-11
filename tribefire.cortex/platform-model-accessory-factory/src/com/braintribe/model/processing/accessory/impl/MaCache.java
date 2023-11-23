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
package com.braintribe.model.processing.accessory.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessory.api.PurgableModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.utils.lcd.NullSafe;

/**
 * @author peter.gazdik
 */
/* package */ class MaCache {

	private static final Logger log = Logger.getLogger(MaCache.class);

	private final BiFunction<String, String, PurgableModelAccessory> factory;
	private final String idName;
	private final String monitor;

	private Map<String, Map<String, PurgableModelAccessory>> cache;

	public MaCache(BiFunction<String, String, PurgableModelAccessory> factory, String idName) {
		this.factory = factory;
		this.idName = idName;
		this.monitor = new String("monitor." + idName);
	}

	public void setCacheModelAccessories(boolean cacheModelAccessories) {
		if (cacheModelAccessories)
			cache = new ConcurrentHashMap<>();
	}

	public ModelAccessory getModelAccessory(String id, String perspective) {
		requireNonNull(id, () -> idName + " cannot be null");

		if (cache == null)
			return factory.apply(id, perspective);

		String perspectiveKey = NullSafe.get(perspective, "<default>");

		Map<String, PurgableModelAccessory> perspectiveToAccessory = cache.computeIfAbsent(id, k -> new ConcurrentHashMap<>());

		PurgableModelAccessory result = perspectiveToAccessory.get(perspectiveKey);

		if (result != null)
			return result;

		synchronized (monitor) {
			result = perspectiveToAccessory.get(perspectiveKey);
			if (result == null) {
				result = factory.apply(id, perspective);
				perspectiveToAccessory.put(perspectiveKey, result);
				log.debug(() -> "Cached ModelAccessory for " + idName + "='" + id + "', perspective: '" + perspective + "'");
			}
		}

		return result;
	}

	public void onChange(String id) {
		log.debug(() -> "Received onChange notification for " + idName + "='" + id + "'");

		if (id != null && cache != null)
			purgeEntry(id);
		else
			log.trace(() -> "Ignoring purge request for " + idName + "='" + id + "'");
	}

	private void purgeEntry(String id) {
		Map<String, PurgableModelAccessory> cachedEntry = cache.remove(id);

		if (cachedEntry == null) {
			log.trace(() -> "Component changed but no entry to purge for " + idName + "='" + id + "'");
			return;
		}

		for (Entry<String, PurgableModelAccessory> entry : cachedEntry.entrySet()) {
			String perspectiveKey = entry.getKey();
			PurgableModelAccessory cachedAccessory = entry.getValue();

			log.debug(() -> "Component changed, purging entry for " + idName + "='" + id + "', perspective = '" + perspectiveKey + "' : "
					+ cachedAccessory);

			cachedAccessory.outdated();
		}

	}
}
