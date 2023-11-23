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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools.asManipulation;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * {@link PersistenceAppender} that appends {@link Manipulation}'s to it's delegate iff they pass the configured filter.
 */
public class FilteringPersistenceAppender implements PersistenceAppender {

	private static Logger log = Logger.getLogger(FilteringPersistenceAppender.class);

	private static final AppendedSnippet[] EMPTY_APPEND_RESULT = new AppendedSnippet[2];

	private final PersistenceAppender delegate;
	private final Predicate<? super AtomicManipulation> filter;

	public FilteringPersistenceAppender(PersistenceAppender delegate, Predicate<? super AtomicManipulation> filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	@Override
	public AppendedSnippet[] append(Manipulation manipulation, ManipulationMode mode) throws ManipulationPersistenceException {
		Map<Boolean, List<AtomicManipulation>> splitManipulations = manipulation.stream().collect(Collectors.groupingBy(filter::test));

		List<AtomicManipulation> passedManipulations = splitManipulations.get(Boolean.TRUE);
		List<AtomicManipulation> skippedManipulations = splitManipulations.get(Boolean.FALSE);

		logSkippedManipulations(skippedManipulations);

		if (!isEmpty(passedManipulations))
			return delegate.append(asManipulation(passedManipulations), mode);
		else
			return EMPTY_APPEND_RESULT;
	}

	private void logSkippedManipulations(List<AtomicManipulation> skippedManipulations) {
		if (!isEmpty(skippedManipulations) && log.isTraceEnabled())
			log.trace("Skipped following Manipulation from persistence: " + asManipulation(skippedManipulations).stringify());
	}

	@Override
	public void append(Resource[] gmmlResources, EntityManager entityManager) {
		delegate.append(gmmlResources, entityManager);
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		return delegate.getPersistenceStage();
	}

}
