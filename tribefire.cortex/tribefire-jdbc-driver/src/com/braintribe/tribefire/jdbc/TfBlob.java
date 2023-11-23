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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;

/**
 * The Class TfBlob.
 *
 */
public class TfBlob implements Blob {

	/** The baos. */
	protected ByteArrayOutputStream baos = new ByteArrayOutputStream();

	/* (non-Javadoc)
	 * @see java.sql.Blob#length()
	 */
	@Override
	public long length() throws SQLException {
		return baos.size();
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#getBytes(long, int)
	 */
	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		return Arrays.copyOfRange(baos.toByteArray(), (int) pos, (int) (pos+length));
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#getBinaryStream()
	 */
	@Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(baos.toByteArray());
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#position(byte[], long)
	 */
	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#position(java.sql.Blob, long)
	 */
	@Override
	public long position(Blob pattern, long start) throws SQLException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#setBytes(long, byte[])
	 */
	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		return setBytes(pos, bytes, 0, bytes.length);
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#setBytes(long, byte[], int, int)
	 */
	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		try {
			ByteArrayOutputStream b2 = new ByteArrayOutputStream();
			if (pos > 0) {
				b2.write(baos.toByteArray(), 0, (int) pos);
			}
			b2.write(bytes, offset, len);
			baos = b2;
		} catch(Exception e) {
			throw new SQLException("Could not set Bytes.", e);
		}
		return bytes.length;
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#setBinaryStream(long)
	 */
	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		try {
			ByteArrayOutputStream b2 = new ByteArrayOutputStream();
			if (pos > 0) {
				b2.write(baos.toByteArray(), 0, (int) pos);
			}
			baos = b2;
		} catch(Exception e) {
			throw new SQLException("Could not set Bytes.", e);
		}
		return baos;
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#truncate(long)
	 */
	@Override
	public void truncate(long len) throws SQLException {
		try {
			ByteArrayOutputStream b2 = new ByteArrayOutputStream();
			if (len > 0) {
				b2.write(baos.toByteArray(), 0, (int) len);
			}
			baos = b2;
		} catch(Exception e) {
			throw new SQLException("Could not truncate.", e);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#free()
	 */
	@Override
	public void free() throws SQLException {
		//Nothing to do
	}

	/* (non-Javadoc)
	 * @see java.sql.Blob#getBinaryStream(long, long)
	 */
	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		byte[] byteArray = baos.toByteArray();
		byte[] copyOfRange = Arrays.copyOfRange(byteArray, (int) pos, (int) (pos+length)); 
		return new ByteArrayInputStream(copyOfRange);
	}

}
