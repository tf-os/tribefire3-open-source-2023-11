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
package tribefire.platform.impl.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.util.servlet.HttpFilter;

public class CaptureFilter implements HttpFilter {
	private static final Logger logger = Logger.getLogger(CaptureFilter.class);

	private File captureDir;

	@Required
	@Configurable
	public void setCaptureDir(File captureDir) {
		this.captureDir = captureDir;
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
	
		// boolean captureRequest = getBooleanHeader(request, "Capture-Request");
		boolean captureResponse = getBooleanHeader(request, "Capture-Response");
		
		if (!captureResponse) {
			chain.doFilter(request, response);
			return;
		}
		
		String callId = request.getHeader("Call-Id");
		
		if (callId == null)
			callId = UUID.randomUUID().toString();
		
		try (CapturedHttpServletResponse capturedHttpServletResponse = new CapturedHttpServletResponse(response, request.getRequestURI(), callId)) {
			chain.doFilter(request, capturedHttpServletResponse);
		}
	}

	private static boolean getBooleanHeader(HttpServletRequest request, String name) {
		String value = request.getHeader(name);

		if (value == null)
			return false;

		return Boolean.TRUE.toString().equals(value);
	}

	private static class CaptureOutputStream extends ServletOutputStream {
		private OutputStream out;
		private OutputStream cOut;

		public CaptureOutputStream(OutputStream out, OutputStream cOut) {
			super();
			this.out = out;
			this.cOut = cOut;
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			cOut.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			cOut.write(b, off, len);
		}
	}

	private class CapturedHttpServletResponse extends HttpServletResponseWrapper implements AutoCloseable {
		private CaptureOutputStream out;
		private	OutputStream cOut;
		private PrintWriter printWriter;
		private String callId;
		private String path;

		public CapturedHttpServletResponse(HttpServletResponse response, String path, String callId) {
			super(response);
			this.path = path;
			this.callId = callId;
		}

		private CaptureOutputStream createOutputStream() throws IOException {
			return out = new CaptureOutputStream(getResponse().getOutputStream(), openCaptureOutputStream());
		}

		private OutputStream openCaptureOutputStream() throws FileNotFoundException {
			File captureFolder = new File(captureDir, path);
			captureFolder.mkdirs();
			File captureFile = new File(captureFolder, callId);
			return cOut = new BufferedOutputStream(new FileOutputStream(captureFile));
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if (this.printWriter != null)
				throw new IllegalStateException("PrintWriter obtained already - cannot get OutputStream");
			
			if (this.out != null)
				return this.out;

			return createOutputStream();
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			if (this.printWriter != null)
				return this.printWriter;

			if (this.out != null)
				throw new IllegalStateException("OutputStream obtained already - cannot get PrintWriter");

			return this.printWriter = new PrintWriter(
					new OutputStreamWriter(createOutputStream(), getResponse().getCharacterEncoding()));
		}
		
		@Override
		public void close() {
			try {
				if (cOut != null)
					cOut.close();
			}
			catch (IOException e) {
				logger.error("Could not close capture file output for path [" + path + "] and call id [" + callId +"]", e);
			}
		}

	}
}
