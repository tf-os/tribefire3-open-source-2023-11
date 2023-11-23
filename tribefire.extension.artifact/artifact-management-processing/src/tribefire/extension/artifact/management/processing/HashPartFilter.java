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
package tribefire.extension.artifact.management.processing;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class HashPartFilter implements Predicate<String> {
	private static Pattern shaPattern = Pattern.compile(".*\\.sha\\d*$");
	public static final HashPartFilter INSTANCE = new HashPartFilter();
	
	@Override
	public boolean test(String partType) {
		if (partType == null)
			return false;
		
		if (partType.endsWith(".md5"))
			return true;
		
		if (shaPattern.matcher(partType).matches())
			return true;
		
		if (partType.endsWith(".asc"))
			return true;
		
		return false;

	}
}
