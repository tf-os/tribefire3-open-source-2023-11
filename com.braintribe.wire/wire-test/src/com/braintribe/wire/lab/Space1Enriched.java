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
package com.braintribe.wire.lab;

import java.util.function.Supplier;

import com.braintribe.wire.api.annotation.Enriched;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.InternalWireContext;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.InstanceParameterization;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.example.ExampleBean;
import com.braintribe.wire.impl.scope.singleton.SingletonScope;
import com.braintribe.wire.test.basic.space.Space1;

@Managed @Enriched
public class Space1Enriched extends Space1 {
	
	private InstanceHolderSupplier $exampleBean;
	private InternalWireContext $;
	private InternalWireContext context;
	
	public Space1Enriched(InternalWireContext context) {
		WireScope scope;
		scope = context.getScope(SingletonScope.class);
		$exampleBean = scope.createHolderSupplier(this, "exampleBean", InstanceParameterization.params);
		this.context = context;
	}
	
	public ExampleBean exampleBean(Supplier<String> ctx) {
		Object $$retVal = null;
		
		InstanceHolder beanHolder = $exampleBean.getHolder(ctx);
		
		if (!context.lockCreation(beanHolder))
			return (ExampleBean)beanHolder.get();
		
		try {
			ExampleBean bean = new ExampleBean();
			
			// insert
			beanHolder.publish(bean);
			// insert_end
			
			bean.setName(ctx.get());
			
			// insert
			// $woven.publish(bean);
			beanHolder.onPostConstruct(bean);
			$$retVal = bean;
			// insert_end
		}
		catch (Throwable t) {
			beanHolder.onCreationFailure(t);
		}
		finally {
			context.unlockCreation(beanHolder);
		}
		
		return (ExampleBean)$$retVal;
	}
	

}
