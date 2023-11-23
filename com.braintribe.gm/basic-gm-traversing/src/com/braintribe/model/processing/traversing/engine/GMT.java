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
package com.braintribe.model.processing.traversing.engine;

import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.engine.api.ClonerConfigurer;
import com.braintribe.model.processing.traversing.engine.api.TraversingConfigurer;
import com.braintribe.model.processing.traversing.engine.api.customize.ModelWalkerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.TraversingConfigurerImpl;
import com.braintribe.model.processing.traversing.engine.impl.clone.Cloner;
import com.braintribe.model.processing.traversing.engine.impl.clone.ClonerConfigurerImpl;
import com.braintribe.model.processing.traversing.engine.impl.walk.ModelWalker;
import com.braintribe.model.processing.traversing.engine.impl.walk.VdeModelWalkerCustomization;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeContextBuilder;

/**
 * Main Access point for all traversing actions.
 * 
 * All methods are declared as static.
 * 
 */
public class GMT {

	/**
	 * Performs standard cloning for an object
	 * 
	 * @param value
	 *            Object that needs to be cloned
	 * @return A cloned instance of the provided object
	 */
	public static <V> V clone(Object value) throws GmTraversingException {
		Cloner cloner = new Cloner();
		doClone().visitor(cloner).doFor(value);

		return cloner.getClonedValue();
	}

	/**
	 * Allows a customised setup for cloning operation via
	 * {@link ClonerConfigurer}
	 * 
	 */
	public static ClonerConfigurer<? extends ClonerConfigurer<?>> doClone() {
		return new ClonerConfigurerImpl();
	}

	/**
	 * Perform a standard traversing of an assembly with default settings {@link ModelWalker} in addition to another provided visitor
	 * 
	 * @param visitor
	 *            Visitor that should be used
	 * @param value
	 *            Object that will be traversed
	 */
	public static void traverse(GmTraversingVisitor visitor, Object value) throws GmTraversingException {
		traverse().visitor(visitor).doFor(value);
	}

	/**
	 * Allows a customized setup for traversing operation via
	 * {@link TraversingConfigurer}
	 * 
	 */
	public static TraversingConfigurer<? extends TraversingConfigurer<?>> traverse() {
		return new TraversingConfigurerImpl();
	}

	/**
	 * Provides a clone of an assembly, where all {@link ValueDescriptor} are
	 * evaluated. The extent of the evaluation depends on:
	 * <ul>
	 * <li>If last layer: All ValueDescriptors must be evaluated, which means
	 * that all corresponding experts and aspects must be provided, otherwise an
	 * exception will be thrown</li>
	 * <li>If NOT last layer: All ValueDescriptors that could be evaluated would
	 * be, otherwise they will be simply cloned normally</li>
	 * </ul>
	 * 
	 * @param vdeContext
	 *            A {@link VdeContextBuilder} that holds the settings for VDE
	 *            evaluation
	 * @param value
	 *            Object that needs to requires evaluation
	 * @param isLastLayer
	 *            boolean indicating if this is the last layer of an evaluation
	 *            process
	 * @return A cloned assembly where all the value descriptors have been
	 *         evaluated based on the the last layer status
	 */
	public static <V> V evaluteTemplate(VdeContextBuilder vdeContext, Object value, boolean isLastLayer) throws GmTraversingException {
		if (isLastLayer) {
			vdeContext.withEvaluationMode(VdeEvaluationMode.Final);
		} else {
			vdeContext.withEvaluationMode(VdeEvaluationMode.Preliminary);
		}
		Cloner cloner = new Cloner();
		ModelWalkerCustomization walkerCustomization = new VdeModelWalkerCustomization(vdeContext, !isLastLayer);
		doClone().customizeDefaultWalker(walkerCustomization).visitor(cloner).doFor(value);
		return cloner.getClonedValue();
	}

}
