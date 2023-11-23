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
package com.braintribe.gm.config.yaml;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.config.ConfigurationEvaluationError;
import com.braintribe.gm.model.reason.config.UnresolvedProperty;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.ve.api.VirtualEnvironment;

public class ConfigVariableResolver {

	private static final String ENV_PREFIX = "env.";
	private Reason failure;
	private String dirProperty;
	private String fileProperty;
	private File file;
	private VirtualEnvironment virtualEnvironment;
	private Function<String, String> variableResolver = n -> null;

	public ConfigVariableResolver(VirtualEnvironment virtualEnvironment, File file) {
		super();
		this.virtualEnvironment = virtualEnvironment;
		this.file = file;
		Path filePath = file.toPath().toAbsolutePath().normalize();
		this.dirProperty = filePath.getParent().toString();
		this.fileProperty = filePath.toString();
	}

	@Configurable
	public void setVariableResolver(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
	}

	public Reason getFailure() {
		return failure;
	}

	public String resolve(Variable var) {
		return resolve(var.getName());
	}

	private String resolve(String var) {
		if (var.startsWith(ENV_PREFIX)) {
			String envName = var.substring(ENV_PREFIX.length());

			String value = virtualEnvironment.getEnv(envName);

			if (value == null) {
				acquireFailure().getReasons().add(Reasons.build(NotFound.T) //
						.text("Could not resolve property " + var) //
						.toReason());
				return "${" + var + "}";
			}

			// return var;
			return value;
		}
		
		switch (var) {
		case "config.base":
		case "config.dir":
			return dirProperty;
		case "config.file":
			return fileProperty;
		default:
			break;
		}

		String value = variableResolver.apply(var);

		if (value != null)
			return value;

		value = virtualEnvironment.getProperty(var);

		if (value == null) {
			acquireFailure().getReasons().add(UnresolvedProperty.create(var)); //
			return "${" + var + "}";
		}

		return value;
	}

	private Reason acquireFailure() {
		if (failure == null) {
			failure = Reasons.build(ConfigurationEvaluationError.T)
					.text("Configuration evaluation failed for " + file.getAbsolutePath()).toReason();
		}

		return failure;
	}
}