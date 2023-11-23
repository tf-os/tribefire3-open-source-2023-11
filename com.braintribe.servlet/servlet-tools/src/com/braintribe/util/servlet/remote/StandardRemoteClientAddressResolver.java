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
package com.braintribe.util.servlet.remote;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.logging.Logger;
import com.braintribe.util.servlet.exception.InvalidForwardedHeader;
import com.braintribe.util.servlet.exception.RemoteHostNotTrustedException;
import com.braintribe.utils.StringTools;

/**
 * Utility class that tries to acquire the IP address of the originating client of an HttpServletRequest.
 * 
 * The first source of information is the IP address returned by the request itself
 * (see {@link javax.servlet.http.HttpServletRequest#getRemoteAddr()}).
 * However, if the IP traffic is routed through a proxy or load balancer, this
 * is probably not the IP address of the originating client but that of the load
 * balancer or proxy.
 * This class tries to take several other header values into account that have to be
 * set by the proxy / load balancer.
 * 
 * The following headers will be used (in this order):
 * 
 * 1. Any header that is configured in the {@link #customClientIpHeaders} field.
 * 
 * 2. Forwarded: (see https://tools.ietf.org/html/rfc7239)
 * 
 * 3. X-Forwarded-For: (see https://en.wikipedia.org/wiki/X-Forwarded-For)
 * 
 * 4. X-Real-IP
 * 
 * 5. The address directly returned by getRemoteAddr().
 * 
 * Each of the first four options can be deactivated (by default, they are activated).
 * 
 * Since it is very easy to fake HTTP headers, it is also possible to configure a whitelist
 * of IP addresses. When this list is configured, the address returned by getRemoteAddr()
 * must be in this list.
 * 
 * @author roman.kurmanowytsch
 *
 */
public class StandardRemoteClientAddressResolver implements RemoteClientAddressResolver {

	private static final Logger logger = Logger.getLogger(StandardRemoteClientAddressResolver.class);

	public final static String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
	public final static String HEADER_FORWARDED = "Forwarded";
	public final static String HEADER_X_REAL_IP = "X-Real-IP";

	protected boolean includeXForwardedFor = true;
	protected boolean includeForwarded = true;
	protected boolean includeXRealIp = true;
	protected List<String> sourceWhiteList = null;
	protected List<String> customClientIpHeaders = null;
	protected boolean lenientParsing = true;

	@Override
	public String getRemoteIpLenient(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		try {
			RemoteAddressInformation rai = this.getRemoteAddressInformation(request);
			return rai.getRemoteIp();
		} catch(Exception e) {
			logger.debug("Could not get remote address from request: "+request, e);
			return request.getRemoteAddr();
		}
	}

