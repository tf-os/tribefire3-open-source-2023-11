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
package tribefire.cortex.initializer.support.impl;

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;

/**
 * @author peter.gazdik
 */
public class CortexInitializerTools {

	/** Similar to {@link #addToCortexModel(ManagedGmSession, String...)}, but first extracts globalIds from given {@link Model}s. */
	public static void addToCortexModel(ManagedGmSession session, Model... models) {
		String[] gids = Stream.of(models) //
				.map(Model::globalId) //
				.toArray(n -> new String[n]);

		addToCortexModel(session, gids);
	}

	/**
	 * Adds models with given globalIds to the cortex model (so that types from given models can be instantiated in cortex). This useful for example
	 * for deployment or meta-data models.
	 */
	public static void addToCortexModel(ManagedGmSession session, String... modelGlobalIds) {
		GmMetaModel cortexModel = session.getEntityByGlobalId(CoreInstancesContract.cortexModelGlobalId);
		List<GmMetaModel> cortexDeps = cortexModel.getDependencies();

		for (String mgid : modelGlobalIds) {
			GmMetaModel customModel = session.getEntityByGlobalId(mgid);
			cortexDeps.add(customModel);
		}
	}

}
