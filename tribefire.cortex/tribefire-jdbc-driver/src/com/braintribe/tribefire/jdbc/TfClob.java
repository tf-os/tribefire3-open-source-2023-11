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
package com.braintribe.tribefire.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.commons.io.output.StringBuilderWriter;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;

/**
 * The Class TfClob.
 *
 */
public class TfClob implements Clob {

	protected static Logger logger = Logger.getLogger(TfClob.class);
	protected StringBuilder sb = new StringBuilder();
	
	/* (non-Javadoc)
	 * @see java.sql.Clob#length()
	 */
	@Override
	public long length() throws SQLException {
		return sb.length();
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#getSubString(long, int)
	 */
	@Override
	public String getSubString(long pos, int length) throws SQLException {
		return sb.substring((int) pos, (int) pos+length);
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		return new StringReader(sb.toString());
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		try {
			return new ByteArrayInputStream(sb.toString().getBytes("ASCII"));
		} catch (Exception e) {
			throw new SQLException("Could not get ASCII stream.", e);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#position(java.lang.String, long)
	 */
	@Override
	public long position(String searchstr, long start) throws SQLException {
		return sb.indexOf(searchstr, (int) start); 
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#position(java.sql.Clob, long)
	 */
	@Override
	public long position(Clob searchstr, long start) throws SQLException {
		String searchString = null;
		Reader r = null;
		try {
			r = searchstr.getCharacterStream();
			searchString = IOTools.slurp(r);
			r.close();
		} catch(Exception e) {
			throw new SQLException("Could not read content from searchstr "+searchstr, e);
		} finally {
			IOTools.closeCloseable(r, logger);
		}

		return position(searchString, start);
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#setString(long, java.lang.String)
	 */
	@Override
	public int setString(long pos, String str) throws SQLException {
		sb.insert((int) pos, str); 
		return str.length();
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#setString(long, java.lang.String, int, int)
	 */
	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
		sb.insert((int) pos, str.toCharArray(), offset, len); 
		return len;
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#setAsciiStream(long)
	 */
	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#setCharacterStream(long)
	 */
	@Override
	public Writer setCharacterStream(long pos) throws SQLException {
		sb.setLength((int) pos);
		StringBuilderWriter sbw = new StringBuilderWriter(this.sb);
		return sbw;
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#truncate(long)
	 */
	@Override
	public void truncate(long len) throws SQLException {
		sb.setLength((int) len);
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#free()
	 */
	@Override
	public void free() throws SQLException {
		//Nothing to do
	}

	/* (non-Javadoc)
	 * @see java.sql.Clob#getCharacterStream(long, long)
	 */
	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		StringReader sr = new StringReader(sb.substring((int) pos, (int) (pos+length)));
		return sr;
	}

}
