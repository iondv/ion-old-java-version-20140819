// Generated from Selection.g4 by ANTLR 4.4
package ion.framework.dao.jdbc.antlr;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SelectionParser}.
 */
public interface SelectionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SelectionParser#selectAttribute}.
	 * @param ctx the parse tree
	 */
	void enterSelectAttribute(@NotNull SelectionParser.SelectAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#selectAttribute}.
	 * @param ctx the parse tree
	 */
	void exitSelectAttribute(@NotNull SelectionParser.SelectAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(@NotNull SelectionParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(@NotNull SelectionParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(@NotNull SelectionParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(@NotNull SelectionParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(@NotNull SelectionParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(@NotNull SelectionParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(@NotNull SelectionParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(@NotNull SelectionParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(@NotNull SelectionParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(@NotNull SelectionParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#values}.
	 * @param ctx the parse tree
	 */
	void enterValues(@NotNull SelectionParser.ValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#values}.
	 * @param ctx the parse tree
	 */
	void exitValues(@NotNull SelectionParser.ValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(@NotNull SelectionParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(@NotNull SelectionParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SelectionParser#conditions}.
	 * @param ctx the parse tree
	 */
	void enterConditions(@NotNull SelectionParser.ConditionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SelectionParser#conditions}.
	 * @param ctx the parse tree
	 */
	void exitConditions(@NotNull SelectionParser.ConditionsContext ctx);
}