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
package com.braintribe.wire.impl.scope.referee;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.InstanceParameterization;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.scope.AbstractWireScope;
import com.braintribe.wire.impl.scope.singleton.SingleThreadedSingletonBeanHolder;
import com.braintribe.wire.impl.scope.singleton.SingleThreadedSingletonScope;

public class AggregateScope extends AbstractWireScope {
	private static Logger logger = Logger.getLogger(AggregateScope.class.getName());
	
	public AggregateSubScope acquireScope() {
		InstanceHolder lastHolder = context.currentInstancePath().firstElement();
		
		// is the referee on the path already in referee scope or is a new to be announced 
		if (lastHolder != null && lastHolder.scope() == this)
			return ((AggregateBeanHolder)lastHolder).aggregateSubScope();
		
		return startScope(lastHolder);
	}
	
	@Override
	public void close() throws Exception {
		// noop as closing is done in the sub scopes
	}

	private AggregateSubScope startScope(InstanceHolder instanceHolder) {
		AggregateSubScope subScope = new AggregateSubScope();
		subScope.attachContext(getContext());
		
		if (instanceHolder != null) {
			instanceHolder.config().closeOnDestroy(subScope);
		}
		
		return subScope;
	}
	
	@Override
	public InstanceHolderSupplier createHolderSupplier(WireSpace managedSpace, String name,
			InstanceParameterization parameterization) {
		return new RefereeInstanceHolderSupplier(managedSpace, this, name);
	}
	
	public static class AggregateHolderKey {
		Object context;
		InstanceHolderSupplier holderSupplier;
		
		public AggregateHolderKey(InstanceHolderSupplier holderSupplier, Object context) {
			super();
			this.holderSupplier = holderSupplier;
			this.context = context;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((holderSupplier == null) ? 0 : holderSupplier.hashCode());
			result = prime * result + ((context == null) ? 0 : context.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			AggregateHolderKey other = (AggregateHolderKey) obj;
			
			if (holderSupplier == null) {
				if (other.holderSupplier != null)
					return false;
			} else if (holderSupplier != other.holderSupplier)
				return false;
			
			if (context == null) {
				if (other.context != null)
					return false;
			} else if (!context.equals(other.context))
				return false;
			
			return true;
		}
	}
	
	public class AggregateSubScope extends SingleThreadedSingletonScope {
		protected Map<AggregateHolderKey, InstanceHolder> holdersBySupplier = new HashMap<>();
		
		public InstanceHolder acquire(RefereeInstanceHolderSupplier holder, Object context) {
			AggregateHolderKey key = new AggregateHolderKey(holder, context);
			return holdersBySupplier.computeIfAbsent(key, k -> new AggregateBeanHolder(holder, AggregateScope.this, this));
		}
	}
	
	private static class AggregateBeanHolder extends SingleThreadedSingletonBeanHolder {

		private WireScope originalScope;
		private AggregateSubScope refereeSubScope;

		public AggregateBeanHolder(RefereeInstanceHolderSupplier holderSupplier, WireScope originalScope, AggregateSubScope scope) {
			super(holderSupplier.getSpace(), scope, holderSupplier.getName());
			this.originalScope = originalScope;
			this.refereeSubScope = scope;
		}
		
		public AggregateSubScope aggregateSubScope() {
			return refereeSubScope;
		}
		
		@Override
		public WireScope scope() {
			return originalScope;
		}
	}

}
