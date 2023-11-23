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
package com.braintribe.model.processing.leadership.test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.leadership.api.LeadershipHandle;
import com.braintribe.model.processing.leadership.api.LeadershipManager;
import com.braintribe.model.processing.leadership.test.config.Configurator;
import com.braintribe.model.processing.leadership.test.remote.JvmExecutor;
import com.braintribe.model.processing.leadership.test.remote.RemoteProcess;
import com.braintribe.model.processing.leadership.test.wire.contract.EtcdLeadershipTestContract;
import com.braintribe.model.processing.leadership.test.worker.PortListenerServer;
import com.braintribe.model.processing.leadership.test.worker.PortWriter;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

/*
 * When messaging is enabled, the messaging system needs to provide
 * the following channels:
 * 
 * Topic: "tfTestDblBroadcastTopic"
 * Topic: "tfTestRemoteToDblTopic"
 * Queue: "tfTestRemoteToDblQueue" 
 *
 */

@Category(SpecialEnvironment.class)
public class LeadershipTests {

	protected static EtcdLeadershipTestContract configuration = null;
	protected static Configurator configurator = null;

	protected PortListenerServer server = null;
	protected List<PortWriter> writers = new ArrayList<PortWriter>();
	protected List<Integer> remoteWriterPorts = new ArrayList<Integer>();

	@BeforeClass
	public static void beforeClass() throws Exception {

		configurator = new Configurator();
		configuration = configurator.getConfiguration();
	}

	@AfterClass
	public static void afterClass() throws Exception {

		if (configurator != null) {
			configurator.close();
		}
	}

	@Before
	public void before() throws Exception {

		List<URI> endpointUris = configuration.endpointUrls().stream().map(u -> {
			try {
				return new URI(u);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		Client client = Client.builder().endpoints(endpointUris).build();
		KV kvClient = client.getKVClient();

		try {
			ByteSequence fromKey = ByteSequence.from("leadership/", StandardCharsets.UTF_8);
			ByteSequence toKey = ByteSequence.from("leadershiq/", StandardCharsets.UTF_8);

			GetOption getOption = GetOption.newBuilder().withRange(toKey).build();

			GetResponse response = kvClient.get(fromKey, getOption).get();

			List<KeyValue> kvs = response.getKvs();
			for (KeyValue kv : kvs) {
				ByteSequence delKey = kv.getKey();
				// System.out.println("Deleting key: "+delKey.toStringUtf8());
				kvClient.delete(delKey).get();
			}

		} finally {
			IOTools.closeCloseable(kvClient, null);
			IOTools.closeCloseable(client, null);
		}

		this.server = new PortListenerServer(configuration.port());
		Thread.ofVirtual().name("PortListenerServer").start(this.server);
	}

	@After
	public void after() throws Exception {
		this.stopWriters();
		if (this.server != null) {
			this.server.stopListening();
		}
	}

	@Test
	public void startSingleWriterNoFailProbability() throws Exception {

		this.prepareTest();

		int udpOffset = 300;

		PortWriter writer = new PortWriter(configuration, "SingleWriter", 10, PortWriter.UDP_BASEPORT + udpOffset);
		writer.setFailProbability(0);
		this.writers.add(writer);

		Thread.ofVirtual().name("Writer").start(writer);

		writer.onLeadershipGranted(new LeadershipHandle() {

			@Override
			public String getIdentification() {
				return "SingleWriter";
			}

			@Override
			public void release() { /* Ignore */
			}
		});

		synchronized (this) {
			wait(12000L);
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}
	}

	@Test
	public void startMultipleWriterWithFailProbability() throws Exception {

		this.prepareTest();

		LeadershipManager leadershipManager = configuration.leadershipManager();
		int count = 10;
		int iterations = 10;
		int failProbability = 10;

		int udpOffset = 0;

		for (int i = 0; i < count; ++i) {
			String candidateId = "Writer-" + i + "-" + UUID.randomUUID().toString();
			PortWriter writer = new PortWriter(configuration, candidateId, iterations, PortWriter.UDP_BASEPORT + udpOffset + i);
			writer.setFailProbability(failProbability);
			leadershipManager.addLeadershipListener("multipleWritersWithFailProbability:" + count, candidateId, writer);
			this.writers.add(writer);

			Thread.ofVirtual().name("Writer " + i).start(writer);
		}

		synchronized (this) {
			wait(120000L);
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}
	}

	@Test
	public void startMultipleWriterWithoutFailProbability() throws Exception {

		this.prepareTest();

		LeadershipManager leadershipManager = configuration.leadershipManager();
		int count = 10;
		int iterations = 10;
		int failProbability = 0;

		int udpOffset = 100;

		for (int i = 0; i < count; ++i) {
			String candidateId = "Writer-" + i + "-" + UUID.randomUUID().toString();
			PortWriter writer = new PortWriter(configuration, candidateId, iterations, PortWriter.UDP_BASEPORT + udpOffset + i);
			writer.setFailProbability(failProbability);

			leadershipManager.addLeadershipListener("multipleWritersWithoutFailProbability:" + count, candidateId, writer);
			this.writers.add(writer);

			Thread.ofVirtual().name("Writer " + i).start(writer);
		}

		synchronized (this) {
			wait(120000L);
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}
	}

	@Test
	public void startMultipleWriterWithoutFailProbabilityInfiniteIterations() throws Exception {

		this.prepareTest();

		LeadershipManager leadershipManager = configuration.leadershipManager();

		int count = 10;
		int failProbability = 0;

		String domainId = "multipleWritersWithoutFailProbabilityInfiniteIterations:" + count;
		List<String> candidateIds = new ArrayList<String>();

		int udpOffset = 200;

		for (int i = 0; i < count; ++i) {

			String candidateId = "Writer-" + i + "-" + UUID.randomUUID().toString();
			candidateIds.add(candidateId);

			PortWriter writer = new PortWriter(configuration, candidateId, Integer.MAX_VALUE, PortWriter.UDP_BASEPORT + udpOffset + i);
			writer.setFailProbability(failProbability);

			leadershipManager.addLeadershipListener(domainId, candidateId, writer);
			this.writers.add(writer);

			Thread.ofVirtual().name("Writer " + i).start(writer);
		}

		this.waitAndRandomizeLeadership(candidateIds, 120000L, 10000L);

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}
	}

	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Master]: " + text);
	}

