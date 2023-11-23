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
package tribefire.module.wire.contract;

import java.util.function.Supplier;

import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.processing.worker.api.Worker;

/**
 * Offers methods for binding {@link Worker}s.
 * 
 * @see HardwiredDeployablesContract
 */
public interface HardwiredWorkersContract extends HardwiredDeployablesContract {

	default HardwiredWorker bindWorker(String externalId, String name, Worker worker) {
		return bindWorker(externalId, name, () -> worker);
	}

	/** ExternalId convention: hardwired:worker/${description} (e.g. hardwired:worker/update-checker) */
	HardwiredWorker bindWorker(String externalId, String name, Supplier<Worker> workerSuplier);

}
