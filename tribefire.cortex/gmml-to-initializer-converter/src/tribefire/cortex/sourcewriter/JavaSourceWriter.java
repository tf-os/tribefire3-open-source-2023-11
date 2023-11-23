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
package tribefire.cortex.sourcewriter;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class JavaSourceWriter {

	public final JavaSourceClass sourceClass;

	private final Map<String, JavaSourceClass> simpleNameToClass = newMap();

	private final List<String> classAnnotations = newList();

	private final Set<JavaSourceClass> _extends = newLinkedSet();
	private final Set<JavaSourceClass> _implementes = newLinkedSet();

	private final List<String> fields = newList();
	private final List<String> methods = newList();

	public JavaSourceWriter(String packageName, String shortName, boolean isInterface) {
		this(JavaSourceClass.build(packageName, shortName).isInterface(isInterface).please());
	}

	public JavaSourceWriter(JavaSourceClass sourceClass) {
		this.sourceClass = sourceClass;

		requireImport(JavaSourceClass.StringJsc);
		requireImport(JavaSourceClass.IntegerJsc);
		requireImport(JavaSourceClass.LongJsc);
		requireImport(JavaSourceClass.FloatJsc);
		requireImport(JavaSourceClass.DoubleJsc);
		requireImport(JavaSourceClass.BooleanJsc);
	}

	public void requireImport(JavaSourceClass _import) {
		JavaSourceClass prev = simpleNameToClass.putIfAbsent(_import.simpleName, _import);
		if (prev != null && prev != _import)
			throw new IllegalStateException("Cannot import [" + _import.fullName() + "] because there is already an import for [" + prev.fullName()
					+ "]. Class: " + sourceClass.fullName());
	}

	public boolean tryImport(JavaSourceClass sourceClass) {
		JavaSourceClass prev = simpleNameToClass.putIfAbsent(sourceClass.simpleName, sourceClass);
		return prev == null || prev == sourceClass;
	}

	public void addClassAnnotation(String annotation) {
		classAnnotations.add(annotation);
	}

	public void addExtends(JavaSourceClass sourceClass) {
		tryImport(sourceClass);
		_extends.add(sourceClass);
	}

	public void addImplements(JavaSourceClass sourceClass) {
		tryImport(sourceClass);
		_implementes.add(sourceClass);
	}

	public void addField(String field) {
		fields.add(field);
	}

	public void addMethod(String method) {
		methods.add(method);
	}

	private final StringBuilder sb = new StringBuilder();

	public String write() {
		// Append the package name
		sb.append("package " + sourceClass.packageName + ";\n\n");

		writeImports();

		writeClassAnnotations();
		writeHeader();

		writeSnippets(fields);
		writeSnippets(methods);

		sb.append("}");

		return sb.toString();
	}

	/**
	 * <ul>
	 * <li>Writes groups of imports based on top-level package
	 * <li>First come the java imports, then all other in alphabetical order
	 * <li>groups are separated by empty line
	 * <li>imports for a group are sorted alphabetically
	 * </ul>
	 */
	private void writeImports() {
		Map<String, List<String>> importGroups = simpleNameToClass.values().stream() //
				.filter(JavaSourceClass::requiresImport) //
				.map(JavaSourceClass::fullName) //
				.sorted() //
				.collect( //
						Collectors.groupingBy( //
								JavaSourceWriter::topLevelPackage, //
								TreeMap::new, //
								Collectors.toList() //
						) //
				);

		List<String> javas = importGroups.remove("java");
		if (javas != null)
			writeImports(javas);

		for (List<String> imports : importGroups.values())
			writeImports(imports);
	}

	private void writeImports(List<String> imports) {
		imports.forEach(fullName -> sb.append("import " + fullName + ";\n"));
		sb.append("\n");
	}

	private static String topLevelPackage(String className) {
		return StringTools.findPrefix(className, ".");
	}

	private void writeClassAnnotations() {
		for (String annoCode : classAnnotations) {
			sb.append(annoCode);
			sb.append("\n");
		}
	}

	private void writeHeader() {
		sb.append("public ");
		if (sourceClass.isInterface)
			sb.append("interface ");
		else
			sb.append("class ");

		sb.append(sourceClass.simpleName);
		writeSuper("extends", _extends);
		writeSuper("implements", _implementes);
		sb.append(" {\n\n");
	}

	private void writeSuper(String extendsOrImplementes, Set<JavaSourceClass> supers) {
		if (supers.isEmpty())
			return;

		sb.append(" ");
		sb.append(extendsOrImplementes);
		sb.append(" ");

		String supersCommaSeparated = supers.stream() // \
				.map(this::resolveShortOrImportedName) //
				.collect(Collectors.joining(", "));

		sb.append(supersCommaSeparated);
	}

	private String resolveShortOrImportedName(JavaSourceClass sourceClass) {
		if (simpleNameToClass.get(sourceClass.simpleName) == sourceClass)
			return sourceClass.simpleName;
		else
			return sourceClass.fullName();

	}

	private void writeSnippets(List<String> snippets) {
		for (String s : snippets)
			sb.append(s);
	}

}
