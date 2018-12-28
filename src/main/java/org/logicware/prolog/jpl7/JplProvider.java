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

import java.util.ArrayList;
import java.util.List;

import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.prolog.AbstractProvider;
import org.logicware.prolog.PrologAtom;
import org.logicware.prolog.PrologConverter;
import org.logicware.prolog.PrologDouble;
import org.logicware.prolog.PrologFloat;
import org.logicware.prolog.PrologInteger;
import org.logicware.prolog.PrologList;
import org.logicware.prolog.PrologLong;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologStructure;
import org.logicware.prolog.PrologTerm;
import org.logicware.prolog.PrologVariable;

public abstract class JplProvider extends AbstractProvider implements PrologProvider {

	public JplProvider(PrologConverter<Term> adapter) {
		super(adapter);
	}

	public boolean isCompliant() {
		return true;
	}

	public PrologTerm prologNil() {
		return new JplNil(this);
	}

	public PrologTerm prologCut() {
		return new JplCut(this);
	}

	public PrologTerm prologFail() {
		return new JplFail(this);
	}

	public PrologTerm prologTrue() {
		return new JplTrue(this);
	}

	public PrologTerm prologFalse() {
		return new JplFalse(this);
	}

	public PrologTerm prologEmpty() {
		return new JplEmpty(this);
	}

	public PrologTerm parseTerm(String term) {
		return toTerm(Util.textToTerm(term), PrologTerm.class);
	}

	public PrologTerm[] parseTerms(String stringTerms) {
		PrologTerm[] a = new PrologTerm[0];
		Term ptr = Util.textToTerm(stringTerms);
		List<PrologTerm> terms = new ArrayList<PrologTerm>();
		while (ptr.isCompound() && ptr.hasFunctor(",", 2)) {
			terms.add(toTerm(ptr.arg(1), PrologTerm.class));
			ptr = ptr.arg(2);
		}
		terms.add(toTerm(ptr, PrologTerm.class));
		return terms.toArray(a);
	}

	public PrologAtom newAtom(String functor) {
		return new JplAtom(this, functor);
	}

	public PrologFloat newFloat(Number value) {
		return new JplFloat(this, value);
	}

	public PrologDouble newDouble(Number value) {
		return new JplDouble(this, value);
	}

	public PrologInteger newInteger(Number value) {
		return new JplInteger(this, value);
	}

	public PrologLong newLong(Number value) {
		return new JplLong(this, value);
	}

	public PrologVariable newVariable(int position) {
		return new JplVariable(this);
	}

	public PrologVariable newVariable(String name, int position) {
		return new JplVariable(this, name);
	}

	public PrologList newList() {
		return new JplList(this);
	}

	public PrologList newList(PrologTerm[] arguments) {
		return new JplList(this, arguments);
	}

	public PrologList newList(PrologTerm head, PrologTerm tail) {
		return new JplList(this, head, tail);
	}

	public PrologList newList(PrologTerm[] arguments, PrologTerm tail) {
		return new JplList(this, arguments, tail);
	}

	public PrologStructure newStructure(String functor, PrologTerm... arguments) {
		return new JplStructure(this, functor, arguments);
	}

	public PrologTerm newStructure(PrologTerm left, String operator, PrologTerm right) {
		return new JplStructure(this, left, operator, right);
	}

}
