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
package tribefire.extension.hydrux.model.deployment.domain;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.hydrux.model.deployment.HxComponent;
import tribefire.extension.hydrux.model.deployment.HxScope;

/**
 * Denotes a tribefire service-domain, represented by a supplier, which can be used to resolve a service domain's externalId.
 * <p>
 * For example you can specify the value directly use {@link HxStaticDomainSupplier}, or you can specify the domain id should be read from the URL
 * with {@link HxUrlDomainSupplier}.
 *
 * @see HxScope#getDefaultDomain()
 * 
 * @author peter.gazdik
 */
@Abstract
public interface HxDomainSupplier extends HxComponent {

	EntityType<HxDomainSupplier> T = EntityTypes.T(HxDomainSupplier.class);

}
