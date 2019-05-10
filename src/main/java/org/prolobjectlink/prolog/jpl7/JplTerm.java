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
package org.prolobjectlink.prolog.jpl7;

import static org.prolobjectlink.prolog.PrologTermType.CUT_TYPE;
import static org.prolobjectlink.prolog.PrologTermType.DOUBLE_TYPE;
import static org.prolobjectlink.prolog.PrologTermType.FLOAT_TYPE;
import static org.prolobjectlink.prolog.PrologTermType.INTEGER_TYPE;
import static org.prolobjectlink.prolog.PrologTermType.LONG_TYPE;

import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.prolobjectlink.prolog.AbstractTerm;
import org.prolobjectlink.prolog.PrologProvider;
import org.prolobjectlink.prolog.PrologTerm;
import org.prolobjectlink.prolog.UnknownTermError;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
abstract class JplTerm extends AbstractTerm implements PrologTerm {

	protected Term value;

	public static final Term JPL_TRUE = new Atom("true");
	protected static final String SIMPLE_ATOM_REGEX = ".|[a-z][A-Za-z0-9_]*";

	protected JplTerm(int type, PrologProvider provider) {
		super(type, provider);
	}

	protected JplTerm(int type, PrologProvider provider, Term value) {
		super(type, provider);
		this.value = value;
	}

	public final boolean isAtom() {
		return value.isAtom();
	}

	public final boolean isNumber() {
		return isFloat() || isDouble() || isInteger() || isLong();
	}

	public final boolean isFloat() {
		return type == FLOAT_TYPE && value.isFloat();
	}

	public final boolean isDouble() {
		return type == DOUBLE_TYPE;
	}

	public final boolean isInteger() {
		return type == INTEGER_TYPE && value.isInteger();
	}

	public final boolean isLong() {
		return type == LONG_TYPE;
	}

	public final boolean isVariable() {
		return value.isVariable();
	}

	public final boolean isList() {
		return value.isListNil() || value.isListPair();
	}

	public final boolean isStructure() {
		return isCompound() && !isList();
	}

	public final boolean isNil() {
		if (!isVariable() && !isNumber()) {
			return value.hasFunctor("nil", 0);
		}
		return false;
	}

	public final boolean isEmptyList() {
		return value.isListNil();
	}

	public final boolean isEvaluable() {
		if (!isVariable() && !isList() && !isNumber() && getArity() == 2) {
			String key = "LIST";
			String opQuery = "findall(OP,current_op(_,_,OP)," + key + ")";
			Query query = new Query(opQuery);
			if (query.hasSolution()) {
				Term term = query.oneSolution().get(key);
				Term[] termArray = term.toTermArray();
				for (Term termArray1 : termArray) {
					if (termArray1.name().equals(getFunctor())) {
						return true;
					}
				}
			}
			query.close();
		}
		return false;
	}

	public final boolean isAtomic() {
		return !isCompound() && !isList();
	}

	public final boolean isCompound() {
		return value.isCompound();
	}

	public final PrologTerm getTerm() {
		return this;
	}

	public final boolean unify(PrologTerm o) {
		if (!(o instanceof JplTerm)) {
			throw new UnknownTermError(o);
		}
		return unify(((JplTerm) o).value);
	}

	protected final boolean unify(Term o) {
		String q = "unify_with_occurs_check(" + value + "," + o + ")";
		Query query = new Query(q);
		boolean result = query.hasSolution();
		query.close();
		return result;
	}

	public final int compareTo(PrologTerm o) {

		if (!(o instanceof JplTerm)) {
			throw new UnknownTermError(o);
		}

		String key = "Order";
		Term term = ((JplTerm) o).value;
		String arguments = key + "," + value + "," + term;
		Query query = new Query("compare(" + arguments + ")");

		query.open();
		Term order = query.getSolution().get(key);
		query.close();

		if (order.hasFunctor("<", 0)) {
			return -1;
		} else if (order.hasFunctor(">", 0)) {
			return 1;
		}

		return 0;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type;
		// Term not implement hashCode()
		result = prime * result + ((value == null) ? 0 : value.toString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		JplTerm other = (JplTerm) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!unify(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (type == CUT_TYPE) {
			return getFunctor();
		}
		return "" + value + "";
	}

}