	@Override
	public String getRemoteIp(HttpServletRequest request) throws RemoteHostNotTrustedException, InvalidForwardedHeader, IllegalArgumentException {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null.");
		}
		RemoteAddressInformation rai = this.getRemoteAddressInformation(request);
		return rai.getRemoteIp();
	}

	@Override
	public RemoteAddressInformation getRemoteAddressInformation(HttpServletRequest request) throws RemoteHostNotTrustedException, InvalidForwardedHeader, IllegalArgumentException {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null.");
		}

		String directClientAddress = request.getRemoteAddr();

		if (this.sourceWhiteList != null) {
			if (!this.sourceWhiteList.contains(directClientAddress)) {
				throw new RemoteHostNotTrustedException("The source IP address "+directClientAddress+" is not in the configured white list.");
			}
		}

		RemoteAddressInformation result = new RemoteAddressInformation();

		result.setDirectClientAddress(directClientAddress);

		if (includeXForwardedFor) {
			String xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR);
			List<String> xForwardedForList = this.parseXForwardedFor(xForwardedFor);

			if (!xForwardedForList.isEmpty()) {
				result.setXForwardedFor(xForwardedForList);
			}
		}

		if (includeForwarded) {
			Enumeration<String> forwarded = request.getHeaders(HEADER_FORWARDED);
			List<Forwarded> forwardedList = this.parseForwarded(forwarded);

			if (!forwardedList.isEmpty()) {
				result.setForwarded(forwardedList);
			}
		}

		if (includeXRealIp) {
			String xRealIp = request.getHeader(HEADER_X_REAL_IP);
			if (xRealIp != null) {
				xRealIp = xRealIp.trim();
				if (xRealIp.length() > 0) {
					result.setxRealIp(xRealIp);
				}
			}
		}

		List<String> customIps = this.parseCustomHeaders(request);
		if (customIps != null && !customIps.isEmpty()) {
			result.setCustomIpAddresses(customIps);
		}
		
		return result;
	}



	protected List<String> parseCustomHeaders(HttpServletRequest request) {
		if (customClientIpHeaders == null || customClientIpHeaders.isEmpty()) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		for (String customHeader : customClientIpHeaders) {
			String value = request.getHeader(customHeader);
			if (value != null) {
				value = value.trim();
				if (value.length() > 0) {
					result.add(value);
				}
			}
		}
		
		return result;
	}

	protected List<Forwarded> parseForwarded(Enumeration<String> forwardedEnumeration) throws InvalidForwardedHeader {
		List<Forwarded> result = new ArrayList<>();

		if (forwardedEnumeration == null) {
			return result;
		}

		while(forwardedEnumeration.hasMoreElements()) {
			String forwardedEntry = forwardedEnumeration.nextElement();
			if (forwardedEntry == null) {
				continue;
			}
			forwardedEntry = forwardedEntry.trim();
			if (forwardedEntry.length() == 0) {
				continue;
			}

			String[] entries = StringTools.splitCommaSeparatedString(forwardedEntry, true);
			if (entries == null || entries.length == 0) {
				continue;
			}

			for (String entry : entries) {
				if (entry == null) {
					continue;
				}
				entry = entry.trim();
				if (entry.length() == 0) {
					continue;
				}

				String[] pairs = StringTools.splitSemicolonSeparatedString(entry, true);
				if (pairs == null || pairs.length == 0) {
					continue;
				}

				Forwarded forwarded = new Forwarded();
				result.add(forwarded);

				for (String pair : pairs) {

					int idx = pair.indexOf('=');
					if (idx > 0 && idx < (pair.length()-1)) {

						String key = pair.substring(0, idx).trim();
						String value = pair.substring(idx+1).trim();

						boolean startsWithQuote = value.startsWith("\"");
						boolean endsWithQuote = value.endsWith("\"");

						if (startsWithQuote && endsWithQuote) {
							value = value.substring(1, value.length()-1);
						} else if (startsWithQuote || endsWithQuote) {
							String message = "The value "+value+" is not properly quoted in Forwarded field: "+entry;
							if (lenientParsing) {
								logger.debug(message);
							} else {
								throw new InvalidForwardedHeader(message);
							}
						}

						if (key.equalsIgnoreCase("by")) {
							forwarded.setByAddress(value);
						} else if (key.equalsIgnoreCase("for")) {
							forwarded.setForAddress(value);
						} else if (key.equalsIgnoreCase("host")) {
							forwarded.setHost(value);
						} else if (key.equalsIgnoreCase("proto")) {
							forwarded.setProto(value);
						} else {
							String message = "Unsupported key "+key+" in Forwarded field: "+entry;
							if (lenientParsing) {
								logger.debug(message);
							} else {
								throw new InvalidForwardedHeader(message);
							}
						}

					} else {
						String message = "Invalid Forwarded header: "+entry+". Pair "+pair+" does not follow the 'key = pair' structure.";
						if (lenientParsing) {
							logger.debug(message);
						} else {
							throw new InvalidForwardedHeader(message);
						}
					}

				}


			}
		}

		return result;
	}




	protected List<String> parseXForwardedFor(String xForwardedFor) {
		List<String> result = new ArrayList<>();
		if (xForwardedFor == null || xForwardedFor.trim().length() == 0) {
			return result;
		}
		String[] itemArray = xForwardedFor.split(",");
		if (itemArray == null || itemArray.length == 0) {
			return result;
		}

		for (String item : itemArray) {
			if (item != null) {
				item = item.trim();
				if (item.length() > 0) {
					result.add(item);
				}
			}
		}
		return result;
	}




	public void setIncludeXForwardedFor(boolean includeXForwardedFor) {
		this.includeXForwardedFor = includeXForwardedFor;
	}
	public void setIncludeForwarded(boolean includeForwarded) {
		this.includeForwarded = includeForwarded;
	}
	public void setIncludeXRealIp(boolean includeXRealIp) {
		this.includeXRealIp = includeXRealIp;
	}
	public void setSourceWhiteList(List<String> sourceWhiteList) {
		this.sourceWhiteList = sourceWhiteList;
	}
	public void setLenientParsing(boolean lenientParsing) {
		this.lenientParsing = lenientParsing;
	}
	public void setCustomClientIpHeaders(List<String> customClientIpHeaders) {
		this.customClientIpHeaders = customClientIpHeaders;
	}

}
