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
package com.braintribe.model.processing.smart.mapping.impl;

import java.util.function.Consumer;

import com.braintribe.model.accessdeployment.smart.meta.AsIs;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.SmartUnmapped;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.smart.mapping.api.SmartEntityTypeMappingEditor;

/**
 * @author peter.gazdik
 */
public class BasicSmartEntityTypeMappingEditor implements SmartEntityTypeMappingEditor {

	private static final String QEA = "qea";
	private static final String QPA = "qpa";

	private final BasicSmartMappingEditor mappingEditor;
	private final EntityTypeMetaDataEditor entityMdEditor;

	private String currentAccessId;
	private String currentGlobalIdPart;

	private EntityTypeOracle currentDelegateTypeOracle;

	public BasicSmartEntityTypeMappingEditor(BasicSmartMappingEditor mappingEditor, EntityTypeMetaDataEditor entityMdEditor) {
		this.mappingEditor = mappingEditor;
		this.entityMdEditor = entityMdEditor;
	}

	@Override
	public SmartEntityTypeMappingEditor forDelegate(String accessId) {
		this.currentAccessId = accessId;
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor withGlobalIdPart(String globalIdPart) {
		currentGlobalIdPart = globalIdPart;
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor addMetaData(Consumer<EntityTypeMetaDataEditor> mdConfigurer) {
		mdConfigurer.accept(entityMdEditor);
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor allAsIs() {
		return entityAsIs().propertiesAsIs();
	}

	@Override
	public SmartEntityTypeMappingEditor entityAsIs() {
		currentDelegateTypeOracle = findEntityTypeOracle(entityMdEditor.getEntityType().getTypeSignature());

		AsIs asIs = mappingEditor.acquireAsIs(currentAccessId);
		entityMdEditor.addMetaData(asIs);
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor propertiesAsIs() {
		AsIs asIs = mappingEditor.acquireAsIs(currentAccessId);
		entityMdEditor.addPropertyMetaData(asIs);
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor allUnmapped() {
		return entityUnmapped().propertiesUnmapped();
	}

	@Override
	public SmartEntityTypeMappingEditor entityUnmapped() {
		SmartUnmapped unmapped = mappingEditor.acquireUnmapped(currentAccessId);
		entityMdEditor.addMetaData(unmapped);
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor propertiesUnmapped() {
		SmartUnmapped unmapped = mappingEditor.acquireUnmapped(currentAccessId);
		entityMdEditor.addPropertyMetaData(unmapped);
		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor entityTo(EntityType<?> delegateType) {
		return entityTo(delegateType.getTypeSignature());
	}

	@Override
	public SmartEntityTypeMappingEditor entityTo(String typeSignature) {
		currentDelegateTypeOracle = findEntityTypeOracle(typeSignature);

		QualifiedEntityAssignment qea = mappingEditor.newMapping(QualifiedEntityAssignment.T, globalId(QEA), currentAccessId);
		qea.setEntityType(currentDelegateTypeOracle.asGmEntityType());

		entityMdEditor.addMetaData(qea);

		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor propertyTo(String smartProperty, String delegateProperty) {
		QualifiedPropertyAssignment qpa = mappingEditor.newMapping(QualifiedPropertyAssignment.T, globalId(QPA), currentAccessId);

		qpa.setEntityType(currentDelegateTypeOracle.asGmEntityType());
		qpa.setProperty(currentDelegateTypeOracle.getProperty(delegateProperty).asGmProperty());

		entityMdEditor.addPropertyMetaData(smartProperty, qpa);

		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor propertyUnmapped(String smartProperty) {
		SmartUnmapped unmapped = mappingEditor.acquireUnmapped(currentAccessId);
		entityMdEditor.addPropertyMetaData(smartProperty, unmapped);

		return this;
	}

	@Override
	public SmartEntityTypeMappingEditor propertiesUnmapped(String... smartProperties) {
		SmartUnmapped unmapped = mappingEditor.acquireUnmapped(currentAccessId);
		for (String smartProperty : smartProperties)
			entityMdEditor.addPropertyMetaData(smartProperty, unmapped);

		return this;
	}

	private String globalId(String component) {
		if (currentAccessId == null)
			return mappingEditor.globalId(currentGlobalIdPart, component, "global");
		else
			return mappingEditor.globalId(currentAccessId, currentGlobalIdPart, component);
	}

	private EntityTypeOracle findEntityTypeOracle(String typeSignature) {
		return mappingEditor.lookupOracle.findEntityTypeOracle(typeSignature);
	}

}
