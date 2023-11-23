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
package tribefire.cortex.check.processing;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregatable;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregation;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregationKind;
import com.braintribe.model.deploymentapi.check.data.aggr.CheckBundleResult;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;

/**
 * Utility functions for check bundle processing.
 * 
 * @author christina.wilpernig
 */
public class CheckBundlesUtils {
	
	private static NavigableMap<Double,CheckWeight> msToWeight = new TreeMap<>();
	private static Map<CheckWeight,String> weightToPrettyDesc = new HashMap<>();
	
	static {
		msToWeight.put(100.0, CheckWeight.under100ms);
		msToWeight.put(1000.0, CheckWeight.under1s);
		msToWeight.put(10 * 1000.0, CheckWeight.under10s);
		msToWeight.put(60 * 1000.0, CheckWeight.under1m);
		msToWeight.put(10 * 60 * 1000.0, CheckWeight.under10m);
		msToWeight.put(60 * 60 * 1000.0, CheckWeight.under1h);
		msToWeight.put(10 * 60 * 60 * 1000.0, CheckWeight.under10h);
		msToWeight.put(24 * 60 * 60 * 1000.0, CheckWeight.under1d);
		msToWeight.put(7 * 24 * 60 * 60 * 1000.0, CheckWeight.under1w);
		msToWeight.put(Double.MAX_VALUE, CheckWeight.unlimited);
		
		weightToPrettyDesc.put(CheckWeight.under100ms, "< 100 ms");
		weightToPrettyDesc.put(CheckWeight.under1s, "< 1 s");
		weightToPrettyDesc.put(CheckWeight.under10s, "< 10 s");
		weightToPrettyDesc.put(CheckWeight.under1m, "< 1 m");
		weightToPrettyDesc.put(CheckWeight.under10m, "< 10 m");
		weightToPrettyDesc.put(CheckWeight.under1h, "< 1 h");
		weightToPrettyDesc.put(CheckWeight.under10h, "< 10 h");
		weightToPrettyDesc.put(CheckWeight.under1d, "< 1 d");
		weightToPrettyDesc.put(CheckWeight.under1w, "< 1 w");
		weightToPrettyDesc.put(CheckWeight.unlimited, "unlimited");
	}
	
	public static CheckStatus getStatus(Collection<CheckResult> results) {
		return results.stream() //
				.flatMap(c -> c.getEntries().stream()) //
				.map(CheckResultEntry::getCheckStatus) //
				.max(Comparator.naturalOrder()) //
				.orElse(CheckStatus.ok);
	}
	
	public static CheckStatus getStatus(CheckResult result) {
		return result.getEntries().stream() //
				.map(CheckResultEntry::getCheckStatus) //
				.max(Comparator.naturalOrder()) //
				.orElse(CheckStatus.ok);
	}
	
	public static Predicate<CheckBundle> buildBundleFilter(RunCheckBundles request) {
		Predicate<CheckBundle> bundleFilter = b -> true;
		
		// # Coverage
		Set<CheckCoverage> coverages = request.getCoverage();
		if (!coverages.isEmpty())
			bundleFilter = bundleFilter.and(b -> coverages.contains(b.getCoverage()));
		
		
		// # Weight
		CheckWeight weight = request.getWeight();
		if (weight != null)
			bundleFilter = bundleFilter.and(b -> b.getWeight().ordinal() <= weight.ordinal());
		
		
		// # Deployable
		Set<String> deployables = request.getDeployable();
		if(!deployables.isEmpty())
			bundleFilter = bundleFilter.and(b -> deployables.contains(b.getDeployable().getExternalId()));

		// # Label
		Set<String> labels = request.getLabel();
		Predicate<CheckBundle> labelFilter = b -> true;
		for (String l : labels)
			labelFilter = labelFilter.or(b -> b.getLabels().contains(l));
		
		bundleFilter = bundleFilter.and(labelFilter);
		
		
		// # Module
		Set<String> modules = request.getModule();
		if(!modules.isEmpty())
			bundleFilter = bundleFilter.and(b -> {
				Module module = b.getModule();
				if(module != null)
					return modules.contains(module.getGlobalId());
					
				return false;
			});

		
		// # Check Bundles
		Set<String> names = request.getName();
		if(!names.isEmpty())
			bundleFilter = bundleFilter.and(b -> names.contains(b.getName()));
		
		
		// # Roles
		Set<String> roles = request.getRole();
		Predicate<CheckBundle> rolesFilter = b -> true;
		for (String r : roles)
			rolesFilter = rolesFilter.or(b -> b.getRoles().contains(r));
		bundleFilter = bundleFilter.and(rolesFilter);
		
		
		// # Platform relevant
		Boolean isPlatformRelevant = request.getIsPlatformRelevant();
		if (isPlatformRelevant != null)
			bundleFilter = bundleFilter.and(b -> b.getIsPlatformRelevant() == isPlatformRelevant);
		
		return bundleFilter;
	}
	
	public static Function<CheckBundleResult, Collection<?>> getAccessor(CbrAggregationKind kind) {
		switch (kind) {
		case coverage: 			return r -> Collections.singleton(r.getCoverage());
		case deployable: 		return r -> Collections.singleton(r.getDeployable());
		case label: 			return r -> r.getLabels();
		case module: 			return r -> Collections.singleton(r.getModule());
		case node: 				return r -> Collections.singleton(r.getNode());
		case processor: 		return r -> Collections.singleton(r.getCheck());
		case status: 			return r -> Collections.singleton(r.getStatus());
		case weight: 			return r -> Collections.singleton(r.getWeight());
		case effectiveWeight: 	return r -> Collections.singleton(mapToWeight(r.getResult().getElapsedTimeInMs()));
		case bundle: 			return r -> Collections.singleton(r.getName());
		case role: 				return r -> r.getRoles();
		default:
			throw new IllegalStateException("Unknown CbrAggregationKind: " + kind);
		}
	}

	public static CheckWeight mapToWeight(double elapsedTimeInMs) {
		return msToWeight.tailMap(elapsedTimeInMs, false).firstEntry().getValue();
	}
	
	public static String getIdentification(CbrAggregatable a) {
		if (a.isResult())
			return ((CheckBundleResult)a).getName();
		
		CbrAggregation aggr = (CbrAggregation)a;
		CbrAggregationKind kind = aggr.getKind();
		Object d = aggr.getDiscriminator();
		
		switch (kind) {
			case bundle:
				return (String)d;
			case coverage:
				return ((CheckCoverage)d).name();
			case deployable:
				return ((Deployable)d).getName();
			case label:
				return (String)d;
			case module:
				return ((Module)d).getName();
			case node:
				return (String)d;
			case processor:
				return((CheckProcessor)d).getName();
			case role:
				return (String)d;
			case status:
				return ((CheckStatus)d).name();
			case effectiveWeight:
				return weightToPrettyDesc.get((CheckWeight)d);
			case weight:
				return ((CheckWeight)d).name();
			default:
				throw new IllegalStateException("Unknown CheckAggregationKind: " + kind);
		}
	}
}