	@Ignore
	protected void waitAndRandomizeLeadership(List<String> candidateIds, long duration, long changeInterval) throws Exception {

		long start = System.currentTimeMillis();
		long end = start + duration;
		long lastPriorityChange = 0L;
		int nextPriority = 101;

		while (System.currentTimeMillis() < end) {

			long now = System.currentTimeMillis();
			long span = now - lastPriorityChange;

			if (span > changeInterval) {

				// TODO

				lastPriorityChange = now;
				nextPriority++;
			}

			synchronized (this) {
				wait(1000L);
			}

		}
	}

	@Ignore
	protected List<String> getCandidateIdsFromRemoteProcesses(List<RemoteProcess> remoteProcesses) {
		List<String> candidateIds = new ArrayList<String>(remoteProcesses.size());
		for (RemoteProcess p : remoteProcesses) {
			candidateIds.add(p.getCandidateId());
		}
		return candidateIds;
	}

	@Test
	public void startDblWritersWithFailProbability() throws Exception {

		this.prepareTest();

		int workerCount = 10;
		int iterations = 100;
		int failProbability = 10;
		int udpOffset = 600;

		List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(workerCount, "RemoteWithFailProbability:" + workerCount, failProbability,
				iterations, remoteWriterPorts, udpOffset);
		List<String> candidateIds = this.getCandidateIdsFromRemoteProcesses(remoteProcesses);

		this.waitAndRandomizeLeadership(candidateIds, 120000L, 1000000L);

		this.stopWriters();
		for (RemoteProcess p : remoteProcesses) {
			p.getProcess().waitFor();
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}

	}

	@Test
	public void startDblWritersWithoutFailProbabilityAndChangingPriorities() throws Exception {

		this.prepareTest();

		int workerCount = 10;
		int iterations = 100;
		int failProbability = 0;
		int udpOffset = 600;

		List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(workerCount,
				"RemoteWithoutFailProbabilityAndChangingPriorities:" + workerCount, failProbability, iterations, remoteWriterPorts, udpOffset);
		List<String> candidateIds = this.getCandidateIdsFromRemoteProcesses(remoteProcesses);

		this.waitAndRandomizeLeadership(candidateIds, 120000L, 5000L);

		this.stopWriters();
		for (RemoteProcess p : remoteProcesses) {
			p.getProcess().waitFor();
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}

	}

	@Test
	public void startDblWritersWithoutFailProbabilityLessIterations() throws Exception {

		this.prepareTest();

		int workerCount = 10;
		int iterations = 10;
		int failProbability = 0;
		int udpOffset = 700;

		List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(workerCount, "RemoteWithoutFailProbabilityLessIterations:" + workerCount,
				failProbability, iterations, remoteWriterPorts, udpOffset);
		List<String> candidateIds = this.getCandidateIdsFromRemoteProcesses(remoteProcesses);
		this.waitAndRandomizeLeadership(candidateIds, 120000L, 1000000L);
		this.stopWriters();
		for (RemoteProcess p : remoteProcesses) {
			p.getProcess().waitFor();
		}

		Exception e = this.server.getException();
		if (e != null) {
			throw new Exception("Error in test", e);
		}

	}

	@Ignore
	protected void prepareTest() throws Exception {
		this.server.reset();
		this.stopWriters();
	}
	@Ignore
	protected void stopWriters() throws Exception {
		for (PortWriter writer : this.writers) {
			writer.stopProcessing();
		}
		this.writers.clear();
		if (!this.remoteWriterPorts.isEmpty()) {
			InetAddress address = InetAddress.getLocalHost();
			DatagramSocket datagramSocket = new DatagramSocket();
			byte[] buffer = "stop".getBytes("UTF-8");
			for (Integer port : this.remoteWriterPorts) {
				print("Sending message to port " + port);
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
				datagramSocket.send(packet);
			}
			datagramSocket.close();
			this.remoteWriterPorts.clear();
		}
	}

}
