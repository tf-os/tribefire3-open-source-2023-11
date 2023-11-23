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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import com.braintribe.gm.jdbc.api.GmLobLoadingMode;
import com.braintribe.gm.jdbc.api.GmSelectionContext;
import com.braintribe.gm.jdbc.impl.column.AbstractGmColumn.MultiGmColumn;
import com.braintribe.logging.Logger;
import com.braintribe.model.resource.Resource;
import com.braintribe.util.jdbc.dialect.JdbcDialect;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.ReaderInputStream;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

/**
 * @author peter.gazdik
 */
public class ResourceColumn extends MultiGmColumn<Resource> {

	public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";

	private static final Logger log = Logger.getLogger(ResourceColumn.class);

	private final JdbcDialect dialect;
	private final int maxChars;
	private final StreamPipeFactory streamPipeFactory;

	private final Map<PreparedStatement, InputStream> openStreams = new WeakHashMap<>();

	public ResourceColumn(String name, JdbcDialect dialect, int maxChars, StreamPipeFactory streamPipeFactory) {
		super(name);
		this.dialect = dialect;
		this.maxChars = maxChars;
		this.streamPipeFactory = streamPipeFactory;
	}

	@Override
	public Stream<String> streamSqlColumnDeclarations() {
		return Stream.of( //
				strColumnName() + " " + dialect.nvarchar(maxChars), //
				blobColumnName() + " " + dialect.blobType());
	}

	@Override
	public List<String> getSqlColumns() {
		return asList( //
				strColumnName(), //
				blobColumnName());
	}

	private String strColumnName() {
		return name;
	}

	private String blobColumnName() {
		return name + "_blob";
	}

	@Override
	protected Class<Resource> type() {
		return Resource.class;
	}

	@Override
	protected boolean tryIsStoredAsLob(ResultSet rs) throws SQLException {
		return rs.getBlob(blobColumnName()) != null;
	}

	@Override
	protected Resource tryGetValue(ResultSet rs, GmSelectionContext context) throws Exception {
		// TODO enrich with size/mimeType...
		return getResourceInstance(rs, context);
	}

	private Resource getResourceInstance(ResultSet rs, GmSelectionContext context) throws Exception {
		GmLobLoadingMode lobLoadingMode = context.lobLoadingMode(this);

		String text = rs.getString(name);
		if (text != null)
			return lobLoadingMode == ONLY_LOB ? null : Resource.createTransient(() -> new ReaderInputStream(new StringReader(text)));

		Blob blob = rs.getBlob(blobColumnName());
		if (blob == null || lobLoadingMode == NO_LOB)
			return null;

		StreamPipe pipe = streamPipeFactory.newPipe("Value of column: " + name);

		try (InputStream is = blob.getBinaryStream(); //
				OutputStream os = pipe.openOutputStream()) {
			IOTools.pump(is, os);
		}

		return Resource.createTransient(pipe::openInputStream);
	}

	@Override
	protected void tryBind(PreparedStatement ps, int index, Resource value) throws Exception {
		if (value == null) {
			ps.setString(index, null);
			ps.setBlob(index + 1, null, 0);
			return;
		}

		String text = toShortText(value);

		if (text != null) {
			ps.setString(index, text);
			ps.setBlob(index + 1, null, 0);
		} else {
			ps.setString(index, null);
			ps.setBlob(index + 1, inputStream(ps, value));
		}
	}

	private String toShortText(Resource resource) throws Exception {
		String mimeType = resource.getMimeType();
		if (!TEXT_PLAIN_MIME_TYPE.equals(mimeType))
			return null;

		Long size = resource.getFileSize();
		// If size is unknown, we do not try to load the resource.
		// If size is at most three times the maxChars, there is a theoretical chance the string has few enough chars to fit within maxChars
		if (size == null || size > 3 * maxChars)
			return null;

		String text = toString(resource);
		if (text.length() > maxChars)
			return null;

		return text;
	}

	private String toString(Resource resource) throws Exception {
		return NvarcharBlobColumn.inputStreamToString(resource::openStream);
	}

	private InputStream inputStream(PreparedStatement ps, Resource resource) {
		InputStream result = resource.openStream();

		openStreams.put(ps, result);

		return result;
	}

	@Override
	public void afterStatementExecuted(PreparedStatement ps) {
		InputStream is = openStreams.get(ps);
		if (is != null)
			try {
				is.close();
			} catch (Exception e) {
				log.warn("Error while closing inputStream for Resource column '" + name + "'. Statement: " + ps.toString(), e);
			}
	}

}
