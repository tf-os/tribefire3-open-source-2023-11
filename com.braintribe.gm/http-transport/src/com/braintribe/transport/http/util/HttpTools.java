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
package com.braintribe.transport.http.util;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.util.EntityUtils;

import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

public class HttpTools {

	private static final Logger logger = Logger.getLogger(HttpTools.class);

	public static void closeResponseQuietly(String url, HttpResponse response) {
		if (response instanceof CloseableHttpResponse) {
			closeResponseQuietly(url, (CloseableHttpResponse) response);
		}
	}

	public static void closeResponseQuietly(String url, CloseableHttpResponse response) {
		if (response != null) {
			try {
				response.close();
			} catch(Exception e) {
				final String urlString = url != null ? url : "unknown";
				logger.warn(() -> "Failed to close HTTP response [ "+response+" ] from URL [ "+urlString+" ]", e);					
			}
		}
	}
	
	public static void consumeResponse(String url, HttpResponse response) {
		consumeResponse(url, response, null);
	}

	public static void consumeResponse(HttpResponse response) {
		consumeResponse(null, response, null);
	}

	public static void consumeResponse(HttpResponse response, Throwable t) {
		consumeResponse(null, response, t);
	}

	public static void consumeResponse(String url, HttpResponse response, Throwable t) {
		if (response != null) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					EntityUtils.consume(entity);
				} catch (Exception ce) {
					final String urlString = url != null ? url : "unknown";
					if (t == null) {
						logger.warn(() -> "Failed to consume entity [ "+entity+" ] from URL [ "+urlString+" ]", ce);
					} else {
						logger.warn(() -> "Failed to consume entity [ "+response.getEntity()+" ] from URL [ "+urlString+" ] upon request failure [ "+asString(t)+" ] due to [ "+asString(ce)+" ]", ce);					
					}
				}
			}
		}
	}
	
	/**
	 * Parses a Content-Type header value and returns the MIME-type part of it (without any attributes)
	 * @param spec The content-type header value
	 * @param lenient In case of an error, this specifies whether just a 
	 * 	debug log (true) should be printed or a RuntimeException (false) should be thrown.
	 * @return The MIME-type part or null if the source value was null/empty or the value could not be retrieved.
	 */
	public static String getMimeTypeFromContentType(String spec, boolean lenient) {
		if (StringTools.isBlank(spec)) {
			return null;
		}
		try {
			ContentType contentType = ContentType.parse(spec);
			return contentType.getMimeType();
		} catch(Exception e) {
			if (lenient) {
				logger.debug(() -> "Could not parse MIME-type: "+spec, e);
				return null;
			} else {
				throw Exceptions.unchecked(e, "Could not parse MIME-type: "+spec);
			}
		}
	}

	private static String asString(Throwable t) {
		return t.getClass().getName()+(t.getMessage() != null ? ": "+t.getMessage() : "");
	}
	
	/**
	 * Downloads the content returned by the provided URL. The return value is a pair of a temporary file
	 * containing the content and the filename. The filename is derived either by the <code>Content-Disposition</code> header (if present)
	 * or by taking the last part of the URL (after the last '/').
	 * <br /><br />
	 * Please note that the caller has to take care that the returned file gets deleted. The filename in the result
	 * is a best effort result and must not be used without any validation.
	 * 
	 * @param url The URL that should be used for downloading the content.
	 * @param httpClient The HTTP client that should be used for downloading.
	 * @return A {@link Pair} of the downloaded file and the assumed filename.
	 * @throws  RuntimeException Thrown when the download was not successful (e.g., a non-200 response, an IO Exception, etc)
	 */
	public static Pair<File,String> downloadFromUrl(String url, CloseableHttpClient httpClient) {
		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(url);
			response = httpClient.execute(get);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("Could not download content from URL: "+url+". Got response: "+response);
			}
			
			String filename = "download.bin";
			int index = url.lastIndexOf('/');
			if (index != -1) {
				filename = url.substring(index+1);
			}
			
			Header contentDisposition = response.getFirstHeader("Content-Disposition");
			if (contentDisposition != null) {
				String cdFilename = getFilenameFromContentDisposition(contentDisposition.getValue());
				if (cdFilename != null) {
					filename = cdFilename;
				}
			}
			
			String normalizedFilename = FileTools.normalizeFilename(filename, '_');
			String rawName = FileTools.getNameWithoutExtension(normalizedFilename);
			String ext = FileTools.getExtension(normalizedFilename);
			File targetFile = File.createTempFile(rawName, "."+ext);
			
			try (InputStream is = new ResponseEntityInputStream(response)) {
				IOTools.inputToFile(is, targetFile);
			} catch(Exception e) {
				logger.error("Error while downloading banner from "+url, e);
			}
			return new Pair<>(targetFile, filename);
		} catch(Exception e) {
			throw Exceptions.unchecked(e, "Could not download URL "+url);
		} finally {
			HttpTools.consumeResponse(url, response);
			IOTools.closeCloseable(response, logger);
		}
	}
	
	
	
	/**
	 * Extracts the filename of a content-disposition header value. It supports filename and filename* parameter names.
	 * Note that the resulting filename is not necessarily compatible with the OS.
	 * 
	 * @param contentDisposition The header value that should be parsed.
	 * @return The filename specified in the content-disposition, or null if none was located.
	 */
	public static String getFilenameFromContentDisposition(String contentDisposition) {
		if (StringTools.isBlank(contentDisposition)) {
			return null;
		}
		String[] split = StringTools.splitSemicolonSeparatedString(contentDisposition, true);
		if (split == null || split.length == 0) {
			return null;
		}
		for (String entry : split) {
			String lowerCase = entry.toLowerCase();
			String encoding = "ISO-8859-1";

			if (lowerCase.startsWith("filename=")) {
				String filename = entry.substring("filename=".length());
				if (filename.startsWith("\"") && filename.endsWith("\"")) {
					filename = filename.substring(1, filename.length()-1);
				}
				try {
					filename = URLDecoder.decode(filename, encoding);
				} catch (UnsupportedEncodingException e) {
					logger.trace("Could not URL decode filename "+filename, e);
					//We tried
				}
				return filename;
			} else if (lowerCase.startsWith("filename*=")) {
				String filename = entry.substring("filename=*".length());
				if (filename.startsWith("\"") && filename.endsWith("\"")) {
					filename = filename.substring(1, filename.length()-1);
				}
				try {
					int index = filename.indexOf("''");
					if (index != -1) {
						encoding = filename.substring(0, index).trim();
						filename = filename.substring(index+2).trim();
					}
					filename = URLDecoder.decode(filename, encoding);
				} catch (UnsupportedEncodingException e) {
					logger.trace("Could not URL decode filename "+filename+" using encoding "+encoding, e);
					//We tried
				}
				
				return filename;

			}
		}
		return null;
	}

	/**
	 * Creates a String representation of the response. The result should not be used for anything
	 * else than as a contextual information (e.g., for logging purposes).
	 * 
	 * @param response The response to be converted into a single String.
	 * @return The visual representation of the response provided.
	 */
	public static String toString(HttpResponse response) {
		if (response == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null) {
			String statusLineString = BasicLineFormatter.INSTANCE.formatStatusLine(null, statusLine).toString();
			sb.append(statusLineString);
			sb.append('\n');
		}
		
		Header[] headers = response.getAllHeaders();
		if (headers != null) {
			for (Header h : headers) {
				if (h != null) {
					String headerString = BasicLineFormatter.INSTANCE.formatHeader(null, h).toString();
					sb.append(headerString);
					sb.append('\n');
				}
			}
		}
		if (sb.length() > 0) {
			sb.append('\n');
		}
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				String body = EntityUtils.toString(entity);
				sb.append(body);
			} catch (Exception e) {
				logger.debug(() -> "Error while trying to read body from response: "+sb.toString(), e);
			}
		}
		return sb.toString();
	}
}
