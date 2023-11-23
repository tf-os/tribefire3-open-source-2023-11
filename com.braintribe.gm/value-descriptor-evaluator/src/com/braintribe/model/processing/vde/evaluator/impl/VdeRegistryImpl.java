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
package com.braintribe.model.processing.vde.evaluator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;

public class VdeRegistryImpl implements VdeRegistry {

	private Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> concreteExperts = new HashMap<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>();
	private List<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>> abstractExperts = new ArrayList<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>>();

	@Override
	public Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> getConcreteExperts() {
		return this.concreteExperts;
	}

	@Override
	public void setConcreteExperts(Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> concreteExperts) {
		this.concreteExperts.putAll(concreteExperts);
	}

	@Override
	public List<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>> getAbstractExperts() {
		return this.abstractExperts;
	}

	@Override
	public void setAbstractExperts(List<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>> abstractExperts) {
		this.abstractExperts.addAll(abstractExperts);
	}


	@Override
	public <D extends ValueDescriptor> void putConcreteExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator) {
		this.concreteExperts.put(vdType, vdEvaluator);
	}

	@Override
	public <D extends ValueDescriptor> void putAbstractExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator) {
		this.abstractExperts.add(new Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>(vdType, vdEvaluator));
	}

	@Override
	public void resetRegistry() {

		this.concreteExperts = new HashMap<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>();
		this.abstractExperts = new ArrayList<Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>>();

	}

	@Override
	public void loadOtherRegistry(VdeRegistry otherRegistry) {
		this.concreteExperts.putAll(otherRegistry.getConcreteExperts());
		this.abstractExperts.addAll(otherRegistry.getAbstractExperts());
	}


	@Override
	public void removeConcreteExpert(Class<? extends ValueDescriptor> valueDescriptorClass) {
		this.concreteExperts.remove(valueDescriptorClass);
	}

	@Override
	public void removeAbstractExpert(Class<? extends ValueDescriptor> valueDescriptorClass) {
		int counter = -1;
		boolean found = false;
		for (Pair<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> abstractExpertPair : this.abstractExperts) {
			counter++;
			Class<? extends ValueDescriptor> abstractExpertJavaClass = abstractExpertPair.getFirst();
			if (abstractExpertJavaClass.equals(valueDescriptorClass)) {
				found = true;
				break;
			}
		}
		if (found)
			this.abstractExperts.remove(counter);
	}

}
