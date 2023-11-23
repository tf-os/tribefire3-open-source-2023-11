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
package tribefire.extension.shiro.templates.wire.contract;

import java.util.List;

import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.processing.shiro.ShiroAuthenticationUrl;
import com.braintribe.model.shiro.deployment.Login;
import com.braintribe.model.shiro.deployment.SessionValidator;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.model.shiro.deployment.ShiroBootstrappingWorker;
import com.braintribe.model.shiro.deployment.ShiroServiceProcessor;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.shiro.templates.api.ShiroTemplateContext;

public interface ShiroTemplatesContract extends WireSpace {

	ShiroServiceProcessor serviceRequestProcessor(ShiroTemplateContext context);

	ShiroAuthenticationConfiguration authenticationConfiguration(ShiroTemplateContext context);

	Login login(ShiroTemplateContext context);

	ShiroBootstrappingWorker bootstrappingWorker(ShiroTemplateContext context);

	CheckBundle functionalCheckBundle(ShiroTemplateContext context);

	SessionValidator sessionValidator(ShiroTemplateContext context);

	List<ShiroAuthenticationUrl> getAuthenticationUrls(ShiroTemplateContext context);
}
