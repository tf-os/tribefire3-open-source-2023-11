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
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SearchTermParser}.
 */
public interface SearchTermListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#searchTerm}.
	 * @param ctx the parse tree
	 */
	void enterSearchTerm(SearchTermParser.SearchTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#searchTerm}.
	 * @param ctx the parse tree
	 */
	void exitSearchTerm(SearchTermParser.SearchTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(SearchTermParser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(SearchTermParser.NegationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(SearchTermParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(SearchTermParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(SearchTermParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(SearchTermParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#from}.
	 * @param ctx the parse tree
	 */
	void enterFrom(SearchTermParser.FromContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#from}.
	 * @param ctx the parse tree
	 */
	void exitFrom(SearchTermParser.FromContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#to}.
	 * @param ctx the parse tree
	 */
	void enterTo(SearchTermParser.ToContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#to}.
	 * @param ctx the parse tree
	 */
	void exitTo(SearchTermParser.ToContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#cc}.
	 * @param ctx the parse tree
	 */
	void enterCc(SearchTermParser.CcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#cc}.
	 * @param ctx the parse tree
	 */
	void exitCc(SearchTermParser.CcContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#bcc}.
	 * @param ctx the parse tree
	 */
	void enterBcc(SearchTermParser.BccContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#bcc}.
	 * @param ctx the parse tree
	 */
	void exitBcc(SearchTermParser.BccContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#unread}.
	 * @param ctx the parse tree
	 */
	void enterUnread(SearchTermParser.UnreadContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#unread}.
	 * @param ctx the parse tree
	 */
	void exitUnread(SearchTermParser.UnreadContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#older}.
	 * @param ctx the parse tree
	 */
	void enterOlder(SearchTermParser.OlderContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#older}.
	 * @param ctx the parse tree
	 */
	void exitOlder(SearchTermParser.OlderContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#younger}.
	 * @param ctx the parse tree
	 */
	void enterYounger(SearchTermParser.YoungerContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#younger}.
	 * @param ctx the parse tree
	 */
	void exitYounger(SearchTermParser.YoungerContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#recvdate}.
	 * @param ctx the parse tree
	 */
	void enterRecvdate(SearchTermParser.RecvdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#recvdate}.
	 * @param ctx the parse tree
	 */
	void exitRecvdate(SearchTermParser.RecvdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#sentdate}.
	 * @param ctx the parse tree
	 */
	void enterSentdate(SearchTermParser.SentdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#sentdate}.
	 * @param ctx the parse tree
	 */
	void exitSentdate(SearchTermParser.SentdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#body}.
	 * @param ctx the parse tree
	 */
	void enterBody(SearchTermParser.BodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#body}.
	 * @param ctx the parse tree
	 */
	void exitBody(SearchTermParser.BodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#subject}.
	 * @param ctx the parse tree
	 */
	void enterSubject(SearchTermParser.SubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#subject}.
	 * @param ctx the parse tree
	 */
	void exitSubject(SearchTermParser.SubjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SearchTermParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SearchTermParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#decimal}.
	 * @param ctx the parse tree
	 */
	void enterDecimal(SearchTermParser.DecimalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#decimal}.
	 * @param ctx the parse tree
	 */
	void exitDecimal(SearchTermParser.DecimalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchTermParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(SearchTermParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchTermParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(SearchTermParser.OperatorContext ctx);
}