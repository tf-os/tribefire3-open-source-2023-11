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
package com.braintribe.model.access.security.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.security.InterceptorData;
import com.braintribe.model.access.security.testdata.manipulation.SecurityAspectManipulationTestModel;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.NormalizedCompoundManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.UseCaseAspect;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.security.SecurityAspectException;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;
import com.braintribe.model.processing.security.manipulation.ValidationResult;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.auth.BasicSessionAuthorization;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.test.impl.session.TestModelAccessory;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.utils.lcd.SetTools;

/**
 * Base class for different {@link ManipulationSecurityExpert} tests.
 */
public abstract class ValidatorTestBase {

	private static final GmMetaModel metaModel = SecurityAspectManipulationTestModel.enriched();
	private static final ModelOracle modelOracle = new BasicModelOracle(metaModel);

	private CmdResolver cmdResolver;
	protected BasicPersistenceGmSession session;
	protected BasicSessionAuthorization sessionAuthorization;
	protected CompoundManipulation recordedManipulation;
	protected Validator validator;
	protected ValidationResult validationResult;
	protected ManipulationDriver manipulationDriver;

	@Before
	public void setUp() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(metaModel);

		manipulationDriver = new ManipulationDriver(smood);

		sessionAuthorization = new BasicSessionAuthorization();
		sessionAuthorization.setUserRoles(newSet());

		cmdResolver = CmdResolverImpl.create(modelOracle) //
				.addStaticAspect(UseCaseAspect.class, SetTools.asSet("access")) //
				.addDynamicAspectProvider(RoleAspect.class, sessionAuthorization::getUserRoles) //
				.done();

		session = manipulationDriver.newSession();
		session.setModelAccessory(new TestModelAccessory(cmdResolver));
		session.setSessionAuthorization(sessionAuthorization);

		validator = new Validator(new InterceptorData(null, manipulationSecurityExperts(), null));
	}

	protected abstract Set<? extends ManipulationSecurityExpert> manipulationSecurityExperts();

	protected void setUserRoles(String... roles) {
		Set<String> rs = sessionAuthorization.getUserRoles();

		rs.clear();
		rs.addAll(asSet(roles));
	}

	protected void setUserName(String name) {
		sessionAuthorization.setUserName(name);
	}

	protected void commit() {
		session.commit();
	}

	/**
	 * Runs the validation of manipulations generated during {@code r.run()}. The {@link ValidationResult} is stored as
	 * instance variable and may be checked by using some of the "assert" methods (like {@link #assertOk()},
	 * {@link #assertNumberOfErrors(int)}
	 */
	protected void validate(Runnable r) {
		record(r);

		validate();
	}

	protected void record(Runnable r) {
		recordedManipulation = manipulationDriver.dryRun(r, session);
	}

	protected void appendManipulation(Manipulation m) {
		recordedManipulation.getCompoundManipulationList().add(m);
	}

	protected void validate() throws SecurityAspectException {
		validationResult = validator.validate(normalizedRecordedManipulations(), session);
	}

	protected NormalizedCompoundManipulation normalizedRecordedManipulations() {
		try {
			return Normalizer.normalize(recordedManipulation);
		} catch (Exception e) {
			throw new RuntimeException("PROBLEM WITH TEST. Normalization failed.", e);
		}
	}

	/** Checks that there were no errors during validation. */
	protected void assertOk() {
		Collection<SecurityViolationEntry> violationEntries = validationResult.getViolationEntries();
		assertThat(violationEntries).isNullOrEmpty();
	}

	protected void assertNumberOfErrors(int size) {
		if (size < 1)
			throw new IllegalArgumentException("Expected number of errors must be a strictly positive number.");

		Collection<SecurityViolationEntry> violationEntries = validationResult.getViolationEntries();
		assertThat(violationEntries).as("Validation should have FAILED.").isNotNull().isNotEmpty().hasSize(size);
	}

	/** Checks that all errors corresponds to given type and property. */
	protected void assertErrors(EntityType<?> et, String propertyName) {
		for (SecurityViolationEntry entry : validationResult.getViolationEntries())
			assertError(entry, et, propertyName);
	}

	/** Checks that given errors corresponds to given type and property. */
	protected void assertError(SecurityViolationEntry entry, EntityType<?> et, String propertyName) {
		EntityReference reference = entry.getEntityReference();
		assertThat(reference.getTypeSignature()).as("Wrong type signature!").isEqualTo(et.getTypeSignature());
		assertThat(entry.getPropertyName()).as("Wrong property name!").isEqualTo(propertyName);
	}

}
