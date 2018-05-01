/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2017 Logicware Project
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

import static org.logicware.pdb.prolog.PrologTermType.FLOAT_TYPE;

import org.jpl7.Float;
import org.logicware.pdb.prolog.ArityError;
import org.logicware.pdb.prolog.FunctorError;
import org.logicware.pdb.prolog.IndicatorError;
import org.logicware.pdb.prolog.PrologDouble;
import org.logicware.pdb.prolog.PrologFloat;
import org.logicware.pdb.prolog.PrologInteger;
import org.logicware.pdb.prolog.PrologLong;
import org.logicware.pdb.prolog.PrologProvider;
import org.logicware.pdb.prolog.PrologTerm;

public final class JplFloat extends JplTerm implements PrologFloat {

	public JplFloat(PrologProvider provider) {
		super(FLOAT_TYPE, provider, new Float(0));
	}

	public JplFloat(PrologProvider provider, Number value) {
		super(FLOAT_TYPE, provider, new Float(value.floatValue()));
	}

	public PrologInteger getPrologInteger() {
		return new JplInteger(provider, getIntValue());
	}

	public PrologFloat getPrologFloat() {
		return new JplFloat(provider, getFloatValue());
	}

	public PrologDouble getPrologDouble() {
		return new JplDouble(provider, getDoubleValue());
	}

	public PrologLong getPrologLong() {
		return new JplLong(provider, getLongValue());
	}

	public long getLongValue() {
		return ((Float) value).longValue();
	}

	public double getDoubleValue() {
		return ((Float) value).doubleValue();
	}

	public int getIntValue() {
		return ((Float) value).intValue();
	}

	public float getFloatValue() {
		return ((Float) value).floatValue();
	}

	public PrologTerm[] getArguments() {
		return new JplFloat[0];
	}

	public int getArity() {
		throw new ArityError(this);
	}

	public String getFunctor() {
		throw new FunctorError(this);
	}

	public String getIndicator() {
		throw new IndicatorError(this);
	}

	public boolean hasIndicator(String functor, int arity) {
		throw new IndicatorError(this);
	}

}
