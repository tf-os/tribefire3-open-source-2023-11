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
package com.braintribe.model.processing.resource.streaming.cache;

import java.io.File;
import java.util.function.Function;



public class SplittedGuidFolderBuilder implements Function<String, File> {
	@Override
	public File apply(String guid) throws RuntimeException {
		if (guid.length() < 4) {
			return new File(guid);
		}
		String level1 = guid.substring(0, 4);
		if (guid.length() < 8) {
			return new File(level1);
		}
		String level2 = guid.substring(4, 8);
		if (guid.length() < 10) {
			return new File(new File(level1), level2);
		}
		String level3 = guid.substring(8, 10);
		return new File(new File(new File(level1), level2), level3);
	}

}
