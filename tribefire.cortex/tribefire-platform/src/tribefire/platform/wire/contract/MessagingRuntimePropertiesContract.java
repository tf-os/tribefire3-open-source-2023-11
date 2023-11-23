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
package tribefire.platform.wire.contract;

import java.time.Duration;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.wire.api.annotation.Default;

import tribefire.module.wire.contract.PropertyLookupContract;

public interface MessagingRuntimePropertiesContract extends PropertyLookupContract {

	@Default("" + Numbers.MEBIBYTE)
	long TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_THRESHOLD();
	
	@Default("PT30M")
	Duration TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_TTL();
	
	@Default("PT5M")
	Duration TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_CLEANUP_INTERVAL();
}
