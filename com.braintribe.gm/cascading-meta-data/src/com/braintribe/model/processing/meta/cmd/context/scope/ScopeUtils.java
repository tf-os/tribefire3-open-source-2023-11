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
package com.braintribe.model.processing.meta.cmd.context.scope;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

public class ScopeUtils {

	private static final Map<Class<? extends ScopedAspect<?>>, CmdScope> scopeMap;

	static {
		scopeMap = new HashMap<Class<? extends ScopedAspect<?>>, CmdScope>();

		for (CmdScope cs: CmdScope.values()) {
			scopeMap.put(cs.getScope(), cs);
		}
	}

	private boolean singleSession = false;

	/** @See {@link CmdResolverImpl#setSingleSession(boolean)} */
	public void setSingleSession(boolean singleSession) {
		this.singleSession = singleSession;
	}

	/**
	 * @return the most stable scope possible that does not violate the scopes of given aspects (i.e. if something is
	 *         static, we may still handle it as session (the result will always be valid), but not vice versa.)
	 */
	public CmdScope getCommonScope(Collection<Class<? extends SelectorContextAspect<?>>> aspects) {
		CmdScope result = CmdScope.STATIC;

		for (Class<? extends SelectorContextAspect<?>> aspect: aspects) {
			CmdScope cs = getScope(aspect);
			if (cs == CmdScope.VOLATILE) {
				return CmdScope.VOLATILE;
			}

			result = commonScope(result, cs);
		}

		return result;
	}

	private CmdScope commonScope(CmdScope st1, CmdScope st2) {
		return ScopeComparator.commonScope(st1, st2);
	}

	public boolean isStatic(Collection<Class<? extends SelectorContextAspect<?>>> aspects) {
		for (Class<? extends SelectorContextAspect<?>> aspect: aspects) {
			CmdScope cs = getScope(aspect);
			if (cs != CmdScope.STATIC) {
				return false;
			}
		}

		return true;
	}

	private static final CmdScope DEFAULT_SCOPE = CmdScope.VOLATILE;

	private CmdScope getScope(Class<? extends SelectorContextAspect<?>> s) {
		Class<?> sc = s.getSuperclass();

		if (sc != null) {
			CmdScope result = scopeMap.get(sc);
			if (result != null) {
				if (singleSession && result == CmdScope.SESSION) {
					return CmdScope.STATIC;
				}

				return result;
			}
		}

		return DEFAULT_SCOPE;
	}

}
