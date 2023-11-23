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
package tribefire.extension.antivirus.service.connector.clamav;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.braintribe.logging.Logger;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

import tribefire.extension.antivirus.connector.api.AbstractAntivirusConnector;

public class ClamScan {

	private static final Logger logger = Logger.getLogger(ClamScan.class);

	public static final int CHUNK_SIZE = 2048;
	private static final byte[] INSTREAM = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] PING = "zPING\0".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] STATS = "nSTATS\n".getBytes(StandardCharsets.US_ASCII);
	private int timeout;
	private String host;
	private int port;

	public ClamScan() {
	}

	public ClamScan(String host, int port, int timeout) {
		setHost(host);
		setPort(port);
		setTimeout(timeout);
	}

	public String stats() {
		return cmd(STATS);
	}

	public boolean ping() {
		return "PONG\0".equals(cmd(PING));
	}

	public String cmd(byte[] cmd) {
		DataOutputStream dos = null;
		StringBuilder response = new StringBuilder();

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(getHost(), getPort()));
			socket.setSoTimeout(this.timeout);

			dos = new DataOutputStream(socket.getOutputStream());
			dos.write(cmd);
			dos.flush();

			int read = CHUNK_SIZE;
			byte[] buffer = new byte[CHUNK_SIZE];

			try (InputStream is = socket.getInputStream()) {
				while (read > -1) {
					try {
						read = is.read(buffer);
					} catch (IOException e) {
						logger.error("error reading result from socket", e);
						break;
					}
					response.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				throw new UncheckedIOException("Could not open the input stream from " + getHost() + ":" + getPort(), e);
			}

		} catch (IOException e) {
			logger.error("Could not connect to " + getHost() + ":" + getPort(), e);
			throw new UncheckedIOException("Could not connect to " + getHost() + ":" + getPort(), e);
		} finally {
			IOTools.closeCloseable(dos, logger);
		}

		if (logger.isDebugEnabled())
			logger.debug("Response: " + response.toString());

		return response.toString();
	}

	/**
	 * The preferred method to call. This streams the contents of the InputStream to clamd, so the entire content is not
	 * loaded into memory at the same time.
	 *
	 * @return a ScanResult representing the server response
	 */
	public ScanResult scan(Resource resource) {

		DataOutputStream dos = null;
		String response = "";

		try (Socket socket = new Socket(); InputStream in = resource.openStream()) {
			socket.connect(new InetSocketAddress(getHost(), getPort()));
			socket.setSoTimeout(this.timeout);

			dos = new DataOutputStream(socket.getOutputStream());
			dos.write(INSTREAM);

			int read = CHUNK_SIZE;
			byte[] buffer = new byte[CHUNK_SIZE];
			while (read > -1) {
				try {
					read = in.read(buffer);
				} catch (IOException e) {
					logger.debug("error reading from InputStream", e);
					return new ScanResult(e);
				}

				if (read > 0) { // if previous read exhausted the stream
					try {
						dos.writeInt(read);
						dos.write(buffer, 0, read);
					} catch (IOException e) {
						logger.debug("error writing data to socket", e);
						break;
					}
				}
			}

			dos.writeInt(0);
			dos.flush();

			read = socket.getInputStream().read(buffer);
			if (read > 0)
				response = new String(buffer, 0, read, StandardCharsets.UTF_8);

		} catch (IOException e) {
			logger.error("Could not connect to '" + getHost() + "':'" + getPort() + "' - " + AbstractAntivirusConnector.resourceInformation(resource), e);
			return new ScanResult(e);
		} finally {
			IOTools.closeCloseable(dos, logger);
		}

		if (logger.isDebugEnabled())
			logger.debug("Response: " + response);

		return new ScanResult(response.trim());
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	/**
	 * Socket timeout in milliseconds
	 *
	 * @param timeout
	 *            socket timeout in milliseconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
