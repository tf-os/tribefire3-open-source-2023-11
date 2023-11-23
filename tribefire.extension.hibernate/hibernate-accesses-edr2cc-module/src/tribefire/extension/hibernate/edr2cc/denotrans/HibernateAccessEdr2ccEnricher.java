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
package tribefire.extension.hibernate.edr2cc.denotrans;

import static tribefire.module.api.PlatformBindIds.*;
import static tribefire.module.api.PlatformBindIds.TRANSIENT_MESSAGING_DATA_DB_BIND_ID;

import java.util.function.Function;

import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.MetaDataEditorBuilder;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;

import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;

/**
 * Configures mappings for known hibernate accesses.
 * <p>
 * This was created as a replacement for hardcoded Hibernate deployables with static hbm files embedded in web-platform.
 * 
 * @author peter.gazdik
 */
public class HibernateAccessEdr2ccEnricher {

	/* package */ final DenotationTransformationContext context;
	private final HibernateAccess access;
	private final GmMetaModel model;
	private final Function<GmMetaModel, MetaDataEditorBuilder> mdEditorFactory;

	/* package */ ModelMetaDataEditor mdEditor;

	public HibernateAccessEdr2ccEnricher(//
			DenotationTransformationContext context, HibernateAccess access, Function<GmMetaModel, MetaDataEditorBuilder> mdEditorFactory) {

		this.context = context;
		this.access = access;
		this.model = access.getMetaModel();
		this.mdEditorFactory = mdEditorFactory;
	}

	public DenotationEnrichmentResult<HibernateAccess> run() {
		switch (context.denotationId()) {
			case AUTH_DB_BIND_ID:
			case USER_SESSIONS_DB_BIND_ID:
			case USER_STATISTICS_DB_BIND_ID:
			case TRANSIENT_MESSAGING_DATA_DB_BIND_ID:
				if (model == null)
					return DenotationEnrichmentResult.nothingYetButCallMeAgain();
				break;

			default:
				return DenotationEnrichmentResult.nothingNowOrEver();
		}

		//unmapGlobalIdAndPartition();
		initMdEditor();

		switch (context.denotationId()) {
			case AUTH_DB_BIND_ID:
				return auth();
			case USER_SESSIONS_DB_BIND_ID:
				return userSessions();
			case USER_STATISTICS_DB_BIND_ID:
				return userStatistics();
			case TRANSIENT_MESSAGING_DATA_DB_BIND_ID:
				return transientMessaging();
			default:
				throw new IllegalStateException("Error in implementation. This code should be unreachable.");
		}
	}

	private void initMdEditor() {
		mdEditor = mdEditorFactory.apply(model).done();
	}

	private DenotationEnrichmentResult<HibernateAccess> auth() {
		HbmConfigurer_Auth.run(this);
		return allDone("Configured hbm mappings.");
	}

	private DenotationEnrichmentResult<HibernateAccess> userSessions() {
		HbmConfigurer_UserSessions.run(this);
		return allDone("Configured hbm mappings.");
	}

	private DenotationEnrichmentResult<HibernateAccess> userStatistics() {
		HbmConfigurer_UserStatistics.run(this);
		return allDone("Configured hbm mappings.");
	}

	private DenotationEnrichmentResult<HibernateAccess> transientMessaging() {
		return allDone("Just globalId and partition.");
	}

	// ################################################
	// ## . . . . . . . . . Shared . . . . . . . . . ##
	// ################################################

	private DenotationEnrichmentResult<HibernateAccess> allDone(String changeDescription) {
		return DenotationEnrichmentResult.allDone(access, changeDescription);
	}

	/* package */ MetaData unmappedEntity() {
		return context.getEntityByGlobalId("hbm:unmapped-entity");
	}

	/* package */ MetaData mappedEntity() {
		return context.getEntityByGlobalId("hbm:mapped-entity");
	}

	/* package */ MetaData unmappedProperty() {
		return context.getEntityByGlobalId("hbm:unmapped-property");
	}

	/* package */ MetaData mappedProperty() {
		return context.getEntityByGlobalId("hbm:mapped-property");
	}
	
}
