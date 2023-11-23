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
package com.braintribe.logging.handler.lumberjack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.braintribe.logging.handler.lumberjack.logpackage.CombinedLogPackage;
import com.braintribe.logging.handler.lumberjack.logpackage.LogPackage;

public class LumberjackSender implements Callable<Void> {

	protected final static Logger logLogger = Logger.getLogger(LumberjackSender.class.getName());

	protected final static String PROTOCOL_VERSION = "1";
	protected final static String DATA_FRAME_TYPE = "D";
	protected final static String COMPRESS_FRAME_TYPE = "C";
	protected final static String WINDOW_FRAME_TYPE = "W";

	protected LinkedBlockingQueue<CombinedLogPackage> logPackageQueue = null;
	protected boolean shutdown = false;

	protected long pollTimeoutInMs = 500L;

	protected String host = null;
	protected int port = -1;

	protected Socket socket = null;
	protected BufferedOutputStream out = null;
	protected BufferedInputStream in = null;
	protected int workerNumber = -1;

	protected long retryInterval = 1000L; // 1 second
	protected long currentRetryInterval = 1000L;
	protected long maxRetryInterval = 3600000L; // 1 hour
	protected long nextConnectionTry = -1L;

	protected int socketTimeout = 60000;
	private boolean ssl = true;

