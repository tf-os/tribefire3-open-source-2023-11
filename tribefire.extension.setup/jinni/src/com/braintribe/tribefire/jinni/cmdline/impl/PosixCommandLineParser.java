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
package com.braintribe.tribefire.jinni.cmdline.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.gm.model.reason.essential.ParseError;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.jinni.api.From;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.tribefire.jinni.cmdline.api.EntityFactory;
import com.braintribe.tribefire.jinni.cmdline.api.ParseException;
import com.braintribe.tribefire.jinni.cmdline.api.ParsedCommandLine;

public class PosixCommandLineParser extends AbstractCommandLineParser {
	private static final List<String> booleanValues = Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString());

	private static class ForwardReference {
		List<Consumer<Object>> consumers = new ArrayList<>(2);
	}

	enum RawArgumentKind {
		propertyShorthands,
		propertyName,
		value,
	}

	private Function<From, GenericEntity> fromExpert = f -> {
		throw parseError("Unsupported From type: " + f);
	};

	public void setFromExpert(Function<From, GenericEntity> fromExperts) {
		this.fromExpert = fromExperts;
	}

	@Override
	public ParsedCommandLine parse(String[] args, EntityFactory entityFactory, ModelAccessory modelAccessory) {
		return parseReasoned(args, entityFactory, modelAccessory).get();
	}
	
	@Override
	public Maybe<ParsedCommandLine> parseReasoned(String[] args, EntityFactory entityFactory,
			ModelAccessory modelAccessory) {
		ParsedCommandLineImpl parsedCommandLine = new ParsedCommandLineImpl(entityFactory);
		CommandLineParserModelOracle oracle = new CommandLineParserModelOracle(modelAccessory);

		List<RawOptions> splitOptions = splitOptions(args);

		Map<String, Object> variables = new HashMap<>();

		for (RawOptions rawOption : splitOptions) {
			if (rawOption.options.isEmpty())
				continue;

			Maybe<GenericEntity> entityMaybe = new StatefulParser(rawOption.options, entityFactory, oracle, variables).parseReasoned();
			
			if (entityMaybe.isUnsatisfied())
				return entityMaybe.whyUnsatisfied().asMaybe();
			
			GenericEntity entity = entityMaybe.get();

			if (rawOption.name != null) {
				variables.compute(rawOption.name, (k, v) -> {
					if (v != null) {
						if (v.getClass() == ForwardReference.class) {
							((ForwardReference) v).consumers.forEach(c -> c.accept(entity));
						} else
							throw parseError("duplicate variable definition:" + k);
					}
					return entity;
				});
			} else {
				parsedCommandLine.addEntity(entity);
			}
		}

		return Maybe.complete(parsedCommandLine);
	}

	private static RawArgumentKind getKind(String rawArg) {
		if (rawArg.startsWith("--"))
			return RawArgumentKind.propertyName;
		else if (rawArg.startsWith("-")) {
			return RawArgumentKind.propertyShorthands;
		} else {
			return RawArgumentKind.value;
		}

	}

	private static final Object noValue = new Object();

	private static class RawOptions {
		private String name;
		private final List<String> options = new ArrayList<>();
	}

	private List<RawOptions> splitOptions(String args[]) {
		List<RawOptions> optionsList = new ArrayList<>();

		RawOptions currentOptions = new RawOptions();
		optionsList.add(currentOptions);

		for (String arg : args) {
			if (arg.startsWith(":")) {
				currentOptions = new RawOptions();
				if (arg.length() > 1) {
					currentOptions.name = arg.substring(1);
				}
				optionsList.add(currentOptions);
			} else {
				currentOptions.options.add(arg);
			}
		}

		return optionsList;
	}

	private class StatefulParser {

		private GenericEntity options;
		private From from;

		private final List<String> args;
		private int argIndex = 0;

		private final EntityFactory optionFactory;

		private final CommandLineParserModelOracle oracle;

		private final Map<String, Object> variables;

		StatefulParser(List<String> args, EntityFactory optionFactory, CommandLineParserModelOracle oracle,
				Map<String, Object> variables) {
			this.args = args;
			this.optionFactory = optionFactory;
			this.oracle = oracle;
			this.variables = variables;
		}

		private String nextRawArg() {
			if (argIndex < args.size())
				return args.get(argIndex++);
			else
				return null;
		}

		private void rewindOneRawArg() {
			argIndex--;
		}

		Maybe<GenericEntity> parseReasoned() {
			try {
				return Maybe.complete(parse());
			} catch (ParseException e) {
				return Reasons.build(ParseError.T).text(e.getMessage()).toMaybe();
			} catch (UnsatisfiedMaybeTunneling e) {
				return e.getMaybe();
			}
		}
		
		GenericEntity parse() {
			if (!parseInstantiation())
				return null;

			parseSequencedArguments();

			parseNamedArguments();

			if (from != null && from == options) {
				options = fromExpert.apply(from);
			}

			return options;
		}

		private boolean parseInstantiation() {
			String typeIdentifier = nextRawArg();

			if (typeIdentifier == null)
				return false;

			Maybe<GenericEntity> entityMaybe = optionFactory.create(typeIdentifier);
			options = UnsatisfiedMaybeTunneling.getOrTunnel(entityMaybe);

			if (options instanceof From) {
				from = (From) options;
			}

			return true;
		}

		private Consumer<Object> getValueConsumer(GenericEntity entity, Property property) {
			TypeCode typeCode = property.getType().getTypeCode();
			switch (typeCode) {
				case mapType: {
					Map<Object, Object> map = (Map<Object, Object>) property.get(entity);
					map.clear();
					return v -> {
						Pair<Object, Object> entry = (Pair<Object, Object>) v;
						Object key = entry.first();
						Object value = entry.second();

						int deferred = 0;

						if (key != null && key.getClass() == ForwardReference.class)
							deferred |= 1;

						if (value != null && value.getClass() == ForwardReference.class)
							deferred |= 2;

						if (deferred == 0) {
							map.put(key, value);
						} else {
							MapEntryForwardListener.install(map, key, value, deferred);
						}
					};
				}
				case listType:
				case setType: {
					Collection<Object> collection = (Collection<Object>) property.get(entity);
					collection.clear();
					return v -> {
						if (v != null && v.getClass() == ForwardReference.class) {
							ForwardReference forwardReference = (ForwardReference) v;
							if (typeCode == TypeCode.listType) {
								List<Object> list = (List<Object>) collection;
								int pos = collection.size();
								list.add(null);
								forwardReference.consumers.add(d -> list.set(pos, d));
							} else {
								forwardReference.consumers.add(d -> collection.add(d));
							}
						} else {
							collection.add(v);
						}
					};
				}
				default:
					return v -> {
						if (v != null && v.getClass() == ForwardReference.class) {
							ForwardReference forwardReference = (ForwardReference) v;
							forwardReference.consumers.add(d -> property.set(entity, d));
						} else {
							property.set(entity, v);
						}
					};
			}
		}

		private void parsePropertyShorthands(GenericEntity entity, String shorthands) {
			GenericModelType propertyType = null;
			Consumer<Object> consumer = null;

			for (int i = 0; i < shorthands.length(); i++) {
				char s = shorthands.charAt(i);
				Property property = oracle.findProperty(entity.entityType(), String.valueOf(s));

				if (property == null)
					throw parseError(entity.entityType() + " has no argument with name: " + s);

				if (propertyType == null) {
					propertyType = property.getType();
					consumer = getValueConsumer(entity, property);
				} else {
					if (propertyType != property.getType())
						throw parseError("Single character arguments with different type cannot combined: " + shorthands);

					consumer = consumer.andThen(getValueConsumer(entity, property));
				}
			}

			parseValues(consumer, propertyType);
		}
		
		private void parseProperty(GenericEntity entity, String propertyName) {
			Property property = oracle.findProperty(entity.entityType(), propertyName);

			if (property == null)
				throw parseError(entity.entityType() + " has no argument with name: " + propertyName);

			Consumer<Object> consumer = getValueConsumer(entity, property);
			parseValues(consumer, property.getType());
		}

		private void parseValues(Consumer<Object> consumer, GenericModelType inferredType) {
			switch (inferredType.getTypeCode()) {
				case booleanType:
					Object optionalValue = parseOptionalValue(EssentialTypes.TYPE_BOOLEAN, booleanValues);

					if (optionalValue != noValue)
						consumer.accept(optionalValue);
					else
						consumer.accept(Boolean.TRUE);
					return;

				case mapType: {
					MapType mapType = (MapType) inferredType;
					GenericModelType keyType = mapType.getKeyType();
					GenericModelType valueType = mapType.getValueType();
					Object key;
					Object value;

					while ((key = parseOptionalValue(keyType, null)) != noValue) {
						value = parseValue(valueType);
						consumer.accept(Pair.of(key, value));
					}
					return;
				}
				case listType:
				case setType:
					Object value;
					while ((value = parseOptionalValue(((LinearCollectionType) inferredType).getCollectionElementType(), null)) != noValue) {
						consumer.accept(value);
					}
					return;

				default:
					consumer.accept(parseValue(inferredType));
					return;
			}
		}

		private Object parseOptionalValue(GenericModelType inferredType, List<String> positiveValues) {
			String rawArg = nextRawArg();

			if (rawArg == null)
				return noValue;

			if (getKind(rawArg) == RawArgumentKind.value) {
				if (positiveValues != null) {
					if (positiveValues.contains(rawArg)) {
						return parseValue(inferredType, rawArg);
					} else {
						rewindOneRawArg();
						return noValue;
					}
				} else
					return parseValue(inferredType, rawArg);
			} else {
				rewindOneRawArg();
				return noValue;
			}
		}

		private Object parseValue(GenericModelType inferredType) {
			String rawArg = nextRawArg();

			if (rawArg == null)
				throw parseError("Unexpected end of arguments when reading an argument");

			return parseValue(inferredType, rawArg);
		}

		private Object parseValue(GenericModelType inferredType, String rawArg) {
			if (rawArg.startsWith("@")) {
				String name = rawArg.substring(1);
				if (name.startsWith("@"))
					rawArg = name;
				else
					return variables.computeIfAbsent(name, k -> new ForwardReference());
			} else if (rawArg.equals("null")) {
				return null;
			}

			if (inferredType == BaseType.INSTANCE) {
				String typeName = rawArg;
				return parseValue(GMF.getTypeReflection().getType(typeName));
			} else {
				return decodeValue(inferredType, PosixEscaping.unescape(rawArg));
			}

		}

		private boolean parseOptionalExtensionOperator() {
			if (from == null)
				return false;

			String rawArg = nextRawArg();

			if (rawArg == null) {
				return false;
			}

			if (rawArg.equals("+")) {
				options = fromExpert.apply(from);
				from = null;
				parseNamedArguments();
				return true;
			}

			rewindOneRawArg();
			return false;
		}

		private void parseSequencedArguments() {
			List<Property> properties = getPropertySequence();

			for (Property property : properties) {
				if (property.getType().isCollection())
					continue;

				if (parseOptionalExtensionOperator())
					return;

				Object value = parseOptionalValue(property.getType(), null);

				if (value == noValue)
					break;

				property.set(options, value);
			}
		}

		private void parseNamedArguments() {
			while (parseNamedArgument()) { /* noop */ }
		}

		private boolean parseNamedArgument() {
			if (parseOptionalExtensionOperator())
				return false;

			String rawArg = nextRawArg();

			if (rawArg == null)
				return false;

			switch (getKind(rawArg)) {
				case propertyName:
					parseProperty(options, rawArg.substring(2));
					return true;
				case propertyShorthands:
					parsePropertyShorthands(options, rawArg.substring(1));
					return true;
				default:
					throw parseError("Expected a named argument but got: " + rawArg);
			}
		}

		private List<Property> getPropertySequence() {
			return oracle.getPositionalArguments(options.entityType());
		}

	}

	private static class MapEntryForwardListener {
		int deferred;
		Object key;
		Object value;
		Map<Object, Object> map;

		MapEntryForwardListener(Map<Object, Object> map, int deferred) {
			this.map = map;
			this.deferred = deferred;
		}

		public static void install(Map<Object, Object> map, Object key, Object value, int deferred) {
			MapEntryForwardListener listener = new MapEntryForwardListener(map, deferred);
			switch (deferred) {
				case 1:
					listener.value = value;
					((ForwardReference) key).consumers.add(listener::onKey);
					break;
				case 2:
					listener.key = key;
					((ForwardReference) value).consumers.add(listener::onValue);
					break;
				case 3:
					((ForwardReference) key).consumers.add(listener::onKey);
					((ForwardReference) value).consumers.add(listener::onValue);
					break;
			}
		}

		void onKey(Object key) {
			this.key = key;
			deferred ^= 1;
			checkComplete();
		}

		void onValue(Object value) {
			this.value = value;
			deferred ^= 2;
			checkComplete();
		}

		void checkComplete() {
			if (deferred == 0)
				map.put(key, value);
		}
	}

	private ParseException parseError(String msg) {
		return new ParseException(msg);
	}
}
