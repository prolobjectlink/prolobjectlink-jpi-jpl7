/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2018 Logicware Project
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

import static org.logicware.logging.LoggerConstants.FILE_NOT_FOUND;
import static org.logicware.logging.LoggerConstants.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.logging.LoggerUtils;
import org.logicware.prolog.PrologClause;
import org.logicware.prolog.PrologClauses;
import org.logicware.prolog.PrologList;
import org.logicware.prolog.PrologParser;
import org.logicware.prolog.PrologProgram;
import org.logicware.prolog.PrologProvider;
import org.logicware.prolog.PrologStructure;
import org.logicware.prolog.PrologTerm;

public abstract class JplParser extends JplConverter implements PrologParser {

	public PrologList parseList(String stringList) {
		return (PrologList) parseTerm(stringList);
	}

	public PrologStructure parseStructure(String stringStructure) {
		return (PrologStructure) parseTerm(stringStructure);
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

	public JplClause parseClause(String clause) {
		Term clauseTerm = null;
		if (clause.lastIndexOf('.') == clause.length() - 1) {
			String c = clause.substring(0, clause.length() - 1);
			clauseTerm = Util.textToTerm(c);
		} else {
			clauseTerm = Util.textToTerm(clause);
		}
		return getClause(clauseTerm);
	}

	protected abstract JplClause getClause(Term clauseTerm);

	public Set<PrologClause> parseProgram(String file) {
		return parseProgram(new File(file));
	}

	public Set<PrologClause> parseProgram(File in) {

		FileReader reader = null;
		BufferedReader buffer = null;
		PrologProgram program = new JplProgram();

		try {
			reader = new FileReader(in);
			buffer = new BufferedReader(reader);
			String line = buffer.readLine();
			StringBuilder b = new StringBuilder();
			while (line != null) {
				if (line.lastIndexOf('.') == line.length() - 1) {
					b.append(line.substring(0, line.length() - 1));
					Term clauseTerm = Util.textToTerm("" + b + "");
					JplClause c = getClause(clauseTerm);

					b = new StringBuilder();
				} else {
					b.append(line);
				}
				line = buffer.readLine();
			}
		} catch (FileNotFoundException e) {
			LoggerUtils.error(getClass(), FILE_NOT_FOUND, e);
		} catch (IOException e) {
			LoggerUtils.error(getClass(), IO, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO, e);
				}
			}
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					LoggerUtils.error(getClass(), IO, e);
				}
			}
		}

		Set<PrologClause> set = new LinkedHashSet<PrologClause>();
		for (PrologClauses cls : program) {
			for (PrologClause prologClause : cls) {
				set.add(prologClause);
			}
		}
		return set;
	}

	public PrologProvider createProvider() {
		return provider;
	}

}
