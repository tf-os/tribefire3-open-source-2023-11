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
package com.braintribe.model.jvm.reflection;

import static com.braintribe.utils.lcd.CollectionTools2.index;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.generic.reflection.AbstractGenericModelTypeReflection;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.Weavable;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysisException;
import com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesisException;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.processing.async.api.AsyncCallback;

public class JvmGenericModelTypeReflection extends AbstractGenericModelTypeReflection {
	private static Logger logger = Logger.getLogger(JvmGenericModelTypeReflection.class);
	private static JvmGenericModelTypeReflection instance;

	private JavaTypeAnalysis protoJta;
	private JavaTypeAnalysis jta;
	private ReentrantLock jtaLock = new ReentrantLock();
	private GenericModelTypeSynthesis gmts;
	private Map<String, Model> packagedModels;
	private ReentrantLock packagedModelsLock = new ReentrantLock();
	private Map<String, Model> typeSignatureToModel;

	public static JvmGenericModelTypeReflection getInstance() {
		if (instance == null)
			instance = new JvmGenericModelTypeReflection();

		return instance;
	}

	@Override
	public ProtoGmEntityType findProtoGmEntityType(String typeSignature) {
		Class<?> clazz = getClassForName(typeSignature, false);
		if (clazz == null)
			return null;

		if (!GenericEntity.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException(
					"Cannot find ProtoGmEntityType for [" + typeSignature + "]. Such class exists, but is not a GenericEntity.");

		return (ProtoGmEntityType) getProtoAnalysis().getProtoGmType(clazz);
	}

	@Override
	public void deploy(Weavable weavable) throws GmfException {
		getGenericModelTypeSynthesis().ensureModelTypes((ProtoGmMetaModel) weavable);
	}

	@Override
	public void deploy(Weavable weavable, AsyncCallback<Void> asyncCallback) {
		try {
			deploy(weavable);
			asyncCallback.onSuccess(null);

		} catch (GmfException e) {
			asyncCallback.onFailure(e);
		}
	}

	@Override
	public Object getItwClassLoader() {
		return getGenericModelTypeSynthesis().getItwClassLoader();
	}

	@Override
	protected <T extends GenericEntity> EntityType<T> createEntityType(Class<?> entityClass) throws GenericModelException {
		/* We use proto JTA here because we cannot be sure we can use the regular one. In case this method is called from within
		 * a class initializer of on of the meta-model types (e.g. if somebody started with doing GmIntegerType.T.create(), we
		 * must use the proto analysis. Since we cannot easily tell if that's the case, and proto types are probably faster than
		 * regular entities, we simply use always use proto JTA.) */

		/* The above is not entirely true. We can easily detect the first access to ITW, because it always starts with creating
		 * EntityType for GenericEntity (triggered by EntityBase.T static initialization). BUt we use proto analysis, because
		 * it's faster, ignores most MDs. */

		ProtoGmEntityType twEntityType = (ProtoGmEntityType) getProtoAnalysis().getProtoGmType(entityClass);
		return getGenericModelTypeSynthesis().ensureEntityType(twEntityType, false);
	}

	@Override
	protected Class<?> getClassForName(String qualifiedEntityTypeName, boolean require) throws GenericModelException {
		try {
			return Class.forName(qualifiedEntityTypeName, false, getClassLoader());

		} catch (ClassNotFoundException e) {
			if (require)
				throw new GenericModelException("error while create EntityType for qualifiedName " + qualifiedEntityTypeName, e);
		}

		return null;
	}

	@Override
	@Deprecated
	public EnumType getEnumType(String typeName, boolean require) {
		EnumType enumType = super.getEnumType(typeName);

		if (enumType != null)
			return enumType;

		try {
			Class<?> clazz = Class.forName(typeName, false, getClassLoader());
			return getEnumType((Class<? extends Enum<?>>) clazz);

		} catch (ClassNotFoundException e) {
			if (require)
				throw new GenericModelException("aquired custom type " + typeName + " not found", e);

			return null;
		}
	}

	@Override
	@Deprecated
	public EnumType getEnumType(String typeName) {
		return getEnumType(typeName, true);
	}

	@Override
	public <T extends GenericModelType> T getType(Type type) throws GenericModelException {
		T result = (T) javaTypeMap.get(type);
		if (result != null) {
			return result;
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type rawType = parameterizedType.getRawType();

			Class<?> rawClass = (Class<?>) rawType;

			if (Arrays.asList(List.class, Set.class, Map.class).contains(rawClass)) {
				Type typeArgs[] = parameterizedType.getActualTypeArguments();
				GenericModelType parameterization[] = new GenericModelType[typeArgs.length];

				for (int i = 0; i < typeArgs.length; i++) {
					try {
						Type typeParameter = typeArgs[i];
						parameterization[i] = getType(typeParameter);

					} catch (GenericModelException ignored) {
						throw new GenericModelException("Unsupported type: " + type);
					}
				}

				/* We are using the "lookup" method on purpose, as that does the lookup with a different key (CollectionTypeKey) than
				 * what we did now (Type), so it might actually find the instance, and if not, it registers it with that key... */
				T collectionType = (T) getCollectionType(rawClass, parameterization);
				javaTypeMap.put(type, collectionType);

				return collectionType;
			}

		} else if (type instanceof Class<?>) {
			// we know it's not a simple type, as that would have been retrieved from javaTypeMap
			Class<?> classType = (Class<?>) type;
			return createCustomType(classType);
		}

		throw new GenericModelException("Unsupported type: " + type);
	}

	@Override
	protected <T extends GenericModelType> T createCustomType(Class<?> classType) {
		if (classType.isEnum()) {
			return (T) deployEnumType((Class<? extends Enum<?>>) classType);

		} else if (GenericEntity.class.isAssignableFrom(classType)) {
			return (T) createEntityType((Class<?>) classType);

		} else {
			throw new GenericModelException("Incompatible GM class: " + classType + ". Only enums and sub-types of GenericEntity are allowed!");
		}
	}

	private JavaTypeAnalysis getProtoAnalysis() {
		if (protoJta == null)
			deployMetaModelTypesAndInitializeJta();

		return protoJta;
	}

	private JavaTypeAnalysis getJavaTypeAnalysis() {
		if (jta == null)
			deployMetaModelTypesAndInitializeJta();

		return jta;
	}

	private void deployMetaModelTypesAndInitializeJta() {
		if (jta != null)
			return;

		jtaLock.lock();
		try {
			if (jta != null)
				return;

			JavaTypeAnalysis protoAnalysis = new JavaTypeAnalysis();
			protoAnalysis.setProto(true);

			for (Class<? extends GenericEntity> clazz : EagerMetaModelTypes.instantiableJavaTypes())
				ensureProtoEntityType(protoAnalysis, clazz);

			protoJta = protoAnalysis;
			jta = new JavaTypeAnalysis();
		} finally {
			jtaLock.unlock();
		}
	}

	private GenericModelType ensureProtoEntityType(JavaTypeAnalysis protoAnalysis, Class<? extends GenericEntity> clazz) {
		try {
			ProtoGmType twType = protoAnalysis.getProtoGmType(clazz);
			return getGenericModelTypeSynthesis().ensureType(twType, false);

		} catch (GenericModelTypeSynthesisException | JavaTypeAnalysisException e) {
			throw new GenericModelException("Error while ensuring proto entity type: " + clazz.getName(), e);
		}
	}

	private GenericModelTypeSynthesis getGenericModelTypeSynthesis() {
		return gmts == null ? gmts = GenericModelTypeSynthesis.standardInstance() : gmts;
	}

	private static ThreadLocal<Stack<ClassLoader>> classLoaderStackThreadLocal = new ThreadLocal<>();

	public static ClassLoader getClassLoader() {
		Stack<ClassLoader> stack = classLoaderStackThreadLocal.get();

		if (stack != null && !stack.isEmpty()) {
			return stack.peek();
		} else {
			return JvmGenericModelTypeReflection.class.getClassLoader();
		}
	}

	public static void pushClassLoader(ClassLoader classLoader) {
		Stack<ClassLoader> stack = classLoaderStackThreadLocal.get();

		if (stack == null) {
			stack = new Stack<>();
			classLoaderStackThreadLocal.set(stack);
		}

		stack.push(classLoader);
	}

	public static ClassLoader popClassLoader() {
		Stack<ClassLoader> stack = classLoaderStackThreadLocal.get();
		return stack.pop();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isFromAnotherClassLoader(Class<?> entityOrEnumClass) {
		return entityOrEnumClass.getClassLoader() != GenericEntity.class.getClassLoader();
	}

	@Override
	public Model findModel(String modelName) {
		return getPackagedModelsMap().get(modelName);
	}

	@Override
	public Model getModelForType(String customTypeSignature) {
		if (typeSignatureToModel == null)
			typeSignatureToModel = indexModelsByTypeSignature();

		return typeSignatureToModel.get(customTypeSignature);
	}

	private Map<String, Model> indexModelsByTypeSignature() {
		return index((Collection<Model>) getPackagedModels()) //
				.byMany(m -> m.getModelArtifactDeclaration().getTypes()) //
				.unique();
	}

	@Override
	public Collection<? extends Model> getPackagedModels() {
		return getPackagedModelsMap().values();
	}

	private Map<String, Model> getPackagedModelsMap() {
		if (packagedModels == null) {
			initializePackagedModels();
		}
		return packagedModels;
	}

	private void initializePackagedModels() {
		if (packagedModels != null)
			return;

		packagedModelsLock.lock();
		try {
			if (packagedModels != null)
				return;

			Enumeration<URL> declarationUrls = null;
			try {
				declarationUrls = getClassLoader().getResources("model-declaration.xml");
			} catch (IOException e) {
				logger.error("Error while scanning classpath from model declarations", e);
				return;
			}

			ModelDeclarationRegistryImpl modelDeclarationRegistry = new ModelDeclarationRegistryImpl();
			LinkedHashSet<URL> originalModelResourceUrls = new LinkedHashSet<URL>();

			while (declarationUrls.hasMoreElements()) {
				URL url = declarationUrls.nextElement();
				originalModelResourceUrls.add(url);

				try (InputStream in = url.openStream()) {
					ModelDeclarationParser.parse(in, modelDeclarationRegistry);
				} catch (Exception e) {
					logger.error("Error while parsing model declaration from " + url, e);
				}
			}

			Map<String, Model> models = modelDeclarationRegistry.getModels();

			validateModels(originalModelResourceUrls, models.values());

			packagedModels = Collections.unmodifiableMap(models);
		} finally {
			packagedModelsLock.unlock();
		}
	}

	private void validateModels(Collection<URL> urls, Collection<Model> models) {
		StringJoiner sj = new StringJoiner("\n");

		for (Model model : models) {
			if (model.isRootModel()) {
				if (model.getModelArtifactDeclaration().getTypes().isEmpty())
					logInvalidRootModel(sj, model);

			} else if (model.getModelArtifactDeclaration().getDependencies().isEmpty()) {
				logInvalidNodeModel(sj, model, models);
			}
		}

		if (sj.length() > 0) {
			logger.error("[PACKAGED MODELS] Model validation failed:\n"
					+ "This means your model has no dependencies (or no types in case it's root-model). "
					+ "This usually happens when the 'model-declaration.xml' file for that model was not on the classpath. Tips:"
					+ "\n\tGiven artifact is a model. If it is not, maybe it shouldn't have been depended, or only as a functional dependency."
					+ "\n\tIf the problematic model is in your workspace, try cleaning the project.\n\tIf not in your workspace, check that the jar contains the xml.\n"
					+ "\nErrors:\n" + sj + "\n" + "URLs found: " + urls);
			throw new IllegalStateException("Model validation failed. See logs with [PACKAGED MODELS] prefix for more details. Errors:\n" + sj);
		} else {
			logger.info("[PACKAGED MODELS] Models from classpath were initialized. URLs found: " + urls);
		}
	}

	private void logInvalidRootModel(StringJoiner sj, Model model) {
		sj.add("[INVALID ROOT MODEL] no types found in: " + model.getModelArtifactDeclaration().getName());
	}

	private void logInvalidNodeModel(StringJoiner sj, Model model, Collection<Model> models) {
		sj.add("[INVALID MODEL] no dependencie found even though this is not a root-model: " + model.getModelArtifactDeclaration().getName()
				+ "\nThis model is depended by:\n" + findDependerNames(model, models));
	}

	private static String findDependerNames(Model model, Collection<Model> models) {
		ModelDeclaration md = model.getModelArtifactDeclaration();
		return models.stream() //
				.map(Model::getModelArtifactDeclaration) //
				.filter(m -> m.getDependencies().contains(md)) //
				.map(m -> m.getName()) //
				.collect(Collectors.joining("\n    "));
	}

	private class ModelDeclarationRegistryImpl implements ModelDeclarationRegistry {
		private final Map<String, Model> models = new HashMap<>();

		@Override
		public ModelDeclaration acquireModelDeclaration(String name) {
			Model model = models.get(name);

			if (model == null) {
				ModelDeclaration declaration = ModelDeclaration.T.create();
				declaration.setName(name);
				model = new ModelImpl(declaration);
				models.put(name, model);
			}

			return model.getModelArtifactDeclaration();
		}

		public Map<String, Model> getModels() {
			return models;
		}
	}

	private class ModelImpl implements Model {
		private final ModelDeclaration modelDeclaration;
		private Set<Class<?>> types = null;
		private ReentrantLock typesLock = new ReentrantLock();
		private GmMetaModel model;
		private ReentrantLock modelLock = new ReentrantLock();
		private List<Model> dependencies;
		private final boolean isRootModel;

		public ModelImpl(ModelDeclaration modelDeclaration) {
			this.modelDeclaration = modelDeclaration;
			this.isRootModel = rootModelName.equals(modelDeclaration.getName());
		}

		@Override
		public ModelDeclaration getModelArtifactDeclaration() {
			return modelDeclaration;
		}

		@Override
		public String globalId() {
			String declarationGid = modelDeclaration.getModelGlobalId();
			return declarationGid != null ? declarationGid : Model.super.globalId();
		}

		@Override
		public Collection<? extends Model> getDependencies() {
			if (dependencies == null) {
				List<ModelDeclaration> declDeps = modelDeclaration.getDependencies();
				List<Model> deps = new ArrayList<>(declDeps.size());
				for (ModelDeclaration dependency : declDeps) {
					deps.add(getModel(dependency.getName()));
				}
				dependencies = deps;
			}
			return dependencies;
		}

		@Override
		public GmMetaModel getMetaModel() {
			if (model != null) {
				return model;
			}

			modelLock.lock();
			try {
				if (model == null) {
					model = GmMetaModel.T.create(globalId());

					model.setName(modelDeclaration.getName());
					model.setVersion(modelDeclaration.getVersion());

					List<GmMetaModel> dependencies = model.getDependencies();
					for (Model classpathModel : getDependencies()) {
						GmMetaModel metaModel = classpathModel.getMetaModel();
						dependencies.add(metaModel);
					}

					Set<GmType> types = model.getTypes();

					if (isRootModel) {
						GmType gmBaseType = getGmType(Object.class, false);
						types.add(gmBaseType);
						gmBaseType.setDeclaringModel(model);

						for (SimpleType simpleType : SimpleTypes.TYPES_SIMPLE) {
							GmType gmSimpleType = getGmType(simpleType.getJavaType(), false);
							types.add(gmSimpleType);
							gmSimpleType.setDeclaringModel(model);
						}
					}

					for (Class<?> type : getDeclaredJavaTypes()) {
						GmType gmType = getGmType(type, true);

						if (gmType == null) {
							continue;
						}

						types.add(gmType);
						((GmCustomType) gmType).setDeclaringModel(model);
					}
				}

				return model;
			} finally {
				modelLock.unlock();
			}
		}

		private GmType getGmType(Class<?> type, boolean lenient) {
			try {
				return getJavaTypeAnalysis().getGmType(type);
			} catch (JavaTypeAnalysisException e) {
				String msg = "Error while building GmType from classpath for " + type;

				if (lenient) {
					logger.error(msg, e);
					return null;

				} else {
					throw new GenericModelException(msg, e);
				}
			}
		}

		@Override
		public boolean isRootModel() {
			return isRootModel;
		}

		@Override
		public Collection<Class<?>> getDeclaredJavaTypes() {
			if (types == null)
				loadDeclaredJavaTypes();

			return types;
		}

		private void loadDeclaredJavaTypes() {
			typesLock.lock();
			try {
				if (types != null) {
					return;
				}

				Set<Class<?>> _types = newSet();
				for (String type : modelDeclaration.getTypes()) {
					try {
						_types.add(Class.forName(type));
					} catch (ClassNotFoundException e) {
						logger.error("Declared type from model [" + modelDeclaration.getGroupId() + ":" + modelDeclaration.getArtifactId()
								+ "] declaration xml not found on classpath: " + type, e);
					}
				}

				types = _types;
			} finally {
				typesLock.unlock();
			}
		}

		@Override
		public String toString() {
			return "Model[" + name() + "]";
		}

	}

}
