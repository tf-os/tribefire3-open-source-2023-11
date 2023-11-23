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
package com.braintribe.model.access.smood.distributed.test.concurrent.worker;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.smood.distributed.test.wire.contract.DistributedSmoodAccessTestContract;


public class WorkerFactory {

	protected int iterations = -1;
	protected DistributedSmoodAccessTestContract space = null;

	public Worker provideWorker() {
		Worker worker = new Worker(this.iterations, this.space);
		return worker;
	}

	@Configurable
	@Required
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	public int getIterations() {
		return iterations;
	}

	public DistributedSmoodAccessTestContract getSpace() {
		return space;
	}
	@Configurable
	@Required
	public void setSpace(DistributedSmoodAccessTestContract space) {
		this.space = space;
	}

}
