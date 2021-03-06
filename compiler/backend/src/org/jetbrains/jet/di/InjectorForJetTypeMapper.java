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


package org.jetbrains.jet.di;

import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.BindingContext;
import java.util.List;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.codegen.JetTypeMapper;
import org.jetbrains.jet.codegen.BuiltinToJavaTypesMapping;
import org.jetbrains.jet.codegen.ClassBuilderMode;
import org.jetbrains.annotations.NotNull;
import javax.annotation.PreDestroy;

/* This file is generated by org.jetbrains.jet.di.AllInjectorsGenerator. DO NOT EDIT! */
public class InjectorForJetTypeMapper {

    private final BindingTrace bindingTrace;
    private BindingContext bindingContext;
    private final List<JetFile> listOfJetFile;
    private JetTypeMapper jetTypeMapper;
    private BuiltinToJavaTypesMapping builtinToJavaTypesMapping;
    private ClassBuilderMode classBuilderMode;

    public InjectorForJetTypeMapper(
        @NotNull BindingTrace bindingTrace,
        @NotNull List<JetFile> listOfJetFile
    ) {
        this.bindingTrace = bindingTrace;
        this.bindingContext = bindingTrace.getBindingContext();
        this.listOfJetFile = listOfJetFile;
        this.jetTypeMapper = new JetTypeMapper();
        this.builtinToJavaTypesMapping = BuiltinToJavaTypesMapping.ENABLED;
        this.classBuilderMode = ClassBuilderMode.FULL;

        this.jetTypeMapper.setBindingTrace(bindingTrace);
        this.jetTypeMapper.setBuiltinToJavaTypesMapping(builtinToJavaTypesMapping);
        this.jetTypeMapper.setClassBuilderMode(classBuilderMode);

    }

    @PreDestroy
    public void destroy() {
    }

    public BindingTrace getBindingTrace() {
        return this.bindingTrace;
    }

    public List getListOfJetFile() {
        return this.listOfJetFile;
    }

    public JetTypeMapper getJetTypeMapper() {
        return this.jetTypeMapper;
    }

}
