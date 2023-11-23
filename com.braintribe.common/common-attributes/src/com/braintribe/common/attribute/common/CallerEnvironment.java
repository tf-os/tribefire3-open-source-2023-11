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
package com.braintribe.common.attribute.common;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * <p>
 * A {@link TypeSafeAttribute} for specifying the caller environment.
 * 
 * @author christina.wilpernig
 */
public interface CallerEnvironment extends TypeSafeAttribute<CallerEnvironment> {

	boolean isLocal();

	File currentWorkingDirectory();

	static File resolveRelativePath(String path) {
		File file = canonicalFileFor(path);

		if (file.isAbsolute())
			return file;

		return new File(getCurrentWorkingDirectory(), path);
	}

	static File canonicalFileFor(String path) {
		try {
			return new File(path).getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot get canonical file for path: [" + path + "]", e);
		}
	}

	static File getCurrentWorkingDirectory() {
		Optional<CallerEnvironment> ce = AttributeContexts.peek().findAttribute(CallerEnvironment.class);

		if (ce.isPresent()) {
			return ce.get().currentWorkingDirectory();
		} else {
			return new File(System.getProperty("user.dir"));
		}
	}

}
