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
package com.braintribe.spring.support.converter;

import org.springframework.core.convert.converter.Converter;

import com.braintribe.logging.Logger.LogLevel;

public class StringToLogLevelConverter implements Converter<String, LogLevel>{
	
	@Override
	public LogLevel convert(String source) {
		if (source == null) {
			return null;
		} else {
			if (source.equalsIgnoreCase("TRACE")) {
				return LogLevel.TRACE;
			} else if (source.equalsIgnoreCase("DEBUG")) {
				return LogLevel.DEBUG;
			} else if (source.equalsIgnoreCase("INFO")) {
				return LogLevel.INFO;
			} else if (source.equalsIgnoreCase("WARN")) {
				return LogLevel.WARN;
			} else if (source.equalsIgnoreCase("ERROR")) {
				return LogLevel.ERROR;
			} else {
				LogLevel level = LogLevel.valueOf(source);
				return level;
			}
		}		
	}
}
