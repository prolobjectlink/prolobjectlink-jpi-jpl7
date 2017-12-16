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

import org.jpl7.Variable;
import org.logicware.jpi.ArityError;
import org.logicware.jpi.FunctorError;
import org.logicware.jpi.IndicatorError;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologTerm;
import org.logicware.jpi.PrologVariable;

public class JplVariable extends JplTerm implements PrologVariable {

    JplVariable(PrologProvider provider) {
	super(VARIABLE_TYPE, provider, new Variable("_"));
    }

    JplVariable(PrologProvider provider, String name) {
	super(VARIABLE_TYPE, provider, new Variable(name));
    }

    public boolean isAnonymous() {
	return ((Variable) value).name().equals("_");
    }

    public String getName() {
	return ((Variable) value).name();
    }

    public void setName(String name) {
	this.value = new Variable(name);
    }

    @Override
    public PrologTerm[] getArguments() {
	return new JplVariable[0];
    }

    @Override
    public int getArity() {
	throw new ArityError(this);
    }

    @Override
    public String getFunctor() {
	throw new FunctorError(this);
    }

    @Override
    public String getIndicator() {
	throw new IndicatorError(this);
    }

    @Override
    public boolean hasIndicator(String functor, int arity) {
	throw new IndicatorError(this);
    }

    public int getPosition() {
	throw new UnsupportedOperationException("getPosition()");
    }

    @Override
    public PrologTerm clone() {
	String n = getName();
	return new JplVariable(provider, n);
    }

}
