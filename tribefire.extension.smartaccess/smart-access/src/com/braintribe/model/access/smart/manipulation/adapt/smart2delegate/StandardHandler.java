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

import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.add;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.changeValue;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.owner;
import static com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder.remove;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;

import com.braintribe.model.access.smart.manipulation.SmartManipulationContextVariables;
import com.braintribe.model.access.smart.manipulation.SmartManipulationProcessor;
import com.braintribe.model.access.smart.manipulation.conversion.ResolveDelegateValueConversion;
import com.braintribe.model.access.smart.manipulation.tools.ManipulationBuilder;
import com.braintribe.model.accessdeployment.smart.meta.AsIs;
import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ClearCollectionManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;

/**
 * Covers properties with mappings:
 * 
 * <ul>
 * <li>{@link AsIs}</li>
 * <li>{@link PropertyAsIs}</li>
 * <li>{@link QualifiedPropertyAssignment}</li>
 * <li>{@link KeyPropertyAssignment}</li>
 * </ul>
 */
public class StandardHandler implements Smart2DelegateHandler<PropertyAssignment> {

	private final SmartManipulationProcessor smp;
	private final SmartManipulationContextVariables $;

	private EntityProperty currentDelegateOwner;
	private SmartConversion currentConversion;

	/* This is used as local variable inside methods, but is declared here to make code nicer */
	private Manipulation delegateManipulation;

	/**
	 * Add/Remove Manipulations contain entries in it's itemsToAdd/itemsToRemove map where key and value are the same. So in these cases,
	 * when converting this map, we also want to convert the keys.
	 */
	private boolean convertKeys;
	private EntityPropertyMapping epm;

	public StandardHandler(SmartManipulationProcessor smp) {
		this.smp = smp;
		this.$ = smp.context();
	}

	@Override
	public void loadAssignment(PropertyAssignment assignment) {
		EntityPropertyMapping epm = smp.modelExpert().resolveEntityPropertyMapping($.currentSmartType, $.currentAccess,
				$.currentSmartOwner.getPropertyName());

		loadAssignment(assignment, epm);
	}

	protected void loadAssignment(PropertyAssignment assignment, EntityPropertyMapping _epm) {
		epm = _epm;
		currentDelegateOwner = owner($.currentDelegateReference, epm.getDelegatePropertyName());
		currentConversion = getConversion(assignment, epm);
		convertKeys = epm.getDelegatePropertyType() instanceof GmSetType;
	}

	private SmartConversion getConversion(PropertyAssignment pa, EntityPropertyMapping epm) {
		SmartConversion explicitConversion = epm.getConversion();

		if (!(pa instanceof KeyPropertyAssignment)) {
			return explicitConversion;
		}

		String mappedKeyProperty = ((KeyPropertyAssignment) pa).getKeyProperty().getProperty().getName();

		ResolveDelegateValueConversion rdvc = ResolveDelegateValueConversion.T.create();
		rdvc.setNestedConversion(explicitConversion);
		rdvc.setPropertyName(mappedKeyProperty);

		return rdvc;
	}

	@Override
	public void convertToDelegate(ChangeValueManipulation manipulation) {
		Object newDelegateValue = smp.conv2Del(manipulation.getNewValue(), currentConversion);

		smp.propertyValueResolver().notifyChangeValue($.currentSmartReference, $.currentSmartOwner.getPropertyName(),
				currentDelegateOwner.getPropertyName(), newDelegateValue);
		smp.referenceManager().notifyChangeValue($.currentSmartReference, $.currentSmartOwner.getPropertyName(), manipulation.getNewValue(),
				$.currentDelegateReference, epm.getDelegatePropertyName(), epm.isDelegatePropertyId(), newDelegateValue);
		delegateManipulation = changeValue(currentDelegateOwner, newDelegateValue);

		acquireList($.delegateManipulations, $.currentEntityMapping.getAccess()).add(delegateManipulation);
	}

	@Override
	public void convertToDelegate(AddManipulation manipulation) {
		delegateManipulation = add(currentDelegateOwner, smp.conv2Del(manipulation.getItemsToAdd(), currentConversion, convertKeys));

		acquireList($.delegateManipulations, $.currentEntityMapping.getAccess()).add(delegateManipulation);
	}

	@Override
	public void convertToDelegate(RemoveManipulation manipulation) {
		delegateManipulation = remove(currentDelegateOwner, smp.conv2Del(manipulation.getItemsToRemove(), currentConversion, convertKeys));

		acquireList($.delegateManipulations, $.currentEntityMapping.getAccess()).add(delegateManipulation);
	}

	/**
	 * @param manipulation
	 *            is not used (we already got all the information loaded)
	 */
	@Override
	public void convertToDelegate(ClearCollectionManipulation manipulation) {
		delegateManipulation = ManipulationBuilder.clear(currentDelegateOwner);

		acquireList($.delegateManipulations, $.currentEntityMapping.getAccess()).add(delegateManipulation);
	}

}
