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
package com.braintribe.utils.velocity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.Not;
import com.braintribe.utils.lcd.Arguments;

/**
 * A simple <code>String</code> based (i.e. no readers/writers) Velocity template renderer that implements the
 * {@link Function} interface. The implementation relies on the utility methods provided by {@link VelocityTools}
 * , e.g. {@link VelocityTools#newVelocityEngine()}.
 * 
 * @author michael.lafite
 */
public class SimpleVelocityTemplateRenderer implements Function<Object, String> {

	/**
	 * The activation mode can be used to enable/disable the template evaluation.
	 * 
	 * @see SimpleVelocityTemplateRenderer#setActivationMode(ActivationMode)
	 * 
	 * @author michael.lafite
	 */
	public enum ActivationMode {
		/**
		 * Always evaluate.
		 */
		ALWAYS_ON,
		/**
		 * Evaluate unless the template contains the {@link SimpleVelocityTemplateRenderer#getDeactivationString()
		 * deactivation string}.
		 */
		ON_BY_DEFAULT,
		/**
		 * Evaluate if the template contains the {@link SimpleVelocityTemplateRenderer#getActivationString() activation
		 * string}.
		 */
		OFF_BY_DEFAULT,
		/**
		 * Never evaluate.
		 */
		ALWAYS_OFF
	}

	public static final String DEFAULT_ACTIVATION_STRING = "<!-- Velocity Template Renderer ON -->";
	public static final String DEFAULT_DEACTIVATION_STRING = "<!--  Velocity Template Renderer OFF -->";

	private static final Logger logger = Logger.getLogger(SimpleVelocityTemplateRenderer.class);

	/**
	 * The default {@link ActivationMode} is {@link ActivationMode#ON_BY_DEFAULT}.
	 */
	private static final ActivationMode DEFAULT_ACTIVATIONMODE = ActivationMode.ON_BY_DEFAULT;
	private ActivationMode activationMode = DEFAULT_ACTIVATIONMODE;

	private String activationString = DEFAULT_ACTIVATION_STRING;
	private String deactivationString = DEFAULT_DEACTIVATION_STRING;

	private String templateName;

	private Function<Object, String> templateProvider;

	private VelocityEngine engine;

	/**
	 * The name of the context object that is put into the velocity context created by {@link #apply(Object)}.
	 */
	public static final String CONTEXT = "context";

	public SimpleVelocityTemplateRenderer() {
		// nothing to do
	}

	public SimpleVelocityTemplateRenderer(final Function<Object, String> templateProvider) {
		this.templateProvider = templateProvider;
	}

	public SimpleVelocityTemplateRenderer(final Supplier<String> templateProvider) {
		this.templateProvider = k -> templateProvider.get();
	}

	public SimpleVelocityTemplateRenderer(final String template) {
		this.templateProvider = k -> template;
	}

	public ActivationMode getActivationMode() {
		return Not.Null(this.activationMode);
	}

	/**
	 * Sets the {@link ActivationMode}. Default is {@link #DEFAULT_ACTIVATIONMODE}.
	 */
	public void setActivationMode(ActivationMode activationMode) {
		Arguments.notNull(activationMode, "The specified " + ActivationMode.class.getSimpleName()
				+ " must not be null!");
		this.activationMode = activationMode;
	}

	
	public String getActivationString() {
		return this.activationString;
	}

	public void setActivationString(String activationString) {
		this.activationString = activationString;
	}

	
	public String getDeactivationString() {
		return this.deactivationString;
	}

	public void setDeactivationString(String deactivationString) {
		this.deactivationString = deactivationString;
	}

	public Function<Object, String> getTemplateProvider() {
		final Function<Object, String> result = this.templateProvider;
		if (result == null) {
			throw new IllegalStateException("Illegal state: template provider is not set (yet).");
		}
		return result;
	}

	public void setTemplateProvider(final Function<Object, String> templateProvider) {
		this.templateProvider = templateProvider;
	}

	private String getTemplate( final Object context) {
		return CommonTools.provide(getTemplateProvider(), context);
	}

	public String getTemplateName() {
		if (this.templateName == null) {
			this.templateName = VelocityTools.DEFAULT_TEMPLATE_NAME;
		}
		return Not.Null(this.templateName);
	}

	public void setTemplateName(final String templateName) {
		this.templateName = templateName;
	}

	public VelocityEngine getEngine() {
		if (this.engine == null) {
			this.engine = VelocityTools.newVelocityEngine();
		}
		return Not.Null(this.engine);
	}

