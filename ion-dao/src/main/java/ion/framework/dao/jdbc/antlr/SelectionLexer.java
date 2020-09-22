// Generated from Selection.g4 by ANTLR 4.4
package ion.framework.dao.jdbc.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SelectionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__5=1, T__4=2, T__3=3, T__2=4, T__1=5, T__0=6, SELECT=7, FROM=8, WHERE=9, 
		AS=10, Attribute=11, Value=12, Condition=13, Selection=14, COMMA=15, WS=16;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'"
	};
	public static final String[] ruleNames = {
		"T__5", "T__4", "T__3", "T__2", "T__1", "T__0", "Alpha", "Alphanumeric", 
		"SELECT", "FROM", "WHERE", "AS", "Attribute", "Value", "Condition", "Selection", 
		"COMMA", "WS"
	};


	public SelectionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Selection.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\22\u0082\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3"+
		"\7\3\b\5\b\67\n\b\3\t\3\t\5\t;\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\5\nI\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13S\n"+
		"\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f_\n\f\3\r\3\r\3\r\3\16"+
		"\3\16\6\16f\n\16\r\16\16\16g\3\17\3\17\6\17l\n\17\r\17\16\17m\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20y\n\20\3\21\3\22\3\22\3\23\6\23"+
		"\177\n\23\r\23\16\23\u0080\2\2\24\3\3\5\4\7\5\t\6\13\7\r\b\17\2\21\2\23"+
		"\t\25\n\27\13\31\f\33\r\35\16\37\17!\20#\21%\22\3\2\4\5\2C\\aac|\4\2\13"+
		"\13\"\"\u008a\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2"+
		"\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2"+
		"\3\'\3\2\2\2\5*\3\2\2\2\7-\3\2\2\2\t/\3\2\2\2\13\61\3\2\2\2\r\63\3\2\2"+
		"\2\17\66\3\2\2\2\21:\3\2\2\2\23H\3\2\2\2\25R\3\2\2\2\27^\3\2\2\2\31`\3"+
		"\2\2\2\33e\3\2\2\2\35i\3\2\2\2\37x\3\2\2\2!z\3\2\2\2#{\3\2\2\2%~\3\2\2"+
		"\2\'(\7K\2\2()\7P\2\2)\4\3\2\2\2*+\7k\2\2+,\7p\2\2,\6\3\2\2\2-.\7*\2\2"+
		".\b\3\2\2\2/\60\7+\2\2\60\n\3\2\2\2\61\62\7<\2\2\62\f\3\2\2\2\63\64\7"+
		",\2\2\64\16\3\2\2\2\65\67\t\2\2\2\66\65\3\2\2\2\67\20\3\2\2\28;\5\17\b"+
		"\29;\4\62;\2:8\3\2\2\2:9\3\2\2\2;\22\3\2\2\2<=\7U\2\2=>\7G\2\2>?\7N\2"+
		"\2?@\7G\2\2@A\7E\2\2AI\7V\2\2BC\7u\2\2CD\7g\2\2DE\7n\2\2EF\7g\2\2FG\7"+
		"e\2\2GI\7v\2\2H<\3\2\2\2HB\3\2\2\2I\24\3\2\2\2JK\7H\2\2KL\7T\2\2LM\7Q"+
		"\2\2MS\7O\2\2NO\7h\2\2OP\7t\2\2PQ\7q\2\2QS\7o\2\2RJ\3\2\2\2RN\3\2\2\2"+
		"S\26\3\2\2\2TU\7Y\2\2UV\7J\2\2VW\7G\2\2WX\7T\2\2X_\7G\2\2YZ\7y\2\2Z[\7"+
		"j\2\2[\\\7g\2\2\\]\7t\2\2]_\7g\2\2^T\3\2\2\2^Y\3\2\2\2_\30\3\2\2\2`a\7"+
		"C\2\2ab\7U\2\2b\32\3\2\2\2cf\5\21\t\2df\7\60\2\2ec\3\2\2\2ed\3\2\2\2f"+
		"g\3\2\2\2ge\3\2\2\2gh\3\2\2\2h\34\3\2\2\2ik\5\17\b\2jl\5\21\t\2kj\3\2"+
		"\2\2lm\3\2\2\2mk\3\2\2\2mn\3\2\2\2n\36\3\2\2\2oy\4>@\2pq\7#\2\2qy\7?\2"+
		"\2rs\7>\2\2sy\7?\2\2tu\7@\2\2uy\7?\2\2vw\7k\2\2wy\7p\2\2xo\3\2\2\2xp\3"+
		"\2\2\2xr\3\2\2\2xt\3\2\2\2xv\3\2\2\2y \3\2\2\2{|\7.\2\2|$\3\2\2\2}\177"+
		"\t\3\2\2~}\3\2\2\2\177\u0080\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2"+
		"\2\u0081&\3\2\2\2\r\2\66:HR^egmx\u0080\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}