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
package com.braintribe.model.processing.meta.cmd.context;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.ResolutionContextInfo;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.scope.ScopeUtils;
import com.braintribe.model.processing.meta.cmd.tools.CmdTools;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * Holds all information for given {@link CmdResolverImpl}.
 * <p>
 * This class must be immutable.
 */
public class ResolutionContext {

	public final ModelOracle modelOracle;
	public final MdSelectorResolverImpl mdSelectorResolver;

	private final StaticContext staticContext;
	private final Supplier<?> sessionProvider; // nullable
	private final ScopeUtils scopeUtils = new ScopeUtils();
	private final int maxSessionCacheSize;

	private Map<EntityType<? extends MetaData>, MetaData> defaultMetaData = Collections.emptyMap();


	public ResolutionContext(ResolutionContextInfo rci) {
		this.modelOracle = rci.getModelOracle();
		this.staticContext = new StaticContext(modelOracle, rci.getStaticAspects());
		this.sessionProvider = rci.getSessionProvider();
		this.maxSessionCacheSize = rci.getMaxSessionCacheSize();

		this.mdSelectorResolver = new MdSelectorResolverImpl(rci.getExperts());

		setDefaultMetaData(rci.getDefaultMetaData());
	}

	/** {@link CmdResolverImpl#setSingleSession(boolean)} */
	public void setSigleSession(boolean singleSession) {
		scopeUtils.setSingleSession(singleSession);
	}

	public ScopeUtils getScopeUtils() {
		return scopeUtils;
	}

	public Supplier<?> getSessionProvider() {
		return sessionProvider;
	}

	public void setSuppressInconsistencies(boolean shouldSuppress) {
		mdSelectorResolver.setSuppressInconsistencies(shouldSuppress);
	}

	public int getMaxSessionCacheSize() {
		return maxSessionCacheSize;
	}

	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(MetaDataSelector mds) {
		return mdSelectorResolver.getRelevantAspects(mds);
	}

	/** Similar to {@link #filterBySelectors(List, SelectorContext)} but stops on first match. */
	public MetaData filterFirstBySelectors(List<QualifiedMetaData> qmds, SelectorContext selectorContext) {
		for (QualifiedMetaData qmd : qmds) {
			MetaData md = qmd.metaData();
			if (selectorIsActive(md.getSelector(), selectorContext))
				return md;
		}

		return null;
	}

	/**
	 * From given {@code metaData} filters all the matching ones (those whose {@link MetaDataSelector}s are evaluated with {@code true} using given
	 * {@code selectorContext}
	 */
	public List<MetaData> filterBySelectors(List<QualifiedMetaData> metaData, SelectorContext selectorContext) {
		List<MetaData> result = newList();

		for (QualifiedMetaData qmd : metaData) {
			MetaData md = qmd.metaData();
			if (selectorIsActive(md.getSelector(), selectorContext))
				result.add(md);
		}

		return result;
	}

	private boolean selectorIsActive(MetaDataSelector mds, SelectorContext context) {
		if (mds == null)
			return true;

		return ((ExtendedSelectorContext) context).shouldIgnoreSelectors() ? //
				!Boolean.FALSE.equals(mdSelectorResolver.maybeMatches(mds, context)) : mdSelectorResolver.matches(mds, context);
	}

	/**
	 * Similar to {@link #filterBySelectors(List, SelectorContext)} but does not use the {@link SelectorContext}. This method is used during cache
	 * initialization (before any actual context-related query is evaluated), to remove all the {@link MetaData} which are always inactive.
	 */
	public List<QualifiedMetaData> filterByStaticSelectors(List<QualifiedMetaData> staticValue) {
		List<QualifiedMetaData> result = newList();

		try {
			for (QualifiedMetaData qmd : staticValue)
				if (isSelectorStaticallyActiveOrUnknown(qmd.metaData()))
					result.add(qmd);

		} catch (CascadingMetaDataException e) {
			throw new RuntimeException("Problem while filtering meta data by static selectors.", e);
		}

		return result;
	}

	/**
	 * This method filters selectors which are never active (unknown means we cannot evaluate them just from static context, so we consider them same
	 * as active here).
	 */
	private boolean isSelectorStaticallyActiveOrUnknown(MetaData md) {
		MetaDataSelector selector = md.getSelector();

		if (selector == null)
			return true;

		Collection<Class<? extends SelectorContextAspect<?>>> relevantAspects = mdSelectorResolver.getRelevantAspects(selector);

		if (!staticContext.containsAllAspects(relevantAspects))
			// selector cannot be evaluated right now -> selector is unknown -> result is true
			return true;

		try {
			return mdSelectorResolver.matches(selector, staticContext);

		} catch (Exception e) {
			throw new CascadingMetaDataException("Error while evaluating static selector: " + selector.entityType().getTypeSignature(), e);
		}
	}

	public SelectorExpert<?> findExpertFor(EntityType<? extends MetaDataSelector> et) {
		return mdSelectorResolver.findExpertFor(et);
	}

	public void setDefaultMetaData(Set<? extends MetaData> defaultMetaData) {
		this.defaultMetaData = CmdTools.indexByEntityType(defaultMetaData);
	}

	public <T extends MetaData> T getDefaultValue(EntityType<T> metaDataType) {
		MetaData result = defaultMetaData.get(metaDataType);
		if (!metaDataType.isValueAssignable(result))
			throw new CascadingMetaDataException("Wrong type of default meta-data. Requested type: " + metaDataType.getTypeSignature()
					+ ", actual instance has type: " + result.entityType().getTypeSignature());

		return (T) result;
	}

}
