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
package tribefire.extension.elastic.elasticsearch;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.logging.Logger;

public class Bootstrap implements LifecycleAware {

	protected static Logger logger = Logger.getLogger(Bootstrap.class);

	public static void main(String[] args) throws Exception {
		Bootstrap app = new Bootstrap();

		app.postConstruct();
		app.preDestroy();
	}

	@Override
	public void postConstruct() {
		// Not yet implemented
	}

	@Override
	public void preDestroy() {
		// Not yet implemented
	}
}
