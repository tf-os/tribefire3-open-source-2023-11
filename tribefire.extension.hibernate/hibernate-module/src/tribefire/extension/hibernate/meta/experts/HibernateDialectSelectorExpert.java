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
package tribefire.extension.hibernate.meta.experts;

import java.util.Collection;

import com.braintribe.model.access.hibernate.meta.aspects.HibernateDialectAspect;
import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.model.accessdeployment.hibernate.selector.HibernateDialectSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools;

/**
 * Expert for the {@link UseCaseSelector}. The selector is active if it's use-case value is contained within the ones currently specified in the
 * context.
 */
public class HibernateDialectSelectorExpert implements SelectorExpert<HibernateDialectSelector> {

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(HibernateDialectSelector selector) throws Exception {
		return MetaDataTools.aspects(HibernateDialectAspect.class);
	}

	@Override
	public boolean matches(HibernateDialectSelector selector, SelectorContext context) throws Exception {
		HibernateDialect desiredDialect = selector.getDialect();

		return desiredDialect == null || desiredDialect == context.get(HibernateDialectAspect.class);
	}

}
