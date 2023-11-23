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
package com.braintribe.utils.genericmodel;

import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.utils.RandomTools;

/**
 * Returns a random 32 characters long string containing only hexadecimal characters,
 * {@link #setDtsPrefixEnabled(boolean) optionally} starting with a timestamp prefix. Note that the IDs generated are
 * similar to the GUIDs (usually) used in CSP. The algorithm is different though. <br>
 * This generator can e.g. used in tests, if <code>biz.i2z.service.ecm.access.impl.generator.GUIDGenerator</code> is not
 * available in the classpath.
 *
 * @author michael.lafite
 */
public class GuidGenerator implements IdGenerator, Supplier<String> {

	boolean dtsPrefixEnabled = true;

	public GuidGenerator() {
		// nothing to do
	}

	public boolean isDtsPrefixEnabled() {
		return this.dtsPrefixEnabled;
	}

	public void setDtsPrefixEnabled(final boolean dtsPrefixEnabled) {
		this.dtsPrefixEnabled = dtsPrefixEnabled;
	}

	@Override
	public Object generateId(final GenericEntity entity) throws Exception {
		return get();
	}

	@Override
	public String get() {
		return RandomTools.getRandom32CharactersHexString(isDtsPrefixEnabled());
	}

}
