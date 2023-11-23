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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/*
 *  This is a simple implementation of the Lumberjack client.
 *  GitHub: https://github.com/elasticsearch/logstash-forwarder
 *  Lumberjack protocol page: https://github.com/elasticsearch/logstash-forwarder/blob/master/PROTOCOL.md
 */
public class Main {

	public final static String PROTOCOL_VERSION = "1";
	public final static String DATA_FRAME_TYPE = "D";
	public final static String COMPRESS_FRAME_TYPE = "C";
	public final static String WINDOW_FRAME_TYPE = "W";

	private final static String SERVER_HOST = "192.168.100.115";
	private final static int SERVER_PORT = 5000;

	// a TrustManager that accepts any certificate
	private final static TrustManager[] DUMMY_TRUST_MANAGER = { new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	public static void main(String[] args) {

		/* The message going to be sent. */
		String message = "{\"key1\": \"value1\"}";

		try {
			/* Step 1. Connect to the Lumberjack server with proper SSL settings. (The Lumberjack server is very likely to be a LogStash instance.) */
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, DUMMY_TRUST_MANAGER, new java.security.SecureRandom());
			SSLSocketFactory factory = sslContext.getSocketFactory();
			SSLSocket socket = (SSLSocket) factory.createSocket(SERVER_HOST, SERVER_PORT);

			/* Step 2. Get the output/input streams from SSLSocket. */
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), 5120);
			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

			/* Step 3. Prepare the data to be send.
			 *
			 * Note: One of the keys of the data sent to server MUST BE "line", the value of the key "line" will be the main message parsed by the
			 * server. */
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put("line", message);
			dataMap.put("another_field", "data.of.another.field");

			/* Step 3. Prepare the DATA frame. Note: Every message in Lumberjack protocol is called a "frame": 1. DATA frame: message that carries the
			 * real data to the Lumberjack server. 2. ACK frame: message sent from server to acknowledge receiving of previous DATA frames. 3. WINDOW
			 * SIZE frame: used to set the window size, which is the max number of unacknowledged DATA frames the client will send before blocking for
			 * acks. 4. COMPRESSED frame */
			int fakeSequenceNumber = 1; // maybe we can ignore this:)

			byte[] windowFrame = prepareWindowFrame(1);

			byte[] dataFrame = prepareDataFrame(dataMap, fakeSequenceNumber);

			/* Step 4a. Send the DATA frame. */
			// out.write(dataFrame);
			// out.flush();

			/* Step 4b. Compress several DATA frames to a single COMPRESS frame then send. */
			byte[] anotherDataFrame = prepareDataFrame(dataMap, fakeSequenceNumber);
			byte[][] dataFrames = { windowFrame, dataFrame, anotherDataFrame };
			byte[] compressFrame = prepareCompressFrame(dataFrames);
			// out.write(compressFrame);
			out.write(compressFrame);
			out.flush();

			/* Step 5. Receive the ACK frame. */
			byte[] buff = new byte[1024];
			int bytesReceived = in.read(buff);
			String version = new String(Arrays.copyOfRange(buff, 0, 1));
			String frameType = new String(Arrays.copyOfRange(buff, 1, 2));
			int sequenceNumber = ByteBuffer.wrap(Arrays.copyOfRange(buff, 2, 6)).getInt();

			// clean up
			if (socket != null) {
				socket.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static byte[] prepareWindowFrame(int windowSize) throws IOException {
		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

		bytesOutput.write(PROTOCOL_VERSION.getBytes());
		bytesOutput.write(WINDOW_FRAME_TYPE.getBytes());
		bytesOutput.write(ByteBuffer.allocate(4).putInt(windowSize).array());

		return bytesOutput.toByteArray();
	}

	private static byte[] prepareDataFrame(Map<String, String> dataMap, int sequenceNumber) throws IOException {

		/* Sample: dataMap = {"line": "a"}
		 *
		 * Result data frame (a byte array): {(byte)0x31, // "1" (ASCII code) (byte)0x44, // "D" (ASCII code) (byte)0x0, (byte)0x0, (byte)0x0,
		 * (byte)0x1, // sequence number (4-byte big-endian encoding) (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x1, // number of data key-value pairs
		 * (4-byte big-endian encoding) (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x4, // length of 1st key byte repr. (4-byte big-endian encoding)
		 * (byte)0x6C, (byte)0x69, (byte)0x61, (byte)0x65, // byte representation of the 1st key, i.e., "line" (byte)0x0, (byte)0x0, (byte)0x0,
		 * (byte)0x1, // length of 1st value byte repr. (4-byte big-endian encoding) (byte)0x61 // byte representation of the 1st value, i.e., "a"
		 * .... // length of n-th value byte repr. (4-byte big-endian encoding) .... // byte representation of the n-th value } */

		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();

		bytesOutput.write(PROTOCOL_VERSION.getBytes());
		bytesOutput.write(DATA_FRAME_TYPE.getBytes());
		bytesOutput.write(ByteBuffer.allocate(4).putInt(sequenceNumber).array());
		bytesOutput.write(ByteBuffer.allocate(4).putInt(dataMap.size()).array());

		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			bytesOutput.write(ByteBuffer.allocate(4).putInt(key.length()).array());
			bytesOutput.write(key.getBytes());
			bytesOutput.write(ByteBuffer.allocate(4).putInt(value.getBytes().length).array());
			bytesOutput.write(value.getBytes());
		}

		return bytesOutput.toByteArray();
	}

	private static byte[] prepareCompressFrame(byte[][] dataFrames) throws IOException {

		/* Concatenate all the DATA frames into one single byte array. */
		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
		for (byte[] frame : dataFrames) {
			bytesOutput.write(frame);
		}

		/* Compress this concatenated byte array with ZLib level 6. */
		byte[] output = new byte[5120];
		Deflater deflater = new Deflater(6);
		deflater.setInput(bytesOutput.toByteArray());
		deflater.finish();
		int compressedLength = deflater.deflate(output);
		deflater.end();

		bytesOutput.reset();

		/* Use the compressed payload to compose the COMPRESS frame. */
		bytesOutput.write(PROTOCOL_VERSION.getBytes());
		bytesOutput.write(COMPRESS_FRAME_TYPE.getBytes());
		bytesOutput.write(ByteBuffer.allocate(4).putInt(compressedLength).array());
		bytesOutput.write(Arrays.copyOf(output, compressedLength));

		return bytesOutput.toByteArray();
	}
}
