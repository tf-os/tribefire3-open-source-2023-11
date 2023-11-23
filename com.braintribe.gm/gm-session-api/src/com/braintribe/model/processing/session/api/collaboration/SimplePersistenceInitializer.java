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
package com.braintribe.model.processing.session.api.collaboration;

import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StaticStage;

/**
 * @author peter.gazdik
 */
public abstract class SimplePersistenceInitializer extends AbstractPersistenceInitializer {

	protected StaticStage stage;

	@Override
	public PersistenceStage getPersistenceStage() {
		if (stage == null) {
			stage = StaticStage.T.create();
			stage.setName(stageName());
		}
		return stage;
	}

	protected String stageName() {
		return getClass().getName();
	}

	public static SimplePersistenceInitializer create(String stageName, DataInitializer dataInitializer) {
		return new SimplePersistenceInitializer() {
			// @formatter:off
			@Override protected String stageName() { return stageName; }
			@Override public void initializeData(PersistenceInitializationContext ctx) { dataInitializer.initialize(ctx); }
			// @formatter:on
		};
	}

}
