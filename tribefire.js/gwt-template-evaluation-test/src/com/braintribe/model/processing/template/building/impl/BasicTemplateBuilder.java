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
package com.braintribe.model.processing.template.building.impl;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.template.building.api.TemplateBuilder;
import com.braintribe.model.processing.template.building.api.TemplatePrototypeBuilder;
import com.braintribe.model.processing.template.building.api.TemplatePrototypingContext;
import com.braintribe.model.processing.template.building.api.TemplateRecordingContext;
import com.braintribe.model.processing.template.building.api.VdPushContext;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.template.meta.DynamicTypeMetaDataAssignment;
import com.braintribe.model.template.meta.TemplateMetaData;
import com.braintribe.utils.collection.api.IStack;
import com.braintribe.utils.collection.impl.ArrayStack;

public class BasicTemplateBuilder implements TemplatePrototypeBuilder, TemplateBuilder<Object>, TemplateRecordingContext<Object>, TemplatePrototypingContext, ManipulationListener {

	private DynamicTypeMetaDataAssignment dynamicTypeMetaDataAssignment;
	private Object prototype;
	private final ManagedGmSession session = new BasicManagedGmSession();
	private final List<Manipulation> manipulations = newList();
	private final Template template = Template.T.create();
	private final IStack<AbstractVdPushContext> vdStack = new ArrayStack<>();
	
	public BasicTemplateBuilder(LocalizedString name) {
		template.setName(name);
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		manipulations.add(manipulation);

		if (vdStack.isEmpty())
			return;
		
		switch (manipulation.manipulationType()) {
			case CHANGE_VALUE:
				ChangeValueManipulation cvm = (ChangeValueManipulation) manipulation;
				AbstractVdPushContext variableContext = vdStack.pop();
				ValueDescriptor vd = variableContext.get(cvm.getNewValue());
				cvm.setNewValue(vd);

				String typeSignature = ((LocalEntityProperty) cvm.getOwner()).property().getType().getTypeSignature();
				if (vd instanceof Variable)
					((Variable) vd).setTypeSignature(typeSignature);

				return;

			case ADD:
				// TODO check variable type signatures!!

				AddManipulation am = (AddManipulation) manipulation;

				Map<Object, Object> addMap = new LinkedHashMap<>();

				for (Entry<Object, Object> entry : am.getItemsToAdd().entrySet()) {
					Object key = resolve(entry.getKey(), true);
					Object value = resolve(entry.getValue(), false);

					addMap.put(key, value);
				}

				am.setItemsToAdd(addMap);

				return;

			case REMOVE:
				// TODO check variable type signatures!!

				RemoveManipulation rm = (RemoveManipulation) manipulation;

				Map<Object, Object> remMap = new LinkedHashMap<>();

				for (Entry<Object, Object> entry : rm.getItemsToRemove().entrySet()) {
					Object key = resolve(entry.getKey(), true);
					Object value = resolve(entry.getValue(), false);

					remMap.put(key, value);
				}

				rm.setItemsToRemove(remMap);
				return;

			default:
				return;
		}
	}
	
	private Object resolve(Object value, boolean key) {
		AbstractVdPushContext peek = vdStack.peek();
		
		if (peek != null && key == peek.isForMapKey())
			return vdStack.pop().get(value);
		else
			return value;
	}
	
	@Override
	public <P> TemplateBuilder<P> prototype(Function<TemplatePrototypingContext, P> factory) {
		this.prototype = factory.apply(this);
		// TODO auto-import in case  no session attached
		return (TemplateBuilder<P>) this;
	}

	@Override
	public TemplateBuilder<Object> record(Consumer<TemplateRecordingContext<Object>> producer) {
		session.listeners().add(this);
		try {
			producer.accept(this);
		} finally {
			session.listeners().remove(this);
		}
		
		return this;
	}

	@Override
	public TemplateBuilder<Object> addMetaData(TemplateMetaData metaData) {
		template.getMetaData().add(metaData);
		return this;
	}
	
