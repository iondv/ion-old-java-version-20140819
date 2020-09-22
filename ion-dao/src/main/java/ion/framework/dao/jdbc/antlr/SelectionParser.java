// Generated from Selection.g4 by ANTLR 4.4
package ion.framework.dao.jdbc.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SelectionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__5=1, T__4=2, T__3=3, T__2=4, T__1=5, T__0=6, SELECT=7, FROM=8, WHERE=9, 
		AS=10, Attribute=11, Value=12, Condition=13, Selection=14, COMMA=15, WS=16;
	public static final String[] tokenNames = {
		"<INVALID>", "'IN'", "'in'", "'('", "')'", "':'", "'*'", "SELECT", "FROM", 
		"WHERE", "'AS'", "Attribute", "Value", "Condition", "''", "','", "WS"
	};
	public static final int
		RULE_statement = 0, RULE_query = 1, RULE_selectClause = 2, RULE_selectAttribute = 3, 
		RULE_fromClause = 4, RULE_whereClause = 5, RULE_conditions = 6, RULE_condition = 7, 
		RULE_values = 8;
	public static final String[] ruleNames = {
		"statement", "query", "selectClause", "selectAttribute", "fromClause", 
		"whereClause", "conditions", "condition", "values"
	};

	@Override
	public String getGrammarFileName() { return "Selection.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SelectionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StatementContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(SelectionParser.EOF, 0); }
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SELECT || _la==FROM) {
				{
				{
				setState(18); query();
				}
				}
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(24); match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryContext extends ParserRuleContext {
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_query);
		try {
			setState(41);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(26); selectClause();
				setState(27); match(WS);
				setState(28); fromClause();
				setState(29); match(WS);
				setState(30); whereClause();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(32); fromClause();
				setState(33); match(WS);
				setState(34); whereClause();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(36); selectClause();
				setState(37); match(WS);
				setState(38); fromClause();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(40); fromClause();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectClauseContext extends ParserRuleContext {
		public List<SelectAttributeContext> selectAttribute() {
			return getRuleContexts(SelectAttributeContext.class);
		}
		public TerminalNode WS() { return getToken(SelectionParser.WS, 0); }
		public SelectAttributeContext selectAttribute(int i) {
			return getRuleContext(SelectAttributeContext.class,i);
		}
		public TerminalNode SELECT() { return getToken(SelectionParser.SELECT, 0); }
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitSelectClause(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_selectClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43); match(SELECT);
			setState(44); match(WS);
			setState(46); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(45); selectAttribute();
				}
				}
				setState(48); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__0 || _la==Attribute );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectAttributeContext extends ParserRuleContext {
		public Token attribute;
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public TerminalNode COMMA() { return getToken(SelectionParser.COMMA, 0); }
		public TerminalNode Attribute() { return getToken(SelectionParser.Attribute, 0); }
		public SelectAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterSelectAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitSelectAttribute(this);
		}
	}

	public final SelectAttributeContext selectAttribute() throws RecognitionException {
		SelectAttributeContext _localctx = new SelectAttributeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_selectAttribute);
		int _la;
		try {
			setState(61);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50);
				((SelectAttributeContext)_localctx).attribute = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==Attribute) ) {
					((SelectAttributeContext)_localctx).attribute = (Token)_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51);
				((SelectAttributeContext)_localctx).attribute = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==Attribute) ) {
					((SelectAttributeContext)_localctx).attribute = (Token)_errHandler.recoverInline(this);
				}
				consume();
				setState(52); match(COMMA);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(53);
				((SelectAttributeContext)_localctx).attribute = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==Attribute) ) {
					((SelectAttributeContext)_localctx).attribute = (Token)_errHandler.recoverInline(this);
				}
				consume();
				setState(55);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(54); match(WS);
					}
				}

				setState(57); match(COMMA);
				setState(59);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(58); match(WS);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromClauseContext extends ParserRuleContext {
		public Token className;
		public Token classNameAlias;
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public TerminalNode FROM() { return getToken(SelectionParser.FROM, 0); }
		public TerminalNode Attribute(int i) {
			return getToken(SelectionParser.Attribute, i);
		}
		public List<TerminalNode> Attribute() { return getTokens(SelectionParser.Attribute); }
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitFromClause(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_fromClause);
		try {
			setState(71);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(63); match(FROM);
				setState(64); match(WS);
				setState(65); ((FromClauseContext)_localctx).className = match(Attribute);
				setState(66); match(WS);
				setState(67); ((FromClauseContext)_localctx).classNameAlias = match(Attribute);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(68); match(FROM);
				setState(69); match(WS);
				setState(70); ((FromClauseContext)_localctx).className = match(Attribute);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereClauseContext extends ParserRuleContext {
		public List<ConditionsContext> conditions() {
			return getRuleContexts(ConditionsContext.class);
		}
		public TerminalNode WS() { return getToken(SelectionParser.WS, 0); }
		public TerminalNode WHERE() { return getToken(SelectionParser.WHERE, 0); }
		public ConditionsContext conditions(int i) {
			return getRuleContext(ConditionsContext.class,i);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitWhereClause(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_whereClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73); match(WHERE);
			setState(74); match(WS);
			setState(76); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(75); conditions();
				}
				}
				setState(78); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Attribute );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionsContext extends ParserRuleContext {
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public TerminalNode COMMA() { return getToken(SelectionParser.COMMA, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public ConditionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterConditions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitConditions(this);
		}
	}

	public final ConditionsContext conditions() throws RecognitionException {
		ConditionsContext _localctx = new ConditionsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_conditions);
		int _la;
		try {
			setState(92);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(80); condition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(81); condition();
				setState(82); match(COMMA);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(84); condition();
				setState(86);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(85); match(WS);
					}
				}

				setState(88); match(COMMA);
				setState(90);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(89); match(WS);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionContext extends ParserRuleContext {
		public Token condAttr;
		public Token condType;
		public Token condValue;
		public TerminalNode Value() { return getToken(SelectionParser.Value, 0); }
		public ValuesContext values(int i) {
			return getRuleContext(ValuesContext.class,i);
		}
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public TerminalNode Condition() { return getToken(SelectionParser.Condition, 0); }
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public List<ValuesContext> values() {
			return getRuleContexts(ValuesContext.class);
		}
		public TerminalNode Attribute() { return getToken(SelectionParser.Attribute, 0); }
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitCondition(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		ConditionContext _localctx = new ConditionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_condition);
		int _la;
		try {
			setState(116);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(94); ((ConditionContext)_localctx).condAttr = match(Attribute);
				setState(95); match(WS);
				setState(96); ((ConditionContext)_localctx).condType = match(Condition);
				setState(97); match(WS);
				setState(98); match(T__1);
				setState(99); ((ConditionContext)_localctx).condValue = match(Value);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(100); ((ConditionContext)_localctx).condAttr = match(Attribute);
				setState(101); ((ConditionContext)_localctx).condType = match(Condition);
				setState(102); match(T__1);
				setState(103); ((ConditionContext)_localctx).condValue = match(Value);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(104); ((ConditionContext)_localctx).condAttr = match(Attribute);
				setState(105); match(WS);
				setState(106);
				((ConditionContext)_localctx).condType = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==T__5 || _la==T__4) ) {
					((ConditionContext)_localctx).condType = (Token)_errHandler.recoverInline(this);
				}
				consume();
				setState(107); match(WS);
				setState(108); match(T__3);
				setState(110); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(109); values();
					}
					}
					setState(112); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << SELECT) | (1L << FROM))) != 0) );
				setState(114); match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValuesContext extends ParserRuleContext {
		public Token condValue;
		public TerminalNode Value() { return getToken(SelectionParser.Value, 0); }
		public TerminalNode WS(int i) {
			return getToken(SelectionParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(SelectionParser.WS); }
		public TerminalNode COMMA() { return getToken(SelectionParser.COMMA, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public ValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).enterValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SelectionListener ) ((SelectionListener)listener).exitValues(this);
		}
	}

	public final ValuesContext values() throws RecognitionException {
		ValuesContext _localctx = new ValuesContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_values);
		int _la;
		try {
			setState(133);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(118); match(T__1);
				setState(119); ((ValuesContext)_localctx).condValue = match(Value);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(120); match(T__1);
				setState(121); ((ValuesContext)_localctx).condValue = match(Value);
				setState(122); match(COMMA);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(123); match(T__1);
				setState(124); ((ValuesContext)_localctx).condValue = match(Value);
				setState(126);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(125); match(WS);
					}
				}

				setState(128); match(COMMA);
				setState(130);
				_la = _input.LA(1);
				if (_la==WS) {
					{
					setState(129); match(WS);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(132); query();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\22\u008a\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\7"+
		"\2\26\n\2\f\2\16\2\31\13\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\5\3,\n\3\3\4\3\4\3\4\6\4\61\n\4\r\4\16\4\62\3"+
		"\5\3\5\3\5\3\5\3\5\5\5:\n\5\3\5\3\5\5\5>\n\5\5\5@\n\5\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\5\6J\n\6\3\7\3\7\3\7\6\7O\n\7\r\7\16\7P\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\5\bY\n\b\3\b\3\b\5\b]\n\b\5\b_\n\b\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\6\tq\n\t\r\t\16\tr\3\t\3\t\5"+
		"\tw\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u0081\n\n\3\n\3\n\5\n\u0085"+
		"\n\n\3\n\5\n\u0088\n\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\4\4\2\b\b\r\r"+
		"\3\2\3\4\u0097\2\27\3\2\2\2\4+\3\2\2\2\6-\3\2\2\2\b?\3\2\2\2\nI\3\2\2"+
		"\2\fK\3\2\2\2\16^\3\2\2\2\20v\3\2\2\2\22\u0087\3\2\2\2\24\26\5\4\3\2\25"+
		"\24\3\2\2\2\26\31\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\32\3\2\2\2\31"+
		"\27\3\2\2\2\32\33\7\2\2\3\33\3\3\2\2\2\34\35\5\6\4\2\35\36\7\22\2\2\36"+
		"\37\5\n\6\2\37 \7\22\2\2 !\5\f\7\2!,\3\2\2\2\"#\5\n\6\2#$\7\22\2\2$%\5"+
		"\f\7\2%,\3\2\2\2&\'\5\6\4\2\'(\7\22\2\2()\5\n\6\2),\3\2\2\2*,\5\n\6\2"+
		"+\34\3\2\2\2+\"\3\2\2\2+&\3\2\2\2+*\3\2\2\2,\5\3\2\2\2-.\7\t\2\2.\60\7"+
		"\22\2\2/\61\5\b\5\2\60/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2"+
		"\2\2\63\7\3\2\2\2\64@\t\2\2\2\65\66\t\2\2\2\66@\7\21\2\2\679\t\2\2\28"+
		":\7\22\2\298\3\2\2\29:\3\2\2\2:;\3\2\2\2;=\7\21\2\2<>\7\22\2\2=<\3\2\2"+
		"\2=>\3\2\2\2>@\3\2\2\2?\64\3\2\2\2?\65\3\2\2\2?\67\3\2\2\2@\t\3\2\2\2"+
		"AB\7\n\2\2BC\7\22\2\2CD\7\r\2\2DE\7\22\2\2EJ\7\r\2\2FG\7\n\2\2GH\7\22"+
		"\2\2HJ\7\r\2\2IA\3\2\2\2IF\3\2\2\2J\13\3\2\2\2KL\7\13\2\2LN\7\22\2\2M"+
		"O\5\16\b\2NM\3\2\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q\r\3\2\2\2R_\5\20\t"+
		"\2ST\5\20\t\2TU\7\21\2\2U_\3\2\2\2VX\5\20\t\2WY\7\22\2\2XW\3\2\2\2XY\3"+
		"\2\2\2YZ\3\2\2\2Z\\\7\21\2\2[]\7\22\2\2\\[\3\2\2\2\\]\3\2\2\2]_\3\2\2"+
		"\2^R\3\2\2\2^S\3\2\2\2^V\3\2\2\2_\17\3\2\2\2`a\7\r\2\2ab\7\22\2\2bc\7"+
		"\17\2\2cd\7\22\2\2de\7\7\2\2ew\7\16\2\2fg\7\r\2\2gh\7\17\2\2hi\7\7\2\2"+
		"iw\7\16\2\2jk\7\r\2\2kl\7\22\2\2lm\t\3\2\2mn\7\22\2\2np\7\5\2\2oq\5\22"+
		"\n\2po\3\2\2\2qr\3\2\2\2rp\3\2\2\2rs\3\2\2\2st\3\2\2\2tu\7\6\2\2uw\3\2"+
		"\2\2v`\3\2\2\2vf\3\2\2\2vj\3\2\2\2w\21\3\2\2\2xy\7\7\2\2y\u0088\7\16\2"+
		"\2z{\7\7\2\2{|\7\16\2\2|\u0088\7\21\2\2}~\7\7\2\2~\u0080\7\16\2\2\177"+
		"\u0081\7\22\2\2\u0080\177\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0082\3\2"+
		"\2\2\u0082\u0084\7\21\2\2\u0083\u0085\7\22\2\2\u0084\u0083\3\2\2\2\u0084"+
		"\u0085\3\2\2\2\u0085\u0088\3\2\2\2\u0086\u0088\5\4\3\2\u0087x\3\2\2\2"+
		"\u0087z\3\2\2\2\u0087}\3\2\2\2\u0087\u0086\3\2\2\2\u0088\23\3\2\2\2\22"+
		"\27+\629=?IPX\\^rv\u0080\u0084\u0087";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}