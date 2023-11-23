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
package tribefire.extension.antivirus.connector.api;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.antivirus.model.service.result.AbstractAntivirusResult;

public abstract class AbstractAntivirusConnector<T extends AbstractAntivirusResult> implements AntivirusConnector<T> {

	private static final Logger logger = Logger.getLogger(AbstractAntivirusConnector.class);

	protected Resource resource;
	private String providerType;

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public void setProviderType(String providerType) {
		this.providerType = providerType;
	}

	@Override
	public T scan() {
		long start = System.currentTimeMillis();
		T result = scanViaExpert();
		long end = System.currentTimeMillis();

		long duration = (end - start);
		result.setDurationInMs(duration);

		String detailedInformation = resourceInformation(resource);

		logger.debug(() -> "Executing virus scan of: '" + detailedInformation + "' using: '" + providerType + "' in: '" + duration + "'ms");

		return result;
	}

	abstract protected T scanViaExpert();

	// Health check

	@Override
	public CheckResultEntry actualHealth() {
		long start = System.currentTimeMillis();
		CheckResultEntry result = healthCheck();
		long end = System.currentTimeMillis();

		long duration = (end - start);

		String detailedInformation = resourceInformation(resource);
		result.setDetails(detailedInformation);

		return result;
	}

	abstract protected CheckResultEntry healthCheck();

	// ------------------

	protected static String resourceName(Resource resource) {
		String name = resource.getName();
		if (CommonTools.isEmpty(name)) {
			name = "__notSet__";
		}
		return name;
	}

	public static String resourceInformation(Resource resource) {
		StringBuilder sb = new StringBuilder();
		Object id = resource.getId();
		if (id == null) {
			sb.append("ID: '__notSet__'");
		} else {
			sb.append("ID: '");
			sb.append(id.toString());
			sb.append("'");
		}
		sb.append(",");
		String fileName = resourceName(resource);
		sb.append(fileName);
		return sb.toString();
	}

	protected <R extends AbstractAntivirusResult> R createResult(EntityType<R> entityType, boolean infected, String message) {
		R result = entityType.create();
		String fileName = resourceName(resource);
		result.setResourceId(resource.getId());
		result.setResourceName(fileName);
		result.setInfected(infected);
		result.setMessage(message);

		return result;
	}

	public static <E extends AbstractAntivirusConnector<? extends AbstractAntivirusResult>> E createExpert(Supplier<E> factory,
			Consumer<E> configurer) {
		E expert = factory.get();
		configurer.accept(expert);
		return expert;
	}
}
