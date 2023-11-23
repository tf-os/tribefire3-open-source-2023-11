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
package com.braintribe.model.access.smood.collaboration.deployment.wrappers;

import java.util.function.BiConsumer;

import com.braintribe.common.lcd.function.TriConsumer;
import com.braintribe.model.access.collaboration.CollaborationSmoodSession;
import com.braintribe.model.access.collaboration.distributed.DistributedCollaborativeSmoodAccess;
import com.braintribe.model.access.smood.collaboration.distributed.basic.Dcsa_Manipulation_Correctness_Test;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;

/**
 * DCSA that we use for tests, to have a way to intercept/assert things going on inside.
 * 
 * @see Dcsa_Manipulation_Correctness_Test#updateWithDeleteManipulationsDoesNotQueryForReferences()
 * 
 * @author peter.gazdik
 */
public class DistributedCollaborativeSmoodAccess4Test extends DistributedCollaborativeSmoodAccess {

	@Override
	protected CollaborationSmoodSession newSession() {
		return new CollaborationSmoodSession4Test();
	}

	@Override
	public CollaborationSmoodSession4Test getSmoodSession() {
		return (CollaborationSmoodSession4Test) session;
	}

	public static class CollaborationSmoodSession4Test extends CollaborationSmoodSession {

		public TriConsumer<GenericEntity, DeleteMode, BiConsumer<GenericEntity, DeleteMode>> deleteEntityInterceptor;

		@Override
		public void deleteEntity(GenericEntity entity, DeleteMode deleteMode) {
			if (deleteEntityInterceptor != null)
				deleteEntityInterceptor.accept(entity, deleteMode, super::deleteEntity);
			else
				super.deleteEntity(entity, deleteMode);
		}

	}

}
