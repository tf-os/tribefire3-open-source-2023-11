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
package com.braintribe.gm.html_marshaller.wire.space;

import com.braintribe.codec.marshaller.html.HtmlMarshaller;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.HardwiredMarshallersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class HtmlMarshallerModuleSpace implements TribefireModuleContract {

	@Import
	private TribefirePlatformContract tfPlatform;
	
	@Import
	private HardwiredMarshallersContract hardwiredMarshallers;

	@Override
	public void bindHardwired() {
		hardwiredMarshallers.bindMarshaller("marshaller.html", "HTML Marshaller", this::htmlMarshaller, "text/html;spec=temp");
	}
	
	@Managed
	private HtmlMarshaller htmlMarshaller() {
		return new HtmlMarshaller();
	}

}
