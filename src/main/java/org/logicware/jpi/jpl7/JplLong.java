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
package org.logicware.jpi.jpl7;

import static org.logicware.jpi.PrologTermType.LONG_TYPE;

import org.jpl7.Integer;
import org.logicware.jpi.ArityError;
import org.logicware.jpi.FunctorError;
import org.logicware.jpi.IndicatorError;
import org.logicware.jpi.PrologDouble;
import org.logicware.jpi.PrologFloat;
import org.logicware.jpi.PrologInteger;
import org.logicware.jpi.PrologLong;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologTerm;

public class JplLong extends JplTerm implements PrologLong {

	public JplLong(PrologProvider provider) {
		super(LONG_TYPE, provider, new Integer(0));
	}

	public JplLong(PrologProvider provider, Number value) {
		super(LONG_TYPE, provider, new Integer(value.longValue()));
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
		return new JplLong[0];
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
