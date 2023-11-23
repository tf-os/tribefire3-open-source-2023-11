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
package com.braintribe.model.processing.smart.query.planner.tools;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.processing.smart.query.planner.splitter.DisambiguatedQuery;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.query.From;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;
import com.braintribe.model.smartqueryplan.set.DelegateQueryAsIs;

/**
 * @author peter.gazdik
 */
public class DelegateAsIsTools {

	public static SmartQueryPlan buildDelegateAsIsPlan(DisambiguatedQuery dq) {
		IncrementalAccess access = first(dq.fromMapping.values());

		SelectQuery query = GmReflectionTools.makeDeepCopy(dq.originalQuery);

		DelegateQueryAsIs delegateAsIs = DelegateQueryAsIs.T.create();
		delegateAsIs.setDelegateQuery(query);
		delegateAsIs.setDelegateAccess(access);

		SmartQueryPlan result = SmartQueryPlan.T.create();
		result.setTupleSet(delegateAsIs);
		result.setTotalComponentCount(query.getSelections().size());

		return result;
	}

	public static boolean canDelegateAsIs(DisambiguatedQuery dq, ModelExpert modelExpert) {
		return new CanDelegateResolver(dq, modelExpert).resolve();
	}

	private static class CanDelegateResolver {

		private final DisambiguatedQuery dq;
		private final ModelExpert modelExpert;

		private IncrementalAccess access;

		public CanDelegateResolver(DisambiguatedQuery dq, ModelExpert modelExpert) {
			this.dq = dq;
			this.modelExpert = modelExpert;
		}

		public boolean resolve() {
			return determineSingleDelegateAccess() && //
					eachFromIsFullyDelegatable();
		}

		private boolean determineSingleDelegateAccess() {
			Set<IncrementalAccess> accesses = newSet(dq.fromMapping.values());

			if (accesses.size() > 1)
				return false;

			access = first(accesses);
			return true;
		}

		private boolean eachFromIsFullyDelegatable() {
			for (From from : dq.fromMapping.keySet())
				if (!isFullyDelegatable(from))
					return false;

			return true;
		}

		private boolean isFullyDelegatable(From from) {
			return modelExpert.isMappedAsIs(from.getEntityTypeSignature(), access);
		}

	}

}
