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
package com.braintribe.web.multipart.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.braintribe.exception.Exceptions;
import com.braintribe.utils.ArrayTools;
import com.braintribe.web.multipart.api.PartHeader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.streams.BasicDelegateOutputStream;

abstract public class FormDataWriterWithBoundary extends AbstractFormDataWriter {
	private final byte CRLFPlusBoundaryAsBytes[];
	private final String boundary;


	protected FormDataWriterWithBoundary(OutputStream outputStream, String boundary) {
		super(outputStream);

		this.boundary = boundary;

		if (boundary != null) {
			try {
				byte coreBoundaryAsBytes[] = boundary.getBytes("ISO-8859-1");
				byte boundaryAsBytes[] = (byte[]) ArrayTools.merge(MULTIPART_HYPHENS, coreBoundaryAsBytes);
				CRLFPlusBoundaryAsBytes = (byte[]) ArrayTools.merge(HTTP_LINEBREAK, MULTIPART_HYPHENS, coreBoundaryAsBytes);
				out.write(boundaryAsBytes);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("charset ISO-8859-1 not supported", e);
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Could not write initial boundary to output stream");
			}
		}else {
			CRLFPlusBoundaryAsBytes = null;
		}
	}
	
	public String getBoundary() {
		return boundary;
	}

	protected void writePartClosingBoundary(OutputStream out) {
		try {
			out.write(CRLFPlusBoundaryAsBytes);
			out.flush();
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not write boundary closing the part");
		}
	}
	
	protected class DirectPartWriter extends DelegatingPartHeader implements PartWriter {

		public DirectPartWriter(PartHeader delegate) {
			super(delegate);
		}

		@Override
		public OutputStream outputStream() {
			return new BasicDelegateOutputStream(out) {
				private boolean closed;

				@Override
				public void close() throws IOException {
					if (!closed) {
						out.flush();
						freeCurrentPartWriter();
						writePartClosingBoundary(out);
						closed = true;
					}
				}
			};
		}

	}

}
