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
package com.braintribe.model.processing.accessory.impl;

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;

/**
 * Support implementation for {@link ModelAccessoryFactory#forPerspective(String)} functionality.
 * 
 * @author peter.gazdik
 */
public class MdPerspectiveRegistry {

	public static final String DOMAIN_ESSENTIAL = "ESSENTIAL";
	public static final String DOMAIN_BASIC = "BASIC";
	public static final String PERSPECTIVE_PERSISTENCE_SESSION = "PERSISTENCE_SESSION";

	public static Predicate<MetaData> testMdDeclaredInModel(Model model) {
		return md -> md.entityType().getModel() == model;
	}

	private final Map<String, Set<String>> perspectiveToDomains = newMap();
	private final Map<String, List<Predicate<MetaData>>> domainToMatchingMd = newMap();

	private volatile Map<String, List<Predicate<MetaData>>> perspectiveToMatchingMd;
	private ReentrantLock lock = new ReentrantLock();

	public void extendModelPerspective(String perspective, String... metaDataDomains) {
		lock.lock();
		try {
			acquireSet(perspectiveToDomains, perspective).addAll(Arrays.asList(metaDataDomains));
			perspectiveToMatchingMd = null;
		} finally {
			lock.unlock();
		}
	}

	public void extendMdDomain(String domain, Predicate<MetaData> test) {
		lock.lock();
		try {
			acquireList(domainToMatchingMd, domain).add(test);
			perspectiveToMatchingMd = null;
		} finally {
			lock.unlock();
		}
	}

	public boolean perspectiveContainsMd(String perspective, MetaData md) {
		List<Predicate<MetaData>> tests = perspectiveToMatchingMd().get(perspective);
		if (isEmpty(tests))
			return false;

		for (Predicate<MetaData> test : tests)
			if (test.test(md))
				return true;

		return false;
	}

	private Map<String, List<Predicate<MetaData>>> perspectiveToMatchingMd() {
		Map<String, List<Predicate<MetaData>>> result = perspectiveToMatchingMd;
		if (result == null)
			result = initPerspectiveToMatchingMd();

		return result;
	}

	private Map<String, List<Predicate<MetaData>>> initPerspectiveToMatchingMd() {
		lock.lock();
		try {
			if (perspectiveToMatchingMd != null)
				return perspectiveToMatchingMd;

			Map<String, List<Predicate<MetaData>>> result = newMap();

			for (Entry<String, Set<String>> e : perspectiveToDomains.entrySet()) {
				String perspective = e.getKey();
				for (String domain : e.getValue()) {
					List<Predicate<MetaData>> tests = domainToMatchingMd.get(domain);
					acquireList(result, perspective).addAll(tests);
				}
			}

			return perspectiveToMatchingMd = result;
		} finally {
			lock.unlock();
		}
	}

}
