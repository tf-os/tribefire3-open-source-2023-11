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
package tribefire.extension.metrics.service.aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.metrics.connector.api.MetricsConnector;

/**
 * Abstract base class for all metrics aspects.
 * 
 *
 */
// TODO: enable/disable without redeployment
// TODO: enable/disable by type signature / maybe profiles
public abstract class MetricsAspect implements ServiceAroundProcessor<ServiceRequest, Object> {

	protected Set<MetricsConnector> metricsConnectors;

	protected String name;
	protected String description;
	protected String[] tagsSuccess;
	protected String[] tagsError;

	private boolean addDomainIdTag;
	private boolean addPartitionTag;
	private boolean addTypeSignatureTag;
	private boolean addRequiresAuthenticationTag;

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	protected String[] enrichTags(ServiceRequest request, String[] tags) {
		// TODO: maybe enriching the builder with a tag instead of complicated adding multiple tags

		List<String> clone = new ArrayList<>(Arrays.asList(tags));

		if (addDomainIdTag) {
			String domainId = request.domainId();
			clone.add("domain.id");
			if (CommonTools.isEmpty(domainId)) {
				clone.add("no-domain");
			} else {
				clone.add(domainId);
			}
		}
		if (addPartitionTag) {
			String partition = request.getPartition();
			clone.add("partition");
			if (CommonTools.isEmpty(partition)) {
				clone.add("no-partition");
			} else {
				clone.add(partition);
			}
		}
		if (addTypeSignatureTag) {
			String typeSignature = request.entityType().getTypeSignature();
			clone.add("typeSignature");
			clone.add(typeSignature);
		}
		if (addRequiresAuthenticationTag) {
			boolean requiresAuthentication = request.requiresAuthentication();
			clone.add("requiresAuthentication");
			clone.add(Boolean.toString(requiresAuthentication));
		}

		return clone.toArray(new String[clone.size()]);
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setMetricsConnectors(Set<MetricsConnector> metricsConnectors) {
		this.metricsConnectors = metricsConnectors;
	}

	@Configurable
	@Required
	public void setName(String name) {
		this.name = name;
	}

	@Configurable
	public void setDescription(String description) {
		this.description = description;
	}

	@Configurable
	@Required
	public void setTagsSuccess(String[] tagsSuccess) {
		this.tagsSuccess = tagsSuccess;
	}

	@Configurable
	@Required
	public void setTagsError(String[] tagsError) {
		this.tagsError = tagsError;
	}

	@Configurable
	@Required
	public void setAddDomainIdTag(boolean addDomainIdTag) {
		this.addDomainIdTag = addDomainIdTag;
	}

	@Configurable
	@Required
	public void setAddPartitionTag(boolean addPartitionTag) {
		this.addPartitionTag = addPartitionTag;
	}

	@Configurable
	@Required
	public void setAddTypeSignatureTag(boolean addTypeSignatureTag) {
		this.addTypeSignatureTag = addTypeSignatureTag;
	}

	@Configurable
	@Required
	public void setAddRequiresAuthenticationTag(boolean addRequiresAuthenticationTag) {
		this.addRequiresAuthenticationTag = addRequiresAuthenticationTag;
	}

}
