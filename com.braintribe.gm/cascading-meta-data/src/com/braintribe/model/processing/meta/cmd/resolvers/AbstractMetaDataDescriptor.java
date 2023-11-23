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
package com.braintribe.model.processing.meta.cmd.resolvers;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.processing.meta.cmd.context.ExtendedSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.ResolutionContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.scope.CmdScope;
import com.braintribe.model.processing.meta.cmd.context.scope.ScopeUtils;
import com.braintribe.model.processing.meta.cmd.tools.CmdGwtUtils;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * 
 */
abstract class AbstractMetaDataDescriptor<T> {

	public ResolutionContext resolutionContext;
	public List<List<QualifiedMetaData>> ownerMetaData; // This never contains empty lists

	private volatile T staticValue;
	private volatile ScopedMdIndex sessionValueIndex;
	private final ReentrantLock sessionValueIndexLock = new ReentrantLock();
	private volatile CmdScope globalScope;
	private final ReentrantLock globalScopeLock = new ReentrantLock();

	public T provideValue(ExtendedSelectorContext selectorContext) {
		if (selectorContext.shouldIgnoreSelectors())
			return ignoreSelectorsValue(selectorContext);

		if (globalScope == null)
			initializeScope(selectorContext);

		switch (globalScope) {
			case STATIC:
				return staticValue;
			case SESSION:
				return sessionValue(selectorContext);
			case MOMENTARY:
			case VOLATILE:
				return volatileValue(selectorContext);
		}

		throw new UnsupportedEnumException("Unsupported scope: " + globalScope);
	}

	private T ignoreSelectorsValue(ExtendedSelectorContext selectorContext) {
		if (allSelectorsIgnored(selectorContext))
			return ignoreSelectorsValue();
		else
			return volatileValue(selectorContext);
	}

	private boolean allSelectorsIgnored(ExtendedSelectorContext selectorContext) {
		return selectorContext.notIgnoredSelectors().isEmpty();
	}

	private void initializeScope(SelectorContext selectorContext) {
		globalScopeLock.lock();
		try {
			if (globalScope != null) {
				return;
			}

			Set<Class<? extends SelectorContextAspect<?>>> aspects = getAspectsFor(ownerMetaData);
			CmdScope scope = scopeUtils().getCommonScope(aspects);

			if (scope == CmdScope.STATIC)
				staticValue = volatileValue(selectorContext);

			globalScope = scope;

		} finally {
			globalScopeLock.unlock();
		}
	}

	private Set<Class<? extends SelectorContextAspect<?>>> getAspectsFor(List<List<QualifiedMetaData>> qmds) {
		Set<Class<? extends SelectorContextAspect<?>>> result = newSet();

		for (List<QualifiedMetaData> nodeMetaData : qmds)
			for (QualifiedMetaData qmd : nodeMetaData)
				result.addAll(resolutionContext.getRelevantAspects(qmd.metaData().getSelector()));

		return result;
	}

	private T sessionValue(ExtendedSelectorContext selectorContext) {
		ensureSessionIndex();
		Object session = selectorContext.provideSession();
		return sessionValueIndex.acquireIndexedValue(session, selectorContext);
	}

	private void ensureSessionIndex() {
		if (sessionValueIndex == null)
			ensureSessionIndexSync();
	}

	private void ensureSessionIndexSync() {
		sessionValueIndexLock.lock();
		try {
			if (sessionValueIndex == null) {
				sessionValueIndex = new ScopedMdIndex();
			}
		} finally {
			sessionValueIndexLock.unlock();
		}
	}

	private ScopeUtils scopeUtils() {
		return resolutionContext.getScopeUtils();
	}

	private class ScopedMdIndex {
		protected Map<Object, T> index = newWeakCacheMap();

		public T acquireIndexedValue(Object scopedValue, SelectorContext selectorContext) {
			T value = index.get(scopedValue);
			if (value == null) {
				value = volatileValue(selectorContext);
				index.put(scopedValue, value);
			}

			return value;
		}

		private <K, V> Map<K, V> newWeakCacheMap() {
			return CmdGwtUtils.newWeakCacheMap(resolutionContext.getMaxSessionCacheSize());
		}

	}

	protected abstract T ignoreSelectorsValue();

	protected abstract T volatileValue(SelectorContext selectorContext);

}
