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
package com.braintribe.wire.test.concurrency.wire.space;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.example.Holder;
import com.braintribe.wire.test.concurrency.bean.ConcurrentlyAccessed;
import com.braintribe.wire.test.concurrency.bean.CycleBean;
import com.braintribe.wire.test.concurrency.wire.contract.ConcurrencyTestMainContract;

@Managed
public class ConcurrencyTestMainSpace implements ConcurrencyTestMainContract {

	@Override
	@Managed
	public ConcurrentlyAccessed concurrentlyAccessed() {
		ConcurrentlyAccessed bean = new ConcurrentlyAccessed();
		String value = holder().get();
		bean.setValue(value);
		return bean;
	}
	
	@Managed
	public Holder<String> holder() {
		Holder<String> bean = new Holder<>("foo");
		bean.accept("done");
		return bean;
	}

	@Override
	@Managed
	public CycleBean cycleBean() {
		CycleBean bean = new CycleBean();
		bean.setOther(backlinkBean());
		return bean;
	}
	
	@Managed
	private CycleBean backlinkBean() {
		CycleBean bean = new CycleBean();
		bean.setOther(cycleBean());
		return bean;
	}
}
