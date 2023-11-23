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
package com.braintribe.testing.tools.gm.meta;

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.testing.tools.gm.meta.ManipulationRecordingMode.GLOBAL;
import static com.braintribe.testing.tools.gm.meta.ManipulationRecordingMode.LOCAL;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;

/**
 * @author peter.gazdik
 */
public class ManipulationRecorder {

	private IncrementalAccess access;
	private ManipulationRecordingMode recordingMode;

	public ManipulationRecorder withAccess(IncrementalAccess access) {
		this.access = access;
		return this;
	}

	public ManipulationRecorder local() {
		return mode(ManipulationRecordingMode.LOCAL);
	}

	public ManipulationRecorder persistent() {
		return mode(ManipulationRecordingMode.PERSISTENT);
	}

	public ManipulationRecorder global() {
		return mode(ManipulationRecordingMode.GLOBAL);
	}

	private ManipulationRecorder mode(ManipulationRecordingMode mode) {
		this.recordingMode = mode;
		return this;
	}

	public ManipulationRecorderResult record(Consumer<PersistenceGmSession> blockToRecord) {
		return trackHelper(blockToRecord, newPersistenceSession(), false);
	}

	private ManipulationRecorderResult trackHelper(Consumer<PersistenceGmSession> blockToRecord, PersistenceGmSession session, boolean undo) {
		blockToRecord.accept(session);

		List<Manipulation> manipulations = session.getTransaction().getManipulationsDone();

		if (undo)
			session.getTransaction().undo(manipulations.size());

		Manipulation manipulation = asManipulation(manipulations);

		manipulation = remotifyIfEligible(manipulation);
		
		return new ManipulationRecorderResult(manipulation);
	}

	private Manipulation remotifyIfEligible(Manipulation cm) {
		if (recordingMode != LOCAL)
			return ManipulationRemotifier.with(cm).globalReferences(recordingMode == GLOBAL).remotify();
		else
			return cm;
	}

	private PersistenceGmSession newPersistenceSession() {
		return new BasicPersistenceGmSession(access);
	}

	// ###################################################
	// ## . . . . . . Manipulation Listener . . . . . . ##
	// ###################################################

	static class SimpleManipulationCollector implements ManipulationListener {
		private final List<AtomicManipulation> manipulations = newList();

		public Manipulation toManipulation() {
			return asManipulation(manipulations);
		}

		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (manipulation.manipulationType() != ManipulationType.COMPOUND)
				manipulations.addAll(manipulation.inline());
			else
				manipulations.add((AtomicManipulation) manipulation);
		}
	}

}
