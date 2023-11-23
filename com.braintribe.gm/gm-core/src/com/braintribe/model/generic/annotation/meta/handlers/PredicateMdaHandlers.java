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
package com.braintribe.model.generic.annotation.meta.handlers;

import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Emphasized;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.NonDeletable;
import com.braintribe.model.generic.annotation.meta.Singleton;
import com.braintribe.model.generic.annotation.meta.TimeZoneless;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.annotation.meta.Unmodifiable;
import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.base.BasicMdaHandler;

/**
 * @author peter.gazdik
 */
public interface PredicateMdaHandlers {

	MdaHandler<Confidential, com.braintribe.model.meta.data.prompt.Confidential> CONFIDENTIAL = new BasicMdaHandler<>( //
			Confidential.class, com.braintribe.model.meta.data.prompt.Confidential.class, Confidential::globalId);

	MdaHandler<Deprecated, com.braintribe.model.meta.data.prompt.Deprecated> DEPRECATED = new DeprecatedMdaHandler();

	MdaHandler<Emphasized, com.braintribe.model.meta.data.display.Emphasized> EMPHASIZED = new BasicMdaHandler<>( //
			Emphasized.class, com.braintribe.model.meta.data.display.Emphasized.class, Emphasized::globalId);

	MdaHandler<Mandatory, com.braintribe.model.meta.data.constraint.Mandatory> MANDATORY = new BasicMdaHandler<>( //
			Mandatory.class, com.braintribe.model.meta.data.constraint.Mandatory.class, Mandatory::globalId);

	MdaHandler<NonDeletable, com.braintribe.model.meta.data.constraint.NonDeletable> NON_DELETABLE = new BasicMdaHandler<>( //
			NonDeletable.class, com.braintribe.model.meta.data.constraint.NonDeletable.class, NonDeletable::globalId);

	MdaHandler<Singleton, com.braintribe.model.meta.data.prompt.Singleton> SINGLETON = new BasicMdaHandler<>( //
			Singleton.class, com.braintribe.model.meta.data.prompt.Singleton.class, Singleton::globalId);

	MdaHandler<TimeZoneless, com.braintribe.model.meta.data.prompt.TimeZoneless> TIME_ZONELESS = new BasicMdaHandler<>( //
			TimeZoneless.class, com.braintribe.model.meta.data.prompt.TimeZoneless.class, TimeZoneless::globalId);

	MdaHandler<Unique, com.braintribe.model.meta.data.constraint.Unique> UNIQUE = new BasicMdaHandler<>( //
			Unique.class, com.braintribe.model.meta.data.constraint.Unique.class, Unique::globalId);

	MdaHandler<Unmodifiable, com.braintribe.model.meta.data.constraint.Unmodifiable> UNMODIFIABLE = new BasicMdaHandler<>( //
			Unmodifiable.class, com.braintribe.model.meta.data.constraint.Unmodifiable.class, Unmodifiable::globalId);

}
