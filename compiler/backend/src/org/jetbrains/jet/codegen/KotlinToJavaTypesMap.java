/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.codegen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.Type;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ClassifierDescriptor;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.java.JvmPrimitiveType;
import org.jetbrains.jet.lang.resolve.name.FqNameUnsafe;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.lang.JetStandardClasses;
import org.jetbrains.jet.lang.types.lang.JetStandardLibrary;
import org.jetbrains.jet.lang.types.lang.PrimitiveType;
import org.jetbrains.jet.lang.types.ref.ClassName;

import java.util.Map;
import java.util.Set;

import static org.jetbrains.jet.codegen.AsmTypeConstants.*;

/**
* @author svtk
*/
public class KotlinToJavaTypesMap {
    private static KotlinToJavaTypesMap instance = null;

    @NotNull
    public static KotlinToJavaTypesMap getInstance() {
        if (instance == null) {
            instance = new KotlinToJavaTypesMap();
        }
        return instance;
    }

    private final Map<FqNameUnsafe, Type> asmTypes = Maps.newHashMap();
    private final Map<FqNameUnsafe, Type> asmNullableTypes = Maps.newHashMap();
    private final Set<String> mappedTypeNames = Sets.newHashSet();

    private KotlinToJavaTypesMap() {
        init();
        initPrimitives();
    }

    private void init() {
        JetStandardLibrary standardLibrary = JetStandardLibrary.getInstance();

        register(JetStandardClasses.getAny(),          OBJECT_TYPE);
        register(standardLibrary.getNumber(),          JAVA_NUMBER_TYPE);
        register(standardLibrary.getString(),          JAVA_STRING_TYPE);
        register(standardLibrary.getThrowable(),       JAVA_THROWABLE_TYPE);
        register(standardLibrary.getCharSequence(),    JAVA_CHAR_SEQUENCE_TYPE);
        register(standardLibrary.getComparable(),      JAVA_COMPARABLE_TYPE);
        register(standardLibrary.getEnum(),            JAVA_ENUM_TYPE);
        register(standardLibrary.getAnnotation(),      JAVA_ANNOTATION_TYPE);
        register(standardLibrary.getIterable(),        JAVA_ITERABLE_TYPE);
        register(standardLibrary.getMutableIterable(), JAVA_ITERABLE_TYPE);
        register(standardLibrary.getIterator(),        JAVA_ITERATOR_TYPE);
        register(standardLibrary.getMutableIterator(), JAVA_ITERATOR_TYPE);
    }

    private void initPrimitives() {
        for (JvmPrimitiveType jvmPrimitiveType : JvmPrimitiveType.values()) {
            ClassName className = jvmPrimitiveType.getPrimitiveType().getClassName();

            register(className, jvmPrimitiveType.getAsmType());
            registerNullable(className, jvmPrimitiveType.getWrapper().getAsmType());
        }
        for (JvmPrimitiveType jvmPrimitiveType : JvmPrimitiveType.values()) {
            PrimitiveType primitiveType = jvmPrimitiveType.getPrimitiveType();
            register(primitiveType.getArrayClassName(), jvmPrimitiveType.getAsmArrayType());
        }
    }

    @Nullable
    public Type getJavaAnalog(@NotNull JetType jetType) {
        ClassifierDescriptor classifier = jetType.getConstructor().getDeclarationDescriptor();
        assert classifier != null;
        FqNameUnsafe className = DescriptorUtils.getFQName(classifier);
        if (jetType.isNullable()) {
            Type nullableType = asmNullableTypes.get(className);
            if (nullableType != null) {
                return nullableType;
            }
        }
        return asmTypes.get(className);
    }

    private void register(@NotNull ClassDescriptor kotlinDescriptor, @NotNull Type javaType) {
        FqNameUnsafe fqName = DescriptorUtils.getFQName(kotlinDescriptor);
        ClassName className = new ClassName(fqName.toSafe(), kotlinDescriptor.getDefaultType().getArguments().size());
        register(className, javaType);
    }

    private void register(@NotNull ClassName className, @NotNull Type type) {
        mappedTypeNames.add(type.getClassName());
        asmTypes.put(className.getFqName().toUnsafe(), type);
    }

    private void registerNullable(@NotNull ClassName className, @NotNull Type nullableType) {
        asmNullableTypes.put(className.getFqName().toUnsafe(), nullableType);
    }

    public boolean isForceReal(@NotNull JvmClassName className) {
        return JvmPrimitiveType.getByWrapperClass(className) != null
               || mappedTypeNames.contains(className.getFqName().getFqName());
    }
}
