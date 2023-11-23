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
package com.braintribe.util.network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;

public class NetworkTools {

	protected static Logger logger = Logger.getLogger(NetworkTools.class);

	protected static boolean preferIPv6 = false;
	protected static Set<String> networkInterfacesBlacklist = new HashSet<String>();

	protected static ReentrantLock networkDetectionContextLock = new ReentrantLock();

	protected static List<NetworkDetectionContext> networkDetectionContextList = null;
	protected static NetworkDetectionContext ip4NetworkDetectionContext = null;
	protected static NetworkDetectionContext ip6NetworkDetectionContext = null;

	private static CompiledFormatter macFormatter = new CompiledFormatter("%02X%s");

	public static void preferIPv6(boolean preferIPv6Setting) {
		boolean resetCachedContextList = false;
		if (preferIPv6 != preferIPv6Setting) {
			resetCachedContextList = true;
		}
		preferIPv6 = preferIPv6Setting;
		if (resetCachedContextList) {
			networkDetectionContextList = null;
		}
	}
	public static void addToNetworkInterfacesBlacklist(String networkInterfaceName) {
		if (networkInterfaceName != null) {
			if (networkInterfacesBlacklist.add(networkInterfaceName.toLowerCase())) {
				networkDetectionContextList = null;
			}
		}
	}

	/**
	 * Get a set of network interfaces IP addresses. This methods return immediately.
	 * 
	 * @param hostType
	 *            {@link StandardProtocolFamily#INET} for IPv4 or {@link StandardProtocolFamily#INET6} for IPv6
	 * @param includeLoopback
	 *            true if loopback addresses should be included
	 * @return set of network interface IP addresses
	 */
	public static Set<String> getHostAddresses(StandardProtocolFamily hostType, boolean includeLoopback) {
		Set<String> host4Addresses = new HashSet<>();
		Set<String> host6Addresses = new HashSet<>();

		Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
			logger.error("Could not get network interfaces.", e);
			return null;
		}

		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			Enumeration<InetAddress> ee = networkInterface.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = ee.nextElement();
				boolean loopbackAddress = i.isLoopbackAddress();

