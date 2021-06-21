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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import io.github.prolobjectlink.prolog.AbstractCompounds;
import io.github.prolobjectlink.prolog.AbstractIterator;
import io.github.prolobjectlink.prolog.PrologMap;
import io.github.prolobjectlink.prolog.PrologProvider;
import io.github.prolobjectlink.prolog.PrologTerm;
import io.github.prolobjectlink.prolog.PrologTermType;

/**
 * A PrologTerm that maps PrologTerm keys to PrologTerm values. A map cannot
 * contain duplicate keys. Each key can map to at most one value.
 * 
 * @author Jose Zalacain
 * @since 1.1
 */
public final class JplMap extends AbstractCompounds implements PrologMap {

	private Map<PrologTerm, PrologTerm> map;

	JplMap(PrologProvider provider, int size) {
		super(PrologTermType.MAP_TYPE, provider);
		map = new LinkedHashMap<PrologTerm, PrologTerm>(size);
	}

	JplMap(PrologProvider provider, Map<? extends PrologTerm, ? extends PrologTerm> m) {
		this(provider);
		putAll(m);
	}

	JplMap(PrologProvider provider) {
		this(provider, 16);
	}

	public boolean isList() {
		return true;
	}

	public boolean isStructure() {
		return false;
	}

	public boolean isEmptyList() {
		return map.size() == 0;
	}

	public String getFunctor() {
		return ".";
	}

	public int getArity() {
		if (map.size() > 0) {
			return 2;
		}
		return 0;
	}

	public PrologTerm[] getArguments() {
		PrologProvider p = getProvider();
		PrologTerm[] args = new PrologTerm[map.size()];
		Set<Entry<PrologTerm, PrologTerm>> s = entrySet();
		Iterator<Entry<PrologTerm, PrologTerm>> i = s.iterator();
		for (int j = 0; j < args.length && i.hasNext(); j++) {
			Entry<PrologTerm, PrologTerm> e = i.next();
			args[j] = new JplEntry(p, e.getKey(), e.getValue());
		}
		return args;
	}

	public PrologTerm getArgument(int index) {
		int idx = 0;
		PrologTerm term = null;
		checkIndex(index, size());
		Iterator<PrologTerm> i = iterator();
		for (; i.hasNext() && idx <= index; idx++) {
			term = i.next();
			if (idx == index) {
				return term;
			}
		}
		return term;
	}

	public int hashCode() {
		int result = 0;
		final int prime = 31;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JplMap other = (JplMap) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		Set<Entry<PrologTerm, PrologTerm>> set = entrySet();
		Iterator<Entry<PrologTerm, PrologTerm>> i = set.iterator();
		b.append('[');
		while (i.hasNext()) {
			Entry<PrologTerm, PrologTerm> entry = i.next();
			b.append(entry.getKey());
			b.append('-');
			b.append(entry.getValue());
			if (i.hasNext()) {
				b.append(',');
				b.append(' ');
			}
		}
		b.append(']');
		return "" + b + "";
	}

	@Override
	public Iterator<PrologTerm> iterator() {
		return new PrologMapIterator();
	}

	@Override
	public PrologTerm getHead() {
		return iterator().next();
	}

	@Override
	public PrologTerm getTail() {
		JplMap m = new JplMap(provider, map);
		m.remove(((Entry<?, ?>) getHead()).getKey());
		return m;
	}

	private class PrologMapIterator extends AbstractIterator<PrologTerm> implements Iterator<PrologTerm> {

		private final Set<PrologTerm> set;
		private final Iterator<PrologTerm> itr;

		private PrologMapIterator() {
			set = new LinkedHashSet<PrologTerm>(map.size());
			for (Iterator<Entry<PrologTerm, PrologTerm>> i = map.entrySet().iterator(); i.hasNext();) {
				Entry<PrologTerm, PrologTerm> e = i.next();
				PrologTerm t = new JplEntry(provider, e.getKey(), e.getValue());
				set.add(t);
			}
			itr = set.iterator();
		}

		@Override
		public boolean hasNext() {
			return itr.hasNext();
		}

		@Override
		public PrologTerm next() {
			return itr.next();
		}

	}

	public PrologTerm put(PrologTerm key, PrologTerm value) {
		return map.put(key, value);
	}

	public Set<Entry<PrologTerm, PrologTerm>> entrySet() {
		return map.entrySet();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public PrologTerm get(Object key) {
		return map.get(key);
	}

	@Override
	public PrologTerm remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends PrologTerm, ? extends PrologTerm> m) {
		map.putAll(m);
	}

	@Override
	public Set<PrologTerm> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<PrologTerm> values() {
		return map.values();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public int size() {
		return map.size();
	}

}
