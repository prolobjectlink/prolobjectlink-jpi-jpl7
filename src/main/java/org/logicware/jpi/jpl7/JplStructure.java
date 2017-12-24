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
import org.logicware.jpi.PrologProvider;
import org.logicware.jpi.PrologStructure;
import org.logicware.jpi.PrologTerm;

public class JplStructure extends JplTerm implements PrologStructure {

	JplStructure(PrologProvider provider, String functor, PrologTerm... arguments) {
		super(STRUCTURE_TYPE, provider);
		Term[] terms = new Term[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			terms[i] = unwrap(arguments[i], JplTerm.class).value;
		}
		value = new Compound(removeQuoted(functor), terms);
	}

	JplStructure(PrologProvider provider, String functor, Term... arguments) {
		super(STRUCTURE_TYPE, provider, new Compound(removeQuoted(functor), arguments));
	}

	JplStructure(PrologProvider provider, PrologTerm left, String operator, PrologTerm right) {
		super(STRUCTURE_TYPE, provider);
		Term leftOperand = left.unwrap(JplTerm.class).value;
		Term rightOperand = right.unwrap(JplTerm.class).value;
		value = new Compound(operator, new Term[] { leftOperand, rightOperand });
	}

	JplStructure(PrologProvider provider, Term left, String functor, Term right) {
		super(STRUCTURE_TYPE, provider, new Compound(functor, new Term[] { left, right }));
	}

	private static final boolean isQuoted(String functor) {
		if (!functor.isEmpty()) {
			char beginChar = functor.charAt(0);
			char endChar = functor.charAt(functor.length() - 1);
			return beginChar == '\'' && endChar == '\'';
		}
		return false;
	}

	private static final String removeQuoted(String functor) {
		if (isQuoted(functor)) {
			String newFunctor = "";
			newFunctor += functor.substring(1, functor.length() - 1);
			return newFunctor;
		}
		return functor;
	}

	private void checkIndexOutOfBound(int index, int lenght) {
		if (index < 0 || index > lenght) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	public PrologTerm getArgument(int index) {
		PrologTerm[] arguments = getArguments();
		checkIndexOutOfBound(index, arguments.length);
		return arguments[index];
	}

	@Override
	public PrologTerm[] getArguments() {
		Compound structure = (Compound) value;
		int arity = structure.arity();
		PrologTerm[] arguments = new PrologTerm[arity];
		for (int i = 0; i < arity; i++) {
			arguments[i] = toTerm(structure.arg(i + 1), PrologTerm.class);
		}
		return arguments;
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
	public String getIndicator() {
		return getFunctor() + "/" + getArity();
	}

	@Override
	public boolean hasIndicator(String functor, int arity) {
		return getFunctor().equals(functor) && getArity() == arity;
	}

	@Override
	public PrologTerm clone() {
		String f = getFunctor();
		PrologTerm[] a = getArguments();
		return new JplStructure(provider, f, a);
	}

}
