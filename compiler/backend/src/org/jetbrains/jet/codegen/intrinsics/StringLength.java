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

package org.jetbrains.jet.codegen.intrinsics;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.asm4.Type;
import org.jetbrains.asm4.commons.InstructionAdapter;
import org.jetbrains.jet.codegen.*;
import org.jetbrains.jet.lang.psi.JetExpression;

import java.util.List;

/**
 * @author alex.tkachman
 */
public class StringLength implements IntrinsicMethod {
    @Override
    public StackValue generate(
            ExpressionCodegen codegen,
            InstructionAdapter v,
            @NotNull Type expectedType,
            PsiElement element,
            List<JetExpression> arguments,
            StackValue receiver,
            @NotNull GenerationState state
    ) {
        receiver.put(AsmTypeConstants.OBJECT_TYPE, v);
        v.invokeinterface("java/lang/CharSequence", "length", "()I");
        return StackValue.onStack(Type.INT_TYPE);
    }
}
