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
package tribefire.extension.modelbrowser.servelet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.TypeOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.api.MinimalStack;
import com.braintribe.utils.lcd.ReflectionTools;
import com.braintribe.utils.lcd.StringTools;

/**
 * 
 * @author Dirk Scheffler
 *
 */

public class ModelBrowser extends HttpServlet {

	private static final Logger logger = Logger.getLogger(ModelBrowser.class);

	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private URL resourceBaseUrl;
	private ModelAccessoryFactory modelAccessoryFactory;
	private Set<String> requiredRoles;
	private MinimalStack<UserSession> userSessionStack;

	@Required
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	@Required
	public void setResourceBaseUrl(URL resourceBaseUrl) {
		this.resourceBaseUrl = resourceBaseUrl;
	}

	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Configurable
	public void setRequiredRoles(Set<String> requiredRoles) {
		this.requiredRoles = requiredRoles;
	}

	@Required
	public void setUserSessionStack(MinimalStack<UserSession> userSessionStack) {
		this.userSessionStack = userSessionStack;
	}

	private String getLoginUrl() {
		String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();
		if (StringTools.isBlank(publicServicesUrl)) {
			return null;
		}
		String relativeSignInPath = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_TRIBEFIRE_WEB_LOGIN_RELATIVE_PATH);
		if (StringTools.isBlank(relativeSignInPath)) {
			return publicServicesUrl;
		}
		if (!publicServicesUrl.endsWith("/") && !relativeSignInPath.startsWith("/")) {
			publicServicesUrl += "/";
		}
		return publicServicesUrl + relativeSignInPath;
	}

	private boolean isAccessAllowed() {
		if (requiredRoles == null || requiredRoles.isEmpty()) {
			return true;
		}
		UserSession userSession = userSessionStack.peek();
		if (userSession == null) {
			logger.debug(() -> "User is not authenticated but requires one of the roles: " + requiredRoles);
			return false;
		}
		Set<String> effectiveRoles = userSession.getEffectiveRoles();
		Set<String> intersection = new HashSet<>(requiredRoles);
		intersection.retainAll(effectiveRoles);
		if (intersection.isEmpty()) {
			logger.debug(() -> "User " + userSession.getUser().getName() + " has roles " + effectiveRoles + ", but requires one of " + requiredRoles);
			return false;
		}
		return true;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!isAccessAllowed()) {
			String loginUrl = getLoginUrl();
			if (StringTools.isBlank(loginUrl)) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				resp.sendRedirect(loginUrl);
			}
			return;
		}

		String model = req.getParameter("model");

		String typeSignature = req.getParameter("type");

		String search = Optional.ofNullable(req.getParameter("search")).orElse("");

		resp.setContentType("text/html");

		PrintWriter writer = resp.getWriter();
		writer.println("<head>");
		writer.println("<link href=\"https://fonts.googleapis.com/css2?family=Roboto&display=swap\" rel=\"stylesheet\">");
		writer.println("<style>");
		try (Reader reader = new InputStreamReader(new URL(resourceBaseUrl, "styles.css").openStream(), "UTF-8")) {
			String styles = IOTools.slurp(reader);
			writer.print(styles);
		}
		writer.println("</style>");
		writer.println("</head>");

		ModelContextualBrowsing contextualBrowsing = new ModelContextualBrowsing();

		contextualBrowsing.session = cortexSessionProvider.get();
		contextualBrowsing.writer = writer;
		contextualBrowsing.search = buildCamelCaseExpansionFilter(search);
		contextualBrowsing.searchText = search;

		if (model != null) {
			ModelAccessory modelAccessory = modelAccessoryFactory.getForModel(model);
			ModelOracle modelOracle = modelAccessory.getOracle();
			contextualBrowsing.modelAccessory = modelAccessory;
			contextualBrowsing.model = modelAccessory.getModel();
			contextualBrowsing.modelOracle = modelOracle;

			if (typeSignature != null && !typeSignature.isEmpty()) {
				TypeOracle typeOracle = modelAccessory.getOracle().findTypeOracle(typeSignature);

				if (typeOracle instanceof EnumTypeOracle) {
					EnumTypeOracle enumTypeOracle = (EnumTypeOracle) typeOracle;
					contextualBrowsing.enumTypeOracle = enumTypeOracle;
					contextualBrowsing.enumType = enumTypeOracle.asGmEnumType();
				} else if (typeOracle instanceof EntityTypeOracle) {
					EntityTypeOracle entityTypeOracle = (EntityTypeOracle) typeOracle;
					contextualBrowsing.entityTypeOracle = entityTypeOracle;
					contextualBrowsing.entityType = entityTypeOracle.asGmEntityType();
				}
			}
		}

		contextualBrowsing.render();
	}

	private Predicate<String> buildCamelCaseExpansionFilter(String search) {
		String lowerCasedSearch = search.toLowerCase();

		if (lowerCasedSearch.equals(search)) {
			StringBuilder builder = new StringBuilder();
			builder.append(".*");
			builder.append(search);
			builder.append(".*");

			Pattern pattern = Pattern.compile(builder.toString());
			return s -> pattern.matcher(s.toLowerCase()).find();
		} else {
			List<String> parts = StringTools.splitCamelCase(search);
			StringBuilder builder1 = new StringBuilder();

			builder1.append(".*");

			builder1.append(parts.stream().collect(Collectors.joining("[a-z0-9_\\-]*")));

			builder1.append(".*");

			Pattern pattern1 = Pattern.compile(builder1.toString());

			StringBuilder builder2 = new StringBuilder();

			builder2.append(parts.stream().map(String::toLowerCase).collect(Collectors.joining("[^-]*-")));

			builder2.append(".*");

			Pattern pattern2 = Pattern.compile(builder2.toString());

			return s -> pattern1.matcher(s).find() || pattern2.matcher(s).find();
		}
	}

	private static GmMetaModel findModel(GmMetaModel model, String name) {
		Map<String, GmMetaModel> models = new HashMap<>();

		collectModel(model, models);

		return models.get(name);
	}

	private static void collectModel(GmMetaModel model, Map<String, GmMetaModel> models) {
		if (models.put(model.getName(), model) != null)
			return;

		for (GmMetaModel dep : model.getDependencies()) {
			collectModel(dep, models);
		}
	}

	private class ModelContextualBrowsing {
		private GmEnumType enumType;
		private ModelAccessory modelAccessory;
		private GmMetaModel model;
		private ModelOracle modelOracle;
		private EntityTypeOracle entityTypeOracle;
		private GmEntityType entityType;
		private EnumTypeOracle enumTypeOracle;
		private PrintWriter writer;
		private PersistenceGmSession session;
		private Predicate<String> search;
		private String searchText;

		private void renderModel() throws IOException {
			Pair<String, String> splitModelName = splitModelName(model.getName());
			String name = splitModelName.first();
			String groupId = splitModelName.second();

			writer.println("<table class='header'");
			writer.print("<tr>");
			writer.print("<td class='property-name main-subject'>");
			writer.print("Model");
			writer.print("</td>");
			writer.print("<td class='main-subject'>");
			writer.print(name);
			writer.print("</td>");
			writer.print("</tr>");
			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("GroupId");
			writer.print("</td>");
			writer.print("<td>");
			writer.print(groupId);
			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Direct Model Dependencies");
			writer.print("</td>");
			writer.print("<td>");
			boolean firstX = true;
			for (GmMetaModel modelDependency : model.getDependencies()) {
				if (firstX)
					firstX = false;
				else
					writer.print(", ");
				renderModel(modelDependency);
			}
			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Model Dependencies");
			writer.print("</td>");
			writer.print("<td>");

			firstX = true;

			List<GmMetaModel> modelDependencies = getModelDependencies(model);

			for (GmMetaModel modelDependency : modelDependencies) {
				if (firstX)
					firstX = false;
				else
					writer.print(", ");
				renderModel(modelDependency);
			}
			writer.print("</td>");
			writer.print("</tr>");

			writer.println("</table><br/>");

			writer.println("<form method='GET'>");
			writer.println("<input type='hidden' name='model' value='" + model.getName() + "'/>");
			writer.println("<span class='minor-text'>Filter</span> <input autofocus name='search' value='" + searchText + "'/>");
			writer.println("</form>");

			Predicate<GmCustomType> filter = t -> search.test(ReflectionTools.getSimpleName(t.getTypeSignature()));

			List<GmCustomType> types = modelOracle.getTypes() //
					.onlyDeclared() //
					.asGmTypes() //
					.filter(filter) //
					.sorted(Comparator.comparing(t -> ReflectionTools.getSimpleName(t.getTypeSignature()))) //
					.collect(Collectors.toList());

			writer.println("<table class='property-table'>");
			if (!types.isEmpty()) {

				writer.println("<tr><th>Declared Type</th><th>Namespace</th></tr>");
				for (GmType type : types) {
					String typeSignature = type.getTypeSignature();
					writer.println("<tr>");
					Pair<String, String> nameParts = splitTypeName(typeSignature);
					String namespace = nameParts.second();

					writer.print("<td class='type-col'>");
					renderType(type);
					writer.print("</td>");
					writer.print("<td class='namespace-col'>");
					writer.print(namespace);
					writer.print("</td>");
					writer.println("</tr>");
				}
			}

			List<GmCustomType> inheritedTypes = modelOracle.getTypes() //
					.onlyInherited() //
					.asGmTypes() //
					.filter(filter) //
					.sorted(Comparator.comparing(t -> ReflectionTools.getSimpleName(t.getTypeSignature()))) //
					.collect(Collectors.toList());

			if (!inheritedTypes.isEmpty()) {
				if (!types.isEmpty())
					writer.println("<tr class='spacer-row'></tr>");
				writer.println("<tr><th>Inherited Type</th><th>Namespace</th><th>Declaring Model</th></tr>");
				for (GmType type : inheritedTypes) {
					String typeSignature = type.getTypeSignature();
					writer.println("<tr>");
					Pair<String, String> nameParts = splitTypeName(typeSignature);
					String namespace = nameParts.second();

					writer.print("<td class='type-col'>");
					renderType(type);
					writer.print("</td>");
					writer.print("<td class='namespace-col'>");
					writer.print(namespace);
					writer.print("</td>");
					writer.print("<td class='model-col'>");
					renderModel(type.getDeclaringModel());
					writer.print("</td>");
					writer.println("</tr>");
				}
			}
			writer.println("</table>");
		}

		public void render() throws IOException {
			writer.println("<div>");
			writer.println("<div class='header'>");
			if (model != null) {
				if (entityType != null || enumType != null) {
					writer.print("You are currently browsing model ");
					renderModel(model);
					writer.print(". ");
				}
				writer.print("Pick another <a href='?'>model</a>.");
			} else {
				writer.println("Pick a Model");
			}
			writer.println("</div>");

			writer.println("<div class='view'>");
			if (entityType != null) {
				renderEntityType();
			} else if (enumType != null) {
				renderEnumType();
			} else if (model != null) {
				renderModel();
			} else {
				renderModels();
			}
			writer.println("</div>");
			writer.println("</div>");
		}

		private void renderEnumType() {
			Pair<String, String> splitTypeName = splitTypeName(enumType.getTypeSignature());
			String typeName = splitTypeName.first();
			String namespace = splitTypeName.second();
			String modelName = enumType.getDeclaringModel().getName();
			String contextModelName = model.getName();

			writer.println("<table class='header'");
			writer.print("<tr>");
			writer.print("<td class='property-name main-subject'>");
			writer.print("Enum Type");
			writer.print("</td>");
			writer.print("<td class='main-subject'>");
			writer.print(typeName);
			writer.print("</td>");
			writer.print("</tr>");
			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Namespace");
			writer.print("</td>");
			writer.print("<td>");
			writer.print(namespace);
			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Declared By");
			writer.print("</td>");
			writer.print("<td>");
			writer.print("<a href='?model=" + modelName + "'>" + splitModelName(modelName).first() + "</a>");
			writer.print("</td>");
			writer.print("</tr>");

			TypeUsage typeUsage = new TypeUsage(enumType);
			List<GmEntityType> usages = typeUsage.usages;

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Direct Usages");
			writer.print("</td>");
			writer.print("<td>");
			if (usages.isEmpty()) {
				writer.print("n/a");
			} else {
				boolean firstX = true;
				for (GmType subType : usages) {
					if (firstX)
						firstX = false;
					else
						writer.print(", ");
					renderType(subType);
				}
			}
			writer.print("</td>");
			writer.print("</tr>");

			writer.println("</table><br/>");

			List<GmEnumConstant> constants = enumTypeOracle.getConstants().asGmEnumConstants().collect(Collectors.toList());

			if (!constants.isEmpty()) {
				writer.println("<table class='property-table'>");
				writer.println("<tr><th>Constant</th></tr>");
				for (GmEnumConstant constant : constants) {
					String name = constant.getName();
					writer.print("<tr>");
					writer.print("<td class='property-col'>");
					writer.print(name);
					writer.print("</td>");
					writer.print("</tr>");
				}
				writer.println("</table>");
			}
		}

		private void renderModels() throws IOException {
			List<GmMetaModel> models = session.query().entities(EntityQuery.create(GmMetaModel.T)).list();
			Predicate<GmMetaModel> filter = m -> search.test(splitModelName(m.getName()).first());

			models = models.stream().filter(filter).sorted(Comparator.comparing(m -> splitModelName(m.getName()).first()))
					.collect(Collectors.toList());

			writer.println(
					"<form ><span class='minor-text'>Filter</span> <input autofocus type='text' name='search' value='" + searchText + "'/></form>");

			writer.println("<table class='property-table'>");
			writer.print("<tr><th>Model</th><th>Group</th></tr>");

			for (GmMetaModel model : models) {
				writer.print("<tr>");
				String qualifiedName = model.getName();
				Pair<String, String> nameParts = splitModelName(qualifiedName);
				String name = nameParts.first();
				String namespace = nameParts.second();

				writer.print("<td>");
				writer.print("<a href='?model=" + qualifiedName + "'>" + name + "</a>");
				writer.print("</td>");
				writer.print("<td>");
				writer.print(namespace);
				writer.print("</td>");
				writer.println("</tr>");
			}
		}

		private void renderModel(GmMetaModel aModel) {
			writer.print("<a href='");
			writer.print("?model=");
			writer.print(aModel.getName());
			writer.print("'>");
			writer.print(splitModelName(aModel.getName()).first());
			writer.print("</a>");
		}

		private void renderType(GmType type) {
			String typeSignature = type.getTypeSignature();
			switch (type.typeKind()) {
				case ENTITY:
				case ENUM:
					writer.print("<a href='");
					writer.print("?model=");
					writer.print(model.getName());
					writer.print("&type=");
					writer.print(typeSignature);
					writer.print("'>");
					writer.print(ReflectionTools.getSimpleName(typeSignature));
					writer.print("</a>");
					break;
				case LIST:
					writer.print("<span class='collection-type'>list</span> <span class='minor-text'>of</span> ");
					renderType(((GmListType) type).getElementType());
					break;
				case MAP:
					GmMapType mapType = (GmMapType) type;
					writer.print("<span class='collection-type'>map</span> <span class='minor-text'>from</span> ");
					renderType(mapType.getKeyType());
					writer.print(" <span class='minor-text'>to</span> ");
					renderType(mapType.getValueType());
					break;
				case SET:
					writer.print("<span class='collection-type'>set</span> <span class='minor-text'>of</span> ");
					renderType(((GmSetType) type).getElementType());
					break;
				default:
					writer.print("<span class='base-type'>");
					writer.print(typeSignature);
					writer.print("</span>");
					break;

			}
		}

		private class TypeUsage {
			List<GmEntityType> usages = new ArrayList<>();
			GmCustomType type;

			public TypeUsage(GmCustomType type) {
				this.type = type;
				modelOracle.getTypes().onlyEntities().<GmEntityType> asGmTypes().forEach(this::scanType);
				usages.sort(Comparator.comparing(t -> ReflectionTools.getSimpleName(t.getTypeSignature())));
			}

			private void scanType(GmEntityType candidate) {
				if (_scanType(candidate)) {
					usages.add(candidate);
				}
			}

			private boolean _scanType(GmEntityType candidate) {
				if (candidate.getEvaluatesTo() == type)
					return true;

				for (GmProperty property : candidate.getProperties()) {
					GmType propertyType = property.getType();
					if (propertyType == type) {
						return true;
					}

					switch (propertyType.typeKind()) {
						case LIST:
						case SET:
							GmLinearCollectionType collectionType = (GmLinearCollectionType) propertyType;

							if (collectionType.getElementType() == type)
								return true;

							break;
						case MAP:
							GmMapType mapType = (GmMapType) propertyType;

							if (mapType.getKeyType() == type)
								return true;

							if (mapType.getValueType() == type)
								return true;

							break;
						default:
							break;
					}
				}

				return false;
			}
		}

		private void renderEntityType() throws IOException {
			Pair<String, String> splitTypeName = splitTypeName(entityType.getTypeSignature());
			String typeName = splitTypeName.first();
			String namespace = splitTypeName.second();
			String modelName = entityType.getDeclaringModel().getName();
			String contextModelName = model.getName();

			writer.println("<table class='header'");
			writer.print("<tr>");
			writer.print("<td class='property-name main-subject'>");
			writer.print("Entity Type");
			writer.print("</td>");
			writer.print("<td class='main-subject'>");
			writer.print(typeName);
			writer.print("</td>");
			writer.print("</tr>");
			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Namespace");
			writer.print("</td>");
			writer.print("<td>");
			writer.print(namespace);
			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Declared By");
			writer.print("</td>");
			writer.print("<td>");
			writer.print("<a href='?model=" + modelName + "'>" + splitModelName(modelName).first() + "</a>");
			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Direct Super Types");
			writer.print("</td>");
			writer.print("<td>");
			boolean firstX = true;
			List<GmEntityType> superTypes = entityType.getSuperTypes();
			if (superTypes.isEmpty()) {
				writer.print("n/a");
			} else {
				for (GmEntityType superType : superTypes) {
					if (firstX)
						firstX = false;
					else
						writer.print(", ");
					renderType(superType);
				}
			}

			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Super Types");
			writer.print("</td>");
			writer.print("<td>");

			Comparator<GmType> comparator = Comparator.comparing(t -> ReflectionTools.getSimpleName(t.getTypeSignature()));

			firstX = true;
			List<GmType> transitiveSuperTypes = entityTypeOracle.getSuperTypes().transitive().asGmTypes().stream().sorted(comparator)
					.collect(Collectors.toList());

			if (transitiveSuperTypes.isEmpty()) {
				writer.print("n/a");
			} else {
				for (GmType superType : transitiveSuperTypes) {
					if (firstX)
						firstX = false;
					else
						writer.print(", ");
					renderType(superType);
				}
			}

			writer.print("</td>");
			writer.print("</tr>");

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Sub Types");
			writer.print("</td>");
			writer.print("<td>");

			List<GmType> subTypes = entityTypeOracle.getSubTypes().transitive().asGmTypes().stream().sorted(comparator).collect(Collectors.toList());

			if (subTypes.isEmpty()) {
				writer.print("n/a");
			} else {
				firstX = true;
				for (GmType subType : subTypes) {
					if (firstX)
						firstX = false;
					else
						writer.print(", ");
					renderType(subType);
				}
			}
			writer.print("</td>");
			writer.print("</tr>");

			GmType evaluatesTo = entityTypeOracle.getEvaluatesTo().orElse(null);
			if (evaluatesTo != null) {
				writer.print("<tr>");
				writer.print("<td class='property-name'>");
				writer.print("Evaluates To");
				writer.print("</td>");
				writer.print("<td>");
				renderType(evaluatesTo);
				writer.print("</td>");
				writer.print("</tr>");
			}

			TypeUsage typeUsage = new TypeUsage(entityType);
			List<GmEntityType> usages = typeUsage.usages;

			writer.print("<tr>");
			writer.print("<td class='property-name'>");
			writer.print("Direct Usages");
			writer.print("</td>");
			writer.print("<td>");
			if (usages.isEmpty()) {
				writer.print("n/a");
			} else {
				firstX = true;
				for (GmType subType : usages) {
					if (firstX)
						firstX = false;
					else
						writer.print(", ");
					renderType(subType);
				}
			}
			writer.print("</td>");
			writer.print("</tr>");

			writer.println("</table><br/>");

			writer.println("<form method='GET'>");
			writer.println("<input type='hidden' name='model' value='" + model.getName() + "'/>");
			writer.println("<input type='hidden' name='type' value='" + entityType.getTypeSignature() + "'/>");
			writer.println("<span class='minor-text'>Filter</span> <input autofocus name='search' value='" + searchText + "'/>");
			writer.println("</form>");

			Predicate<PropertyOracle> filter = p -> search.test(p.getName());

			List<PropertyOracle> properties = entityTypeOracle.getProperties() //
					.onlyDeclared() //
					.asPropertyOracles() //
					.filter(filter).sorted(Comparator.comparing(o -> o.asGmProperty().getName())).collect(Collectors.toList());

			writer.println("<table class='property-table'>");
			if (!properties.isEmpty()) {
				writer.println("<tr><th>Declared Property</th><th>Type</th><th>Default</th></tr>");
				for (PropertyOracle property : properties) {
					String name = property.getName();
					GmType type = property.asGmProperty().getType();
					writer.print("<tr>");
					writer.print("<td class='property-col'>");
					writer.print(name);
					writer.print("</td>");

					writer.print("<td class='type-col'>");
					renderType(type);
					writer.print("</td>");

					writer.print("<td>");
					Object initializer = property.getGmPropertyInfos().stream().map(GmPropertyInfo::getInitializer).filter(i -> i != null).findFirst()
							.orElse(null);
					if (initializer != null) {
						final String valueAsStr;
						if (initializer instanceof EnumReference) {
							EnumReference enumReference = (EnumReference) initializer;
							valueAsStr = enumReference.constant().name();
						} else if (initializer instanceof Now) {
							valueAsStr = "now()";
						} else {
							valueAsStr = String.valueOf(initializer);
						}
						writer.print(valueAsStr);
					}
					writer.print("</td>");

					writer.print("</tr>");
				}
			}

			List<PropertyOracle> inheritedProperties = entityTypeOracle.getProperties() //
					.onlyInherited() //
					.asPropertyOracles() //
					.filter(filter) //
					.sorted(Comparator.comparing(o -> o.asGmProperty().getName())) //
					.collect(Collectors.toList());

			if (!inheritedProperties.isEmpty()) {
				if (!properties.isEmpty())
					writer.println("<tr class='spacer-row'></tr>");
				writer.println("<tr><th>Inherited Property</th><th>Type</th><th>Default</th><th>Declared By</th></tr>");
				for (PropertyOracle property : inheritedProperties) {
					String name = property.getName();
					GmType type = property.asGmProperty().getType();
					writer.print("<tr>");
					writer.print("<td class='property-col'>");
					writer.print(name);
					writer.print("</td>");

					writer.print("<td class='type-col'>");
					renderType(type);
					writer.print("</td>");

					writer.print("<td>");
					// Object initializer = property.getGmPropertyInfos().stream().map(GmPropertyInfo::getInitializer).filter(i -> i !=
					// null).findFirst().orElse(null);
					Object initializer = property.asGmProperty().getInitializer();
					if (initializer != null) {
						final String valueAsStr;
						if (initializer instanceof EnumReference) {
							EnumReference enumReference = (EnumReference) initializer;
							valueAsStr = enumReference.constant().name();
						} else if (initializer instanceof Now) {
							valueAsStr = "now()";
						} else {
							valueAsStr = String.valueOf(initializer);
						}
						writer.print(valueAsStr);
					}
					writer.print("</td>");

					writer.print("<td class='type-col'>");
					boolean first = true;
					// List<GmEntityType> declaringTypes = property.getGmPropertyInfos().stream() //
					// .map(i -> (GmEntityType)i.declaringTypeInfo()) //
					// .collect(Collectors.toList());
					List<GmEntityType> declaringTypes = Collections.singletonList(property.asGmProperty().getDeclaringType());

					for (GmEntityType declaringType : declaringTypes) {
						if (first)
							first = false;
						else
							writer.print(", ");

						renderType(declaringType);
					}
					writer.print("</td>");
					writer.print("</tr>");
				}
			}
			writer.println("</table>");
		}

		private List<GmEntityType> determineSuperTypes(GmEntityType contextType) {
			Set<GmEntityType> superTypes = new LinkedHashSet<>();
			collectSuperTypes(superTypes, contextType);
			return superTypes.stream().sorted(Comparator.comparing(t -> ReflectionTools.getSimpleName(t.getTypeSignature())))
					.collect(Collectors.toList());
		}

		private void collectSuperTypes(Set<GmEntityType> superTypes, GmEntityType type) {
			if (!superTypes.add(type))
				return;

			for (GmEntityType superType : type.getSuperTypes()) {
				collectSuperTypes(superTypes, superType);
			}
		}

		private void renderEnumConstants(HttpServletRequest req, HttpServletResponse resp, List<GmEnumConstant> constants,
				PersistenceGmSession session) {
			// TODO Auto-generated method stub

		}

		private Set<GmCustomType> getAllTypes(GmCustomType customType) {
			Set<GmCustomType> customTypes = new HashSet<>();

			getAllTypes(customTypes, customType, false);

			return customTypes;
		}

		private void getAllTypes(Set<GmCustomType> customTypes, GmCustomType customType, boolean includeSelf) {
			if (includeSelf && !customTypes.add(customType))
				return;

			if (customType instanceof GmEntityType) {
				GmEntityType entityType = (GmEntityType) customType;
				for (GmEntityType superType : entityType.getSuperTypes()) {
					getAllTypes(customTypes, superType, true);
				}
			}
		}

		private List<GmMetaModel> getModelDependencies(GmMetaModel metaModel) {
			Set<GmMetaModel> models = new HashSet<>();

			getModelDependencies(models, metaModel, false);

			return models.stream() //
					.sorted(Comparator.comparing(m -> splitModelName(m.getName()).first())).collect(Collectors.toList());
		}

		private void getModelDependencies(Set<GmMetaModel> models, GmMetaModel metaModel, boolean includeSelf) {
			if (includeSelf && !models.add(metaModel))
				return;

			for (GmMetaModel dep : metaModel.getDependencies()) {
				getModelDependencies(models, dep, true);
			}
		}

	}

	private static Pair<String, String> splitTypeName(String nsName) {
		return splitNamespacedName(nsName, '.');
	}

	private static Pair<String, String> splitModelName(String nsName) {
		return splitNamespacedName(nsName, ':');
	}

	private static Pair<String, String> splitNamespacedName(String nsName, char delimiter) {
		int split = nsName.lastIndexOf(delimiter);
		String namespace = nsName.substring(0, split);
		String name = nsName.substring(split + 1);

		return Pair.of(name, namespace);
	}

}
