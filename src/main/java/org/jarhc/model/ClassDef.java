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

package org.jarhc.model;

import org.jarhc.utils.JavaVersion;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef extends AccessFlags implements Comparable<ClassDef> {

	/**
	 * ASM class definition.
	 */
	private final ClassNode classNode;

	/**
	 * List of field definitions.
	 */
	private final List<FieldDef> fieldDefs;

	/**
	 * List of method definitions.
	 */
	private final List<MethodDef> methodDefs;

	/**
	 * List with references to other classes.
	 */
	private final List<ClassRef> classRefs;

	/**
	 * List with references to fields.
	 */
	private final List<FieldRef> fieldRefs;

	/**
	 * List with references to methods.
	 */
	private final List<MethodRef> methodRefs;

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	/**
	 * Create a class definition for the given class and class references.
	 *
	 * @param classNode ASM class definition
	 * @param classRefs References to other classes
	 * @throws IllegalArgumentException If <code>classNode</code> or <code>classRefs</code> is <code>null</code>
	 */
	private ClassDef(ClassNode classNode, List<FieldDef> fieldDefs, List<MethodDef> methodDefs, List<ClassRef> classRefs, List<FieldRef> fieldRefs, List<MethodRef> methodRefs) {
		super(classNode.access);
		if (classNode == null) throw new IllegalArgumentException("classNode");
		if (classRefs == null) throw new IllegalArgumentException("classRefs");
		this.classNode = classNode;
		this.fieldDefs = new ArrayList<>(fieldDefs);
		this.methodDefs = new ArrayList<>(methodDefs);
		this.classRefs = new ArrayList<>(classRefs);
		this.fieldRefs = new ArrayList<>(fieldRefs);
		this.methodRefs = new ArrayList<>(methodRefs);
		// TODO: remove unused information from class node?
	}

	public String getClassName() {
		return classNode.name;
	}

	public String getSuperName() {
		return classNode.superName;
	}

	public List<String> getInterfaceNames() {
		return Collections.unmodifiableList(classNode.interfaces);
	}

	public int getMajorClassVersion() {
		return classNode.version & 0xFF;
	}

	public int getMinorClassVersion() {
		return classNode.version >> 16;
	}

	/**
	 * Get a human readable Java version string based on the class version.
	 *
	 * @return Java version string (examples: "Java 1.4", "Java 8")
	 * @see JavaVersion#fromClassVersion(int)
	 */
	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(getMajorClassVersion());
	}

	public List<FieldDef> getFieldDefs() {
		return Collections.unmodifiableList(fieldDefs);
	}

	public Optional<FieldDef> getFieldDef(String fieldName) {
		// TODO: what if there is more than one field with the same name?
		return fieldDefs.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst();
	}

	public List<MethodDef> getMethodDefs() {
		return Collections.unmodifiableList(methodDefs);
	}

	public List<ClassRef> getClassRefs() {
		return Collections.unmodifiableList(classRefs);
	}

	public List<FieldRef> getFieldRefs() {
		return Collections.unmodifiableList(fieldRefs);
	}

	public List<MethodRef> getMethodRefs() {
		return Collections.unmodifiableList(methodRefs);
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getModifiers() {
		List<String> parts = new ArrayList<>();

		// access flags
		if (isPublic()) parts.add("public");
		if (isProtected()) parts.add("protected");
		if (isPrivate()) parts.add("private");

		// modifiers
		if (isStatic()) parts.add("static");
		if (isFinal()) parts.add("final");
		if (isVolatile()) parts.add("volatile");
		if (isTransient()) parts.add("transient");
		if (isAbstract()) parts.add("abstract");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		if (isSuper()) parts.add("(super)");
		if (isDeprecated()) parts.add("@Deprecated");

		// type
		if (isInterface()) parts.add("interface");
		if (isAnnotation()) parts.add("@interface");
		if (isEnum()) parts.add("enum");
		if (!isInterface() && !isEnum() && !isAnnotation()) parts.add("class");

		return String.join(" ", parts);
	}

	public String getDisplayName() {
		String className = classNode.name.replace('/', '.');
		String modifiers = getModifiers();
		return String.format("%s %s", modifiers, className);
	}

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d.%d]", getDisplayName(), getMajorClassVersion(), getMinorClassVersion());
	}

	@Override
	public int compareTo(ClassDef classDef) {
		int diff = this.getClassName().compareTo(classDef.getClassName());
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

	// BUILDER --------------------------------------------------------------------------------------

	public static Builder forClassNode(ClassNode classNode) {
		return new Builder(classNode);
	}

	public static Builder forClassName(String className) {
		return new Builder(className);
	}

	public static class Builder {

		private final ClassNode classNode;
		private final List<FieldDef> fieldDefs = new ArrayList<>();
		private final List<MethodDef> methodDefs = new ArrayList<>();
		private final List<ClassRef> classRefs = new ArrayList<>();
		private final List<FieldRef> fieldRefs = new ArrayList<>();
		private final List<MethodRef> methodRefs = new ArrayList<>();

		private Builder(ClassNode classNode) {
			this.classNode = classNode;
		}

		private Builder(String className) {
			this.classNode = new ClassNode();
			this.classNode.name = className;
			this.classNode.version = 52; // Java 8
		}

		public Builder withVersion(int majorClassVersion, int minorClassVersion) {
			this.classNode.version = majorClassVersion + (minorClassVersion << 16);
			return this;
		}

		public Builder withFieldDefs(List<FieldDef> fieldDefs) {
			this.fieldDefs.addAll(fieldDefs);
			return this;
		}

		public Builder withMethodDefs(List<MethodDef> methodDefs) {
			this.methodDefs.addAll(methodDefs);
			return this;
		}

		public Builder withClassRefs(List<ClassRef> classRefs) {
			this.classRefs.addAll(classRefs);
			return this;
		}

		public Builder withClassRef(ClassRef classRef) {
			this.classRefs.add(classRef);
			return this;
		}

		public Builder withFieldRefs(List<FieldRef> fieldRefs) {
			this.fieldRefs.addAll(fieldRefs);
			return this;
		}

		public Builder withMethodRefs(List<MethodRef> methodRefs) {
			this.methodRefs.addAll(methodRefs);
			return this;
		}

		public ClassDef build() {
			return new ClassDef(classNode, fieldDefs, methodDefs, classRefs, fieldRefs, methodRefs);
		}

	}

}
