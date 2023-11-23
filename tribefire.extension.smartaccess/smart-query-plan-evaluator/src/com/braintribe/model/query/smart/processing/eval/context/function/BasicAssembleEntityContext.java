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
package com.braintribe.model.query.smart.processing.eval.context.function;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.smartquery.eval.api.AssembleEntityContext;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.functions.PropertyMapping;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;

/**
 * 
 */
public class BasicAssembleEntityContext implements AssembleEntityContext {

	private final Map<String, PropertyMappingNode> signatureToPropertyMappingNode;
	private final Map<String, PropertyMapping> pNameToMapping = newMap();

	public BasicAssembleEntityContext(AssembleEntity aeFunction) {
		this.signatureToPropertyMappingNode = aeFunction.getSignatureToPropertyMappingNode();
	}

	@Override
	public PropertyMappingNode getPropertyMappingNode(String entitySignature) {
		return signatureToPropertyMappingNode.get(entitySignature);
	}

	@Override
	public PropertyMapping getPropertyMapping(String propertyName) {
		return pNameToMapping.get(propertyName);
	}

}
