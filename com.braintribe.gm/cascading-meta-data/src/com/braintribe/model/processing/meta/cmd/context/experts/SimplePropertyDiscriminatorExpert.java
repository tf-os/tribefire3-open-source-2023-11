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
package com.braintribe.model.processing.meta.cmd.context.experts;

import java.util.Collection;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.selector.SimplePropertyDiscriminator;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.tools.CmdGwtUtils;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

/**
 * 
 */
public abstract class SimplePropertyDiscriminatorExpert<T extends SimplePropertyDiscriminator, P> implements CmdSelectorExpert<T> {

	private static final Logger log = Logger.getLogger(SimplePropertyDiscriminatorExpert.class);

	private final Class<P> propertyType;
	private boolean suppressInconsistencies;

	protected SimplePropertyDiscriminatorExpert(Class<P> propertyType) {
		this.propertyType = propertyType;
	}

	public void setSuppressInconsistencies(boolean shouldSuppress) {
		this.suppressInconsistencies = shouldSuppress;
	}

	@Override
	public final Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(T selector) throws Exception {
		return MetaDataTools.aspects(EntityAspect.class);
	}

	protected P getPropertyCasted(T selector, SelectorContext context) throws CascadingMetaDataException {
		Object o = getProperty(selector, context);

		if (o != null && !CmdGwtUtils.isInstanceOf(propertyType, o)) {
			handleWrongType(selector, context, o.getClass());
		}

		return (P) o;
	}

	protected Object getProperty(T selector, SelectorContext context) throws CascadingMetaDataException {
		GenericEntity entity = context.get(EntityAspect.class);

		if (entity == null)
			return null;

		EntityType<?> et = entity.entityType();

		String propertyName = selector.getDiscriminatorProperty().getName();
		Property property;
		try {
			property = et.getProperty(propertyName);

		} catch (GenericModelException e) {
			handlePropertyProblem(et, propertyName, e);
			return null;
		}

		return property.get(entity);
	}

	private void handlePropertyProblem(EntityType<?> et, String propertyName, GenericModelException e) {
		if (suppressInconsistencies) {
			log.warn("Problem with property '" + propertyName + "' of entity: " + et.getTypeSignature(), e);
		} else {
			throw e;
		}
	}

	private void handleWrongType(T selector, SelectorContext context, Class<?> actualType) throws CascadingMetaDataException {
		EntityType<?> et = context.get(EntityTypeAspect.class);
		String propertyName = selector.getDiscriminatorProperty().getName();

		String s = "MetaData for wrong property type. Property: '" + et.getTypeSignature() + "#" + propertyName + " MetaData for: " +
				propertyType.getName() + ", Actual type: " + actualType.getName();

		if (suppressInconsistencies) {
			log.warn(s);
		} else {
			throw new RuntimeException(s);
		}
	}

}
