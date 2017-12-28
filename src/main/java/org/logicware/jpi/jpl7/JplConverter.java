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

import static org.logicware.jpi.PrologTermType.ATOM_TYPE;
import static org.logicware.jpi.PrologTermType.CUT_TYPE;
import static org.logicware.jpi.PrologTermType.DOUBLE_TYPE;
import static org.logicware.jpi.PrologTermType.EMPTY_TYPE;
import static org.logicware.jpi.PrologTermType.FAIL_TYPE;
import static org.logicware.jpi.PrologTermType.FALSE_TYPE;
import static org.logicware.jpi.PrologTermType.FLOAT_TYPE;
import static org.logicware.jpi.PrologTermType.INTEGER_TYPE;
import static org.logicware.jpi.PrologTermType.LIST_TYPE;
import static org.logicware.jpi.PrologTermType.LONG_TYPE;
import static org.logicware.jpi.PrologTermType.NIL_TYPE;
import static org.logicware.jpi.PrologTermType.STRUCTURE_TYPE;
import static org.logicware.jpi.PrologTermType.TRUE_TYPE;
import static org.logicware.jpi.PrologTermType.VARIABLE_TYPE;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Float;
import org.jpl7.Integer;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;
import org.logicware.jpi.AbstractConverter;
import org.logicware.jpi.PrologAtom;
import org.logicware.jpi.PrologConverter;
import org.logicware.jpi.PrologDouble;
import org.logicware.jpi.PrologFloat;
import org.logicware.jpi.PrologInteger;
import org.logicware.jpi.PrologLong;
import org.logicware.jpi.PrologStructure;
import org.logicware.jpi.PrologTerm;
import org.logicware.jpi.PrologVariable;
import org.logicware.jpi.UnknownTermError;

public abstract class JplConverter extends AbstractConverter<Term> implements PrologConverter<Term> {

	public PrologTerm toTerm(Term prologTerm) {
		if (prologTerm.isAtom()) {
			String functor = prologTerm.name();
			if (functor.equals("nil")) {
				return new JplNil(provider);
			} else if (functor.equals("!")) {
				return new JplCut(createProvider());
			} else if (functor.equals("fail")) {
				return new JplFail(provider);
			} else if (functor.equals("true")) {
				return new JplTrue(provider);
			} else if (functor.equals("false")) {
				return new JplFalse(provider);
			} else if (functor.equals("[]")) {
				return new JplEmpty(provider);
			}
			return new JplAtom(provider, functor);
		} else if (prologTerm.isListNil()) {
			return new JplEmpty(provider);
		} else if (prologTerm.isFloat()) {
			return new JplFloat(provider, ((Float) prologTerm).floatValue());
		} else if (prologTerm.isBigInteger()) {
			return new JplLong(provider, ((Integer) prologTerm).longValue());
		} else if (prologTerm.isInteger()) {
			return new JplInteger(provider, ((Integer) prologTerm).intValue());
		} else if (prologTerm.isVariable()) {
			String name = ((Variable) prologTerm).name();
			PrologVariable variable = sharedVariables.get(name);
			if (variable == null) {
				variable = new JplVariable(provider, name);
				sharedVariables.put(variable.getName(), variable);
			}
			return variable;
		} else if (prologTerm.isListPair()) {
			return new JplList(provider, prologTerm.toTermArray());
		} else if (prologTerm.isCompound()) {
			Compound compound = (Compound) prologTerm;
			int arity = compound.arity();
			String functor = compound.name();
			Term[] arguments = new Term[arity];

			if (arity == 2) {
				String key = "LIST";
				String opQuery = "findall(OP,current_op(_,_,OP)," + key + ")";
				Query query = new Query(opQuery);
				if (query.hasSolution()) {

					Term term = query.oneSolution().get(key);
					Term[] termArray = term.toTermArray();
					for (int i = 0; i < termArray.length; i++) {
						if (termArray[i].name().equals(functor)) {
							Term left = compound.arg(1);
							Term right = compound.arg(2);
							return new JplStructure(provider, left, functor, right);
						}
					}

				}
			}

			for (int i = 0; i < arity; i++) {
				arguments[i] = compound.arg(i + 1);
			}
			return new JplStructure(provider, functor, arguments);

		}

		throw new UnknownTermError(prologTerm);
	}

	public Term fromTerm(PrologTerm term) {
		switch (term.getType()) {
		case NIL_TYPE:
			return new Atom("nil");
		case CUT_TYPE:
			return new Atom("!");
		case FAIL_TYPE:
			return new Atom("fail");
		case TRUE_TYPE:
			return new Atom("true");
		case FALSE_TYPE:
			return new Atom("false");
		case EMPTY_TYPE:
			return JplEmpty.EMPTY;
		case ATOM_TYPE:
			return new Atom(removeQuoted(((PrologAtom) term).getStringValue()));
		case FLOAT_TYPE:
			return new Float(((PrologFloat) term).getFloatValue());
		case INTEGER_TYPE:
			return new Integer(((PrologInteger) term).getIntValue());
		case DOUBLE_TYPE:
			return new Float(((PrologDouble) term).getDoubleValue());
		case LONG_TYPE:
			return new Integer(((PrologLong) term).getLongValue());
		case VARIABLE_TYPE:
			String name = ((PrologVariable) term).getName();
			Term variable = sharedPrologVariables.get(name);
			if (variable == null) {
				variable = new Variable(name);
				sharedPrologVariables.put(name, variable);
			}
			return variable;
		case LIST_TYPE:
			PrologTerm[] array = term.getArguments();
			Term list = JplEmpty.EMPTY;
			for (int i = array.length - 1; i >= 0; --i) {
				list = new Compound("[|]", new Term[] { fromTerm(array[i]), list });
			}
			return list;
		case STRUCTURE_TYPE:
			String functor = term.getFunctor();
			Term[] arguments = fromTermArray(((PrologStructure) term).getArguments());
			return new Compound(functor, arguments);
		default:
			throw new UnknownTermError(term);
		}
	}

	public Term[] fromTermArray(PrologTerm[] terms) {
		Term[] prologTerms = new Term[terms.length];
		for (int i = 0; i < terms.length; i++) {
			prologTerms[i] = fromTerm(terms[i]);
		}
		return prologTerms;
	}

	public Term fromTerm(PrologTerm head, PrologTerm[] body) {
		Term clauseHead = fromTerm(head);
		if (body != null && body.length > 0) {
			Term clauseBody = fromTerm(body[body.length - 1]);
			for (int i = body.length - 2; i >= 0; --i) {
				clauseBody = new Compound(",", new Term[] { fromTerm(body[i]), clauseBody });
			}
			return new Compound(":-", new Term[] { clauseHead, clauseBody });
		}
		return clauseHead;
	}

}
