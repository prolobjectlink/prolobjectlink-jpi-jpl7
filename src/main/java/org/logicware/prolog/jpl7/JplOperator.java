package org.logicware.prolog.jpl7;

import org.logicware.prolog.AbstractOperator;
import org.logicware.prolog.PrologOperator;

public final class JplOperator extends AbstractOperator implements PrologOperator {

	public JplOperator(int priority, String specifier, String operator) {
		super(priority, specifier, operator);
	}

}
