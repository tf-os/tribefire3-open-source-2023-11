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
package tribefire.module.wire.contract;

import java.util.function.Supplier;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.marshallerdeployment.HardwiredMarshaller;

/**
 * Offers methods for binding {@link Marshaller}s.
 * 
 * @see HardwiredDeployablesContract
 */
public interface HardwiredMarshallersContract extends HardwiredDeployablesContract {

	default HardwiredMarshaller bindMarshaller(String externalId, String name, Marshaller marshaller, String... mimeTypes) {
		return bindMarshaller(externalId, name, () -> marshaller, mimeTypes);
	}

	/** Recommended externalId convention: marshaller.${type} (e.g. marshaller.xml) */
	HardwiredMarshaller bindMarshaller(String externalId, String name, Supplier<Marshaller> marshaller, String... mimeTypes);

}
