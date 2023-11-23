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
package com.braintribe.tribefire.jdbc.statement;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.PatternBuilder;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.query.fluent.ConditionBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.OperandBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.fluent.ValueComparisonBuilder;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.tribefire.jdbc.TfAssociationTable;
import com.braintribe.tribefire.jdbc.TfConnection;
import com.braintribe.tribefire.jdbc.TfJdbcException;
import com.braintribe.tribefire.jdbc.TfMetadata;
import com.braintribe.tribefire.jdbc.adapter.SqlToGmqlTranslator;
import com.braintribe.tribefire.jdbc.statement.TargetExpression.Type;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.WithinGroupExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * The Class SqlQueryParser.
 *
 */
public class SqlQueryParser implements SelectVisitor, ExpressionVisitor {

	protected SelectQuery query = null;
	protected SelectQueryBuilder builder = null;
	private ConditionBuilder<?> whereBuilder;
	private SqlToGmqlTranslator translator;

	private PlainSelect plainSelect;
	private List<PropertyWrapper> selectedProperties = new ArrayList<PropertyWrapper>();
	private Map<String, PropertyWrapper> selectedPropertiesMap = new HashMap<>();
	private EntityType<?> entityType;

	private int indent = 0;
	private Select select;
	private TfConnection connection;
	private TargetExpression targetExpression;

	private int fromIndex = 0;
	private int toIndex = Integer.MAX_VALUE;
	private int currentParameterIndex = 1;

	private List<PlaceholderOperandBuilder> operandBuilders = new ArrayList<>();
	private List<Join> joins;
	private boolean containsVirtualTables;
	private Set<TfAssociationTable> involvedVirtualTables = new HashSet<>();

	private Logger logger = Logger.getLogger(SqlQueryParser.class);

	/**
	 * Instantiates a new sql query parser and prepares the select query.
	 *
	 * @param select
	 *            the select
	 * @param connection
	 *            the connection
	 * @throws SQLFeatureNotSupportedException
	 *             the SQL feature not supported exception
	 */
	public SqlQueryParser(Select select, TfConnection connection) throws SQLFeatureNotSupportedException {
		this.select = select;
		this.connection = connection;
		this.translator = new SqlToGmqlTranslator(connection);

		SelectBody selectBody = this.select.getSelectBody();
		if (selectBody instanceof PlainSelect) {
			plainSelect = (PlainSelect) selectBody;
		} else {
			throw new SQLFeatureNotSupportedException("Only Plain Selects are supported in version " + TfMetadata.DRIVER_VERSION);
		}
		selectBody.accept(this);
		this.query = builder.done();
	}

