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
package tribefire.platform.wire.space.cortex.accesses;

import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_AUTH;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_AUTH_WB;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_PLATFORM_SETUP;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_PLATFORM_SETUP_WB;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_TRANSIENT_MESSAGING_DATA;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_TRANSIENT_MESSAGING_DATA_WB;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_SESSIONS_WB;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_STATISTICS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_STATISTICS_WB;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_SERVICE_MODEL_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.TF_WB_MODEL;
import static com.braintribe.wire.api.util.Sets.linkedSet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.gm._BasicResourceModel_;
import com.braintribe.gm._PersistenceUserSessionModel_;
import com.braintribe.gm._SecurityServiceApiModel_;
import com.braintribe.gm._UserModel_;
import com.braintribe.gm._UserSessionModel_;
import com.braintribe.gm._UserSessionServiceModel_;
import com.braintribe.gm._UserStatisticsModel_;

import tribefire.cortex._PlatformSetupModel_;
import tribefire.cortex._PlatformSetupWorkbenchModel_;
import tribefire.cortex._UserWorkbenchModel_;
import tribefire.cortex._WorkbenchModel_;

public class TribefireProductModels {

	// ######################################
	// ## . . . . . . . Auth . . . . . . . ##
	// ######################################

	public static final TribefireProductModel authAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_AUTH, //
			_UserModel_.reflection, //
			_BasicResourceModel_.reflection);

	public static final TribefireProductModel authWorkbenchtAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_AUTH_WB, //
			_UserWorkbenchModel_.reflection, //
			_BasicResourceModel_.reflection);

	// ######################################
	// ## . . . . . User Sessions . . . . .##
	// ######################################

	public static final TribefireProductModel userSessionsAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_USER_SESSIONS, //
			_PersistenceUserSessionModel_.reflection);

	public static final TribefireProductModel userSessionsAccessServiceModel = new TribefireProductModel(//
			ACCESS_SERVICE_MODEL_USER_SESSIONS, //
			_UserSessionServiceModel_.reflection, //
			_SecurityServiceApiModel_.reflection);

	public static final TribefireProductModel userSessionsWorkbenchAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_USER_SESSIONS_WB, //
			_PersistenceUserSessionModel_.reflection, //
			_UserSessionModel_.reflection, //
			_UserSessionServiceModel_.reflection, //
			_SecurityServiceApiModel_.reflection, //
			_WorkbenchModel_.reflection);

	// ######################################
	// ## . . . . . User Statistics . . . .##
	// ######################################

	public static final TribefireProductModel userStatisticsAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_USER_STATISTICS, //
			_UserStatisticsModel_.reflection);

	public static final TribefireProductModel userStatisticsWorkbrenchAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_USER_STATISTICS_WB, //
			_UserStatisticsModel_.reflection, //
			_WorkbenchModel_.reflection);

	// ######################################
	// ## . . . . Platform Setup . . . . . ##
	// ######################################
	public static final TribefireProductModel platformSetupAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_PLATFORM_SETUP, //
			_PlatformSetupModel_.reflection);
	public static final TribefireProductModel platformSetupWorkbenchAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_PLATFORM_SETUP_WB, //
			_PlatformSetupWorkbenchModel_.reflection);

	// ######################################
	// ## . . . Transient Messaging . . . .##
	// ######################################

	public static final TribefireProductModel transientMessagingDataAccessModel = new TribefireProductModel(//
			ACCESS_MODEL_TRANSIENT_MESSAGING_DATA, //
			_BasicResourceModel_.reflection);
	public static final TribefireProductModel transientMessagingDataWorkbenchAccessModel = new TribefireProductModel(
			ACCESS_MODEL_TRANSIENT_MESSAGING_DATA_WB, //
			_BasicResourceModel_.reflection, //
			_WorkbenchModel_.reflection);

	public static final TribefireProductModel workbenchAccessModel = new TribefireProductModel( //
			TF_WB_MODEL, //
			_WorkbenchModel_.reflection);

	// public static final TribefireProductModel auditAccessModel = new TribefireProductModel("tribefire.cortex:audit-model");
	// public static final TribefireProductModel auditWorkbenchAccessModel = new TribefireProductModel("tribefire.cortex:audit-workbench-model");
	// public static final TribefireProductModel authAccessModel = new TribefireProductModel("com.braintribe.gm:user-model");
	// public static final TribefireProductModel authWorkbenchtAccessModel = new TribefireProductModel("tribefire.cortex:user-workbench-model");
	// public static final TribefireProductModel userSessionsAccessModel = new TribefireProductModel("com.braintribe.gm:user-session-model");
	// public static final TribefireProductModel userStatisticsAccessModel = new TribefireProductModel("com.braintribe.gm:user-statistics-model");
	// public static final TribefireProductModel workbenchAccessModel = new TribefireProductModel("tribefire.cortex:workbench-model");

	public static final Set<TribefireProductModel> productModels = linkedSet( //
			authAccessModel, //
			authWorkbenchtAccessModel, //
			userSessionsAccessModel, //
			userSessionsAccessServiceModel, //
			userSessionsWorkbenchAccessModel, //
			userStatisticsAccessModel, //
			userStatisticsWorkbrenchAccessModel, //
			workbenchAccessModel, //
			platformSetupAccessModel, //
			platformSetupWorkbenchAccessModel, //
			transientMessagingDataAccessModel, //
			transientMessagingDataWorkbenchAccessModel //
	);

	public static class TribefireProductModel {
		public String modelName;
		public Set<String> dependencies = Collections.emptySet();

		public TribefireProductModel(String modelName, ArtifactReflection... dependencies) {
			this.modelName = modelName;
			if (dependencies != null) {
				this.dependencies = Stream.of(dependencies).map(ArtifactReflection::name).collect(Collectors.toCollection(LinkedHashSet::new));
			}
		}

		public TribefireProductModel(String modelName) {
			this(modelName, (ArtifactReflection[]) null);
		}

	}
}
