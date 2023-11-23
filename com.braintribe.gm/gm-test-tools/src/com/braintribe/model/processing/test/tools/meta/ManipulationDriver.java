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
package com.braintribe.model.processing.test.tools.meta;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulationRequest;
import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.createInverse;
import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.GLOBAL;
import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.LOCAL;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.tracking.SimpleManipulationCollector;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationRemotifier;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.smood.Smood;

/**
 * Helper class for working with manipulations on a session. There are more different methods, some only for convenience
 * and backwards compatibility. I (PGA) currently use these two for everything: {@link #run(SessionRunnable)} and
 * {@link #dryRun(SessionRunnable)}.
 */
public class ManipulationDriver {

	private final IncrementalAccess access;
	private ManipulationTrackingMode trackingMode = ManipulationTrackingMode.PERSISTENT;

	public ManipulationDriver() {
		this(new Smood(EmptyReadWriteLock.INSTANCE));
	}

	public ManipulationDriver(IncrementalAccess access) {
		this.access = access;
	}

	public IncrementalAccess getAccess() {
		return access;
	}

	public void setTrackingMode(ManipulationTrackingMode trackingMode) {
		this.trackingMode = trackingMode;
	}

	public CompoundManipulation track(NotifyingSessionRunnable r) {
		return track(r, newNotifyingSession());
	}

	public CompoundManipulation track(NotifyingSessionRunnable r, NotifyingGmSession session) {
		SimpleManipulationCollector collector = new SimpleManipulationCollector();
		collector.setCollectCompoundManipulations(false);

		session.listeners().add(collector);

		try {
			r.run(session);

		} finally {
			session.listeners().remove(collector);
		}

		List<Manipulation> manipulations = collector.getManipulations();

		CompoundManipulation cm = compound(manipulations);
		cm.setInverseManipulation(createInverse(cm));

		return remotifyIfEligible(cm);
	}

	public CompoundManipulation dryRun(Runnable r, PersistenceGmSession session) {
		r.run();

		List<Manipulation> manipulationsDone = rollback(session);

		CompoundManipulation cm = compound(manipulationsDone);
		cm.setInverseManipulation(createInverse(cm));

		return remotifyIfEligible(cm);
	}

	/**
	 * Executes a dry run of given {@link SessionRunnable}. It does not commit the manipulations, but rolls them back
	 * and returns a {@link ManipulationRequest} containing the tracked manipulations. Yes, this mens the touched
	 * entities will not be changed at all by the given {@link SessionRunnable} (well, maybe except some
	 * {@link AbsenceInformation} could be resolved).
	 */
	public ManipulationRequest dryRunAsRequest(SessionRunnable r) {
		Manipulation manipulation = dryRun(r);
		return asManipulationRequest(manipulation);
	}

	public Manipulation dryRun(SessionRunnable r) {
		PersistenceGmSession session = newSession();

		run(r, session);

		List<Manipulation> manipulationsDone = rollback(session);

		CompoundManipulation cm = compound(manipulationsDone);
		cm.setInverseManipulation(createInverse(cm));

		return remotifyIfEligible(cm);
	}

	/** Executes given {@link SessionRunnable} and commits all the manipulations. */
	public void run(SessionRunnable r) {
		PersistenceGmSession session = newSession();

		run(r, session);
		session.commit();
	}

	private void run(SessionRunnable r, PersistenceGmSession session) {
		try {
			r.run(session);
		} catch (Exception e) {
			throw new RuntimeException("Running manipulations failed.", e);
		}
	}

	private static List<Manipulation> rollback(PersistenceGmSession session) {
		List<Manipulation> md = newList(session.getTransaction().getManipulationsDone());
		session.getTransaction().undo(md.size());
		return md;
	}

	private CompoundManipulation remotifyIfEligible(CompoundManipulation cm) {
		if (trackingMode != LOCAL)
			return remotify(cm, trackingMode);
		else
			return cm;
	}

	public static CompoundManipulation remotify(CompoundManipulation cm, ManipulationTrackingMode tm) {
		return (CompoundManipulation) ManipulationRemotifier.with(cm).globalReferences(tm == GLOBAL).remotify();
	}

	public static interface NotifyingSessionRunnable {
		void run(NotifyingGmSession session);
	}

	public static NotifyingGmSession newNotifyingSession() {
		BasicNotifyingGmSession session = new BasicNotifyingGmSession();
		session.interceptors() //
				.with(ManipulationTracking.class) //
				.before(CollectionEnhancer.class) //
				.add(new ManipulationTrackingPropertyAccessInterceptor());
		session.interceptors() //
				.with(CollectionEnhancer.class) //
				.add(new CollectionEnhancingPropertyAccessInterceptor());

		return session;
	}

	public static interface SessionRunnable {
		void run(PersistenceGmSession session) throws Exception;
	}

	public BasicPersistenceGmSession newSession() {
		return new BasicPersistenceGmSession(access);
	}

}
