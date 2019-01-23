package jp.ac.osaka_u.ist.sel.ccvolti.analyze;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class SilentErrorListener extends BaseErrorListener {
	public static SilentErrorListener INSTANCE = new SilentErrorListener();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		throw e;
	}
}