	public DynamicTypeMetaDataAssignment getDynamicTypeMetaDataAssignment() {
		if (dynamicTypeMetaDataAssignment == null) {
			dynamicTypeMetaDataAssignment = DynamicTypeMetaDataAssignment.T.create();
			addMetaData(dynamicTypeMetaDataAssignment);
		}

		return dynamicTypeMetaDataAssignment;
	}

	@Override
	public TemplateBuilder<Object> addMetaData(MetaData metaData) {
		getDynamicTypeMetaDataAssignment().getMetaData().add(metaData);
		return this;
	}
	
	@Override
	public VdPushContext pushVariable(String identifier) {
		Variable variable = Variable.T.create();
		variable.setName(identifier);
		VariablePushContext variableContext = new VariablePushContext(variable);
		vdStack.push(variableContext);
		return variableContext;
	}
	
	@Override
	public VdPushContext pushVd(ValueDescriptor vd) {
		GenericVdPushContext vdContext = new GenericVdPushContext(vd);
		vdStack.push(vdContext);
		return vdContext;
	}

	@Override
	public Template build() {
		ListIterator<Manipulation> listIterator = manipulations.listIterator(manipulations.size());
		while (listIterator.hasPrevious()) {
			Manipulation manipulation = listIterator.previous();
			session.manipulate().mode(ManipulationMode.LOCAL).apply(manipulation.getInverseManipulation());
			manipulation.setInverseManipulation(null);
		}

		Manipulation script = null;
		
		switch (manipulations.size()) {
			case 0:
				break;
				
			case 1:
				script = manipulations.get(0);
				break;
				
			default:
				CompoundManipulation compoundManipulation = CompoundManipulation.T.create();
				compoundManipulation.setCompoundManipulationList(manipulations);
				script = compoundManipulation;
				break;
		}
		
		template.setScript(script);
		template.setPrototype(prototype);
		template.setPrototypeTypeSignature(GMF.getTypeReflection().getType(prototype).getTypeSignature());
		
		return template;
	}
	
	@Override
	public TemplateBuilder<Object> setDescription(LocalizedString description) {
		template.setDescription(description);
		return this;
	}

	
	@Override
	public Object getPrototype() {
		return prototype;
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType) {
		return session.create(entityType);
	}
	
	private abstract class AbstractVdPushContext implements VdPushContext {
		private boolean appliesToMapKey;
		
		@Override
		public VdPushContext appliesToMapKey() {
			appliesToMapKey = true;
			return this;
		}
		
		public boolean isForMapKey() {
			return appliesToMapKey;
		}
		
		public abstract ValueDescriptor get(Object substituted);
		
	}
	
	private class GenericVdPushContext extends AbstractVdPushContext {
		private final ValueDescriptor vd;
		
		public GenericVdPushContext(ValueDescriptor vd) {
			this.vd = vd;
		}

		@Override
		public VdPushContext addMetaData(MetaData metaData) {
			// no registration because no support for MD on other than Variable
			return this;
		}
		
		@Override
		public ValueDescriptor get(Object substituted) {
			return vd;
		}
	}
	
	private class VariablePushContext extends AbstractVdPushContext {
		private final Variable variable;
		private DynamicPropertyMetaDataAssignment dynamicPropertyMdAss;
		
		public VariablePushContext(Variable variable) {
			this.variable = variable;
		}

		public DynamicPropertyMetaDataAssignment getDynamicPropertyMetaDataAssignment() {
			if (dynamicPropertyMdAss == null) {
				dynamicPropertyMdAss = DynamicPropertyMetaDataAssignment.T.create();
				dynamicPropertyMdAss.setVariable(variable);

				BasicTemplateBuilder.this.addMetaData(dynamicPropertyMdAss);
			}

			return dynamicPropertyMdAss;
		}
		
		@Override
		public VdPushContext addMetaData(MetaData metaData) {
			getDynamicPropertyMetaDataAssignment().getMetaData().add(metaData);
			return this;
		}
		
		@Override
		public ValueDescriptor get(Object substituted) {
			variable.setDefaultValue(substituted);
			return variable;
		}
		
	}

}