	// a TrustManager that accepts any certificate
	private final static TrustManager[] ALLOW_ALL_TRUST_MANAGER = { new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// Intentionally left empty
		}
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			// Intentionally left empty
		}
	} };

	public LumberjackSender(LinkedBlockingQueue<CombinedLogPackage> logPackageQueue, String host, int port, int workerNumber, int socketTimeout,
			boolean ssl) {
		this.logPackageQueue = logPackageQueue;
		this.host = host;
		this.port = port;
		this.workerNumber = workerNumber;
		this.socketTimeout = socketTimeout;
		this.ssl = ssl;
	}

	public void stopProcessing() {
		this.shutdown = true;
	}

	@Override
	public Void call() throws Exception {

		Thread.currentThread().setName("LumberjackSender to " + this.host + ":" + this.port + "-" + this.workerNumber);
		try {
			while (!this.shutdown) {
				try {
					CombinedLogPackage logPackages = this.logPackageQueue.poll(this.pollTimeoutInMs, TimeUnit.MILLISECONDS);
					if (logPackages != null) {

						boolean proceed = true;

						if (this.nextConnectionTry > 0) {
							long now = System.currentTimeMillis();

							if (now < this.nextConnectionTry) {
								proceed = false;
							}
						}

						if (proceed) {
							this.sendLogPackage(logPackages);

							// We obviously had success; resetting retry interval
							this.nextConnectionTry = -1;
							this.currentRetryInterval = this.retryInterval;
						}
					}
				} catch (InterruptedException ie) {
					logLogger.logp(Level.FINE, "LumberjackSender", "call", "Got interrupted. Stopping processing.");
					return null;
				} catch (Exception e) {

					this.nextConnectionTry = System.currentTimeMillis() + this.currentRetryInterval;

					String errorMsg = "Error while sending frame to " + this.host + ":" + this.port + ". Waiting for " + this.currentRetryInterval
							+ " ms until the next retry. See detailed stacktrace in debug log!";

					// On ConnectException: only send one liner
					if (e instanceof ConnectException) {
						logLogger.logp(Level.INFO, "LumberjackSender", "call", errorMsg);
						logLogger.logp(Level.FINE, "LumberjackSender", "call", errorMsg, e);
					} else {
						logLogger.logp(Level.INFO, "LumberjackSender", "call", errorMsg, e);
					}

					if (this.currentRetryInterval < this.maxRetryInterval) {
						this.currentRetryInterval = this.currentRetryInterval * 2;
						if (this.currentRetryInterval > this.maxRetryInterval) {
							this.currentRetryInterval = this.maxRetryInterval;
						}
					}

				}

			}
		} finally {
			this.disconnect();
		}

		return null;
	}

	protected void sendLogPackage(CombinedLogPackage logPackages)
			throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {
		this.connect();

		List<LogPackage> logPackageList = logPackages.getLogPackages();
		int size = logPackageList.size();
		byte[][] dataFrames = new byte[size + 1][];

		for (int i = 0; i < size; ++i) {

			LogPackage logPackage = logPackageList.get(i);

			int sequenceNumber = logPackage.getSequenceNumber();
			String line = logPackage.getLine();
			Map<String, String> properties = logPackage.getProperties();

			Map<String, String> dataMap = new HashMap<>();
			dataMap.put("line", line);
			if (properties != null) {
				dataMap.putAll(properties);
			}

			byte[] dataFrame = prepareDataFrame(dataMap, sequenceNumber);
			dataFrames[i + 1] = dataFrame;

		}

		byte[] windowFrame = prepareWindowFrame(size);
		dataFrames[0] = windowFrame;
		byte[] compressFrame = prepareCompressFrame(dataFrames);

		try {
			this.out.write(compressFrame);
			this.out.flush();
		} catch (Exception e) {
			logLogger.logp(Level.FINEST, "LumberjackSender", "sendLogPackage", "Error while sending frame to " + this.host + ":" + this.port + ".",
					e);
			this.disconnect();
			this.connect();
			// retry it for once after a reconnect...
			this.out.write(compressFrame);
			this.out.flush();
		}

		/* Step 5. Receive the ACK frame. */
		try {
			byte[] buff = new byte[1024];
			if (in.available() > 0) {
				in.read(buff);
			}
		} catch (SocketException se) {
			logLogger.logp(Level.FINEST, "LumberjackSender", "sendLogPackage", "Could not read ACK from " + this.host + ":" + this.port + ".", se);
			this.disconnect();
		} catch (Exception e) {
			logLogger.logp(Level.FINEST, "LumberjackSender", "sendLogPackage",
					"Unexpected error while trying read ACK from " + this.host + ":" + this.port + ".", e);
			this.disconnect();
		}
		// int bytesReceived = in.read(buff);

		// Ignoring ack messages for the moment
		// if (bytesReceived != -1) {
		// if (logLogger.isLoggable(Level.FINEST)) {
		// String version = new String(Arrays.copyOfRange(buff, 0, 1));
		// String frameType = new String(Arrays.copyOfRange(buff, 1, 2));
		// int sequenceNumber = ByteBuffer.wrap(Arrays.copyOfRange(buff, 2, 6)).getInt();
		// logLogger.finest("Received ack: "+version+"/"+frameType+"/"+sequenceNumber);
		// }
		// }
	}

	protected static byte[] prepareWindowFrame(int windowSize) throws IOException {
		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

		bytesOutput.write(PROTOCOL_VERSION.getBytes("ASCII"));
		bytesOutput.write(WINDOW_FRAME_TYPE.getBytes("ASCII"));
		bytesOutput.write(ByteBuffer.allocate(4).putInt(windowSize).array());

		return bytesOutput.toByteArray();
	}
	protected static byte[] prepareDataFrame(Map<String, String> dataMap, int sequenceNumber) throws IOException {

		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

		bytesOutput.write(PROTOCOL_VERSION.getBytes("ASCII"));
		bytesOutput.write(DATA_FRAME_TYPE.getBytes("ASCII"));
		bytesOutput.write(ByteBuffer.allocate(4).putInt(sequenceNumber).array());
		bytesOutput.write(ByteBuffer.allocate(4).putInt(dataMap.size()).array());

		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (key == null) {
				key = "";
			}
			if (value == null) {
				value = "";
			}
			byte[] keyBytes = key.getBytes("UTF-8");
			byte[] valueBytes = value.getBytes("UTF-8");

			bytesOutput.write(ByteBuffer.allocate(4).putInt(keyBytes.length).array());
			bytesOutput.write(keyBytes);
			bytesOutput.write(ByteBuffer.allocate(4).putInt(valueBytes.length).array());
			bytesOutput.write(valueBytes);
		}

		return bytesOutput.toByteArray();
	}
	protected static byte[] prepareCompressFrame(byte[][] dataFrames) throws IOException {

		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
		for (byte[] frame : dataFrames) {
			bytesOutput.write(frame);
		}

		/* Compress this concatenated byte array with ZLib level 6. */
		Deflater deflater = new Deflater(6);
		deflater.setInput(bytesOutput.toByteArray());
		deflater.finish();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytesOutput.size());
		byte[] buffer = new byte[5120];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		int compressedLength = output.length;
		deflater.end();

		bytesOutput.reset();

		/* Use the compressed payload to compose the COMPRESS frame. */
		bytesOutput.write(PROTOCOL_VERSION.getBytes("ASCII"));
		bytesOutput.write(COMPRESS_FRAME_TYPE.getBytes("ASCII"));
		bytesOutput.write(ByteBuffer.allocate(4).putInt(compressedLength).array());
		bytesOutput.write(Arrays.copyOf(output, compressedLength));

		return bytesOutput.toByteArray();
	}

	public void connect() throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {
		if (this.socket != null && this.in != null && this.out != null) {
			return;
		}

		if (this.ssl) {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, ALLOW_ALL_TRUST_MANAGER, new java.security.SecureRandom());
			SSLSocketFactory factory = sslContext.getSocketFactory();
			this.socket = factory.createSocket();
		} else {
			this.socket = new Socket();
		}
		this.socket.connect(new InetSocketAddress(this.host, this.port), socketTimeout);

		this.out = new BufferedOutputStream(socket.getOutputStream(), 5120);
		this.in = new BufferedInputStream(socket.getInputStream());
	}
	public void disconnect() {
		if (this.in != null) {
			try {
				this.in.close();
			} catch (Exception e) {
				logLogger.logp(Level.FINEST, "LumberjackSender", "disconnect", "Error while closing InputStream.", e);
			}
		}
		if (this.out != null) {
			try {
				this.out.close();
			} catch (Exception e) {
				logLogger.logp(Level.FINEST, "LumberjackSender", "disconnect", "Error while closing OutputStream.", e);
			}
		}
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (Exception e) {
				logLogger.logp(Level.FINEST, "LumberjackSender", "disconnect", "Error while closing socket.", e);
			}
		}
		this.in = null;
		this.out = null;
		this.socket = null;
	}

	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
		this.currentRetryInterval = retryInterval;
	}
	public void setMaxRetryInterval(long maxRetryInterval) {
		this.maxRetryInterval = maxRetryInterval;
	}

}
