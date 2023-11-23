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

import java.util.function.BiConsumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.web.servlet.auth.WebCredentialsProvider;
import com.braintribe.web.servlet.home.model.LinkCollection;

import tribefire.module.api.DenotationTransformerRegistry;
import tribefire.module.api.EnvironmentDenotations;

/**
 * Contract for binding {@link EnvironmentDenotations Environment Denotation Registry} transformers.
 * 
 * @author peter.gazdik
 */
public interface WebPlatformHardwiredExpertsContract extends HardwiredExpertsContract {

	<T extends GenericEntity> void bindLandingPageLinkConfigurer(String groupPattern, EntityType<T> type, BiConsumer<T, LinkCollection> configurer);

	void registerWebCredentialsProvider(String key, WebCredentialsProvider webCredentialsProvider);
}
