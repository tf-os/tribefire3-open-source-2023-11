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
package com.braintribe.tomcat.extension.realm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Engine;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TfRestRealm extends RealmBase {

	private static final Logger logger = Logger.getLogger(TfRestRealm.class.getName());

	protected String fullAccessAlias = "tf-admin";
	protected String tfsDefaultUrl = "http://localhost:8080/tribefire-services";
	protected String tfsUrl = null;

	@Override
	public Principal authenticate(String username, String credentials) {
		try {
			String baseUrl = getTribefireServicesUrl();

			logger.fine("Accessing " + baseUrl);

			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(baseUrl + "/api/v1/authenticate");

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("user", username);
			jsonObject.put("password", credentials);
			String body = jsonObject.toString();

			httppost.setEntity(new StringEntity(body));
			httppost.setHeader("Content-Type", "application/json");

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);

			String sessionId = readBody(response);
			if (sessionId != null) {
				if (sessionId.startsWith("\"") && sessionId.endsWith("\"") && sessionId.length() > 1) {
					sessionId = sessionId.substring(1, sessionId.length() - 1);
				}
				logger.fine("Session ID from " + baseUrl + ": " + sessionId);
				if (!sessionId.toLowerCase().matches("[a-f0-9\\-]+")) {
					logger.info("Unexpected response for authentication: " + sessionId);
					return null;
				}

				HttpGet httpget = new HttpGet(baseUrl + "/api/v1/sessions/validate?sessionId=" + sessionId);
				response = httpclient.execute(httpget);

				String responseBody = readBody(response);
				logger.fine("Validation result from " + baseUrl + ": " + responseBody);

				if (responseBody != null) {
					JSONParser parser = new JSONParser();
					JSONObject userSession = (JSONObject) parser.parse(responseBody);

					JSONArray rolesArray = (JSONArray) userSession.get("effectiveRoles");
					Set<String> roles = new HashSet<>();
					int size = rolesArray.size();
					for (int i = 0; i < size; ++i) {
						String role = (String) rolesArray.get(i);
						if (fullAccessAlias != null && fullAccessAlias.length() > 0 && role.equals(fullAccessAlias)) {
							roles.add("tomcat");
							roles.add("manager-gui");
							roles.add("manager-script");
						}
						roles.add(role);
					}

					return new GenericPrincipal(username, credentials, new ArrayList<>(roles));
				}
			}

		} catch (Exception e) {
			logger.log(Level.INFO, "Could not authenticate user " + username, e);
		}

		return null;
	}

	private String getTribefireServicesUrl() {
		if (tfsUrl != null) {
			return tfsUrl;
		}
		try {
			if (super.container instanceof Engine) {
				Engine se = (Engine) super.container;
				Service service = se.getService();
				Connector[] connectors = service.findConnectors();
				int httpPort = 8080;
				if (connectors != null && connectors.length > 0) {
					for (Connector connector : connectors) {
						String scheme = connector.getScheme();
						if (scheme != null && scheme.equalsIgnoreCase("http")) {
							httpPort = connector.getPort();
							break;
						}
					}
				}
				Server server = service.getServer();
				String host = server.getAddress();

				tfsUrl = "http://" + host + ":" + httpPort + "/tribefire-services";
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "Error while trying to determine the local TFS URL.", e);
		} finally {
			if (tfsUrl == null) {
				tfsUrl = tfsDefaultUrl;
			}
		}
		return tfsUrl;
	}

	private static String readBody(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();

		String body = null;

		if (entity != null) {
			try (InputStream instream = entity.getContent()) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				final byte[] buffer = new byte[1 << 15];

				int count;
				long totalCount = 0;

				while ((count = instream.read(buffer)) != -1) {
					try {
						outputStream.write(buffer, 0, count);
					} catch (Exception e) {
						throw new IOException(
								"Error while transfering data. Data transferred so far: " + totalCount + ". Current buffer size: " + count, e);
					}
					totalCount += count;
				}

				body = outputStream.toString("UTF-8");

				return body;
			}
		}
		return null;
	}

	@Override
	protected String getPassword(String username) {
		return null;
	}

	@Override
	protected Principal getPrincipal(String username) {
		return null;
	}

	public void setFullAccessAlias(String fullAccessAlias) {
		this.fullAccessAlias = fullAccessAlias;
	}
	public void setTfsUrl(String tfsUrl) {
		if (tfsUrl != null && tfsUrl.trim().length() > 0) {
			if (tfsUrl.endsWith("/")) {
				tfsUrl = tfsUrl.substring(0, tfsUrl.length() - 1);
			}
			this.tfsUrl = tfsUrl;
		}
	}

}
