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
package tribefire.extension.scheduling.templates.util;

import java.util.zip.Adler32;

import com.braintribe.utils.lcd.StringTools;

public class NameShortening {

	public static String shorten(String hashSource, String name) {

		Adler32 adler = new Adler32();
		adler.update(hashSource.getBytes()); // Note: we don't really care about encoding here
		String hash = Long.toHexString(adler.getValue());

		String trimmed = StringTools.truncateIfRequired(name, 7);
		return trimmed + "_" + hash;
	}

}
