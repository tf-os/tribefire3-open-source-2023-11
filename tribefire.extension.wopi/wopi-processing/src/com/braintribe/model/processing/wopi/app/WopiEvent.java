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
package com.braintribe.model.processing.wopi.app;

import java.util.regex.Pattern;

/**
 * 
 * Getting information if file/folder/Content/Children based on URL information
 */
public enum WopiEvent {

	/**
	 * Provides access to information about a file and allows for file-level operations.
	 */
	FILES("^\\/files\\/(?<id>[^\\/]+)$"),

	/**
	 * Provides access to information about a folder and allows for folder level operations.
	 */
	FOLDERS("^\\/folders\\/(?<id>[^\\/]+)$"),

	/**
	 * Provides access to operations that get and update the contents of a file.
	 */
	CONTENTS("^\\/files\\/(?<id>[^\\/]+)\\/contents$"),

	/**
	 * Provides access to the files and folders in a folder.
	 */
	CHILDREN("^\\/folders\\/(?<id>[^\\/]+)\\/children$"),

	// -------------

	RESOURCE("^/tf-resource.*$");

	private final Pattern pattern;

	private WopiEvent(String regex) {
		pattern = Pattern.compile(regex);
	}

	public Pattern pattern() {
		return pattern;
	}

	@Override
	public String toString() {
		return String.format("%s(\"%s\")", super.toString(), pattern);
	}

	public static WopiEvent eventOf(String input) {
		for (WopiEvent event : values()) {
			if (event.pattern.matcher(input).matches()) {
				return event;
			}
		}
		return null;
	}
}