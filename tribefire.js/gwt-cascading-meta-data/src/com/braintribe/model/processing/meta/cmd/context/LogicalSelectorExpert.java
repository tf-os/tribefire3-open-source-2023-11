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
package com.braintribe.model.processing.meta.cmd.context;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;
import com.braintribe.model.processing.meta.cmd.context.experts.CmdSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;

/**
 * A base for {@link CmdSelectorExpert} for logical {@link MetaDataSelector}s.
 * <p>
 * One special aspect of logical selectors is they are automatically not-ignored in case we resolve with
 * {@link MdResolver#ignoreSelectorsExcept(EntityType...)} option.
 * 
 * @see #maybeMatches(MetaDataSelector, SelectorContext)
 */
public interface LogicalSelectorExpert<T extends MetaDataSelector> extends CmdSelectorExpert<T> {

	/**
	 * Similar to {@link SelectorExpert#matches(MetaDataSelector, SelectorContext)}, but may return {@code null} which indicates the answer is not
	 * clear as some relevant {@link MetaDataSelector} was ignored. MD for which this method returns {@code null} or {@code true} is resolved, because
	 * only {@code false} returned from this method indicates the MD is not resolvable.
	 * <p>
	 * For example when ignoring all selectors except for {@link UseCaseSelector}, any MD with a non-matching use-case will be ignored, while all MD
	 * with matching use-case are considered.
	 * <p>
	 * See part about caching in the description of {@link CmdResolver}.
	 */
	Boolean maybeMatches(T selector, SelectorContext context) throws Exception;

}
