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
package tribefire.cortex.asset.resolving.ng.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContext;
import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContextBuilder;

public class BasicAssetResolutionContext implements AssetResolutionContext, AssetResolutionContextBuilder {

	private ManagedGmSession session = new BasicManagedGmSession();
	private DenotationMap<PlatformAssetNature, List<String>> natureParts;
	private boolean includeDocumentation;
	private boolean selectorFiltering;
	private boolean verboseOutput;
	private boolean runtime;
	private boolean designtime;
	private String stage;
	private final Set<String> tags = new HashSet<>();
	private boolean lenient = false;

	@Override
	public AssetResolutionContextBuilder session(ManagedGmSession session) {
		this.session = session;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder natureParts(DenotationMap<PlatformAssetNature, List<String>> natureParts) {
		this.natureParts = natureParts;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder selectorFiltering(boolean selectorFiltering) {
		this.selectorFiltering = selectorFiltering;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder includeDocumentation(boolean includeDocumentation) {
		this.includeDocumentation = includeDocumentation;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder verboseOutput(boolean verboseOutput) {
		this.verboseOutput = verboseOutput;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder runtime(boolean runtime) {
		this.runtime = runtime;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder designtime(boolean designtime) {
		this.designtime = designtime;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder stage(String stage) {
		this.stage = stage;
		return this;
	}

	@Override
	public AssetResolutionContextBuilder tags(Set<String> tags) {
		this.tags.addAll(tags);
		return this;
	}
	
	@Override
	public AssetResolutionContextBuilder lenient(boolean lenient) {
		this.lenient  = lenient;
		return this;
	}

	@Override
	public AssetResolutionContext done() {
		return this;
	}

	@Override
	public boolean isRuntime() {
		return runtime;
	}

	@Override
	public boolean isDesigntime() {
		return designtime;
	}

	@Override
	public String getStage() {
		return stage;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}
	
	@Override
	public boolean lenient() {
		return lenient;
	}

	@Override
	public ManagedGmSession session() {
		return session;
	}

	@Override
	public DenotationMap<PlatformAssetNature, List<String>> natureParts() {
		return natureParts;
	}

	@Override
	public boolean selectorFiltering() {
		return selectorFiltering;
	}

	@Override
	public boolean includeDocumentation() {
		return includeDocumentation;
	}

	@Override
	public boolean verboseOutput() {
		return verboseOutput;
	}

}
