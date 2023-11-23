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
package com.braintribe.model.access.smart.manipulation.adapt.smart2delegate;

import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.changeValue;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.delete;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.instantiationManipulation;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.owner;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.persistentRef;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.preliminaryRef;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.first;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * Covers properties with {@link LinkPropertyAssignment} mapping.
 */
public class LpaEntityHandler implements Smart2DelegateHandler<LinkPropertyAssignment> {

	private static final Logger log = Logger.getLogger(LpaEntityHandler.class);

	private final SmartManipulationProcessor smp;
	private final SmartManipulationContextVariables $;

	private LinkPropertyAssignment currentLpa;
	private String linkEntitySignature;
	private Object keyValue;
	private List<Manipulation> linkAccessManipulations;
	private IncrementalAccess linkAccess;

	public LpaEntityHandler(SmartManipulationProcessor smp) {
		this.smp = smp;
		this.$ = smp.context();
	}

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	protected Manipulation delegateManipulation;

	@Override
	public void loadAssignment(LinkPropertyAssignment assignment) {
		currentLpa = assignment;

		linkEntitySignature = currentLpa.getLinkKey().getDeclaringType().getTypeSignature();

		keyValue = smp.propertyValueResolver().acquireDelegatePropertyValue($.currentSmartReference, currentLpa.getKey().getProperty().getName());

		linkAccess = currentLpa.getLinkAccess();
		linkAccessManipulations = acquireList($.delegateManipulations, linkAccess);
	}

	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) throws ModelAccessException {
		Object otherKeyValue = findOtherKeyValue((EntityReference) manipulation.getNewValue());
		Object existingLinkId = findLinkIdFor(keyValue);

		if (otherKeyValue != null) {
			setNewLinkEntity(otherKeyValue, existingLinkId);
		} else {
			removeExistingLinkEntity(existingLinkId);
		}
	}

	@Override
	public void convertToDelegate(AddManipulation manipulation) throws ModelAccessException {
		throwIllegalManipulation("Add");
	}

	@Override
	public void convertToDelegate(RemoveManipulation manipulation) throws ModelAccessException {
		throwIllegalManipulation("Remove");
	}

	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) throws ModelAccessException {
		throwIllegalManipulation("ClearCollection");
	}

	private void throwIllegalManipulation(String manipulation) {
		QualifiedProperty key = currentLpa.getKey();
		GmProperty keyProperty = key.getProperty();

		throw new SmartAccessException(manipulation + "Manipulation is illegal for entity-property. Property: " +
				key.getEntityType().getTypeSignature() + "." + keyProperty.getName() + " of type: " + keyProperty.getType().getTypeSignature());
	}

	private void setNewLinkEntity(Object otherKeyValue, Object existingLinkId) {
		if (existingLinkId != null) {
			PersistentEntityReference ref = ManipulationBuilder.persistentRef(linkEntitySignature, existingLinkId);
			linkAccessManipulations.add(changeValue(owner(ref, currentLpa.getLinkOtherKey()), otherKeyValue));

		} else {

			PreliminaryEntityReference ref = preliminaryRef(linkEntitySignature);

			linkAccessManipulations.add(instantiationManipulation(ref));
			linkAccessManipulations.add(changeValue(owner(ref, currentLpa.getLinkKey()), keyValue));
			linkAccessManipulations.add(changeValue(owner(ref, currentLpa.getLinkOtherKey()), otherKeyValue));
		}
	}

	private void removeExistingLinkEntity(Object existingLinkId) {
		if (existingLinkId != null) {
			PersistentEntityReference ref = persistentRef(linkEntitySignature, existingLinkId);
			linkAccessManipulations.add(delete(ref, DeleteMode.ignoreReferences));
		}
	}

	/** @return value of property which is used as key for the link */
	private Object findOtherKeyValue(EntityReference otherReference) {
		if (otherReference == null) {
			return null;
		}

		String otherKey = currentLpa.getOtherKey().getProperty().getName();
		otherKey = smp.findDelegatePropertyForKeyPropertyOfCurrentSmartType(otherKey, otherReference);

		return smp.propertyValueResolver().acquireDelegatePropertyValue(otherReference, otherKey);
	}

	/**
	 * @return id of LinkEntity corresponding to given <tt>linkKeyValue</tt>.
	 */
	private Object findLinkIdFor(Object linkKeyValue) throws ModelAccessException {
		final String linkKey = currentLpa.getLinkKey().getName();

		// @formatter:off
		SelectQuery query = new SelectQueryBuilder()
						.from(linkEntitySignature, "l")
						.select("l", GenericEntity.id)
						.where()
							.property("l", linkKey).eq(linkKeyValue)
				.done();
		// @formatter:on

		SelectQueryResult qResult = smp.getAccessImpl(linkAccess).query(query);
		List<Object> results = qResult.getResults();

		if (results.size() > 1) {
			log.warn("Data inconsistency. Multiple entries LinkEntity instances exist for for key: '" + linkKeyValue + "'. LinkEntity: '" +
					linkEntitySignature + "', key property: '" + linkKey + "'.");
		}

		if (results.isEmpty()) {
			return null;
		}

		return first(results);
	}

}
