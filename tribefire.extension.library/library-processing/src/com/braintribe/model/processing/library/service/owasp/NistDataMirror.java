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
package com.braintribe.model.processing.library.service.owasp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.braintribe.logging.Logger;

import us.springett.nistdatamirror.MetaProperties;
import us.springett.nistdatamirror.MirrorException;

public class NistDataMirror {

	private final static Logger logger = Logger.getLogger(NistDataMirror.class);

	private static final String CVE_JSON_11_MODIFIED_URL = "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.json.gz";
	private static final String CVE_JSON_11_BASE_URL = "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-%d.json.gz";
	private static final String CVE_MODIFIED_11_META = "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-modified.meta";
	private static final String CVE_BASE_11_META = "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-%d.meta";
	private static final Map<String, Map<String, String>> versionToFilenameMaps = new HashMap<>();
	private static final int START_YEAR = 2002;
	private static final int END_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	private File outputDir;
	private final Proxy proxy;

	{
		Map<String, String> version11Filenames = new HashMap<>();
		version11Filenames.put("cveJsonModifiedUrl", CVE_JSON_11_MODIFIED_URL);
		version11Filenames.put("cveJsonBaseUrl", CVE_JSON_11_BASE_URL);
		version11Filenames.put("cveModifiedMeta", CVE_MODIFIED_11_META);
		version11Filenames.put("cveBaseMeta", CVE_BASE_11_META);
		versionToFilenameMaps.put("1.1", version11Filenames);
	}

	public NistDataMirror(String outputDirPath) {
		if (outputDirPath.startsWith("file://")) {
			outputDir = new File(outputDirPath.substring("file://".length()));
		} else {
			outputDir = new File(outputDirPath);
		}
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		proxy = initProxy();
	}

	private Proxy initProxy() {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort != null && !proxyPort.trim().isEmpty()) {
			// throws NumberFormatException if proxy port is not numeric
			logger.debug(() -> "Using proxy " + proxyHost + ":" + proxyPort);
			String proxyUser = System.getProperty("http.proxyUser");
			String proxyPassword = System.getProperty("http.proxyPassword");
			if (proxyUser != null && !proxyUser.trim().isEmpty() && proxyPassword != null && !proxyPassword.trim().isEmpty()) {
				logger.debug(() -> "Using proxy user" + proxyUser + ":" + proxyPassword);
				Authenticator authenticator = new Authenticator() {

					@Override
					public PasswordAuthentication getPasswordAuthentication() {
						return (new PasswordAuthentication(proxyUser, proxyPassword.toCharArray()));
					}

				};
				Authenticator.setDefault(authenticator);
			}
			return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
		}
		return Proxy.NO_PROXY;
	}

	public boolean mirror(String version) throws MirrorException, IOException {

		Date currentDate = new Date();
		logger.debug(() -> "Downloading files at " + currentDate);
		MetaProperties before = readLocalMetaForURL(versionToFilenameMaps.get(version).get("cveModifiedMeta"));
		if (before != null) {
			long seconds = ZonedDateTime.now().toEpochSecond() - before.getLastModifiedDate();
			long hours = seconds / 60 / 60;
			if (hours < 2) {
				logger.debug(() -> "Using local NVD cache as last update was within two hours");
				return false;
			}
		}
		doDownload(versionToFilenameMaps.get(version).get("cveModifiedMeta"));
		MetaProperties after = readLocalMetaForURL(versionToFilenameMaps.get(version).get("cveModifiedMeta"));
		if (before == null || after.getLastModifiedDate() > before.getLastModifiedDate()) {
			doDownload(versionToFilenameMaps.get(version).get("cveJsonModifiedUrl"));
		}
		for (int year = START_YEAR; year <= END_YEAR; year++) {
			downloadVersionForYear(version, year);
		}
		return true;
	}

	private void downloadVersionForYear(String version, int year) throws MirrorException, IOException {
		MetaProperties before;
		MetaProperties after;
		String cveBaseMetaUrl = versionToFilenameMaps.get(version).get("cveBaseMeta").replace("%d", String.valueOf(year));
		before = readLocalMetaForURL(cveBaseMetaUrl);
		doDownload(cveBaseMetaUrl);
		after = readLocalMetaForURL(cveBaseMetaUrl);
		if (before == null || after.getLastModifiedDate() > before.getLastModifiedDate()) {
			String cveJsonBaseUrl = versionToFilenameMaps.get(version).get("cveJsonBaseUrl").replace("%d", String.valueOf(year));
			doDownload(cveJsonBaseUrl);
		}
	}

	private MetaProperties readLocalMetaForURL(String metaUrl) throws MirrorException {
		URL url;
		try {
			url = new URL(metaUrl);
		} catch (MalformedURLException ex) {
			throw new MirrorException("Invalid url: " + metaUrl, ex);
		}
		MetaProperties meta = null;
		String filename = url.getFile();
		filename = filename.substring(filename.lastIndexOf('/') + 1);
		File file = new File(outputDir, filename).getAbsoluteFile();
		if (file.isFile()) {
			meta = new MetaProperties(file);
		}
		return meta;
	}

	private void doDownload(String nvdUrl) throws MirrorException, IOException {
		URL url;
		try {
			url = new URL(nvdUrl);
		} catch (MalformedURLException ex) {
			throw new MirrorException("Invalid url: " + nvdUrl, ex);
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		File file = null;
		boolean success = false;
		try {
			String filename = url.getFile();
			filename = filename.substring(filename.lastIndexOf('/') + 1);
			file = new File(outputDir, filename).getAbsoluteFile();

			URLConnection connection = url.openConnection(proxy);
			logger.debug(() -> "Downloading " + url.toExternalForm());
			bis = new BufferedInputStream(connection.getInputStream());
			file = new File(outputDir, filename);
			bos = new BufferedOutputStream(new FileOutputStream(file));

			int i;
			while ((i = bis.read()) != -1) {
				bos.write(i);
			}
			success = true;
		} finally {
			close(bis);
			close(bos);
		}
		if (success) {
			logger.debug("Download succeeded " + file.getName());
			if (file.getName().endsWith(".gz")) {
				uncompress(file);
			}
		}
	}

	private void uncompress(File file) {
		byte[] buffer = new byte[1024];
		GZIPInputStream gzis = null;
		FileOutputStream out = null;
		try {
			File outputFile = new File(file.getAbsolutePath().replaceAll(".gz", ""));
			gzis = new GZIPInputStream(new FileInputStream(file));
			out = new FileOutputStream(outputFile);
			int len;
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			logger.debug(() -> "Uncompressed " + outputFile.getName());
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			close(gzis);
			close(out);
		}
	}

	private void close(Closeable object) {
		if (object != null) {
			try {
				object.close();
			} catch (IOException e) {
				logger.debug(() -> "Error while trying to close a stream.", e);
			}
		}
	}
}
