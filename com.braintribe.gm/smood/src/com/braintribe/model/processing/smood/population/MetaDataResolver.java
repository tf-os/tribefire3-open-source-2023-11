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
package com.braintribe.model.processing.smood.population;

import static com.braintribe.utils.lcd.CollectionTools2.newConcurrentMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.query.Index;
import com.braintribe.model.meta.data.query.IndexType;
import com.braintribe.model.processing.meta.cmd.CmdResolver;

/**
 * 
 */
class MetaDataResolver {

	private CmdResolver cmdResolver;
	private final Map<String, IndexType> indexTypeCache = newConcurrentMap();

	public void setCmdResolver(CmdResolver cmdResolver) {
		this.cmdResolver = cmdResolver;
		this.indexTypeCache.clear();
	}

	public IndexType resolveIndexType(EntityType<?> et, Property p) {
		return resolveIndexType(et, p.getName());
	}

	/**
	 * TODO DOCUMENTATION
	 * <p>
	 * Note that the result of this method is being cached. We can afford that, since it makes no sense for the CMD resolver to resolve different
	 * values on different runs.
	 */
	public IndexType resolveIndexType(EntityType<?> et, String propertyName) {
		String indexId = SmoodIndexTools.indexId(et, propertyName);

		IndexType result = indexTypeCache.get(indexId);
		if (result == null) {
			result = resolveIndexTypeHelper(et, propertyName);
			indexTypeCache.put(indexId, result);
		}

		return result;
	}

	private IndexType resolveIndexTypeHelper(EntityType<?> et, String p) {
		Index md = resolveIndexMdSafe(et, p);
		if (md == null)
			return IndexType.none;

		IndexType indexType = md.getIndexType();

		return indexType == null ? IndexType.auto : indexType;
	}

	private Index resolveIndexMdSafe(EntityType<?> et, String p) {
		if (cmdResolver == null)
			return null;

		try {
			return cmdResolver.getMetaData().lenient(true).entityType(et).property(p).meta(Index.T).exclusive();

		} catch (Exception e) {
			return null;
		}
	}

	public boolean isUnique(EntityType<?> et, Property p) {
		return cmdResolver == null ? false : cmdResolver.getMetaData().lenient(true).entityType(et).property(p).is(Unique.T);
	}

}
