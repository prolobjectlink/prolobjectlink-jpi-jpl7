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

import static org.prolobjectlink.prolog.PrologLogger.FILE_NOT_FOUND;
import static org.prolobjectlink.prolog.PrologLogger.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Term;
import org.jpl7.Util;

/**
 * 
 * @author Jose Zalacain
 * @since 1.0
 */
final class JplParser {

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
					if (clauseTerm.hasFunctor(":-", 1)) {
						String absoluteString = "";
						Term arg = clauseTerm.arg(1);
						if (arg.hasFunctor("consult", 1)) {
							Term relative = arg.arg(1);
							String path = relative.name();
							String[] array = path.split("\\.\\./");
							if (array.length > 1) {
								String ok = array[array.length - 1];
								File currentPtr = in.getCanonicalFile();
								for (int i = 0; i < array.length; i++) {
									currentPtr = currentPtr.getParentFile();
								}
								String ptr = currentPtr.getCanonicalPath();
								File abs = new File(ptr + File.separator + ok);
								absoluteString = abs.getCanonicalPath();
							}
							Atom absolute = new Atom(absoluteString.toLowerCase().replace(File.separatorChar, '/'));
							Compound c = new Compound("consult", new Term[] { absolute });
							program.addDirective(c);
						} else {
							program.addDirective(clauseTerm);
						}

					} else {
						program.add(clauseTerm);
					}
					b = new StringBuilder();
				} else {
					b.append(line);
				}
				line = buffer.readLine();
			}
		} catch (FileNotFoundException e) {
			JplProvider.logger.error(getClass(), FILE_NOT_FOUND, e);
		} catch (IOException e) {
			JplProvider.logger.error(getClass(), IO, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					JplProvider.logger.error(getClass(), IO, e);
				}
			}
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					JplProvider.logger.error(getClass(), IO, e);
				}
			}
		}

		return program;
	}

	public JplProgram parseProgram(Reader in) {

		BufferedReader buffer = null;
		JplProgram program = new JplProgram();

		try {
			buffer = new BufferedReader(in);
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
		} catch (IOException e) {
			JplProvider.logger.error(getClass(), IO, e);
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					JplProvider.logger.error(getClass(), IO, e);
				}
			}
		}

		return program;
	}

}
