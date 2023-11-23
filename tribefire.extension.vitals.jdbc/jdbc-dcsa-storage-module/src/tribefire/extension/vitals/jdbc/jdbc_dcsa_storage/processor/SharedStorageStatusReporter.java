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
package tribefire.extension.vitals.jdbc.jdbc_dcsa_storage.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.ResourceBuilder;

import tribefire.extension.jdbc.gmdb.dcsa.TemporaryJdbc2GmDbSharedStorage;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageState;
import tribefire.extension.vitals.jdbc.model.migration.SharedStorageStatus;

/**
 * @author peter.gazdik
 */
public class SharedStorageStatusReporter {

	private final String implementation;
	private final ResourceBuilder resourceBuilder;

	private Jdbc2GmDbUpgrader upgrader;

	private Date date = new Date();
	private SharedStorageStatus status = SharedStorageStatus.NORMAL;
	private Resource statusDetail;

	public SharedStorageStatusReporter(TemporaryJdbc2GmDbSharedStorage sharedStorage, ResourceBuilder resourceBuilder) {
		this.resourceBuilder = resourceBuilder;
		this.implementation = sharedStorage.isGmDbImplementation() ? "GmDb (New)" : "JDBC (Old)";
	}

	public void onStartUpgrade(Jdbc2GmDbUpgrader upgrader) {
		this.upgrader = upgrader;
		this.date = new Date();
		this.status = SharedStorageStatus.UPGRADING;
	}

	public void onStartDowngrade() {
		this.upgrader = null;
		this.date = new Date();
		this.status = SharedStorageStatus.DOWNGRADED;
	}

	public void onActionFinished(SharedStorageStatus status, Exception e) {
		this.status = status;
		this.statusDetail = e == null ? null : printStackTrace(e);
	}

	private Resource printStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		return errorResource(sw.toString());
	}

	public Resource errorResource(String errorText) {
		return resourceBuilder.newResource() //
				.withName("DCSA migration error.txt") //
				.withMimeType("plain/text") //
				.string(errorText);
	}

	public SharedStorageState getState() {
		SharedStorageState state = SharedStorageState.T.create();
		state.setDate(date);
		state.setImplementation(implementation);
		state.setStatus(status);
		state.setDetails(statusDetail);

		if (upgrader != null)
			state.setMigratedAccesses(upgrader.migratedAccesses());

		return state;
	}

}