	public void setEngine(final VelocityEngine engine) {
		this.engine = engine;
	}

	/**
	 * {@link VelocityTools#evaluate(VelocityEngine, VelocityContext, String, String) Evaluates} the specified
	 * <code>template</code> using the passed <code>velocityContext</code>.
	 */
	private String evaluate(final VelocityContext velocityContext, final String template) {
		if (logger.isTraceEnabled()) {
			logger.trace("Evaluating template:" + Constants.LINE_SEPARATOR + template);
		}

		String result = template;
		boolean activationStringFound = getActivationString() != null && template.contains(getActivationString());
		boolean deactivationStringFound = getDeactivationString() != null && template.contains(getDeactivationString());

		if (activationStringFound && deactivationStringFound) {
			throw new IllegalArgumentException(
					"The provided template contains both, the activation and the deactivation string! "
							+ CommonTools.getParametersString(template));
		}

		if (activationStringFound) {
			result = result.replace(getActivationString(), "");
		} else if (deactivationStringFound) {
			result = result.replace(getDeactivationString(), "");
		}

		boolean doEvaluateTemplate;
		switch (getActivationMode()) {
		case ALWAYS_ON:
			doEvaluateTemplate = true;
			break;
		case ALWAYS_OFF:
			doEvaluateTemplate = false;
			break;
		case ON_BY_DEFAULT:
			doEvaluateTemplate = !deactivationStringFound;
			break;
		case OFF_BY_DEFAULT:
			doEvaluateTemplate = activationStringFound;
			break;
		default:
			throw new UnknownEnumException(getActivationMode());
		}

		if (doEvaluateTemplate) {
			result = VelocityTools.evaluate(getEngine(), velocityContext, template, getTemplateName());
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("Skipping evaluation of this template (because of configured activation mode and/or (de)activation string.");
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Returning result:" + Constants.LINE_SEPARATOR + result);
		}
		return Not.Null(result);
	}

	/**
	 * {@link VelocityTools#evaluate(VelocityEngine, VelocityContext, String, String) Evaluates} the configured
	 * {@link #getTemplate(Object)} template} using the passed <code>context</code>.
	 */
	private String evaluate(final VelocityContext velocityContext) {
		final String template = getTemplate(getContext(velocityContext));
		return evaluate(velocityContext, template);
	}

	/**
	 * See {@link #evaluate(VelocityContext)}.
	 * 
	 * @param context
	 *            the context, which is NOT a {@link VelocityContext} but context object that will be put into the
	 *            created <code>VelocityContext</code> instance (key = {@link SimpleVelocityTemplateRenderer#CONTEXT} (
	 *            {@value #CONTEXT})) and that will also be passed to the {@link #getTemplateProvider() template
	 *            provider}.
	 */
	@Override
	public String apply( final Object context) {
		if (context instanceof VelocityContext) {
			throw new IllegalArgumentException(
					"This method doesn't expect a VelocityContext instance as context (instead it will create the VelocityInstance itself)");
		}

		final VelocityContext velocityContext = VelocityTools.newVelocityContext();
		putContext(velocityContext, context);

		String result = evaluate(velocityContext);
		return result;
	}

	/**
	 * Puts the <code>contextEntryKeyAndValuePairs</code> into a map, creates a new
	 * {@link SimpleVelocityTemplateRenderer} instance and then {@link #apply(Object) provides} the result for the
	 * specified <code>template</code>. This is a convenience method mainly used in tests.
	 */
	public static String quickEvaluate(final String template, final Object... contextEntryKeyAndValuePairs) {
		final Map<String, Object> context = MapTools.getParameterizedMap(String.class, Object.class,
				contextEntryKeyAndValuePairs);
		final String result = new SimpleVelocityTemplateRenderer(template).apply(context);
		return result;
	}

	private static void putContext(final VelocityContext velocityContext,  final Object context) {
		velocityContext.put(CONTEXT, context);

		// if the context is a map, we also put all its entries into the context
		if (context instanceof Map) {
			Map<String, Object> properties = null;

			try {
				properties = MapTools.getParameterizedMap(String.class, Object.class, (Map<?, ?>) context);
			} catch (final ClassCastException e) {
				// ignore
			}

			if (properties != null) {
				for (final Entry<String, Object> entry : properties.entrySet()) {
					velocityContext.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	
	private static Object getContext(final VelocityContext velocityContext) {
		return velocityContext.get(CONTEXT);
	}
}
