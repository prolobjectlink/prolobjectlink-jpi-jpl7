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

import static org.logicware.pdb.prolog.PrologTermType.INTEGER_TYPE;

import org.jpl7.Integer;
import org.logicware.pdb.prolog.ArityError;
import org.logicware.pdb.prolog.FunctorError;
import org.logicware.pdb.prolog.IndicatorError;
import org.logicware.pdb.prolog.PrologDouble;
import org.logicware.pdb.prolog.PrologFloat;
import org.logicware.pdb.prolog.PrologInteger;
import org.logicware.pdb.prolog.PrologLong;
import org.logicware.pdb.prolog.PrologProvider;
import org.logicware.pdb.prolog.PrologTerm;

public class JplInteger extends JplTerm implements PrologInteger {

	public JplInteger(PrologProvider provider) {
		super(INTEGER_TYPE, provider, new Integer(0));
	}

	public JplInteger(PrologProvider provider, Number value) {
		super(INTEGER_TYPE, provider, new Integer(value.intValue()));
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
		return ((Integer) value).longValue();
	}

	public double getDoubleValue() {
		return ((Integer) value).doubleValue();
	}

	public int getIntValue() {
		return ((Integer) value).intValue();
	}

	public float getFloatValue() {
		return ((Integer) value).floatValue();
	}

	public PrologTerm[] getArguments() {
		return new JplInteger[0];
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
