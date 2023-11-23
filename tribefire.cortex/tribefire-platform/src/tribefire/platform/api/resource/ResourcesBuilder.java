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
package tribefire.platform.api.resource;

import java.io.UncheckedIOException;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;

import tribefire.module.api.ResourceHandle;

/**
 * @see ResourceHandle
 */
public interface ResourcesBuilder extends ResourceHandle {

	/**
	 * <p>
	 * Returns an unmarshalled object from the resource contents.
	 * 
	 * <p>
	 * Assumes the resource is a marshalled representation of a object compatible with the given {@link Marshaller}
	 * instance.
	 * 
	 * @param marshaller
	 *            The {@link Marshaller} to be used for unmarshalling the resource.
	 * @return An unmarshalled object from the resource contents.
	 * @throws UncheckedIOException
	 *             In case of IOException(s) while reading the resource contents.
	 * @throws MarshallException
	 *             Upon failures while unmarshalling the resource.
	 */
	<T> T asAssembly(Marshaller marshaller) throws UncheckedIOException, MarshallException;

	/**
	 * Similar to {@link #asAssembly(Marshaller)}, but returns the default value if the underlying resource (e.g. File)
	 * does not exist.
	 */
	<T> T asAssembly(Marshaller marshaller, T defaultValue);

}
