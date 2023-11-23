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
package com.braintribe.model.access.sql.test.base;

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulationRequest;

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.testing.tools.gm.meta.ManipulationRecorder;

/**
 * TODO extract as common tool once it's mature enough.
 * 
 * @author peter.gazdik
 */
public class ManipulationElf {

	protected final IncrementalAccess access;
	protected final ManipulationRecorder manipulationRecorder;

	protected List<AtomicManipulation> recordedManipulations;
	protected ManipulationResponse response;

	public ManipulationElf(IncrementalAccess access) {
		this.access = access;
		this.manipulationRecorder = new ManipulationRecorder().withAccess(access).persistent();
	}

	public ManipulationRecorder getManipulationRecorder() {
		return manipulationRecorder;
	}

	public List<AtomicManipulation> getRecordedManipulations() {
		return recordedManipulations;
	}

	public ManipulationResponse getResponse() {
		return response;
	}

	// ###########################################
	// ## . . . . . . . Record/Apply . . . . . .##
	// ###########################################

	public void record(Consumer<PersistenceGmSession> r) {
		recordedManipulations = manipulationRecorder.record(r).asAtomicManipulations();
	}

	public ManipulationResponse apply(Consumer<PersistenceGmSession> r) {
		record(r);
		return apply();
	}

	public ManipulationResponse apply() {
		ManipulationRequest manipulationRequest = asManipulationRequest(asManipulation(recordedManipulations));
		return response = access.applyManipulation(manipulationRequest);
	}

}
