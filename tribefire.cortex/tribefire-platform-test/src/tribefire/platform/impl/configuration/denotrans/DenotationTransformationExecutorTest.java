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
package tribefire.platform.impl.configuration.denotrans;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

import tribefire.module.api.DenotationEnricher;
import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationMorpher;

/**
 * Tests for {@link DenotationTransformationExecutor}
 * 
 * @author peter.gazdik
 */
public class DenotationTransformationExecutorTest extends AbstractDenotransTest {

	private DenotationTransformationExecutor executor;
	private ManagedGmSession session;
	private ManipulationComment startEntity;

	private static final String DENOTATION_ID = "DENOTATION_ID";

	@Before
	public void setup() {
		executor = new DenotationTransformationExecutor();
		executor.setTransformerRegistry(transformerRegistry);

		session = new BasicManagedGmSession();

		startEntity = session.create(ManipulationComment.T);
	}

	@Test
	public void emptyRegistry_NoEnrichingButWorks() throws Exception {
		ManipulationComment transformed = executor.transform(DENOTATION_ID, session, startEntity, ManipulationComment.T).get();

		assertThat(transformed).isSameAs(startEntity);
	}

	@Test
	public void typeMatch_OnlyEnriching() throws Exception {
		registerEnricher(commentAuthorSetter());

		ManipulationComment transformed = executor.transform(DENOTATION_ID, session, startEntity, ManipulationComment.T).get();

		assertThat(transformed).isSameAs(startEntity);
		assertThat(transformed.getAuthor()).isEqualTo("Author");
	}

	@Test
	public void singleMetamorphosis() throws Exception {
		registerMorpher(commentToGmEntityType());

		GmEntityType transformed = executor.transform(DENOTATION_ID, session, startEntity, GmEntityType.T).get();

		assertThat(transformed).isNotNull();
	}

	@Test
	public void enrichingAnd_singleMetamorphosis() throws Exception {
		registerEnricher(commentAuthorSetter());
		registerMorpher(commentToGmEntityType());

		GmEntityType transformed = executor.transform(DENOTATION_ID, session, startEntity, GmEntityType.T).get();

		assertThat(transformed).isNotNull();
		assertThat(transformed.getTypeSignature()).isEqualTo("Author");
	}

	//

	private DenotationEnricher<ManipulationComment> commentAuthorSetter() {
		return directEnricher("CommentAuthorSetter", ManipulationComment.T, (ctx, e) -> e.setAuthor("Author"));
	}

	//

	private DenotationMorpher<ManipulationComment, GmEntityType> commentToGmEntityType() {
		return directMorpher(ManipulationComment.T, GmEntityType.T, //
				(ctx, comment) -> {
					GmEntityType gmEntityType = ctx.create(GmEntityType.T);
					gmEntityType.setTypeSignature(comment.getAuthor());

					return gmEntityType;
				});
	}

	//

	private static final String FINAL_TYPE_SIGNATURE = "FINAL!";

	@Test
	public void complex() throws Exception {
		registerEnricher(genericEntity_DoNothing());
		registerMorpher(manipulation_To_EntityReference());
		registerEnricher(refEnricher_Step1());
		registerEnricher(refEnricher_Step2And_Final());
		registerEnricher(refEnricher_Step3());
		registerMorpher(enrityRef_To_GmEntityType());

		GmEntityType transformed = executor.transform(DENOTATION_ID, session, startEntity, GmEntityType.T).get();

		assertThat(transformed).isNotNull();
		assertThat(transformed.getTypeSignature()).isEqualTo(FINAL_TYPE_SIGNATURE);
	}

	private DenotationEnricher<?> genericEntity_DoNothing() {
		return directEnricher("GenericEntity_DoNothing", GenericEntity.T, (ctx, e) -> {/* DO NOTHING */});
	}

	// ManipulationComment -> EntityReference
	private DenotationMorpher<Manipulation, EntityReference> manipulation_To_EntityReference() {
		return directMorpher(Manipulation.T, EntityReference.T, (ctx, m) -> ctx.create(PersistentEntityReference.T));
	}

	// null -> STEP 1
	private DenotationEnricher<?> refEnricher_Step1() {
		return directEnricher("EntityReferenceEnricher_Step1", EntityReference.T, //
				(ctx, e) -> {
					if (e.getTypeSignature() != null)
						throw new IllegalStateException(
								"EntityReferene Transformation Starter should only be called with typeSignature being null, but it is: "
										+ e.getTypeSignature());

					e.setTypeSignature("STEP 1");
				});
	}

	// STEP 1 -> STEP 2
	// STEP 3 -> FINAL_TYPE_SIGNATURE
	private DenotationEnricher<?> refEnricher_Step2And_Final() {
		return DenotationEnricher.create("EntityReferenceEnricher_Step2_And_Final", EntityReference.T, (ctx, e) -> {
			String typeSignature = e.getTypeSignature();

			if (typeSignature == null)
				return DenotationEnrichmentResult.nothingYetButCallMeAgain();

			if (typeSignature.equals("STEP 1")) {
				e.setTypeSignature("STEP 2");
				return DenotationEnrichmentResult.somethingAndCallMeAgain(e, "Set typeSignature to STEP 2");
			}

			if (typeSignature.equals("STEP 3")) {
				e.setTypeSignature(FINAL_TYPE_SIGNATURE);
				return DenotationEnrichmentResult.allDone(e, "Set typeSignature to " + FINAL_TYPE_SIGNATURE);
			}

			throw new IllegalStateException(
					"EntityReferene TWO STEP ENRICHER should only be called with typeSignature being null or STEP 1 or STEP 3, but it is: "
							+ typeSignature);
		});
	}

	// STEP 2 -> STEP 3
	private DenotationEnricher<?> refEnricher_Step3() {
		return DenotationEnricher.create("EntityReferenceEnricher_Step3", EntityReference.T, (ctx, e) -> {
			String typeSignature = e.getTypeSignature();

			if (typeSignature == null || typeSignature.equals("STEP 1"))
				return DenotationEnrichmentResult.nothingYetButCallMeAgain();

			if (typeSignature.equals("STEP 2")) {
				e.setTypeSignature("STEP 3");
				return DenotationEnrichmentResult.allDone(e, "Set typeSignature to STEP 3");
			}

			throw new IllegalStateException(
					"EntityReferene Transformation Continuer should only be called with typeSignature being null, STEP 1 or STEP 2, but it is: "
							+ typeSignature);
		});
	}

	// EntityReference -> GmEntityType
	private DenotationMorpher<?, ?> enrityRef_To_GmEntityType() {
		return directMorpher(EntityReference.T, GmEntityType.T, //
				(ctx, e) -> {
					GmEntityType result = ctx.create(GmEntityType.T);
					result.setTypeSignature(e.getTypeSignature());

					return result;
				});
	}

	

}
