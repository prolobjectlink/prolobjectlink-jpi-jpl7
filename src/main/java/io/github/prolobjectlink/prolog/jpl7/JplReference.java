/*-
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2019 Prolobjectlink Project
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

import static io.github.prolobjectlink.prolog.PrologTermType.OBJECT_TYPE;

import org.jpl7.JPL;
import org.jpl7.Term;

import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologReference;
import io.github.prolobjectlink.prolog.PrologTerm;

public final class JplReference extends JplTerm implements PrologReference {

	JplReference(PrologProvider provider, Term reference) {
		super(OBJECT_TYPE, provider, reference);
	}

	JplReference(PrologProvider provider, Object reference) {
		super(OBJECT_TYPE, provider, JPL.newJRef(reference));
	}

	@Override
	public Class<?> getReferenceType() {
		return getObject().getClass();
	}

	@Override
	public int getArity() {
		return value.arity();
	}

	@Override
	public String getFunctor() {
		return "<jref>";
	}

	@Override
	public PrologTerm[] getArguments() {
		return new PrologTerm[0];
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
		if (!(obj instanceof JplTerm))
			return false;
		JplTerm other = (JplTerm) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!getObject().equals(other.getObject())) {
			return false;
		}
		return true;
	}

	public boolean unify(PrologTerm term) {
		PrologTerm thisTerm = this;
		PrologTerm otherTerm = term;
		if (thisTerm == otherTerm) {
			return true;
		} else if (thisTerm.isVariable()) {
			if (thisTerm == thisTerm.getTerm()) {
				return true;
			}
			return thisTerm.getTerm().unify(otherTerm);
		} else if (otherTerm.isVariable()) {
			if (otherTerm == otherTerm.getTerm()) {
				return true;
			}
			return otherTerm.getTerm().unify(thisTerm);
		}
		return equals(term);
	}

	public int compareTo(PrologTerm term) {
		PrologTerm thisTerm = this;
		PrologTerm otherTerm = term;

		Integer thisHashCode = thisTerm.hashCode();
		Integer otherHashCode = otherTerm.hashCode();

		if (thisHashCode < otherHashCode) {
			return -1;
		} else if (thisHashCode > otherHashCode) {
			return 1;
		}

		return 0;
	}

}
