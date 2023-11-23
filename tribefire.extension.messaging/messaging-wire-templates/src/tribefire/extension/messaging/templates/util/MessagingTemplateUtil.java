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
package tribefire.extension.messaging.templates.util;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.messaging.MessagingConstants;
import tribefire.extension.messaging.templates.api.MessagingTemplateContext;

/**
 * Utility class related to handling of {@link MessagingTemplateContext}
 *
 *
 */
public class MessagingTemplateUtil {

	private static final Logger logger = Logger.getLogger(MessagingTemplateUtil.class);

	/**
	 * Create a globalId based on the {@link MessagingTemplateContext}
	 *
	 * @param context
	 *            {@link MessagingTemplateContext}
	 * @param instanceConfiguration
	 *            {@link InstanceConfiguration}
	 * @return context based globalId
	 */
	public static String resolveContextBasedGlobalId(MessagingTemplateContext context, InstanceConfiguration instanceConfiguration) {
		String contextString = resolveContext(context);

		String globalIdPrefix = CommonTools.isEmpty(context.getGlobalIdPrefix()) ? "" : context.getGlobalIdPrefix() + "/";

		InstanceQualification qualification = instanceConfiguration.qualification();
		String globalId = "wire://" + globalIdPrefix + qualification.space().getClass().getSimpleName() + "/" + qualification.name() + "/"
				+ contextString;

		return globalId;
	}

	/**
	 * Create a human-readable Deployable name based on the {@link MessagingTemplateContext}
	 *
	 * @param deployableName
	 *            Deployable name without context information
	 * @param context
	 *            {@link MessagingTemplateContext}
	 * @return context based Deployable name
	 */
	public static String resolveContextBasedDeployableName(String deployableName, MessagingTemplateContext context) {
		String contextBasedDeployableName = deployableName + " - " + resolvePrettyName(context);
		logger.debug(() -> "Resolve context based deployable name: '" + contextBasedDeployableName + "' for context: '" + context + "'");
		return contextBasedDeployableName;
	}

	public static String resolveContext(MessagingTemplateContext context) {
		String _context = context.getContext();

		if (CommonTools.isEmpty(_context)) {
			throw new IllegalStateException("'context' must be specified!");
		}

		String idPrefix = StringTools.camelCaseToDashSeparated(_context);

		logger.debug(() -> "Resolve idPrefix: '" + idPrefix + "' for context: '" + context + "'");

		return idPrefix;
	}

	public static String resolveServiceModelName(MessagingTemplateContext context) {
		return MessagingConstants.SERVICE_MODEL_QUALIFIEDNAME + "-" + MessagingTemplateUtil.resolveContext(context);
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static String resolvePrettyName(MessagingTemplateContext context) {
		String _context = resolveContext(context);
		String prettyName = StringTools.prettifyCamelCase(_context);

		logger.debug(() -> "Resolve prettyName: '" + prettyName + "' for context: '" + context + "'");
		return prettyName;
	}

	public MessagingTemplateUtil() {
	}
}
