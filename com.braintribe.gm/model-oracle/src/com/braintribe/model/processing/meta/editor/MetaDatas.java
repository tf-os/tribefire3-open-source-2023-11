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
package com.braintribe.model.processing.meta.editor;

import java.util.function.Consumer;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;

/**
 * @author peter.gazdik
 */
public interface MetaDatas {

	static <M extends MetaData> M md(EntityType<M> mdType) {
		return mdType.create();
	}

	static <M extends MetaData> M md(EntityType<M> mdType, MetaDataSelector selector) {
		M result = mdType.create();
		result.setSelector(selector);
		return result;
	}
	
	static <M extends MetaData> M md(EntityType<M> mdType, Double priority) {
		M result = mdType.create();
		result.setConflictPriority(priority);
		return result;
	}
	
	static <M extends MetaData> M md(EntityType<M> mdType, Consumer<? super M> mdConsumer) {
		M result = mdType.create();
		mdConsumer.accept(result);
		return result;
	}

}
