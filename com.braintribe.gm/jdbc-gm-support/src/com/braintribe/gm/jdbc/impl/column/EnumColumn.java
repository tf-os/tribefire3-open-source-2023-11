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

import static com.braintribe.utils.lcd.CollectionTools2.index;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.AbstractDelegatingGmColumn;

/**
 * @author peter.gazdik
 */
public class EnumColumn<E extends Enum<E>> extends AbstractDelegatingGmColumn<E, String> {

	private final Map<String, E> constants;
	private final Class<E> enumClass;

	public EnumColumn(AbstractGmColumn<String> stringColumn, Class<E> enumClass) {
		super(stringColumn);
		this.enumClass = enumClass;

		this.constants = index(enumClass.getEnumConstants()).by(Enum::name).unique();
	}

	@Override
	protected Class<E> type() {
		return enumClass;
	}

	@Override
	protected E tryGetValue(ResultSet rs, GmSelectionContext context) throws Exception {
		String enumName = delegate.tryGetValue(rs, context);
		return constants.get(enumName);
	}

	@Override
	protected void tryBind(PreparedStatement statement, int index, E enumConstant) throws Exception {
		String s = enumConstant == null ? null : enumConstant.name();
		delegate.tryBind(statement, index, s);
	}

}
