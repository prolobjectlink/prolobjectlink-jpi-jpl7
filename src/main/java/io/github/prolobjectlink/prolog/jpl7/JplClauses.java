/*-
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2020 - 2021 Prolobjectlink Project
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import io.github.prolobjectlink.prolog.PrologClause;
import io.github.prolobjectlink.prolog.PrologClauses;

class JplClauses extends AbstractList<PrologClause> implements PrologClauses {

	private final int arity;
	private final String functor;
	private final List<PrologClause> list;

	JplClauses(String functor, int arity) {
		list = new ArrayList<PrologClause>();
		this.functor = functor;
		this.arity = arity;
	}

	public void add(int index, PrologClause element) {
		list.add(index, element);
	}

	public PrologClause remove(int index) {
		return list.remove(index);
	}

	public PrologClause get(int index) {
		return list.get(index);
	}

	public int size() {
		return list.size();
	}

	public boolean isDynamic() {
		for (PrologClause prologClause : list) {
			if (!prologClause.isDynamic()) {
				return false;
			}
		}
		return true;
	}

	public boolean isMultifile() {
		for (PrologClause prologClause : list) {
			if (!prologClause.isMultifile()) {
				return false;
			}
		}
		return true;
	}

	public boolean isDiscontiguous() {
		for (PrologClause prologClause : list) {
			if (!prologClause.isDiscontiguous()) {
				return false;
			}
		}
		return true;
	}

	public String getIndicator() {
		return functor + "/" + arity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JplClauses other = (JplClauses) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list)) {
			return false;
		}
		return true;
	}

}
