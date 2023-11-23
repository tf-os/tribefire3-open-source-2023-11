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
package com.braintribe.model.processing.lock.impl;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.lock.api.LockService;
import com.braintribe.model.processing.lock.api.LockServiceException;
import com.braintribe.model.processing.lock.api.model.TryLock;
import com.braintribe.model.processing.lock.api.model.Unlock;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;

public class EvaluatingLockService implements LockService {
	
	private final Evaluator<ServiceRequest> evaluator;
	
	public EvaluatingLockService(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public boolean tryLock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive,
			TimeSpan timeout) throws LockServiceException, InterruptedException {
		

		TryLock request = TryLock.T.create();
		request.setCallerSignature(callerSignature);
		request.setExclusive(exclusive);
		request.setHolderId(holderId);
		request.setIdentfication(identification);
		request.setMachineSignature(machineSignature);
		request.setTimeout(timeout);
		
		return request.eval(evaluator).get();
	}

	@Override
	public boolean tryLock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive,
			TimeSpan timeout, TimeSpan lockTtl) throws LockServiceException, InterruptedException {

		TryLock request = TryLock.T.create();
		request.setCallerSignature(callerSignature);
		request.setExclusive(exclusive);
		request.setHolderId(holderId);
		request.setIdentfication(identification);
		request.setLockTtl(lockTtl);
		request.setMachineSignature(machineSignature);
		request.setTimeout(timeout);
		
		return request.eval(evaluator).get();
	}

	@Override
	public void unlock(String identification, String holderId, String callerSignature, String machineSignature, boolean exclusive)
			throws LockServiceException {

		Unlock request = Unlock.T.create();
		request.setCallerSignature(callerSignature);
		request.setExclusive(exclusive);
		request.setHolderId(holderId);
		request.setIdentfication(identification);
		request.setMachineSignature(machineSignature);
		
		request.eval(evaluator).get();
	}

}
