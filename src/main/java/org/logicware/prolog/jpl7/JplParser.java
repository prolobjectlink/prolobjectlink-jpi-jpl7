/*
 * #%L
 * prolobjectlink-jpi-jpl7
 * %%
 * Copyright (C) 2012 - 2018 WorkLogic Project
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
import java.util.List;

import org.jpl7.Term;
import org.jpl7.Util;
import org.logicware.logging.LoggerUtils;

public final class JplParser {

	public Term parseTerm(String term) {
		return Util.textToTerm(term);
	}

	public Term[] parseTerms(Term term) {
		return parseTerms("" + term + "");
	}

	public Term[] parseTerms(String stringTerms) {
		Term[] a = new Term[0];
		Term ptr = Util.textToTerm(stringTerms);
		List<Term> terms = new ArrayList<Term>();
		while (ptr.isCompound() && ptr.hasFunctor(",", 2)) {
			terms.add(ptr.arg(1));
			ptr = ptr.arg(2);
		}
		terms.add(ptr);
		return terms.toArray(a);
	}

	public JplProgram parseProgram(String file) {
		return parseProgram(new File(file));
	}

	public JplProgram parseProgram(File in) {

		FileReader reader = null;
		BufferedReader buffer = null;
		JplProgram program = new JplProgram();

		try {
			reader = new FileReader(in);
			buffer = new BufferedReader(reader);
			String line = buffer.readLine();
			StringBuilder b = new StringBuilder();
			while (line != null) {
				if (!line.isEmpty() && line.lastIndexOf('.') == line.length() - 1) {
					b.append(line.substring(0, line.length() - 1));
					Term clauseTerm = Util.textToTerm("" + b + "");
					program.add(clauseTerm);
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

		return program;
	}

}
