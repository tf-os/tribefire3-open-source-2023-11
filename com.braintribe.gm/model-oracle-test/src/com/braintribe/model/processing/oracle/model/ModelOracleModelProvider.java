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
package com.braintribe.model.processing.oracle.model;

import static com.braintribe.model.util.meta.NewMetaModelGeneration.rootModel;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.oracle.model.basic.animal.Animal;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.model.processing.oracle.model.basic.fish.Fish;
import com.braintribe.model.processing.oracle.model.basic.fish.Goldfish;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.processing.oracle.model.basic.mammal.Husky;
import com.braintribe.model.processing.oracle.model.basic.mammal.Mammal;
import com.braintribe.model.processing.oracle.model.basic.mammal.PurebredDog;
import com.braintribe.model.processing.oracle.model.basic.mammal.Tiger;
import com.braintribe.model.processing.oracle.model.evaluable.GenericEntityEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.GenericEntityEvaluable2;
import com.braintribe.model.processing.oracle.model.evaluable.ListObjectEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.ListStringEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.MammalEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.MammalPetEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.ObjectEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.PetEvaluable;
import com.braintribe.model.processing.oracle.model.evaluable.StringEvaluable;
import com.braintribe.model.processing.oracle.model.extended.Farm;
import com.braintribe.model.processing.oracle.model.extended.Mutant;
import com.braintribe.model.processing.oracle.model.meta.DataOrigin;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class ModelOracleModelProvider implements ModelNames {

	// @formatter:off
	public static final Set<EntityType<?>> animalTypes =  CollectionTools2.<EntityType<?>>asSet (
			Animal.T
	);

	public static final Set<EntityType<?>> mammalTypes =  CollectionTools2.<EntityType<?>>asSet (
			Mammal.T, 
			Dog.T, 
			PurebredDog.T,
			Husky.T,
			Tiger.T
	);
	
	public static final Set<EntityType<?>> fishTypes =  CollectionTools2.<EntityType<?>>asSet (
			Fish.T,
			Goldfish.T
	);

	public static final Set<EntityType<?>> farmTypes =  CollectionTools2.<EntityType<?>>asSet (
			Mutant.T,
			Farm.T
	);

	public static final Set<EntityType<?>> evalTypes =  CollectionTools2.<EntityType<?>>asSet (
			GenericEntityEvaluable.T,
			GenericEntityEvaluable2.T,
			ListObjectEvaluable.T,
			ListStringEvaluable.T,
			MammalEvaluable.T,
			MammalPetEvaluable.T,
			ObjectEvaluable.T,
			PetEvaluable.T,
			StringEvaluable.T
	);
	// @formatter:on

	private static NewMetaModelGeneration mmg = new NewMetaModelGeneration(asList(rootModel())).withValidation();

	private static GmMetaModel serviceRequestModel = GMF.getTypeReflection().getModel("com.braintribe.gm:service-api-model").getMetaModel();

	private static GmMetaModel theFarmModel;
	private static GmMetaModel theEvalModel;

	private static ModelMetaDataEditor farmEditor;
	private static ModelMetaDataEditor mammalEditor;
	private static ModelMetaDataEditor fishEditor;
	private static ModelMetaDataEditor animalEditor;

	private static DataOrigin animalOrigin = origin(ANIMAL);
	private static DataOrigin mammalOrigin = origin(MAMMAL);
	private static DataOrigin fishOrigin = origin(FISH);
	private static DataOrigin farmOrigin = origin(FARM);

	public static GmMetaModel farmModel() {
		if (theFarmModel != null)
			return theFarmModel;

		GmMetaModel animalModel = mmg.buildMetaModel(ANIMAL_MODEL, animalTypes, asList(rootModel().getMetaModel()));
		GmMetaModel mammalModel = mmg.buildMetaModel(MAMMAL_MODEL, mammalTypes, asList(animalModel));
		GmMetaModel fishModel = mmg.buildMetaModel(FISH_MODEL, fishTypes, asList(animalModel));
		// we use animialModel here as dependency, to test that the animal model stuff is still at the end when sorted
		GmMetaModel farmModel = mmg.buildMetaModel(FARM_MODEL, farmTypes, asList(animalModel, mammalModel, fishModel));

		farmEditor = new BasicModelMetaDataEditor(farmModel);
		mammalEditor = new BasicModelMetaDataEditor(mammalModel);
		fishEditor = new BasicModelMetaDataEditor(fishModel);
		animalEditor = new BasicModelMetaDataEditor(animalModel);

		configureModelMetaData();
		configureEntityOverrides();
		configurePropertyOverrides();
		configureEnumOverrides();
		configureEnumConstantOverrides();

		return theFarmModel = farmModel;
	}

	/** Each model has one meta-data with it's own origin. */
	private static void configureModelMetaData() {
		animalEditor.addModelMetaData(animalOrigin);
		mammalEditor.addModelMetaData(mammalOrigin);
		fishEditor.addModelMetaData(fishOrigin);
		farmEditor.addModelMetaData(farmOrigin);
	}

	/** {@link Animal} is defined in AnimalModel and overridden in every sub-model, always with corresponding origin. */
	private static void configureEntityOverrides() {
		animalEditor.onEntityType(Animal.T).addMetaData(animalOrigin);

		mammalEditor.onEntityType(Animal.T).addMetaData(mammalOrigin);

		fishEditor.onEntityType(Animal.T).addMetaData(fishOrigin);

		farmEditor.onEntityType(Animal.T).addMetaData(farmOrigin);
		farmEditor.onEntityType(Dog.T).addMetaData(farmOrigin);
	}

	private static void configurePropertyOverrides() {
		animalEditor.onEntityType(Animal.T).addPropertyMetaData(GENDER, origin(ANIMAL, ANIMAL));

		mammalEditor.onEntityType(Animal.T).addPropertyMetaData(GENDER, origin(MAMMAL, ANIMAL));
		mammalEditor.onEntityType(Mammal.T).addPropertyMetaData(GENDER, origin(MAMMAL, MAMMAL));
		mammalEditor.onEntityType(Dog.T).addPropertyMetaData(GENDER, origin(MAMMAL, DOG));

		fishEditor.onEntityType(Animal.T).addPropertyMetaData(GENDER, origin(FISH, ANIMAL));
		fishEditor.onEntityType(Fish.T).addPropertyMetaData(GENDER, origin(FISH, FISH));

		farmEditor.onEntityType(Animal.T).addPropertyMetaData(GENDER, origin(FARM, ANIMAL));
		farmEditor.onEntityType(Mutant.T).addPropertyMetaData(GENDER, origin(FARM, MUTANT));
	}

	/** {@link Gender} is defined in AnimalModel and overridden in every sub-model, always with corresponding origin. */
	private static void configureEnumOverrides() {
		animalEditor.onEnumType(Gender.class).addMetaData(animalOrigin);

		mammalEditor.onEnumType(Gender.class).addMetaData(mammalOrigin);

		fishEditor.onEnumType(Gender.class).addMetaData(fishOrigin);

		farmEditor.onEnumType(Gender.class).addMetaData(farmOrigin);
	}

	private static void configureEnumConstantOverrides() {
		animalEditor.addConstantMetaData(Gender.M, animalOrigin);

		mammalEditor.addConstantMetaData(Gender.M, mammalOrigin);

		fishEditor.addConstantMetaData(Gender.M, fishOrigin);

		farmEditor.addConstantMetaData(Gender.M, farmOrigin);
	}

	private static DataOrigin origin(String originName) {
		DataOrigin result = DataOrigin.T.create();
		result.setOriginName(originName);

		return result;
	}

	private static DataOrigin origin(String modelName, String typeSignature) {
		DataOrigin result = DataOrigin.T.create();
		result.setOriginName(originName(modelName, typeSignature));

		return result;
	}

	public static String originName(String modelName, String typeSignature) {
		return modelName + "-" + typeSignature;
	}

	public static GmMetaModel evalModel() {
		if (theEvalModel != null)
			return theEvalModel;
		else
			return theEvalModel = mmg.buildMetaModel(EVAL_MODEL, evalTypes, asList(serviceRequestModel, farmModel()));
	}

}
