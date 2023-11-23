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

import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregation;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregationKind;
import com.braintribe.model.deploymentapi.check.data.aggr.CheckBundleResult;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;

public class CbmResultUtils {

	public static CheckBundlesResponse noResultsOk() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);
		return r;
	}
	
	public static CheckBundlesResponse flatResultsOk() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);
		
		CheckBundleResult cbr1 = bundleResult1();
		CheckBundleResult cbr2 = bundleResult2();
		CheckBundleResult cbr3 = bundleResult3();
		
		r.getElements().add(cbr1);
		r.getElements().add(cbr2);
		r.getElements().add(cbr3);
		
		return r;
	}

	// CheckResults
	private static CheckResult checkResult1() {
		CheckResult r = CheckResult.T.create();
			r.getEntries().add(cre1());
			r.getEntries().add(cre2());
		return r;
	}
	
	private static CheckResult checkResult2() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(cre3());
		r.getEntries().add(cre4());
		return r;
	}
	
	private static CheckResult checkResult3() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(cre5());
		r.getEntries().add(cre6());
		return r;
	}
	
	// CheckResultEntries
	private static CheckResultEntry cre1() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE1 - This was a successful test.");
		e.setMessage("CRE1 - Successful test.");
		e.setName("CRE1 - Demo Test");
		
		return e;
	}
	private static CheckResultEntry cre2() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE2 - aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa+"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa+"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa+"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa+"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		e.setMessage("CRE2 - Successful test.");
		e.setName("CRE2 - Demo Test");
		
		return e;
	}
	
	private static CheckResultEntry cre3() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE3 - Successful details.");
		e.setMessage("CRE3 - Successful test.");
		e.setName("CRE3 Demo Test");
		
		return e;
	}
	private static CheckResultEntry cre4() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE4 - Successful detais loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooonger.");
		e.setMessage("CRE4 - Successful test.");
		e.setName("CRE4 Demo Test");
		
		return e;
	}
	
	private static CheckResultEntry cre5() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE5 - Successful detais loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooonger.");
		e.setMessage("CRE5 - Successful test.");
		e.setName("CRE5 Demo Test");
		
		return e;
	}
	
	private static CheckResultEntry cre6() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.ok);
		e.setDetails("CRE6 - Successful detais loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooonger.");
		e.setMessage("CRE6 - Successful test.");
		e.setName("CRE6 Demo Test");
		
		return e;
	}

	private static CheckResult warnCheckResult1() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(warnCheckResultEntry1());
		
		return r;
	}
	private static CheckResult warnCheckResult2() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(warnCheckResultEntry2());
		
		return r;
	}
	private static CheckResult warnCheckResult3() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(warnCheckResultEntry3());
		
		return r;
	}
	private static CheckResult failCheckResult1() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(failCheckResultEntry1());
		
		return r;
	}
	private static CheckResult failCheckResult2() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(failCheckResultEntry2());
		
		return r;
	}
	private static CheckResult unqualifiedFailCheckResult1() {
		CheckResult r = CheckResult.T.create();
		r.getEntries().add(unqualifiedFailCheckResultEntry1());
		
		return r;
	}
	
	private static CheckResultEntry warnCheckResultEntry1() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.warn);
		e.setDetails("Something went wrong.");
		e.setMessage("Warning all over the place.");
		e.setName("Integration Test");
		
		return e;
	}
	private static CheckResultEntry warnCheckResultEntry2() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.warn);
		e.setDetails("Something went wrong.");
		e.setMessage("Warning all over the place.");
		e.setName("Integration Test");
		
		return e;
	}
	private static CheckResultEntry warnCheckResultEntry3() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.warn);
		e.setDetails("Something went wrong.");
		e.setMessage("Warning all over the place.");
		e.setName("Integration Test");
		
		return e;
	}
	private static CheckResultEntry failCheckResultEntry1() {
		
		CheckResultEntry checkResultEntry = CheckResultEntry.T.create();
		
		checkResultEntry.setCheckStatus(CheckStatus.fail);
		
		checkResultEntry.setMessage("Evaluation of CheckProcessor failed.");
		checkResultEntry.setDetails("Stacktrace!");
		checkResultEntry.setName("Failing Demo Check");
		
		return checkResultEntry;
	}
	private static CheckResultEntry unqualifiedFailCheckResultEntry1() {
		
		CheckResultEntry checkResultEntry = CheckResultEntry.T.create();
		checkResultEntry.setCheckStatus(CheckStatus.fail);
		
		checkResultEntry.setMessage("Evaluation of CheckProcessor failed.");
		checkResultEntry.setDetails("Stacktrace!");
		checkResultEntry.setName("Failing Demo Check");
		
		return checkResultEntry;
	}
	private static CheckResultEntry failCheckResultEntry2() {
		CheckResultEntry e = CheckResultEntry.T.create();
		e.setCheckStatus(CheckStatus.fail);
		e.setDetails("Something went wrong.");
		e.setMessage("Warning all over the place.");
		e.setName("Integration Test");
		
		return e;
	}

	public static CheckBundlesResponse oneLevelAggregationOk() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);

		CbrAggregation nodeAggr = CbrAggregation.T.create();
		nodeAggr.setKind(CbrAggregationKind.node);
		nodeAggr.setDiscriminator("master@tf-abcdefghijklmnopqrstuvwxyz");
		nodeAggr.setStatus(CheckStatus.ok);
		
		CheckBundleResult cbr1 = bundleResult1();
		
		nodeAggr.getElements().add(cbr1);
		
		r.getElements().add(nodeAggr);
		
		return r;
	}
	
	public static CheckBundlesResponse twoLevelAggregationOk() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);
		
		CbrAggregation nodeAggr = CbrAggregation.T.create();
		nodeAggr.setKind(CbrAggregationKind.node);
		nodeAggr.setDiscriminator("master@tf-abcdefghijklmnopqrstuvwxyz");
		nodeAggr.setStatus(CheckStatus.ok);
		
		CbrAggregation bundleAggr = CbrAggregation.T.create();
		bundleAggr.setKind(CbrAggregationKind.bundle);
		bundleAggr.setDiscriminator("demo-bundle");
		bundleAggr.setStatus(CheckStatus.ok);
		
		
		CheckBundleResult cbr1 = bundleResult1();
		
		bundleAggr.getElements().add(cbr1);
		nodeAggr.getElements().add(bundleAggr);
		r.getElements().add(nodeAggr);
		
		return r;
	}
	
	public static CheckBundlesResponse threeLevelAggregationOk() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);
		
		CbrAggregation nodeAggr = CbrAggregation.T.create();
		nodeAggr.setKind(CbrAggregationKind.node);
		nodeAggr.setDiscriminator("master@tf-abcdefghijklmnopqrstuvwxyz");
		nodeAggr.setStatus(CheckStatus.ok);
		
		CbrAggregation bundleAggr = CbrAggregation.T.create();
		bundleAggr.setKind(CbrAggregationKind.bundle);
		bundleAggr.setDiscriminator("demo-bundle");
		bundleAggr.setStatus(CheckStatus.ok);
		
		CbrAggregation weightAggr = CbrAggregation.T.create();
		weightAggr.setKind(CbrAggregationKind.weight);
		weightAggr.setDiscriminator(CheckWeight.under1s);
		weightAggr.setStatus(CheckStatus.ok);
		
		
		
		CheckBundleResult cbr1 = bundleResult1();
		
		weightAggr.getElements().add(cbr1);
		bundleAggr.getElements().add(weightAggr);
		nodeAggr.getElements().add(bundleAggr);
		r.getElements().add(nodeAggr);
		
		return r;
	}
	
	public static CbrAggregation nodeAggr1() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.node);
		bean.setDiscriminator("master@tf-abcdefghijklmnopqrstuvwxyz");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation nodeAggr2() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.node);
		bean.setDiscriminator("tribefire-master@tf-1234567");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation nodeAggr3() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.node);
		bean.setDiscriminator("tribefire-master@tf-1234567");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	private static CbrAggregation warnNodeAggr1() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.node);
		bean.setDiscriminator("master@tf-tf-tf-tf-tf-tf");
		bean.setStatus(CheckStatus.warn);
		return bean;
	}
	private static CbrAggregation failNodeAggr1() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.node);
		bean.setDiscriminator("master@tf-tf-tf-tf-tf-tf");
		bean.setStatus(CheckStatus.fail);
		return bean;
	}
	
	public static CbrAggregation bundle1() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.bundle);
		bean.setDiscriminator("Demo Bundle 1");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation bundle2() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.bundle);
		bean.setDiscriminator("Demo Bundle 2");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation bundle3() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.bundle);
		bean.setDiscriminator("Demo Bundle 3");
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation weightLight() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.weight);
		bean.setDiscriminator(CheckWeight.under1s);
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation weightMedium() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.weight);
		bean.setDiscriminator(CheckWeight.under1m);
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation weightHeavy() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.weight);
		bean.setDiscriminator(CheckWeight.under1h);
		bean.setStatus(CheckStatus.ok);
		return bean;
	}

	public static CbrAggregation coverageVitality() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.coverage);
		bean.setDiscriminator(CheckCoverage.vitality);
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CbrAggregation weightConnectivity() {
		CbrAggregation bean = CbrAggregation.T.create();
		bean.setKind(CbrAggregationKind.coverage);
		bean.setDiscriminator(CheckCoverage.connectivity);
		bean.setStatus(CheckStatus.ok);
		return bean;
	}
	
	public static CheckProcessor checkProcessor1() {
		CheckProcessor bean = CheckProcessor.T.create();
		bean.setName("Demo Check Processor");
		return bean;
	}
	public static CheckBundleResult bundleResult1() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.ok);
		bean.setName("Demo Bundle 1");
		bean.setResult(checkResult1());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	
	public static CheckBundleResult bundleResult2() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.ok);
		bean.setName("Demo Bundle 2");
		bean.setResult(checkResult2());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	
	public static CheckBundleResult bundleResult3() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.ok);
		bean.setName("Demo Bundle 2");
		bean.setResult(checkResult3());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	
	public static CheckBundleResult warnBundleResult1() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.warn);
		bean.setName("Warn Test Bundle 1 ");
		bean.setResult(warnCheckResult1());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	public static CheckBundleResult warnBundleResult2() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.warn);
		bean.setName("Warn Test Bundle 2 ");
		bean.setResult(warnCheckResult2());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	public static CheckBundleResult warnBundleResult3() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.warn);
		bean.setName("Warn Test Bundle 3");
		bean.setResult(warnCheckResult3());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	public static CheckBundleResult failBundleResult1() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.fail);
		bean.setName("Fail Test Bundle 1");
		bean.setResult(failCheckResult1());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	public static CheckBundleResult failBundleResult2() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.fail);
		bean.setName("Fail Test Bundle 2");
		bean.setResult(failCheckResult2());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	public static CheckBundleResult failBundleUnqualified1() {
		CheckBundleResult bean = CheckBundleResult.T.create();
		bean.setStatus(CheckStatus.fail);
		bean.setName("Unqualified Fail Test Bundle 1");
		bean.setResult(unqualifiedFailCheckResult1());
		bean.setCheck(checkProcessor1());
		return bean;
	}
	
	public static CheckBundlesResponse fourLevelAggregationTwoNodesOk() {
		//Response
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.ok);
		
		// Assignments 
		CbrAggregation weightLight = weightLight();
		CbrAggregation weightMedium = weightMedium();
		CbrAggregation weightHeavy = weightHeavy();
		
		CheckBundleResult r1 = bundleResult1();
		CheckBundleResult r2 = bundleResult2();
		CheckBundleResult r3 = bundleResult3();
		
		weightLight.getElements().add(r1);
		weightMedium.getElements().add(r2);
		weightHeavy.getElements().add(r3);

		CbrAggregation bundle1 = bundle1();
		CbrAggregation bundle2 = bundle2();
		CbrAggregation bundle3 = bundle3();
		
		CbrAggregation coverageVitality1 = coverageVitality();
		coverageVitality1.getElements().add(bundle1);
		coverageVitality1.getElements().add(bundle2);
		
		CbrAggregation coverageVitality2 = coverageVitality();
		coverageVitality2.getElements().add(bundle3);
		
		bundle1.getElements().add(weightLight);
		bundle2.getElements().add(weightMedium);
		bundle3.getElements().add(weightHeavy);

		CbrAggregation na1 = nodeAggr1();
		CbrAggregation na2 = nodeAggr2();
		
		na1.getElements().add(coverageVitality1);
		na2.getElements().add(coverageVitality2);

		r.getElements().add(na1);
		r.getElements().add(na2);
		
		return r;
	}
	
	public static CheckBundlesResponse oneFailManyOk() {
		//Response
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.fail);
		
		// Assignments 
		CbrAggregation weightLight = weightLight();
		CbrAggregation weightMedium = weightMedium();
		CbrAggregation weightHeavy = weightHeavy();
		
		CheckBundleResult r1 = bundleResult1();
		CheckBundleResult r2 = bundleResult2();
		CheckBundleResult r3 = bundleResult3();
		
		weightLight.getElements().add(r1);
		weightMedium.getElements().add(r2);
		weightHeavy.getElements().add(r3);
		
		CbrAggregation bundle1 = bundle1();
		CbrAggregation bundle2 = bundle2();
		CbrAggregation bundle3 = bundle3();
		
		CbrAggregation coverageVitality1 = coverageVitality();
		coverageVitality1.getElements().add(bundle1);
		coverageVitality1.getElements().add(bundle2);
		
		CbrAggregation coverageVitality2 = coverageVitality();
		coverageVitality2.getElements().add(bundle3);
		
		bundle1.getElements().add(weightLight);
		bundle2.getElements().add(weightMedium);
		bundle3.getElements().add(weightHeavy);
		
		CbrAggregation na1 = nodeAggr1();
		CbrAggregation na2 = nodeAggr2();
		
		na1.setStatus(CheckStatus.fail);
		na1.getElements().add(failBundleUnqualified1());
		na1.getElements().add(coverageVitality1);
		na2.getElements().add(coverageVitality2);
		
		r.getElements().add(na1);
		r.getElements().add(na2);
		
		return r;
	}
	
	public static CheckBundlesResponse oneOkOneWarn() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.warn);
		
		CbrAggregation nodeAggr = CbrAggregation.T.create();
		nodeAggr.setKind(CbrAggregationKind.node);
		nodeAggr.setDiscriminator("master@tf-abcdefghijklmnopqrstuvwxyz");
		nodeAggr.setStatus(CheckStatus.warn);
		
		CheckBundleResult cbr1 = warnBundleResult1();
		
		CheckBundleResult cbr2 = bundleResult1();
		
		
		nodeAggr.getElements().add(cbr1);
		nodeAggr.getElements().add(cbr2);
		
		r.getElements().add(nodeAggr);
		
		return r;
	}

	public static CheckBundlesResponse multipleFailsAndWarns() {
		CheckBundlesResponse r = CheckBundlesResponse.T.create();
		r.setStatus(CheckStatus.fail);
		
		CbrAggregation na1 = warnNodeAggr1();
		CbrAggregation na2 = failNodeAggr1();
		CbrAggregation na3 = nodeAggr3();
		
		CheckBundleResult r1 = warnBundleResult1();
		CheckBundleResult r2 = warnBundleResult2();
		CheckBundleResult r3 = warnBundleResult3();
		CheckBundleResult r4 = failBundleResult1();
		CheckBundleResult r5 = failBundleResult2();
		
		
		CheckBundleResult bundleResult1 = bundleResult1();
		CheckBundleResult bundleResult2 = bundleResult2();
		CheckBundleResult bundleResult3 = bundleResult3();
		
		
		na1.getElements().add(r1);
		na1.getElements().add(r2);
		na1.getElements().add(r3);
		
		na2.getElements().add(r4);
		na2.getElements().add(r5);

		na3.getElements().add(bundleResult1);
		na3.getElements().add(bundleResult2);
		na3.getElements().add(bundleResult3);
		
		r.getElements().add(na2);
		r.getElements().add(na1);
		r.getElements().add(na3);
		
		return r;
	}

	// -----------------------------
	// Warnings
	// -----------------------------
	
	// -----------------------------
	// Fails
	// -----------------------------
	
}
