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
package tribefire.extension.messaging.integration.test.util;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class TestQueryUtil {
	public static GmMetaModel queryMetaModel(PersistenceGmSession cortexSession, String globalId) {
		//@formatter:off
        return cortexSession.query()
                       .abstractQuery(EntityQueryBuilder.from(GmMetaModel.T)
                                              .where()
                                              .property(GmMetaModel.globalId).eq(globalId)
                                              .tc().pattern()
                                              .typeCondition(TypeConditions.isAssignableTo(MetaData.T)).conjunction().property()
                                              .typeCondition(TypeConditions.not(TypeConditions.isKind(TypeKind.scalarType)))
                                              .close()
                                              .close()
                                              .done())
                       .first();
        //@formatter:on
	}

	public static void queryAnDeleteDeployable(PersistenceGmSession cortexSession, String externalId) {
		queryAndDelete(cortexSession, Deployable.T, Deployable.externalId, externalId);
	}

	public static void queryAnDeleteAllProcessWith(PersistenceGmSession cortexSession, String prefix) {
		Arrays.asList(ProcessWith.T, PreProcessWith.T, AroundProcessWith.T)
				.forEach(e -> queryAndDelete(cortexSession, e, GenericEntity.globalId, prefix + "*"));
	}

	public static <T extends GenericEntity> void queryAndDelete(PersistenceGmSession cortexSession, EntityType<T> type, String idField, String like) {
		try {
			//@formatter:off
            List<T> list = cortexSession.query()
                                   .abstractQuery(EntityQueryBuilder.from(type)
                                                          .where()
                                                          .property(idField).like(like+"*")
                                                          .done())
                                   .list();
            //@formatter:on
			list.forEach(v -> cortexSession.deleteEntity(v, DeleteMode.dropReferences));
			cortexSession.commit();
		} catch (Exception e) {
			// ignore
		}
	}

	private TestQueryUtil() {
	}

	public static <T extends GenericEntity> T query(PersistenceGmSession cortexSession, EntityType<GmEntityType> type, String property, String like) {
		//@formatter:off
        return cortexSession.query()
                .abstractQuery(EntityQueryBuilder.from(type)
                                       .where()
                                       .property(property)
                                       .like(like)
                                       .done()).first();
        //@formatter:on
	}
}
