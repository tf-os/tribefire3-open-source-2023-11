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
package tribefire.platform.wire.space.cortex;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.csa.PlugableDcsaSharedStorage;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.utils.ReflectionTools;

import tribefire.module.api.EnvironmentDenotations;
import tribefire.platform.wire.space.bindings.EnvironmentDenotationConstants;

/**
 * Reads {@link PlugableDcsaSharedStorage} from {@link EnvironmentDenotations} and converts it into Deployable {@link DcsaSharedStorage}.
 * 
 * This is used as the last fallback in case {@link PreCortexSpace#FILENAME_DEFAULT_DCSA_SS} file doesn't exist.
 * 
 * @author peter.gazdik
 */
public class LegacyDcsaDenotationSupplier implements Supplier<DcsaSharedStorage> {

	private static final Logger log = Logger.getLogger(LegacyDcsaDenotationSupplier.class);

	private EnvironmentDenotations environmentDenotations;

	@Required
	public void setEnvironmentDenotations(EnvironmentDenotations environmentDenotations) {
		this.environmentDenotations = environmentDenotations;
	}

	@Override
	public DcsaSharedStorage get() {
		PlugableDcsaSharedStorage pluggable = pluggableDcsaSharedStorage();

		return pluggable == null ? null : convertLegacyDssConfigurationIfPossible(pluggable);
	}

	private PlugableDcsaSharedStorage pluggableDcsaSharedStorage() {
		return environmentDenotations.lookup(EnvironmentDenotationConstants.environmentDscaSharedStorage);
	}

	// See comment inside this method
	private DcsaSharedStorage convertLegacyDssConfigurationIfPossible(PlugableDcsaSharedStorage plugable) {
		/* This class exists in tribefire.extension.vitals.jdbc:jdbc-dcsa-storage-plugin-to-module-adjuster */
		/* That artifact is a platform library, so for legacy projects we add it to the setup and it ends up on the main classpath. Then we find the
		 * class here and we use it to convert the plugable DSS to deployable DSS which can be deployed by preCortex. */
		final String legacyConfigConverterClassName = "tribefire.extension.vitals.jdbc.legacy.LegacyDssConverter";

		Class<?> clazz = ReflectionTools.getClass(legacyConfigConverterClassName);
		if (clazz == null)
			return null;

		try {
			Function<PlugableDcsaSharedStorage, com.braintribe.model.dcsadeployment.DcsaSharedStorage> dssConverter = //
					(Function<PlugableDcsaSharedStorage, com.braintribe.model.dcsadeployment.DcsaSharedStorage>) clazz.getConstructor().newInstance();

			return dssConverter.apply(plugable);

		} catch (Exception e) {
			log.error("Error while attempting to convert legacy DcsaSharedStorage configuration to the up-to-date one. ", e);
			return null;
		}
	}

}
