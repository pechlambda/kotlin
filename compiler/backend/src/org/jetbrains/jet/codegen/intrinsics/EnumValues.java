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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.Type;
import org.jetbrains.asm4.commons.InstructionAdapter;
import org.jetbrains.jet.codegen.ExpressionCodegen;
import org.jetbrains.jet.codegen.GenerationState;
import org.jetbrains.jet.codegen.MapTypeMode;
import org.jetbrains.jet.codegen.StackValue;
import org.jetbrains.jet.lang.descriptors.CallableDescriptor;
import org.jetbrains.jet.lang.psi.JetCallExpression;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.calls.ResolvedCall;
import org.jetbrains.jet.lang.types.JetType;

import java.util.List;

/**
 * @author alex.tkachman
 */
public class EnumValues implements IntrinsicMethod {
    @Override
    public StackValue generate(
            ExpressionCodegen codegen, InstructionAdapter v, @NotNull Type expectedType, @Nullable PsiElement element,
            @Nullable List<JetExpression> arguments, StackValue receiver, @NotNull GenerationState state
    ) {
        JetCallExpression call = (JetCallExpression) element;
        ResolvedCall<? extends CallableDescriptor> resolvedCall =
                codegen.getBindingContext().get(BindingContext.RESOLVED_CALL, call.getCalleeExpression());
        assert resolvedCall != null;
        CallableDescriptor resultingDescriptor = resolvedCall.getResultingDescriptor();
        JetType returnType = resultingDescriptor.getReturnType();
        assert returnType != null;
        Type type = state.getInjector().getJetTypeMapper().mapType(
                returnType, MapTypeMode.VALUE);
        v.invokestatic(type.getElementType().getInternalName(), "values", "()" + type);
        StackValue.onStack(type).put(expectedType, v);
        return StackValue.onStack(expectedType);
    }
}
