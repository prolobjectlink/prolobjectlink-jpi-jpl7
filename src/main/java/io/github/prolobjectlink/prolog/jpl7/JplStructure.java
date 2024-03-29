/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2019 Prolobjectlink Project
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package io.github.prolobjectlink.prolog.jpl7;

import static io.github.prolobjectlink.prolog.PrologTermType.STRUCTURE_TYPE;

import org.jpl7.Compound;
import org.jpl7.Term;

import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologStructure;
import io.github.prolobjectlink.prolog.PrologTerm;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
class JplStructure extends JplTerm implements PrologStructure {

	JplStructure(PrologProvider provider, String functor, PrologTerm... arguments) {
		super(STRUCTURE_TYPE, provider);
		Term[] terms = new Term[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			terms[i] = ((JplTerm) arguments[i]).value;
		}
		value = new Compound(removeQuoted(functor), terms);
	}

	JplStructure(PrologProvider provider, String functor, Term... arguments) {
		super(STRUCTURE_TYPE, provider);
		value = new Compound(removeQuoted(functor), arguments);
	}

	JplStructure(PrologProvider provider, PrologTerm left, String operator, PrologTerm right) {
		super(STRUCTURE_TYPE, provider);
		Term leftOperand = ((JplTerm) left).value;
		Term rightOperand = ((JplTerm) right).value;
		value = new Compound(operator, new Term[] { leftOperand, rightOperand });
	}

	JplStructure(PrologProvider provider, Term left, String functor, Term right) {
		super(STRUCTURE_TYPE, provider, new Compound(functor, new Term[] { left, right }));
	}

	JplStructure(int objectType, PrologProvider provider, Term newJRef) {
		super(objectType, provider, newJRef);
	}

	public PrologTerm getArgument(int index) {
		checkIndex(index, getArity());
		return getArguments()[index];
	}

	public PrologTerm[] getArguments() {
		Compound structure = (Compound) value;
		int arity = structure.arity();
		PrologTerm[] arguments = new PrologTerm[arity];
		for (int i = 0; i < arity; i++) {
			arguments[i] = toTerm(structure.arg(i + 1), PrologTerm.class);
		}
		return arguments;
	}

	public int getArity() {
		Compound structure = (Compound) value;
		return structure.arity();
	}

	public String getFunctor() {
		Compound structure = (Compound) value;
		return structure.name();
	}

	public final PrologTerm getRight() {
		return getArgument(1);
	}

	public final PrologTerm getLeft() {
		return getArgument(0);
	}

	public final String getOperator() {
		return getFunctor();
	}

}
