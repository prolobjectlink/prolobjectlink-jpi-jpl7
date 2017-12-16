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

import org.jpl7.Compound;
import org.jpl7.Term;
import org.logicware.jpi.PrologExpression;
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologTerm;

public final class JplExpression extends JplTerm implements PrologExpression {

    @Deprecated
    JplExpression(PrologProvider provider, PrologTerm left, String operator, PrologTerm right) {
	super(EXPRESSION_TYPE, provider);
	Term leftOperand = left.unwrap(JplTerm.class).value;
	Term rightOperand = right.unwrap(JplTerm.class).value;
	value = new Compound(operator, new Term[] { leftOperand, rightOperand });
    }

    @Deprecated
    JplExpression(PrologProvider provider, Term left, String functor, Term right) {
	super(EXPRESSION_TYPE, provider, new Compound(functor, new Term[] { left, right }));
    }

    public String getOperator() {
	return getFunctor();
    }

    public PrologTerm getLeft() {
	return provider.toTerm(((Compound) value).arg(1), PrologTerm.class);
    }

    public PrologTerm getRight() {
	return provider.toTerm(((Compound) value).arg(2), PrologTerm.class);
    }

    @Override
    public int getArity() {
	Compound structure = (Compound) value;
	return structure.arity();
    }

    @Override
    public String getFunctor() {
	Compound structure = (Compound) value;
	return structure.name();
    }

    @Override
    public PrologTerm[] getArguments() {
	return new PrologTerm[] { getLeft(), getRight() };
    }

    @Override
    public String getIndicator() {
	return getFunctor() + "/" + getArity();
    }

    @Override
    public boolean hasIndicator(String functor, int arity) {
	return getFunctor().equals(functor) && getArity() == arity;
    }

    @Override
    public PrologTerm clone() {
	PrologTerm l = getLeft();
	String o = getOperator();
	PrologTerm r = getRight();
	return new JplExpression(provider, l, o, r);
    }

}
