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
package com.braintribe.model.processing.leadership.test.worker;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.braintribe.model.processing.leadership.api.LeadershipHandle;
import com.braintribe.model.processing.leadership.api.LeadershipListener;
import com.braintribe.model.processing.leadership.api.LifeSignProvider;
import com.braintribe.model.processing.leadership.test.remote.ThreadCompleteListener;
import com.braintribe.model.processing.leadership.test.wire.contract.EtcdLeadershipTestContract;
import com.braintribe.utils.DateTools;

public class PortWriter extends Thread implements LeadershipListener, LifeSignProvider {

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.US);

	public final static int UDP_BASEPORT = 3000;
	protected volatile boolean stop = false;
	protected CountDownLatch closed = new CountDownLatch(1);


	protected String host = null;
	protected int port = 2048;
	protected long interval = 1000L;
	protected int iterations = 10;
	protected int failProbability = 0;

	protected volatile boolean leadershipOwner = false;
	protected volatile String identification = null;
	protected volatile LeadershipHandle handle = null;
	protected String workerId = null;

	protected int listeningPort = -1;

	protected Socket socket = null;
	protected OutputStream outputStream = null;

	protected ThreadCompleteListener manager = null;

	public void registerManger(final ThreadCompleteListener managerParam) {
		this.manager = managerParam;
	}

	public PortWriter(EtcdLeadershipTestContract testConfiguration, String workerId, int iterations, int listeningPort) {
		this.host = testConfiguration.host();
		this.port = testConfiguration.port();
		this.interval = testConfiguration.writeInterval();
		this.iterations = iterations;
		this.workerId = workerId;
		this.failProbability = testConfiguration.failProbability();
		this.listeningPort = listeningPort;
	}
	
	private void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT)+" [PortWriter/"+workerId+"]: "+text);
		System.out.flush();
	}

	@Override
	public void run() {

		int count = 0;

		DatagramSocket serverSocket = null;
		try {
			print("Listening on port " + this.listeningPort+" with fail probability "+this.failProbability);

			serverSocket = new DatagramSocket(this.listeningPort);
			serverSocket.setSoTimeout((int) this.interval);

			while (!this.stop) {

				if (this.leadershipOwner) {

					if (this.failProbability > 0) {
						int randomNumber = (new Random(System.currentTimeMillis())).nextInt(100);
						if (randomNumber < this.failProbability) {
							print("Quitting without notice");

							this.handle = null;
							this.leadershipOwner = false;
							this.stop = true;
							break;
						}
					}

					if (socket == null) {
						print("Starting connection to "+host+":"+port);
						System.out.flush();
						socket = new Socket(this.host, this.port);
						outputStream = socket.getOutputStream();
					}
					
					String text = this.identification + " sends his regards at "+DateTools.encode(new Date(), dateFormatter) + " ("+count+")\n";
					outputStream.write(text.getBytes("UTF-8"));
					outputStream.flush();

					count++;
					if (count >= this.iterations) {
						count = 0;
						this.closeCommunicationIfOpen();
						this.leadershipOwner = false;
					}

				} else {

					this.closeCommunicationIfOpen();

				}

				if (!this.stop) {
					try {
						// The socket timeout will provide the interval
						byte[] receiveData = new byte[256];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						serverSocket.receive(receivePacket);
						String receivedMessage = new String(receivePacket.getData(), "UTF-8");
						receivedMessage = receivedMessage.trim();
						print("Received message: \"" + receivedMessage + "\"");

						if (receivedMessage.equals("quitLeadership")) {
							this.leadershipOwner = true;
						} else if (receivedMessage.equals("stop")) {
							print("Stopping now.");
							break;
						} else if (receivedMessage.equals("fail")) {
							break;
						}
					} catch (SocketTimeoutException ste) {
						// ignore
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.closeCommunicationIfOpen();

			if (this.handle != null) {
				this.handle.release();
			}

			try {
				if (this.manager != null) {
					print("Notification to manager that thread is over from workerId [" + workerId + "]");
					this.manager.notifyOfThreadComplete(this);
				}
			} catch (Exception e) {
				// TODO maybe update this
				e.printStackTrace();
			}
			
			closed.countDown();
			
			print("Shut down");
		}
	}

	protected void closeCommunicationIfOpen() {
		if (socket != null) {
			print("Closing connection to " + host + ":" + port);

			this.closeStream(socket, outputStream);
			socket = null;
			outputStream = null;

			if (this.handle != null) {
				print("Releasing through handle.");
				this.handle.release();
				this.handle = null;
			}
		}
	}

	protected void closeStream(Socket s, OutputStream os) {
		if (os != null) {
			try {
				os.write("quit\n".getBytes("UTF-8"));
				os.flush();
				os.close();
			} catch (Exception e) {
				//Ignore e.printStackTrace();
			}
		}
		if (s != null) {
			try {
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stopProcessing() {
		this.stop = true;
		try {
			if (!closed.await(10000L, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Timeout while waiting for close");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting for close", e);
		}
	}

	@Override
	public void onLeadershipGranted(LeadershipHandle handleParam) {
		print("Received leadership");
		this.leadershipOwner = true;
		this.identification = handleParam.getIdentification();
		this.handle = handleParam;
	}

	@Override
	public void surrenderLeadership(LeadershipHandle handleParam) {
		print("Leadership revoked");
		this.leadershipOwner = false;
		this.closeCommunicationIfOpen();
		this.handle = handleParam;
	}

	@Override
	public boolean isWorkingAsLeader(String candidateId) {
		if (this.workerId.equals(candidateId)) {
			return (!this.stop) && (this.leadershipOwner);
		}
		return false;
	}

	@Override
	public boolean isAvailableAsCandidate(String candidateId) {
		if (this.workerId.equals(candidateId)) {
			// System.out.println("Candidate "+this.workerId+" is available: "+(!stop));
			return !this.stop;
		}
		return false;
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

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int getFailProbability() {
		return failProbability;
	}

	public void setFailProbability(int failProbability) {
		this.failProbability = failProbability;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
}
