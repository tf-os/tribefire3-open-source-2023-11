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
package com.braintribe.web.servlet.about;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;

public class ParameterTools {

	private static Logger logger = Logger.getLogger(ParameterTools.class);
	
	public static String getParameterMapAsString(HttpServletRequest req) {
		StringBuilder result = new StringBuilder();
		Map<String, String[]> parameterMap = req.getParameterMap();
		if (parameterMap != null) {
			boolean first = true;
			for (Map.Entry<String,String[]> entry : parameterMap.entrySet()) {
				String key = entry.getKey();
				String[] values = entry.getValue();
				String valuesAsString = StringTools.createStringFromArray(values);
				if (!first) {
					result.append(", ");
				} else {
					first = false;
				}
				result.append(key != null ? key : "null");
				result.append("=");
				result.append(valuesAsString != null ? valuesAsString : "null");
			}
		}
		return result.toString();
	}
	public static String getTypeOfRequest(HttpServletRequest req) {
		Map<String, String[]> parameters = req.getParameterMap();
		String type = getSingleParameter(parameters, "type");
		return type;
	}
	public static String getSingleParameter(Map<String, String[]> parameters, String key) {
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	public static String getSingleParameterAsString(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	public static Long getSingleParameterAsLong(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		try {
			return Long.parseLong(values[0]);
		} catch (NumberFormatException nfe) {
			logger.error("Invalid long value: " + values[0]);
			return null;
		}
	}
	public static Integer getSingleParameterAsInteger(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		try {
			return Integer.parseInt(values[0]);
		} catch (NumberFormatException nfe) {
			logger.error("Invalid integer value: " + values[0]);
			return null;
		}
	}
	public static Boolean getSingleParameterAsBoolean(HttpServletRequest req, String key) {
		Map<String, String[]> parameters = req.getParameterMap();
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return Boolean.parseBoolean(values[0]);
	}

}
