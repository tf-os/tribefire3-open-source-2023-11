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
package tribefire.extension.messaging.templates.wire.contract;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.messaging.templates.api.MessagingTemplateContext;

/**
 * Contract to access basic managed instances - e.g. from CoreInstancesContract
 * 
 *
 */
public interface BasicInstancesContract extends WireSpace {

	IncrementalAccess workbenchAccess(MessagingTemplateContext context);

	GmMetaModel essentialMetaDataModel(MessagingTemplateContext context);

	GmMetaModel workbenchModel(MessagingTemplateContext context);

}
