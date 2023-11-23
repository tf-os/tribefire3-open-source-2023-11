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
package com.braintribe.tribefire.jdbc;

import java.net.URI;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.braintribe.logging.Logger;

/**
 * The Class TfDriver.
 *
 */
public class TfDriver implements Driver {

	protected static Logger logger = Logger.getLogger(TfDriver.class);
	
	static {
		try {
			TfDriver driverInst = new TfDriver();
			DriverManager.registerDriver(driverInst);
		} catch (SQLException e) {
			logger.error("Could not register driver", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (url == null || url.trim().length() == 0) {
			throw new SQLException("The url must not be null or empty.");
		}
		if (!url.toLowerCase().startsWith("jdbc:tribefire://")) {
			throw new SQLException("The url must start with jdbc:tribefire://");
		}
		
		//remove jdbc: prefix
		String realUri = url.substring(5);
		
		
		String accessId = null;
		String username = info != null ? info.getProperty("user") : null;
		String password = info != null ? info.getProperty("password") : null;
		String hostname = "localhost";
		String path = "/tribefire-services";
		int port = 8443;
		boolean ssl = true;

		try {
			URI uri = new URI(realUri);
			
			hostname = uri.getHost();
			int tmpPort = uri.getPort();
			if (tmpPort > 0) {
				port = tmpPort;
			}
			String tmpPath = uri.getPath();
			if (tmpPath != null & tmpPath.trim().length() > 0) {
				path = tmpPath;
			}
			
			Map<String, String> query_pairs = new LinkedHashMap<String, String>();
			String query = uri.getQuery();
			if (query == null) {
				throw new SQLException("Could not parse the query part of the URI "+uri);
			}
			String[] pairs = query.split("&");
		    for (String pair : pairs) {
		        int idx = pair.indexOf("=");
		        String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8").toLowerCase();
		        String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
		        query_pairs.put(key, value);
		        
		        switch(key) {
		        	case "accessid":
		        		accessId = value;
		        		break;
		        	case "username":
		        		username = value;
		        		break;
		        	case "password":
		        		password = value;
		        		break;
		        	case "ssl":
		        		ssl = value.equalsIgnoreCase("true");
		        		break;
		        	default:
		        		logger.debug("Unknown parameter: "+key+"="+value);
		        }
		    }
		    
		} catch (Exception e) {
			throw new SQLException("Invalid URL format: "+url, e);
		}
		
		if (accessId == null) {
			throw new SQLException("accessId parameter is missing in URL: "+url);
		}
		
		String protocol = ssl ? "https" : "http";
		String tfUrl = protocol+"://"+hostname+":"+port+path;

		
		TfConnection con = new TfConnection(tfUrl, accessId, username, password, info);
		return con;
	}

	/* (non-Javadoc)
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		if (url == null) {
			return false;
		}
		return url.toLowerCase().startsWith("jdbc:tribefire://");
	}

	/* (non-Javadoc)
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		List<DriverPropertyInfo> list = new ArrayList<>();
		DriverPropertyInfo username = new DriverPropertyInfo("username", "cortex");
		list.add(username);
		DriverPropertyInfo password = new DriverPropertyInfo("password", "cortex");
		list.add(password);
		DriverPropertyInfo access = new DriverPropertyInfo("accessId", "cortex");
		list.add(access);
		DriverPropertyInfo ssl = new DriverPropertyInfo("ssl", "true");
		list.add(ssl);
		return list.toArray(new DriverPropertyInfo[list.size()]);
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	@Override
	public boolean jdbcCompliant() {
		return false;
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException(TfMetadata.getUnsupportedMessage());
	}

}
