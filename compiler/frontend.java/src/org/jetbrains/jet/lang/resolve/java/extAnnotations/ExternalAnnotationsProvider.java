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

package org.jetbrains.jet.lang.resolve.java.extAnnotations;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class should be eliminated when Kotlin will depend on IDEA 12.x (KT-2326)
 * @author Evgeny Gerashchenko
 * @since 6/26/12
 */
@Deprecated
public abstract class ExternalAnnotationsProvider {
    @Nullable
    public abstract PsiAnnotation findExternalAnnotation(@NotNull PsiModifierListOwner listOwner, @NotNull String annotationFQN);

    @Nullable
    public abstract PsiAnnotation[] findExternalAnnotations(@NotNull PsiModifierListOwner listOwner);

    public static ExternalAnnotationsProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ExternalAnnotationsProvider.class);
    }
}