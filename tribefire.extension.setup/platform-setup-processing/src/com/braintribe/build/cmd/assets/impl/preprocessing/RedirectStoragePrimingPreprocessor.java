package com.braintribe.build.cmd.assets.impl.preprocessing;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.build.cmd.assets.api.preprocessing.PlatformAssetPreprocessor;
import com.braintribe.build.cmd.assets.api.preprocessing.PlatformAssetPreprocessorContext;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.AccessIds;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.RedirectableStoragePriming;
import com.braintribe.model.asset.preprocessing.RedirectStoragePriming;

public class RedirectStoragePrimingPreprocessor implements PlatformAssetPreprocessor<RedirectStoragePriming> {

	@Override
	public void process(PlatformAssetPreprocessorContext context, PlatformAsset asset, RedirectStoragePriming preprocessing) {
		Set<PlatformAsset> candidates = new HashSet<>();
		
		String accessId = preprocessing.getSourceAccessId();
		collectCandidates(asset, candidates, accessId);
		
		for (PlatformAsset candidate: candidates) {
			RedirectableStoragePriming redirectableStoragePriming = (RedirectableStoragePriming)candidate.getNature();
			
			redirectableStoragePriming.getRedirects().compute(accessId, (k,v) -> {
				if (v == null)
					v = AccessIds.T.create();
				
				v.getAccessIds().addAll(preprocessing.getTargetAccessIds());

				return v;
			});

			candidate.setIsContextualized(true);
		}
	}

	private void collectCandidates(PlatformAsset asset, Set<PlatformAsset> candidates, String accessId) {
		for (PlatformAssetDependency dependency: asset.getQualifiedDependencies()) {
			PlatformAsset dependedAsset = dependency.getAsset();
			PlatformAssetNature nature = dependedAsset.getNature();
			
			if (nature instanceof RedirectableStoragePriming) {
				RedirectableStoragePriming redirectableStoragePriming = (RedirectableStoragePriming) nature;
				
				boolean configured = redirectableStoragePriming.configuredAccesses().filter(accessId::equals).findFirst().isPresent();
				
				if (configured) {
					candidates.add(dependedAsset);
				}
			}
			
			collectCandidates(dependedAsset, candidates, accessId);
		}
	}
}