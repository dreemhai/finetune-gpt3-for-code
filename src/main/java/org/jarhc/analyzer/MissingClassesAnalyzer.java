/*
 * Copyright 2018 Stephan Markwalder
 *
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
 */

package org.jarhc.analyzer;

import org.jarhc.env.JavaRuntime;
import org.jarhc.java.ClassResolver;
import org.jarhc.java.ClassResolverImpl;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.jarhc.utils.StringUtils.joinLines;

public class MissingClassesAnalyzer extends Analyzer {

	private final JavaRuntime javaRuntime;

	public MissingClassesAnalyzer(JavaRuntime javaRuntime) {
		if (javaRuntime == null) throw new IllegalArgumentException("javaRuntime");
		this.javaRuntime = javaRuntime;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Missing Classes", "References to classes not found on the classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {
		ReportTable table = new ReportTable("JAR file", "Missing classes");

		ClassResolver classResolver = new ClassResolverImpl(classpath, javaRuntime);

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// find all missing classes
			Set<String> missingClasses = collectMissingClasses(jarFile, classResolver);
			if (missingClasses.isEmpty()) continue;

			table.addRow(jarFile.getFileName(), joinLines(missingClasses));
		}

		return table;
	}

	private Set<String> collectMissingClasses(JarFile jarFile, ClassResolver classResolver) {
		Set<String> missingClasses = Collections.synchronizedSet(new TreeSet<>());

		// for every class definition (in parallel) ...
		List<ClassDef> classDefs = jarFile.getClassDefs();
		classDefs.parallelStream().forEach(classDef -> {

			// for every class reference ...
			List<ClassRef> classRefs = classDef.getClassRefs();
			for (ClassRef classRef : classRefs) {
				String className = classRef.getClassName();

				// check if class exists
				boolean exists = classResolver.getClassDef(className).isPresent();
				if (!exists) {
					missingClasses.add(className);
				}
			}
		});

		return missingClasses;
	}

}
