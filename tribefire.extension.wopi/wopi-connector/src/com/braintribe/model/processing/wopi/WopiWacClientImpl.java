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
package com.braintribe.model.processing.wopi;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.velocity.shaded.commons.io.FilenameUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.uncheckedcounterpartexceptions.UncheckedMalformedURLException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.wopi.model.WopiDiscovery;
import com.braintribe.model.processing.wopi.model.WopiDiscovery.ProofKey;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class WopiWacClientImpl implements WopiWacClient, InitializationAware {

	private final static Logger logger = Logger.getLogger(WopiWacClientImpl.class);

	private static final String WopiHostFormat = "%s?%sWOPISrc=%s&access_token=%s&access_token_ttl=%s";
	private static final String WopiFileFormat = "%s/files/%s";
	private static final Pattern WopiPattern = Pattern.compile("(?<arg>\\w*=\\w*&)|(<(?<key>\\w*)=(?<opt>\\w*)&>)");

	private WopiDiscovery discovery;

	private Map<String, String> mimeTypeExtensionsMap;

	private String wacDiscoveryEndpoint;

	private com.braintribe.model.wopi.connector.WopiWacConnector deployable;

	// -----------------------------------------------------------------------
	// INITIALIZATIONAWARE
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		discovery = null;
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	// get information which document it is and form URL (e.g. for .doc .xls .ppt etc.)
	@Override
	public String getUrlsrc(String actionName, String ext) {

		tryConnect();

		for (WopiDiscovery.NetZone.App app : discovery.getNetZone().getApp()) {

			logger.debug(() -> {
				StringBuilder sb = new StringBuilder();
				sb.append("Checking Apps - Class: '");
				sb.append(app.getClass().getName());
				sb.append("' FavIconUrl: '");
				sb.append(app.getFavIconUrl());
				sb.append("' Name: '");
				sb.append(app.getName());
				sb.append("'");

				return sb.toString();
			});
			for (WopiDiscovery.NetZone.App.Action action : app.getAction()) {
				logger.trace(() -> getActionInformation(action));

				if (StringUtils.equalsIgnoreCase(action.getName(), actionName)) {
					if (StringUtils.equalsIgnoreCase(action.getExt(), ext)) {
						logger.debug(() -> "Picked: '" + getActionInformation(action) + "'");
						String urlsrc = action.getUrlsrc();
						// the host will be replaced by the one configured in WopiWacConnector
						urlsrc = replaceExistingUrl(urlsrc, wacDiscoveryEndpoint);

						return urlsrc;
					}
				}
			}
		}
		logger.error(
				() -> "Could not find wacDiscoveryEndpoint for actionName: '" + actionName + "' ext: '" + ext + "'. This filetype is not supported!");
		return null;
	}

	@Override
	public ProofKey proofKey() {
		tryConnect();
		return discovery.getProofKey();
	}

	private void tryConnect() {
		if (discovery != null) {
			logger.trace(() -> "Already connected to wacDiscoveryEndpoint: '" + wacDiscoveryEndpoint + "' using mimeTypeExtensionsMap with: '"
					+ mimeTypeExtensionsMap.size() + "' entries");
			return;
		}

		connect();
	}

	private synchronized void connect() {
		if (discovery != null) {
			return;
		}

		//@formatter:off
		RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
				  .handle(GenericRuntimeException.class)
				  .withDelay(Duration.ofMillis(deployable.getDelayOnRetryInMs()))
				  .withMaxRetries(deployable.getConnectionRetries());
		//@formatter:on

		Failsafe.with(retryPolicy).run(() -> {

			URL url = null;
			try {
				logger.debug(() -> "Trying to connect to wacDiscoveryEndpoint: '" + wacDiscoveryEndpoint + "' using mimeTypeExtensionsMap with: '"
						+ mimeTypeExtensionsMap.size() + "' entries");
				url = new URL(wacDiscoveryEndpoint);

				try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
				//@formatter:off
				RequestConfig requestConfig = RequestConfig.custom()
						.setConnectionRequestTimeout(deployable.getConnectionRequestTimeoutInMs())
						.setConnectTimeout(deployable.getConnectTimeoutInMs())
						.setSocketTimeout(deployable.getSocketTimeoutInMs())
					.build();
				//@formatter:on
					HttpGet request = new HttpGet(url.toString());
					request.setConfig(requestConfig);
					CloseableHttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					try (InputStream is = entity.getContent()) {
						this.discovery = JAXB.unmarshal(is, WopiDiscovery.class);
					}
				}
			} catch (Exception e) {
				discovery = null; // reset in case of an error
				throw new GenericRuntimeException("Could not connect to url: '" + url + "'", e);
			}
		});
	}

	private String replaceExistingUrl(String originalUrlString, String targetUrlString) {
		try {
			URL originalUrl = new URL(originalUrlString);

			URL targetUrl = new URL(targetUrlString);
			String newProtocol = targetUrl.getProtocol();
			String newHost = targetUrl.getHost();
			int newPort = targetUrl.getPort();

			String userInfo = targetUrl.getUserInfo();
			URL newURL = new URL(newProtocol, newHost, newPort, originalUrl.getFile());
			String newUrlString = newURL.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("Replaced: '" + originalUrl + "' with: '" + newUrlString + "'");
			}
			if (userInfo != null) {
				logger.debug(() -> "Additionally added basic authentication for user: '" + userInfo.split(":")[0] + "'");
				newUrlString = newUrlString.replace("://", "://" + userInfo + "@");
			}

			return newUrlString;
		} catch (MalformedURLException e) {
			throw new UncheckedMalformedURLException("Could not create URL from: '" + originalUrlString + "'", e);
		}
	}

	@Override
	public URL getDocumentLink(String action, URL requestURL, String correlationId, String mimeType, String name, String accessToken,
			String accessTokenTtl) throws MalformedURLException {
		String ext = mimeTypeExtensionsMap.get(mimeType);
		if (StringUtils.equalsIgnoreCase(ext, "bin")) {
			ext = FilenameUtils.getExtension(name);
		}
		String id = String.format(WopiFileFormat, requestURL, correlationId);

		String replaceUrl = WopiConnectorUtil.getPublicServicesUrl(deployable.getCustomPublicServicesUrl());

		id = replaceExistingUrl(id, replaceUrl);

		URL documentLink = getDocumentLink(action, ext, id, accessToken, accessTokenTtl);
		logger.debug(() -> "Returning document link: '" + documentLink + "'");
		return documentLink;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private URL getDocumentLink(String action, String ext, String id, String token, String accessTokenTtl) throws MalformedURLException {
		URL url = new URL(getUrlsrc(action, ext));
		StringBuilder query = new StringBuilder();
		for (Matcher args = WopiPattern.matcher(StringUtils.trimToEmpty(url.getQuery())); args.find();) {
			query.append(StringUtils.trimToEmpty(args.group("arg")));
		}

		String file = String.format(WopiHostFormat, url.getPath(), query, id, token, accessTokenTtl);
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
	}

	private String getActionInformation(WopiDiscovery.NetZone.App.Action action) {
		StringBuilder sb = new StringBuilder();
		sb.append("Checking App - Class: '");
		sb.append(action.getClass());
		sb.append("' Ext: '");
		sb.append(action.getExt());
		sb.append("' Name: '");
		sb.append(action.getName());
		sb.append("' Newext: '");
		sb.append(action.getNewext());
		sb.append("' Newprogid: '");
		sb.append(action.getNewprogid());
		sb.append("' Progid: '");
		sb.append(action.getProgid());
		sb.append("' Requires: '");
		sb.append(action.getRequires());
		sb.append("' Urlsrc: '");
		sb.append(action.getUrlsrc());
		sb.append("'");

		return sb.toString();
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setMimeTypeExtensionsMap(Map<String, String> mimeTypeExtensionsMap) {
		this.mimeTypeExtensionsMap = mimeTypeExtensionsMap;
	}

	@Required
	@Configurable
	public void setWacDiscoveryEndpoint(String wacDiscoveryEndpoint) {
		this.wacDiscoveryEndpoint = wacDiscoveryEndpoint;
	}

	@Required
	@Configurable
	public void setDeployable(com.braintribe.model.wopi.connector.WopiWacConnector deployable) {
		this.deployable = deployable;
	}
}
