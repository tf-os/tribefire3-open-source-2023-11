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
package com.braintribe.devrock.zarathud.wirings.console.space;

import com.braintribe.devrock.zarathud.wirings.console.contract.ZedConsoleOutContract;
import com.braintribe.devrock.zed.api.output.ConsoleContainerOut;
import com.braintribe.devrock.zed.output.BasicConsoleContainerOut;
import com.braintribe.wire.api.annotation.Managed;
@Managed
public class ZedConsoleOutSpace implements ZedConsoleOutContract {

	@Override
	public ConsoleContainerOut consoleOut() {
		BasicConsoleContainerOut bean = new BasicConsoleContainerOut();
		return bean;
	}

}
