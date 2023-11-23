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
package tribefire.platform.wire.space.security.accesses;

import com.braintribe.common.MutuallyExclusiveReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.space.SchrodingerBeansSpace;
import tribefire.platform.wire.space.cortex.accesses.SchrodingerBeanSystemAccessSpaceBase;
import tribefire.platform.wire.space.cortex.accesses.TribefireProductModels;
import tribefire.platform.wire.space.cortex.accesses.TribefireProductModels.TribefireProductModel;

@Managed
public class UserStatisticsAccessSpace extends SchrodingerBeanSystemAccessSpaceBase {

	public static final String id = TribefireConstants.ACCESS_USER_STATISTICS;
	public static final String name = TribefireConstants.ACCESS_USER_STATISTICS_NAME;
	public static final TribefireProductModel model = TribefireProductModels.userStatisticsAccessModel;
	public static final String modelName = model.modelName;

	// @formatter:off
	@Override public String id() { return id; }
	@Override public String name() { return name; }
	@Override public String modelName() { return modelName; }
	// @formatter:on

	@Import
	private SchrodingerBeansSpace schrodingerBeans;

	@Managed
	@Override
	public IncrementalAccess access() {
		return accessSchrodingerBean().proxy();
	}

	@Managed
	public SchrodingerBean<IncrementalAccess> accessSchrodingerBean() {
		return schrodingerBeans.newBean("UserStatisticsAccess", CortexConfiguration::getUserStatisticsAccess, binders.incrementalAccess());
	}

	@Managed
	public SmoodAccess defaultAccess() {
		SmoodAccess bean = new SmoodAccess();
		bean.setAccessId(id());
		bean.setReadWriteLock(new MutuallyExclusiveReadWriteLock());
		bean.setDataDelegate(fileAccess());
		bean.setManipulationBuffer(manipulationStorage());
		bean.setBufferFlushThresholdInBytes(52428800L);
		return bean;
	}

	@Managed
	private XmlAccess fileAccess() {
		XmlAccess bean = new XmlAccess();
		bean.setFilePath(dataStorageFile());
		bean.setModelProvider(metaModelProvider());
		return bean;
	}

}