				if (i instanceof Inet4Address) {
					if (!loopbackAddress || (loopbackAddress && includeLoopback)) {
						host4Addresses.add(i.getHostAddress());
					}
				} else if (i instanceof Inet6Address) {
					if (!loopbackAddress || (loopbackAddress && includeLoopback)) {
						host6Addresses.add(i.getHostAddress());
					}
				} else {
					logger.error(InetAddress.class.getName() + ": " + i + " neither " + Inet4Address.class.getName() + " nor "
							+ Inet6Address.class.getName() + ". This should never happen!");
				}
			}
		}

		if (hostType == StandardProtocolFamily.INET) {
			return host4Addresses;
		} else if (hostType == StandardProtocolFamily.INET6) {
			return host6Addresses;
		} else {
			Set<String> all = new HashSet<>(host4Addresses);
			all.addAll(host6Addresses);
			return all;
		}
	}

	/**
	 * Get a set of network interfaces encapsulated in a set of {@link NetworkDetectionContext}. This method based on
	 * reachability checks which might take a bit in the very first time. The result will be cached - therefore the
	 * following calls return immediately.
	 * 
	 * @param preferredType
	 *            type of the interface - either {@link Inet4Address} or {@link Inet6Address}
	 * @param includeLoopback
	 *            true if loopback addresses should be included
	 * @param includeVmNet
	 *            true if loopback addresses should be included
	 * @return a set of network interfaces
	 */
	public static Set<NetworkDetectionContext> getHostAddressNetworkContexts(Class<? extends InetAddress> preferredType, boolean includeLoopback,
			boolean includeVmNet) {
		Set<NetworkDetectionContext> networkDetectionContextSet = new HashSet<>();

		for (NetworkDetectionContext context : getNetworkDetectionContexts()) {
			if (context.isOfType(preferredType)) {
				if (context.isLoopback()) {
					if (includeLoopback) {
						networkDetectionContextSet.add(context);
					}
				} else if (context.isVmNet()) {
					if (includeVmNet) {
						networkDetectionContextSet.add(context);
					}
				} else {
					networkDetectionContextSet.add(context);
				}
			}
		}

		return networkDetectionContextSet;
	}

	public static InetAddress getNetworkAddress() {
		InetAddress result = null;
		if (preferIPv6) {
			result = getIPv6NetworkInterface();
			if (result == null) {
				result = getIPv4NetworkInterface();
			}
		} else {
			result = getIPv4NetworkInterface();
			if (result == null) {
				result = getIPv6NetworkInterface();
			}
		}
		if (result == null) {
			try {
				InetAddress localhost = InetAddress.getLocalHost();
				result = localhost;
			} catch (UnknownHostException e) {
				logger.debug("Could not get local address: ", e);
			}
		}
		return result;
	}

	public static InetAddress getIPv4NetworkInterface() {
		if (ip4NetworkDetectionContext != null) {
			return ip4NetworkDetectionContext.getInet4Address();
		}
		NetworkDetectionContext context = getNetworkContext(Inet4Address.class);
		if (context != null) {
			ip4NetworkDetectionContext = context;
			return context.getInet4Address();
		}
		return null;
	}
	public static InetAddress getIPv6NetworkInterface() {
		if (ip6NetworkDetectionContext != null) {
			return ip6NetworkDetectionContext.getInet6Address();
		}
		NetworkDetectionContext context = getNetworkContext(Inet6Address.class);
		if (context != null) {
			ip6NetworkDetectionContext = context;
			return context.getInet6Address();
		}
		return null;
	}
	/**
	 * Return the IPv6 address - skip the zone_id. The IPv6 address in Java looks like the following: "address%zone_id".
	 * Most of the time the address is the only interesting part.
	 * 
	 * Keep in mind that the picking of the IPv6 address is done randomly with some hints for the best interface. It might
	 * be the case that the interface is <b>not</b> the interface is expected.
	 * 
	 * @return one (randomly picked) IPv6 address or null if it could not be determined
	 */
	public static String getIPv6Address() {
		InetAddress inetAddress = getIPv6NetworkInterface();
		if (inetAddress != null) {
			String hostAddress = inetAddress.getHostAddress();
			if (hostAddress != null) {
				String[] splittedHostAddress = hostAddress.split("%");
				if (splittedHostAddress != null) {
					return splittedHostAddress[0];
				}
			}
		}
		return null;
	}
	public static NetworkDetectionContext getNetworkContext(Class<? extends InetAddress> preferredType) {

		List<NetworkDetectionContext> contextList = getNetworkDetectionContexts();
		NetworkDetectionContext currentWinner = null;
		for (NetworkDetectionContext context : contextList) {
			if (context.isOfType(preferredType)) {
				if (currentWinner == null) {
					currentWinner = context;
				} else if (currentWinner.isLoopback() && !context.isLoopback()) {
					currentWinner = context;
				} else if (currentWinner.isVmNet() && !context.isVmNet()) {
					currentWinner = context;
				}
			}
		}

		return currentWinner;
	}

	public static List<NetworkDetectionContext> getNetworkDetectionContexts() {

		if (networkDetectionContextList == null) {
			networkDetectionContextLock.lock();
			try {
				if (networkDetectionContextList == null) {
					List<NetworkDetectionContext> list = computeNetworkDetectionContextListSynchronized();
					networkDetectionContextList = list;
					return list;
				}
			} finally {
				networkDetectionContextLock.unlock();
			}
		}
		return networkDetectionContextList;

	}

	private static List<NetworkDetectionContext> computeNetworkDetectionContextListSynchronized() {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("Creating network detection contexts ...");

		List<NetworkDetectionContext> list = Collections.synchronizedList(new ArrayList<>());

		Enumeration<NetworkInterface> net = null;
		try {
			net = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
			logger.error("Could not get network interfaces.", e);
			networkDetectionContextList = list;
			return list;
		}

		if (!net.hasMoreElements()) {
			logger.warn("No network interfaces found!");
		} else {

			// Putting elements from Enumeration into a list to allow parallel streaming
			List<NetworkInterface> interfaces = new ArrayList<>();
			while (net.hasMoreElements()) {
				NetworkInterface ni = net.nextElement();
				interfaces.add(ni);
			}

			interfaces.stream().parallel().forEach(ni -> {

				try {
					String niName = ni.getName();

					if (debug)
						logger.debug("Examining network interface " + niName);

					if (niName != null) {
						if (networkInterfacesBlacklist.contains(niName.toLowerCase())) {
							if (debug)
								logger.debug("The network interface " + niName + " is on the blacklist. Ignoring this interface.");
							return;
						}
					} else {
						niName = "[network interface name not available]";
					}

					if (!ni.isUp()) {
						if (debug)
							logger.debug("The network interface " + niName + " is down. Ignoring this interface.");
						return;
					}

					if (trace)
						logger.trace("Checking active network interface " + niName + " ...");

					NetworkDetectionContext context = new NetworkDetectionContext();
					context.setNetworkInterface(ni);
					context.setMacAddress(getMacAddress(ni));

					Inet4Address inet4Address = getInetAddress(ni, Inet4Address.class, true);
					if (inet4Address == null) {
						inet4Address = getInetAddress(ni, Inet4Address.class, false);
						if (trace)
							logger.trace("Detected IPv4 address " + inet4Address + " on interface " + niName);
					}
					Inet6Address inet6Address = getInetAddress(ni, Inet6Address.class, true);
					if (inet6Address == null) {
						inet6Address = getInetAddress(ni, Inet6Address.class, false);
						if (trace)
							logger.trace("Detected IPv6 address " + inet4Address + " on interface " + niName);
					}

					context.setInet4Address(inet4Address);
					context.setInet6Address(inet6Address);

					boolean reachable4 = false;
					if (inet4Address != null) {
						try {
							if (trace)
								logger.trace("Begin to check reachability of " + inet4Address + " on interface " + niName);
							reachable4 = inet4Address.isReachable(1000);
							if (trace)
								logger.trace("Checked reachability of " + inet4Address + ": " + reachable4 + " on interface " + niName);
							if (!reachable4 && inet4Address.isLoopbackAddress()) {
								if (debug)
									logger.debug("Loopback address " + inet4Address + " is not reachable. Trying again with a higher timeout.");
								reachable4 = inet4Address.isReachable(3000);
							}
						} catch (Exception reachableException) {
							if (trace)
								logger.trace("Could not check address " + inet4Address, reachableException);
						}
					}
					boolean reachable6 = false;
					if (inet6Address != null) {
						try {
							if (trace)
								logger.trace("Begin to check reachability of " + inet6Address + " on interface " + niName);
							reachable6 = inet6Address.isReachable(1000);
							if (trace)
								logger.trace("Checked reachability of " + inet6Address + ": " + reachable6 + " on interface " + niName);
							if (!reachable6 && inet6Address.isLoopbackAddress()) {
								if (debug)
									logger.debug("Loopback address " + inet6Address + " is not reachable. Trying again with a higher timeout.");
								reachable6 = inet6Address.isReachable(3000);
							}
						} catch (Exception reachableException) {
							if (trace)
								logger.trace("Could not check address " + inet6Address + " on interface " + niName, reachableException);
						}
					}

					if (reachable4 || reachable6) {
						list.add(context);
					} else {
						if (debug)
							logger.debug("Discarding non-reachable network " + context);
					}

				} catch (Exception e) {
					if (debug)
						logger.debug("Error while processing network interface " + ni, e);
				}
			});

		}

		if (debug)
			logger.debug("Created " + list.size() + " network detection contexts.");

		return list;
	}

	public static String getMacAddress(NetworkInterface ni) throws Exception {
		byte[] mac = ni.getHardwareAddress();
		if (mac == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(macFormatter.format(mac[i], (i < mac.length - 1) ? ":" : ""));
		}
		String address = sb.toString().toLowerCase();
		return address;
	}

	protected static <T extends InetAddress> T getInetAddress(NetworkInterface ni, Class<T> type, boolean excludeLoopback) {

		InetAddress currentWinner = null;

		Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();

		while (inetAddresses.hasMoreElements()) {
			InetAddress inetAddress = inetAddresses.nextElement();

			if (inetAddress.getClass().isAssignableFrom(type)) {

				if (!excludeLoopback) {
					if (currentWinner == null || currentWinner.isLoopbackAddress()) {
						currentWinner = inetAddress;
					}
				}

				if (inetAddress.isLoopbackAddress()) {
					if (excludeLoopback) {
						continue;
					}
				} else {
					if (currentWinner == null) {
						currentWinner = inetAddress;
					}
				}
			}
		}

		return (T) currentWinner;
	}

	/**
	 * Convenience for {@link #getUnusedPortInRange(int, int, Set)} without having a exclude ports specified.
	 */
	public static int getUnusedPortInRange(int from, int to) {
		return getUnusedPortInRange(from, to, null);
	}
	/**
	 * Convenience for {@link #getUnusedPortInRange(int, int, Set, String)} without having a host specified.
	 */
	public static int getUnusedPortInRange(int from, int to, Set<Integer> excludes) {
		return getUnusedPortInRange(from, to, excludes, null);
	}

	/**
	 * Tries to find a port number that is unused at the given host address and greater or equal <code>from</code> and less
	 * equal <code>to</code>. <br>
	 * If no host is specified a valid local address will be used to check.
	 * 
	 * @param from
	 *            the start port of the range.
	 * @param to
	 *            the end port of the range.
	 * @param excludes
	 *            a set of ports that should be excluded from the range.
	 * @param host
	 *            The host
	 * @return A free port or -1 if no port is available
	 */
	public static int getUnusedPortInRange(int from, int to, Set<Integer> excludes, String host) {
		for (int port = from; port <= to; port++) {
			if (excludes != null && excludes.contains(port)) {
				continue;
			}
			logger.debug("try port: " + port + " whether it is available.");
			if (isPortAvailable(port, host)) {
				logger.debug("port: " + port + " is available");
				return port;
			} else {
				logger.debug("checked port: " + port + " is not available.");
			}
		}
		return -1;
	}

	/**
	 * Convenience for {@link #isPortAvailable(int, String)} without specifying a host address.
	 * 
	 * @param port
	 *            The port to check
	 * @return True, if port is available, false otherwise
	 */
	public static boolean isPortAvailable(int port) {
		return isPortAvailable(port, null);
	}

	/**
	 * Checks whether the given port is unused at the given host address.<br>
	 * If no host is specified a valid local address will be used to check.
	 * 
	 * @param port
	 *            The port to check
	 * @param host
	 *            The host to check
	 * @return True, if port is available, false otherwise
	 */
	public static boolean isPortAvailable(int port, String host) {

		ServerSocket socket = null;
		try {
			SocketAddress socketAddress;
			if (host != null) {
				socketAddress = new InetSocketAddress(host, port);
			} else {
				socketAddress = new InetSocketAddress(port);
			}
			socket = new ServerSocket();
			socket.setReuseAddress(true);
			socket.bind(socketAddress);

			return true;
		} catch (Exception e) {
			if (logger.isTraceEnabled()) {
				logger.trace("Cannot bind a server socket on port: " + port + " at address: " + host + ". Seems to be in use.");
			}
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					logger.error("Could not close test socket on port: " + port + "anymore.");
				}
			}
		}

	}

	public static List<InetAddress> resolveHostname(final String hostname, Duration timeout) throws UnknownHostException {

		List<InetAddress> resultList = new ArrayList<>();

		Thread t = new Thread(() -> {
			try {
				for (InetAddress address : InetAddress.getAllByName(hostname)) {
					resultList.add(address);
				}
			} catch (Exception e) {
				logger.error("Error while trying to resolve " + hostname, e);
			}
		});
		t.setDaemon(true);
		t.setName("Resolver for " + hostname);
		t.start();

		try {
			if (timeout != null) {
				t.join(timeout.toMillis());
			} else {
				t.join();
			}
		} catch (InterruptedException ie) {
			logger.debug(() -> "Unexpected interrupton while resolving host " + hostname, ie);
			Thread.currentThread().interrupt();
		}

		if (resultList.isEmpty()) {
			throw new UnknownHostException(hostname + " after timeout: " + timeout);
		}
		return resultList;
	}
}
