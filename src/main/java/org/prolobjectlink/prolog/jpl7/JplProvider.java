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

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineFactory;

import org.jpl7.Term;
import org.jpl7.Util;
import org.prolobjectlink.prolog.AbstractProvider;
import org.prolobjectlink.prolog.PrologAtom;
import org.prolobjectlink.prolog.PrologConverter;
import org.prolobjectlink.prolog.PrologDouble;
import org.prolobjectlink.prolog.PrologFloat;
import org.prolobjectlink.prolog.PrologInteger;
import org.prolobjectlink.prolog.PrologList;
import org.prolobjectlink.prolog.PrologLogger;
import org.prolobjectlink.prolog.PrologLong;
import org.prolobjectlink.prolog.PrologProvider;
import org.prolobjectlink.prolog.PrologStructure;
import org.prolobjectlink.prolog.PrologTerm;
import org.prolobjectlink.prolog.PrologVariable;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
public abstract class JplProvider extends AbstractProvider implements PrologProvider {

	static final PrologLogger logger = new JplLogger();

	public JplProvider(PrologConverter<Term> adapter) {
		super(adapter);
	}

	public final boolean isCompliant() {
		return true;
	}

	public final PrologTerm prologNil() {
		return new JplNil(this);
	}

	public final PrologTerm prologCut() {
		return new JplCut(this);
	}

	public final PrologTerm prologFail() {
		return new JplFail(this);
	}

	public final PrologTerm prologTrue() {
		return new JplTrue(this);
	}

	public final PrologTerm prologFalse() {
		return new JplFalse(this);
	}

	public final PrologTerm prologEmpty() {
		return new JplEmpty(this);
	}

	public PrologTerm prologInclude(String file) {
		return newStructure("consult", newAtom(file));
	}

	public final PrologTerm parseTerm(String term) {
		return toTerm(Util.textToTerm(term), PrologTerm.class);
	}

	public final PrologTerm[] parseTerms(String stringTerms) {
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

	public final PrologAtom newAtom(String functor) {
		return new JplAtom(this, functor);
	}

	public final PrologFloat newFloat(Number value) {
		return new JplFloat(this, value);
	}

	public final PrologDouble newDouble(Number value) {
		return new JplDouble(this, value);
	}

	public final PrologInteger newInteger(Number value) {
		return new JplInteger(this, value);
	}

	public final PrologLong newLong(Number value) {
		return new JplLong(this, value);
	}

	public final PrologVariable newVariable(int position) {
		return new JplVariable(this);
	}

	public final PrologVariable newVariable(String name, int position) {
		return new JplVariable(this, name);
	}

	public final PrologList newList() {
		return new JplList(this);
	}

	public final PrologList newList(PrologTerm[] arguments) {
		return new JplList(this, arguments);
	}

	public final PrologList newList(PrologTerm head, PrologTerm tail) {
		return new JplList(this, head, tail);
	}

	public final PrologList newList(PrologTerm[] arguments, PrologTerm tail) {
		return new JplList(this, arguments, tail);
	}

	public final PrologStructure newStructure(String functor, PrologTerm... arguments) {
		return new JplStructure(this, functor, arguments);
	}

	public final PrologTerm newStructure(PrologTerm left, String operator, PrologTerm right) {
		return new JplStructure(this, left, operator, right);
	}

	public ScriptEngineFactory getScriptFactory() {
		return new JplScriptFactory(newEngine());
	}

	public final PrologLogger getLogger() {
		return logger;
	}

}