	/* (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser. statement.select.PlainSelect) */
	@Override
	public void visit(PlainSelect plainSelect) {
		try {
			builder = new SelectQueryBuilder();

			String typeSignature = null;

			FromItem fromItem = plainSelect.getFromItem();
			String name = null;
			// PROCESS FROMs
			if (!(fromItem instanceof Table)) {
				throw new UnsupportedOperationException("Only table SELECTs are supported in version " + TfMetadata.DRIVER_VERSION);
			}
			Alias alias = fromItem.getAlias();
			String aliasName = null;
			if (alias != null) {
				aliasName = alias.getName();
			}
			Table table = (Table) fromItem;

			name = table.getName();
			entityType = connection.getEntityTypeMap().get(name);
			containsVirtualTables = entityType == null;

			TfAssociationTable assTabFrom = null;
			if (!containsVirtualTables) {
				typeSignature = entityType.getTypeSignature();
				builder = builder.from(typeSignature, aliasName);
				EntityType<GenericEntity> fromEntity = GMF.getTypeReflection().getEntityType(typeSignature);
				if (connection.getAssociationTableMap().containsKey(fromEntity.getShortName())) {
					involvedVirtualTables.addAll(connection.getAssociationTableMap().get(fromEntity.getShortName()));
				}
			} else {
				assTabFrom = connection.getAssociationTableByName(name);
				if (assTabFrom == null) {
					throw new SQLException("Table " + name + " is unknown.");
				}
				involvedVirtualTables.add(assTabFrom);
				builder = translator.virtualFrom(builder, table);
			}

			Set<TfAssociationTable> assTabJoins = new HashSet<>();
			joins = plainSelect.getJoins();
			Set<Join> fromsToAdd = new HashSet<>();
			Set<Join> virtualFromsToAdd = new HashSet<>();
			if (joins != null) {
				for (Join join : joins) {
					Table joinTable = (Table) join.getRightItem();
					String joinName = joinTable.getName();
					if (connection.getAssociationTableByName(joinName) == null) {
						// this is not a virtual association table
						fromsToAdd.add(join);
					} else {
						// this is a virtual association table join
						virtualFromsToAdd.add(join);
					}
				}
			}

			// join non-virtually associated joins
			for (Join joinToAdd : fromsToAdd) {
				Table joinTable = (Table) joinToAdd.getRightItem();
				String joinName = joinTable.getName();
				EntityType<?> joinEntityType = connection.getEntityTypeMap().get(joinName);
				String joinTypeSignature = joinEntityType.getTypeSignature();

				builder = builder.from(joinTypeSignature, joinTable.getAlias().getName());
			}

			for (Join joinToAdd : virtualFromsToAdd) {
				// this is a virtual association table
				Table joinTable = (Table) joinToAdd.getRightItem();
				String joinName = joinTable.getName();
				assTabJoins.add(connection.getAssociationTableByName(joinName));
				involvedVirtualTables.add(connection.getAssociationTableByName(joinName));
				containsVirtualTables = true;
				builder = translator.virtualJoin(builder, joinToAdd, plainSelect);
			}

			// TC
			TraversingCriterion tc = null;

			// PROCESS DISTINCT
			Distinct distinct = plainSelect.getDistinct();
			if (distinct != null) {
				List<SelectItem> onSelectItems = distinct.getOnSelectItems();
				if (onSelectItems != null && !onSelectItems.isEmpty()) {
					builder.distinct(true);
				}
			} else {
				builder.distinct(false);
			}

			// SKIP
			Skip skip = plainSelect.getSkip();
			if (skip != null) {
				Long rc = skip.getRowCount();
				if (rc != null) {
					this.fromIndex = rc.intValue();
				}
			}

			// FETCH
			Fetch fetch = plainSelect.getFetch();
			if (fetch != null) {
				long rowCount = fetch.getRowCount();
				this.toIndex = ((int) rowCount) + this.fromIndex;
			}

			// TOP
			Top top = plainSelect.getTop();
			if (top != null) {
				Expression expression = top.getExpression();
				if (expression instanceof LongValue) {
					LongValue lv = (LongValue) expression;
					this.fromIndex = 0;
					this.toIndex = (int) lv.getValue();
				}
			}

			List<SelectItem> selectItems = plainSelect.getSelectItems();

			// PROCESS SELECT ITEMS
			for (SelectItem si : selectItems) {
				if (si instanceof SelectExpressionItem) {
					SelectExpressionItem sei = (SelectExpressionItem) si;
					String aliasNameItem = null;
					Alias aliasItem = sei.getAlias();
					if (aliasItem != null) {
						aliasNameItem = aliasItem.getName();
					}
					Expression expression = sei.getExpression();

					targetExpression = null;
					expression.accept(this);
					if (targetExpression != null) {
						switch (targetExpression.getType()) {
							case property:
								Property p = targetExpression.getProperty(entityType);

								if (p == null) {
									throw new TfJdbcException("Unable to look up property by expression name: " + targetExpression.getName());
								}

								if (aliasNameItem == null) {
									aliasNameItem = targetExpression.getName();
								}
								PropertyWrapper pw = new PropertyWrapper(p, aliasNameItem);
								selectedProperties.add(pw);
								break;
							case value:
								PropertyWrapper pwV = new PropertyWrapper(aliasNameItem, targetExpression.getValue());
								selectedProperties.add(pwV);
								break;
							default:
								break;
						}
					}
				}
			}

			// SELECT ALL
			if (selectedProperties.isEmpty()) {
				// if (!containsVirtualTables) {
				if (null != entityType) {
					for (Property p : entityType.getProperties()) {
						PropertyWrapper pw = new PropertyWrapper(p, TfMetadata.getPropertyName(p));
						selectedProperties.add(pw);
					}
				}

				if (assTabFrom != null) {
					selectedProperties.add(new PropertyWrapper(assTabFrom.getLeftEntity().getIdProperty(), assTabFrom.getLeftPropertyDisplayName(),
							assTabFrom.getLeftEntityType()));
					if (assTabFrom.getRightEntity() != null) {
						selectedProperties.add(new PropertyWrapper(assTabFrom.getRightEntity().getIdProperty(),
								assTabFrom.getRightPropertyDisplayName(), assTabFrom.getRightEntityType()));
					} else {
						selectedProperties.add(new PropertyWrapper(null, "value", assTabFrom.getRightEntityType()));
					}
				}
				if (!assTabJoins.isEmpty()) {
					for (TfAssociationTable assTabJoin : assTabJoins) {
						selectedProperties.add(new PropertyWrapper(assTabJoin.getLeftEntity().getIdProperty(),
								assTabJoin.getLeftPropertyDisplayName(), assTabJoin.getLeftEntityType()));
						if (assTabJoin.getRightEntity() != null) {
							selectedProperties.add(new PropertyWrapper(assTabJoin.getRightEntity().getIdProperty(),
									assTabJoin.getRightPropertyDisplayName(), assTabJoin.getRightEntityType()));
						} else {
							selectedProperties.add(new PropertyWrapper(null, "value", assTabJoin.getRightEntityType()));
						}
					}
				}
				if (joins != null) { // && !containsVirtualTables) {
					for (Join join : joins) {
						Table joinTable = (Table) join.getRightItem();
						String joinName = joinTable.getName();
						EntityType<?> joinEntityType = connection.getEntityTypeMap().get(joinName);
						if (null == joinEntityType) {
							continue;
						}
						String prefix = (null != joinTable.getAlias()) ? joinTable.getAlias().getName() : joinName;
						for (Property p : joinEntityType.getProperties()) {
							PropertyWrapper pw = new PropertyWrapper(p, prefix + "." + p.getName());
							selectedProperties.add(pw);
						}
					}
				}
			}

			buildPropertiesMap();
			if (!containsVirtualTables) {
				tc = getSelectiveTc();
			} // TODO else?

			// output("FROM "+name);

			Expression where = plainSelect.getWhere();
			where = translator.translateWhere(where);

			if (where != null) {
				whereBuilder = builder.where();
				where.accept(this);
			}

			if (tc != null) {
				builder.tc(tc);
			}

			List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
			if (orderByElements != null && !orderByElements.isEmpty()) {

				for (OrderByElement obe : orderByElements) {
					Expression expression = obe.getExpression();
					targetExpression = null;
					expression.accept(this);
					if (targetExpression != null) {
						OrderingDirection od = obe.isAsc() ? OrderingDirection.ascending : OrderingDirection.descending;
						if (targetExpression.getAlias() == null) {
							builder = (SelectQueryBuilder) builder.orderBy(od).property(targetExpression.getName());
						} else {
							builder = (SelectQueryBuilder) builder.orderBy(od).property(targetExpression.getAlias(), targetExpression.getName());
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Exception occurred while processing the select " + plainSelect.toString(), ex);
			throw new TfJdbcException("Exception occurred while processing the select " + plainSelect.toString(), ex);
		}
	}

	/**
	 * Gets the placeholder operand builder.
	 *
	 * @param name
	 *            the name
	 * @return the placeholder operand builder
	 */
	public PlaceholderOperandBuilder getPlaceholderOperandBuilder(String name) {
		for (PlaceholderOperandBuilder pob : operandBuilders) {
			if (pob.hasParameterName(name)) {
				return pob;
			}
		}
		return null;
	}

	/**
	 * Gets the placeholder operand builder.
	 *
	 * @param index
	 *            the index
	 * @return the placeholder operand builder
	 */
	public PlaceholderOperandBuilder getPlaceholderOperandBuilder(Integer index) {
		return this.operandBuilders.get(index - 1);
	}

	/**
	 * Builds the properties map.
	 */
	private void buildPropertiesMap() {
		for (PropertyWrapper pw : selectedProperties) {
			selectedPropertiesMap.put(pw.getAlias(), pw);
		}
	}

	protected TraversingCriterion getSelectiveTc() {
		// @formatter:off
		com.braintribe.model.generic.processing.pr.fluent.JunctionBuilder<PatternBuilder<com.braintribe.model.generic.processing.pr.fluent.JunctionBuilder<TC>>> disjunction = TC
				.create().conjunction().property()
				.typeCondition(or(isKind(TypeKind.entityType), isKind(TypeKind.collectionType))).negation().pattern()
				.entity(entityType).disjunction();
		for (PropertyWrapper pw : selectedProperties) {
			if (pw.getProperty() != null) {
				disjunction = disjunction.property(pw.getProperty().getName());
			}
		}

		TraversingCriterion tc = disjunction.close().close().close().done();

		return tc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.
	 * statement.select.SetOperationList)
	 */
	@Override
	public void visit(SetOperationList setOpList) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jsqlparser.statement.select.SelectVisitor#visit(net.sf.jsqlparser.
	 * statement.select.WithItem)
	 */
	@Override
	public void visit(WithItem withItem) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.NullValue)
	 */
	@Override
	public void visit(NullValue nullValue) {
		this.targetExpression = new TargetExpression(Type.value);
		this.targetExpression.setValue(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.Function)
	 */
	@Override
	public void visit(Function function) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.SignedExpression)
	 */
	@Override
	public void visit(SignedExpression signedExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.JdbcParameter)
	 */
	@Override
	public void visit(JdbcParameter jdbcParameter) {
		output("JdbcParameter: " + jdbcParameter.getIndex());
		this.targetExpression = new TargetExpression(Type.indexedparameter);
		this.targetExpression.setIndex(currentParameterIndex);
		currentParameterIndex++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.JdbcNamedParameter)
	 */
	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		this.targetExpression = new TargetExpression(Type.namedparameter);
		this.targetExpression.setName(jdbcNamedParameter.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.DoubleValue)
	 */
	@Override
	public void visit(DoubleValue doubleValue) {
		this.targetExpression = new TargetExpression(Type.value);
		this.targetExpression.setValue(Double.valueOf(doubleValue.getValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.LongValue)
	 */
	@Override
	public void visit(LongValue longValue) {
		// output("LongValue: "+longValue.getValue());
		this.targetExpression = new TargetExpression(Type.value);
		this.targetExpression.setValue(Long.valueOf(longValue.getValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.HexValue)
	 */
	@Override
	public void visit(HexValue hexValue) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.DateValue)
	 */
	@Override
	public void visit(DateValue dateValue) {
		this.targetExpression = new TargetExpression(Type.value);
		this.targetExpression.setValue(dateValue.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.TimeValue)
	 */
	@Override
	public void visit(TimeValue timeValue) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.TimestampValue)
	 */
	@Override
	public void visit(TimestampValue timestampValue) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.Parenthesis)
	 */
	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.StringValue)
	 */
	@Override
	public void visit(StringValue stringValue) {
		// output("StringValue: "+stringValue.getValue()+", non-escaped:
		// "+stringValue.getNotExcapedValue());
		this.targetExpression = new TargetExpression(Type.value);
		this.targetExpression.setValue(stringValue.getNotExcapedValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Addition)
	 */
	@Override
	public void visit(Addition addition) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Division)
	 */
	@Override
	public void visit(Division division) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Multiplication)
	 */
	@Override
	public void visit(Multiplication multiplication) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Subtraction)
	 */
	@Override
	public void visit(Subtraction subtraction) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.conditional.AndExpression)
	 */
	@Override
	public void visit(AndExpression andExpression) {

		// output("AND");
		++indent;

		ConditionBuilder<?> origBuilder = whereBuilder;

		JunctionBuilder<?> conjunction = whereBuilder.conjunction();
		whereBuilder = conjunction;
		Expression left = andExpression.getLeftExpression();
		left.accept(this);
		Expression right = andExpression.getRightExpression();
		right.accept(this);
		conjunction.close();

		whereBuilder = origBuilder;

		--indent;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.conditional.OrExpression)
	 */
	@Override
	public void visit(OrExpression orExpression) {

		// output("OR");

		++indent;

		ConditionBuilder<?> origBuilder = whereBuilder;

		JunctionBuilder<?> disjunction = whereBuilder.disjunction();
		whereBuilder = disjunction;
		Expression left = orExpression.getLeftExpression();
		left.accept(this);
		Expression right = orExpression.getRightExpression();
		right.accept(this);
		disjunction.close();

		whereBuilder = origBuilder;

		--indent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.Between)
	 */
	@Override
	public void visit(Between between) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.EqualsTo)
	 */
	@Override
	public void visit(EqualsTo equalsTo) {

		// output("=");

		++indent;

		Expression left = equalsTo.getLeftExpression();
		Expression right = equalsTo.getRightExpression();
		processOperand(left, right, x -> x.eq());

		--indent;

	}

	/**
	 * Process operand.
	 *
	 * @param left
	 *            the left
	 * @param right
	 *            the right
	 * @param operandBuilder
	 *            the operand builder
	 */
	public void processOperand(Expression left, Expression right,
			java.util.function.Function<ValueComparisonBuilder<?>, OperandBuilder<?>> operandBuilder) {

		if (left instanceof Column) {
			Column col = (Column) left;
			col.setTable(translator.translateColumnAndTable(plainSelect, col));
		}
		left.accept(this);
		TargetExpression leftExpression = this.targetExpression;

		if (right instanceof Column) {
			Column col = (Column) right;
			col.setTable(translator.translateColumnAndTable(plainSelect, col));
		}
		right.accept(this);
		TargetExpression rightExpression = this.targetExpression;

		this.processOperand(leftExpression, rightExpression, operandBuilder);
	}

	/**
	 * Process operand.
	 *
	 * @param leftExpression
	 *            the left expression
	 * @param rightExpression
	 *            the right expression
	 * @param operandSupplier
	 *            the operand supplier
	 */
	public void processOperand(TargetExpression leftExpression, TargetExpression rightExpression,
			java.util.function.Function<ValueComparisonBuilder<?>, OperandBuilder<?>> operandSupplier) {

		// output("=");

		++indent;

		if (leftExpression.getType() == Type.indexedparameter || leftExpression.getType() == Type.namedparameter
				|| rightExpression.getType() == Type.indexedparameter || rightExpression.getType() == Type.namedparameter) {
			PlaceholderOperandBuilder pob = new PlaceholderOperandBuilder(whereBuilder, leftExpression, rightExpression,
					operandSupplier);
			for (int i = 0; i < pob.getNumberOfParameters(); ++i) {
				operandBuilders.add(pob);
			}
			return;
		}

		doLeftSide(leftExpression, rightExpression, operandSupplier);

		--indent;

	}

	/**
	 * Do left side.
	 *
	 * @param leftExpression
	 *            the left expression
	 * @param rightExpression
	 *            the right expression
	 * @param operandSupplier
	 *            the operand supplier
	 */
	private void doLeftSide(TargetExpression leftExpression, TargetExpression rightExpression,
			java.util.function.Function<ValueComparisonBuilder<?>, OperandBuilder<?>> operandSupplier) {
		switch (leftExpression.getType()) {
		case property:
			ValueComparisonBuilder<?> property;
			if (leftExpression.getAlias() == null) {
				property = whereBuilder.property(leftExpression.getName());
				// TODO what if virtual table without alias?
				// Table leftTable = translator.getTableByAlias(plainSelect,
				// leftExpression.name);
			} else {
				if (containsVirtualTables) {
					Table leftTable = translator.getTableByAlias(plainSelect, leftExpression.getAlias());
					Table rightTable = translator.getTableByAlias(plainSelect, rightExpression.getAlias());
					TfAssociationTable assTabLeft = connection.getAssociationTableByName(leftTable.getName());
					TfAssociationTable assTabRight = (rightTable == null) ? null
							: connection.getAssociationTableByName(rightTable.getName());

					if (assTabLeft != null && assTabLeft.isForTableAndVirtualTable(leftTable, rightTable)
							|| assTabRight != null && assTabRight.isForTableAndVirtualTable(leftTable, rightTable)) {

						// The carthesian binding WHERE is not necessary, its .contains would duplicate
						// the association which already bound via the join
						break;
					}

					if (assTabLeft != null) {
						// left expression side is virtual, but it corresponds to the right side of the
						// virtual table
						property = whereBuilder.property(leftExpression.getAlias(), assTabLeft.getRightProperty().getName());
						doRightSide(leftExpression, rightExpression, operandSupplier, property);
						break;
					}

				}
				property = whereBuilder.property(leftExpression.getAlias(), leftExpression.getName());
			}
			doRightSide(leftExpression, rightExpression, operandSupplier, property);
			break;
		case value:
			ValueComparisonBuilder<?> value = whereBuilder.value(leftExpression.getValue());
			doRightSide(leftExpression, rightExpression, operandSupplier, value);
			break;
		default:
			break;

		}
	}

	/**
	 * Do right side.
	 *
	 * @param leftExpression
	 *            the left expression
	 * @param rightExpression
	 *            the right expression
	 * @param operandSupplier
	 *            the operand supplier
	 * @param vcb
	 *            the vcb
	 */
	private void doRightSide(TargetExpression leftExpression, TargetExpression rightExpression,
			java.util.function.Function<ValueComparisonBuilder<?>, OperandBuilder<?>> operandSupplier,
			ValueComparisonBuilder<?> vcb) {
		switch (rightExpression.getType()) {
		case property:
			if (rightExpression.getAlias() == null) {
				OperandBuilder<?> namedOp = operandSupplier.apply(vcb);
				namedOp.property(rightExpression.getName());
				// TODO what if virtual table without alias?
			} else {
				if (containsVirtualTables) {
					Table leftTable = translator.getTableByAlias(plainSelect, leftExpression.getAlias());
					Table rightTable = translator.getTableByAlias(plainSelect, rightExpression.getAlias());
					TfAssociationTable assTab = connection.getAssociationTableByName(rightTable.getName());
					if (assTab != null && leftTable != null
							&& assTab.isForTableAndVirtualTable(leftTable, rightTable)) {
						// The carthesian binding WHERE is not necessary, its .contains would duplicate
						// the association which already bound via the join
						break;
					}
					if (assTab != null) {
						// right expression side is virtual, left expression side
						// is the left virtual side
						OperandBuilder<?> aliasedOp = operandSupplier.apply(vcb);
						aliasedOp.property(rightExpression.getAlias(), assTab.getLeftProperty().getName());
					}
				}
				OperandBuilder<?> aliasedOp = operandSupplier.apply(vcb);
				aliasedOp.property(rightExpression.getAlias(), rightExpression.getName());
			}
			break;
		case value:
			OperandBuilder<?> valueOp = operandSupplier.apply(vcb);
			valueOp.value(rightExpression.getValue());
			break;
		default:
			throw new UnsupportedOperationException(
					"Unsupported expression type " + rightExpression.getType() + " (" + rightExpression + ")");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.GreaterThan)
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
		Expression left = greaterThan.getLeftExpression();
		Expression right = greaterThan.getRightExpression();
		processOperand(left, right, x -> x.gt());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.GreaterThanEquals)
	 */
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		Expression left = greaterThanEquals.getLeftExpression();
		Expression right = greaterThanEquals.getRightExpression();
		processOperand(left, right, x -> x.ge());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.InExpression)
	 */
	@Override
	public void visit(InExpression inExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.IsNullExpression)
	 */
	@Override
	public void visit(IsNullExpression isNullExpression) {
		Expression left = isNullExpression.getLeftExpression();
		left.accept(this);
		TargetExpression leftExpression = this.targetExpression;
		TargetExpression rightExpression = new TargetExpression(Type.value);
		rightExpression.setValue(null);

		processOperand(leftExpression, rightExpression, x -> x.eq());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.LikeExpression)
	 */
	@Override
	public void visit(LikeExpression likeExpression) {
		Expression left = likeExpression.getLeftExpression();
		Expression right = likeExpression.getRightExpression();
		boolean caseInsensitive = likeExpression.isCaseInsensitive();

		left.accept(this);
		TargetExpression leftExpression = this.targetExpression;

		right.accept(this);
		TargetExpression rightExpression = this.targetExpression;

		if (leftExpression.getType() == Type.property) {

			if (!(rightExpression.getValue() instanceof String)) {
				throw new UnsupportedOperationException(
						"like is only supported when a property is compared to a value");
			}

			ValueComparisonBuilder<?> property;
			if (leftExpression.getAlias() == null) {
				property = whereBuilder.property(leftExpression.getName());
			} else {
				property = whereBuilder.property(leftExpression.getAlias(), leftExpression.getName());
			}

			switch (rightExpression.getType()) {
			case value:
				if (caseInsensitive) {
					property.ilike((String) rightExpression.getValue());
				} else {
					property.like((String) rightExpression.getValue());
				}
				break;
			default:
				throw new UnsupportedOperationException(
						"Unsupported expression type " + rightExpression.getType() + " (" + rightExpression + ")");
			}

		} else {
			throw new UnsupportedOperationException("like is only supported when a property is compared to a value");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.MinorThan)
	 */
	@Override
	public void visit(MinorThan minorThan) {
		Expression left = minorThan.getLeftExpression();
		Expression right = minorThan.getRightExpression();
		processOperand(left, right, x -> x.lt());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.MinorThanEquals)
	 */
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		Expression left = minorThanEquals.getLeftExpression();
		Expression right = minorThanEquals.getRightExpression();
		processOperand(left, right, x -> x.le());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.NotEqualsTo)
	 */
	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		Expression left = notEqualsTo.getLeftExpression();
		Expression right = notEqualsTo.getRightExpression();
		processOperand(left, right, x -> x.ne());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.NotExpression)
	 */
	@Override
	public void visit(NotExpression aThis) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.schema
	 * .Column)
	 */
	@Override
	public void visit(Column tableColumn) {
		String columnName = tableColumn.getColumnName();
		this.targetExpression = new TargetExpression(Type.property);
		this.targetExpression.setName(columnName);
		if (tableColumn.getTable() != null && tableColumn.getTable().getAlias() != null) {
			this.targetExpression.setAlias(tableColumn.getTable().getAlias().getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * statement.select.SubSelect)
	 */
	@Override
	public void visit(SubSelect subSelect) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.CaseExpression)
	 */
	@Override
	public void visit(CaseExpression caseExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.WhenClause)
	 */
	@Override
	public void visit(WhenClause whenClause) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.ExistsExpression)
	 */
	@Override
	public void visit(ExistsExpression existsExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.AllComparisonExpression)
	 */
	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.AnyComparisonExpression)
	 */
	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Concat)
	 */
	@Override
	public void visit(Concat concat) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.Matches)
	 */
	@Override
	public void visit(Matches matches) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseAnd)
	 */
	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseOr)
	 */
	@Override
	public void visit(BitwiseOr bitwiseOr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.BitwiseXor)
	 */
	@Override
	public void visit(BitwiseXor bitwiseXor) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.CastExpression)
	 */
	@Override
	public void visit(CastExpression cast) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.arithmetic.Modulo)
	 */
	@Override
	public void visit(Modulo modulo) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.AnalyticExpression)
	 */
	@Override
	public void visit(AnalyticExpression aexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.WithinGroupExpression)
	 */
	@Override
	public void visit(WithinGroupExpression wgexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.ExtractExpression)
	 */
	@Override
	public void visit(ExtractExpression eexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.IntervalExpression)
	 */
	@Override
	public void visit(IntervalExpression iexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.OracleHierarchicalExpression)
	 */
	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.RegExpMatchOperator)
	 */
	@Override
	public void visit(RegExpMatchOperator rexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.JsonExpression)
	 */
	@Override
	public void visit(JsonExpression jsonExpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.RegExpMySQLOperator)
	 */
	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.UserVariable)
	 */
	@Override
	public void visit(UserVariable var) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.NumericBind)
	 */
	@Override
	public void visit(NumericBind bind) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.KeepExpression)
	 */
	@Override
	public void visit(KeepExpression aexpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.MySQLGroupConcat)
	 */
	@Override
	public void visit(MySQLGroupConcat groupConcat) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.RowConstructor)
	 */
	@Override
	public void visit(RowConstructor rowConstructor) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.OracleHint)
	 */
	@Override
	public void visit(OracleHint hint) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.TimeKeyExpression)
	 */
	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.DateTimeLiteralExpression)
	 */
	@Override
	public void visit(DateTimeLiteralExpression literal) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/**
	 * Output.
	 *
	 * @param text
	 *            the text
	 */
	public void output(String text) {
		for (int i = 0; i < indent; ++i)
			System.out.print(" ");
		System.out.println(text);
	}

	/**
	 * To json string.
	 *
	 * @return the string
	 * @throws SQLException
	 *             the SQL exception
	 */
	public String toJsonString() throws SQLException {
		try {
			JsonStreamMarshaller m = new JsonStreamMarshaller();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			m.marshall(baos, query);
			return baos.toString("UTF-8");
		} catch (Exception e) {
			throw new SQLException("Could not create JSON string from query.", e);
		}
	}

	public SelectQuery getQuery() {
		return query;
	}

	public int getFromIndex() {
		return fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public List<PropertyWrapper> getSelectedProperties() {
		return selectedProperties;
	}

	public Map<String, PropertyWrapper> getSelectedPropertiesMap() {
		return selectedPropertiesMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return toJsonString();
		} catch (SQLException e) {
			throw new IllegalArgumentException("Problem converting to JSON string.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.
	 * expression.operators.relational.JsonOperator)
	 */
	@Override
	public void visit(JsonOperator jsonExpr) {
		throw new UnsupportedOperationException(TfMetadata.getUnsupportedMessage());
	}

	/**
	 * Contains virtual tables.
	 *
	 * @return true, if successful
	 */
	public boolean containsVirtualTables() {
		return containsVirtualTables;
	}

	public void setContainsVirtualTables(boolean containsVirtualTables) {
		this.containsVirtualTables = containsVirtualTables;
	}

}
