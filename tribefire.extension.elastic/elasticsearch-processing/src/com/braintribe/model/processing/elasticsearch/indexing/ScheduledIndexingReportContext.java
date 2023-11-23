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
package com.braintribe.model.processing.elasticsearch.indexing;

import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexing;
import com.braintribe.model.elasticsearchdeployment.reindex.ReIndexingStatus;
import com.braintribe.model.processing.elasticsearch.util.ReportContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.ThrowableTools;
import com.braintribe.utils.lcd.StringTools;

public class ScheduledIndexingReportContext extends ReportContext {

	private String accessId;
	private Long duration;
	private Integer indexedEntities = 0;

	public ScheduledIndexingReportContext(PersistenceGmSession session) {
		super(session);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.getProperty("line.separator");

		sb.append("Id of the access the data has been submitted to be indexed: ");
		sb.append(this.accessId);
		sb.append(lineSeparator);

		if (exception != null) {

			sb.append(lineSeparator);
			sb.append("An exception was thrown during the indexing process.");
			sb.append(lineSeparator);
			sb.append("---- Start of Exception Trace ----");
			sb.append(lineSeparator);
			sb.append(ThrowableTools.getStackTraceString(exception)); // TODO
			sb.append(lineSeparator);
			sb.append("---- End of Exception Trace ----");

		} else {

			if (this.message != null) {
				sb.append(lineSeparator);
				sb.append(lineSeparator);
				sb.append("---- Report Summary ----");
				sb.append(lineSeparator);
				sb.append(this.message);

				sb.append(lineSeparator);
			}

			sb.append(lineSeparator);
			sb.append(lineSeparator);
			sb.append("The full-indexing process took: ");

			sb.append((this.duration == null) ? "<not set>" : (StringTools.prettyPrintMilliseconds(this.duration, true)));
			sb.append(lineSeparator);

			sb.append("Number of entities indexed: ");
			sb.append((this.indexedEntities == null) ? "<not set>" : this.indexedEntities);
			sb.append(lineSeparator);

		}

		if (this.detailedMessage != null) {
			sb.append(lineSeparator);
			sb.append(lineSeparator);
			sb.append("---- Start of detailed Report----");
			sb.append(lineSeparator);
			sb.append(this.detailedMessage);
			sb.append(lineSeparator);
			sb.append("---- End of detailed Report ----");
			sb.append(lineSeparator);
		}

		return sb.toString();
	}

	public void updateReIndexing(ReIndexing reIndexing, ReIndexingStatus status) {

		Resource report = this.createReport();

		reIndexing.setReIndexingStatus(status);
		reIndexing.setReport(report);
		if (this.message != null) {
			reIndexing.setMessage(this.message);
		}
		if (this.indexedEntities != null) {
			reIndexing.setIndexableEntities(this.indexedEntities);
		}

		if (this.duration != null) {
			reIndexing.setDuration(this.duration);
		}

	}

	/**
	 * Duration of the migration process in milliseconds
	 */
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	/**
	 * Number of indexed entities
	 */
	public Integer getIndexedEntities() {
		return indexedEntities;
	}
	public void setIndexedEntities(Integer indexedEntities) {
		this.indexedEntities = indexedEntities;
	}

	public void incrementIndexedEntitiesCount(Integer count) {
		this.indexedEntities += count;
	}

	/**
	 * The id of the access the data is migrated from
	 */
	public String getAccessId() {
		return accessId;
	}
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

}
