/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2017 WorkLogic Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.logicware.prolog.jpl7;

import static org.logicware.prolog.PrologTermType.FLOAT_TYPE;

import org.jpl7.Float;
import org.jpl7.Term;
import org.logicware.prolog.PrologFloat;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologTerm;

public class JplFloat extends JplNumber implements PrologFloat {

	public JplFloat(PrologProvider provider) {
		super(FLOAT_TYPE, provider, new Float(0));
	}

	public JplFloat(PrologProvider provider, Number value) {
		super(FLOAT_TYPE, provider, new Float(value.floatValue()));
	}

	public JplFloat(int type, PrologProvider provider, Term value) {
		super(type, provider, value);
	}

	public final long getLongValue() {
		return ((Float) value).longValue();
	}

	public final double getDoubleValue() {
		return ((Float) value).doubleValue();
	}

	public final int getIntValue() {
		return ((Float) value).intValue();
	}

	public final float getFloatValue() {
		return ((Float) value).floatValue();
	}

	public final PrologTerm[] getArguments() {
		return new JplFloat[0];
	}

}
