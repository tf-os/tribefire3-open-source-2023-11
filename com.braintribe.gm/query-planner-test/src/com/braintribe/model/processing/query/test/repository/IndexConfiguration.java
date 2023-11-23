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
package com.braintribe.model.processing.query.test.repository;

import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.smood.population.info.IndexInfoImpl;

/**
 * 
 */
public class IndexConfiguration {

	final Set<IndexInfo> indexInfos = newSet();
	final Map<String, Map<String, IndexInfo>> indexInfoMap = newMap();

	public void addMetricIndex(EntityType<?> et, String propertyName) {
		addIndex(et, propertyName, true);
	}

	public void addLookupIndex(EntityType<?> et, String propertyName) {
		addIndex(et, propertyName, false);
	}

	private void addIndex(EntityType<?> et, String propertyName, boolean metric) {
		IndexInfoImpl indexInfo = new IndexInfoImpl();

		indexInfo.setEntitySignature(et.getTypeSignature());
		indexInfo.setIndexId(indexId(et, propertyName));
		indexInfo.setPropertyName(propertyName);
		indexInfo.setHasMetric(metric);

		indexInfos.add(indexInfo);

		acquireMap(indexInfoMap, et.getTypeSignature()).put(propertyName, indexInfo);
	}

	public static String indexId(EntityType<?> et, String propertyName) {
		return et.getTypeSignature() + "#" + propertyName;
	}

}
