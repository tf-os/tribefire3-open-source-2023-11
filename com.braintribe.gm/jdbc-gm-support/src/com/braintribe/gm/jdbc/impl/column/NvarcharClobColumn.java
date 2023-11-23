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

import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.NO_LOB;
import static com.braintribe.gm.jdbc.api.GmLobLoadingMode.ONLY_LOB;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.gm.jdbc.api.GmLobLoadingMode;
import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.MultiGmColumn;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */
public class NvarcharClobColumn extends MultiGmColumn<String> {

	private final JdbcDialect dialect;
	private final int maxChars;

	public NvarcharClobColumn(String name, JdbcDialect dialect, int maxChars) {
		super(name);
		this.dialect = dialect;
		this.maxChars = maxChars;
	}

	@Override
	public Stream<String> streamSqlColumnDeclarations() {
		return Stream.of( //
				strColumnName() + " " + dialect.nvarchar(maxChars), //
				clobColumnName() + " " + dialect.clobType());
	}

	@Override
	public List<String> getSqlColumns() {
		return asList( //
				strColumnName(), //
				clobColumnName());
	}

	private String strColumnName() {
		return name;
	}

	private String clobColumnName() {
		return name + "_clob";
	}

	@Override
	protected Class<String> type() {
		return String.class;
	}

	@Override
	protected boolean tryIsStoredAsLob(ResultSet rs) throws SQLException {
		return rs.getClob(clobColumnName()) != null;
	}

	@Override
	protected String tryGetValue(ResultSet rs, GmSelectionContext context) throws Exception {
		GmLobLoadingMode lobLoadingMode = context.lobLoadingMode(this);

		String result = rs.getString(name);
		if (result != null)
			return lobLoadingMode == ONLY_LOB ? null : result;

		Clob clob = rs.getClob(clobColumnName());
		if (clob == null || clob.length() == 0 || lobLoadingMode == NO_LOB)
			return null;

		try (Reader reader = clob.getCharacterStream()) {
			StringWriter sw = new StringWriter();
			IOTools.pump(reader, sw);
			return sw.toString();
		}
	}

	@Override
	protected void tryBind(PreparedStatement ps, int index, String value) throws SQLException {
		if (value == null || value.length() <= maxChars) {
			ps.setString(index, value);
			ps.setClob(index + 1, null, 0);
		} else {
			StringReader reader = new StringReader(value);
			ps.setString(index, null);
			ps.setClob(index + 1, reader, value.length());
		}
	}
}
