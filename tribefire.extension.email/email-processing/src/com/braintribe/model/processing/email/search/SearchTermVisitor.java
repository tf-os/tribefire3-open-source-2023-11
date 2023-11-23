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
package com.braintribe.model.processing.email.search;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SearchTermParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SearchTermVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#searchTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchTerm(SearchTermParser.SearchTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#negation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegation(SearchTermParser.NegationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#conjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunction(SearchTermParser.ConjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#disjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisjunction(SearchTermParser.DisjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom(SearchTermParser.FromContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#to}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo(SearchTermParser.ToContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#cc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCc(SearchTermParser.CcContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#bcc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBcc(SearchTermParser.BccContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#unread}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnread(SearchTermParser.UnreadContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#older}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOlder(SearchTermParser.OlderContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#younger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYounger(SearchTermParser.YoungerContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#recvdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecvdate(SearchTermParser.RecvdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#sentdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentdate(SearchTermParser.SentdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(SearchTermParser.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#subject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubject(SearchTermParser.SubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SearchTermParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#decimal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimal(SearchTermParser.DecimalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SearchTermParser#operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator(SearchTermParser.OperatorContext ctx);
}