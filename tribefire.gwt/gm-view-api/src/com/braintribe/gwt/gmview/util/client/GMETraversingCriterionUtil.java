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
package com.braintribe.gwt.gmview.util.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyTypeCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.CollectionTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.TypeMatch;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.meta.data.prompt.AutoExpand;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.template.Template;
import com.sencha.gxt.core.shared.FastMap;

/**
 * Util class for handling some {@link TraversingCriterion} related operations.
 * @author michel.docouto
 *
 */
@SuppressWarnings("deprecation")
public class GMETraversingCriterionUtil {
	private static final Logger logger = new Logger(GMETraversingCriterionUtil.class);
	
	private static Map<String, TraversingCriterion> depthMap;
	
	static {
		prepareMap();
	}
	
	/**
	 * Checks for the {@link AutoExpand} metadata, and prepares a TC accordingly.
	 */
	public static TraversingCriterion prepareForDepthTC(TraversingCriterion currentTC, Object queryOrTemplate, String entityTypeSignature, PersistenceGmSession gmSession, String useCase) {
		AutoExpand autoExpand = null;
		if (queryOrTemplate instanceof Template) {
			Template template = (Template) queryOrTemplate;
			autoExpand = GMEMetadataUtil.getTemplateMetaData(template, AutoExpand.T, null);
		}
		
		if (autoExpand == null && entityTypeSignature != null) {
			autoExpand = gmSession.getModelAccessory().getMetaData().entityTypeSignature(entityTypeSignature).lenient(true).useCase(useCase).meta(AutoExpand.T)
					.exclusive();
		}
		
		if (autoExpand == null)
			return currentTC;
		
		TraversingCriterion depthTC = getDepthTC(autoExpand.getDepth());
		if (depthTC == null)
			return currentTC;
		
		if (currentTC == null)
			return depthTC;
		
		return join(currentTC, depthTC);
	}
	
	/**
	 * Joins two TC via a conjunction.
	 */
	public static TraversingCriterion join(TraversingCriterion currentTC, TraversingCriterion newTC) {
		if (currentTC == null)
			return newTC;
		
		return TC.create().conjunction().criterion(currentTC).criterion(newTC).close().done();
	}

	/**
	 * Gets a TC according to the given depth
	 * @param depth - For now it can be either "shallow", "reachable" or a number for the level.
	 */
	public static TraversingCriterion getDepthTC(String depth) {
		if (depth == null)
			return null;
		
		TraversingCriterion traversingCriterion = depthMap.get(depth);
		return traversingCriterion != null ? traversingCriterion : getLevelTC(depth);
	}
	
	private static TraversingCriterion getLevelTC(String depth) {
		try {
			int level = Integer.parseInt(depth);
			return getLevelTC(level);
		} catch (NumberFormatException ex) {
			logger.error("There is no known TC expert for the given depth: " + depth);
		}
		
		return null;
	}

	private static TraversingCriterion getLevelTC(int level) {
		TraversingCriterion shallow = getShallowTC();
		// @formatter:off
		return TC.create()
				.pattern() // pattern 1
					.recursion(level, level)
						.pattern() // pattern 2
							.entity()
							.disjunction() // disjunction 1
								.property()
								.pattern() // pattern 3
									.property()
									.disjunction() // disjunction 2
										.listElement()
										.setElement()
										.pattern().map().mapKey().close()
										.pattern().map().mapValue().close()
									.close() // disjunction 2
								.close() // pattern 3
							.close() // disjunction 1
						.close() // pattern 2
					.criterion(shallow)
				.close() // pattern 1
			.done();
		// @formatter:on
	}

	private static void prepareMap() {
		depthMap = new FastMap<>();
		
		depthMap.put("shallow", getShallowTC());
		depthMap.put("reachable", getReachableTC());
	}

	private static TraversingCriterion getReachableTC() {
		TraversingCriterion bean = TC.create().negation().joker().done();
		return bean;
	}

	private static TraversingCriterion getShallowTC() {
		PropertyTypeCriterion ptc = PropertyTypeCriterion.T.create();
		ptc.setTypes(new HashSet<TypeMatch>(Arrays.asList(EntityTypeMatch.T.create(), CollectionTypeMatch.T.create())));
		
		PatternCriterion bean = PatternCriterion.T.create();
		bean.setCriteria(Arrays.asList(com.braintribe.model.generic.pr.criteria.EntityCriterion.T.create(), ptc));
		return bean;
	}
	
}
