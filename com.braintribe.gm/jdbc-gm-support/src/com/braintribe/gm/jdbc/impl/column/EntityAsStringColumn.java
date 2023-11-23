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
package com.braintribe.gm.jdbc.impl.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.AbstractDelegatingGmColumn;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @author peter.gazdik
 */
public class EntityAsStringColumn<T extends GenericEntity> extends AbstractDelegatingGmColumn<T, String> {

	private final EntityType<T> entityType;
	private final GmCodec<Object, String> codec;

	public EntityAsStringColumn(AbstractGmColumn<String> stringColumn, EntityType<T> entityType, GmCodec<Object, String> codec) {
		super(stringColumn);
		this.entityType = entityType;
		this.codec = codec;
	}

	@Override
	protected Class<T> type() {
		return entityType.getJavaType();
	}

	@Override
	protected T tryGetValue(ResultSet rs, GmSelectionContext context) throws Exception {
		String s = delegate.tryGetValue(rs, context);
		return decodeFromString(s);
	}

	@Override
	protected void tryBind(PreparedStatement statement, int index, T entity) throws Exception {
		String s = encodeAsString(entity);
		delegate.tryBind(statement, index, s);
	}

	private String encodeAsString(T entity) {
		return codec.encode(entity);
	}

	private T decodeFromString(String s) {
		return (T) codec.decode(s);
	}

}
