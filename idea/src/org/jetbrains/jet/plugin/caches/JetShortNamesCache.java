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

package org.jetbrains.jet.plugin.caches;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.asJava.JavaElementFinder;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTraceContext;
import org.jetbrains.jet.lang.resolve.ImportPath;
import org.jetbrains.jet.lang.resolve.QualifiedExpressionResolver;
import org.jetbrains.jet.lang.resolve.lazy.ResolveSession;
import org.jetbrains.jet.lang.resolve.lazy.ResolveSessionUtils;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils;
import org.jetbrains.jet.lang.types.lang.JetStandardClasses;
import org.jetbrains.jet.lang.types.lang.JetStandardLibrary;
import org.jetbrains.jet.plugin.stubindex.JetExtensionFunctionNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetFullClassNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetShortClassNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetShortFunctionNameIndex;

import java.util.*;

/**
 * Will provide both java elements from jet context and some special declarations special to jet.
 * All those declaration are planned to be used in completion.
 *
 * @author Nikolay Krasko
 */
public class JetShortNamesCache extends PsiShortNamesCache {

    private static final PsiMethod[] NO_METHODS = new PsiMethod[0];
    private static final PsiField[] NO_FIELDS = new PsiField[0];
    private final Project project;
    private final JavaElementFinder javaElementFinder;

    public JetShortNamesCache(Project project) {
        this.project = project;
        this.javaElementFinder = new JavaElementFinder(project);
    }

    /**
     * Return jet class names form jet project sources which should be visible from java.
     */
    @NotNull
    @Override
    public String[] getAllClassNames() {
        Collection<String> classNames = JetShortClassNameIndex.getInstance().getAllKeys(project);
        return ArrayUtil.toStringArray(classNames);
    }

    /**
     * Return class names form jet sources in given scope which should be visible as Java classes.
     */
    @NotNull
    @Override
    public PsiClass[] getClassesByName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope) {
        // Quick check for classes from getAllClassNames()
        Collection<JetClassOrObject> classOrObjects = JetShortClassNameIndex.getInstance().get(name, project, scope);
        if (classOrObjects.isEmpty()) {
            return PsiClass.EMPTY_ARRAY;
        }

        List<PsiClass> result = new ArrayList<PsiClass>();
        for (JetClassOrObject classOrObject : classOrObjects) {
            if (classOrObject instanceof JetNamedDeclaration) {
                FqName fqName = JetPsiUtil.getFQName((JetNamedDeclaration) classOrObject);
                if (fqName != null && fqName.shortName().getName().equals(name)) {
                    PsiClass psiClass = javaElementFinder.findClass(fqName.getFqName(), scope);
                    if (psiClass != null) {
                        result.add(psiClass);
                    }
                }
            }
        }

        return result.toArray(new PsiClass[result.size()]);
    }

    @Override
    public void getAllClassNames(@NotNull HashSet<String> destination) {
        destination.addAll(Arrays.asList(getAllClassNames()));
    }

    /**
     * Types that should be visible in completion from kotlin but should be absent in java.
     * @return
     */
    @NotNull
    public static Collection<DeclarationDescriptor> getJetOnlyTypes() {
        Collection<DeclarationDescriptor> standardTypes = JetStandardClasses.getAllStandardClasses();
        standardTypes.addAll(
                Collections2.transform(JetStandardLibrary.getInstance().getStandardTypes(),
                                       new Function<ClassDescriptor, DeclarationDescriptor>() {
                                           @Override
                                           public DeclarationDescriptor apply(@Nullable ClassDescriptor classDescriptor) {
                                               assert classDescriptor != null;
                                               return classDescriptor;
                                           }
                                       }));

        return standardTypes;
    }

    /**
     * Get jet non-extension top-level function names. Method is allowed to give invalid names - all result should be
     * checked with getTopLevelFunctionDescriptorsByName().
     *
     * @return
     */
    @NotNull
    public Collection<String> getAllTopLevelFunctionNames() {
        Set<String> functionNames = new HashSet<String>();
        functionNames.addAll(JetShortFunctionNameIndex.getInstance().getAllKeys(project));
        functionNames.addAll(JetFromJavaDescriptorHelper.getPossiblePackageDeclarationsNames(project, GlobalSearchScope.allScope(project)));
        return functionNames;
    }

    // TODO: Make it work for properties
    @NotNull
    public Collection<FunctionDescriptor> getTopLevelFunctionDescriptorsByName(
            @NotNull String name,
            @NotNull JetSimpleNameExpression expression,
            @NotNull ResolveSession resolveSession,
            @NotNull GlobalSearchScope scope
    ) {
        BindingContext context = ResolveSessionUtils.getExpressionBindingContext(resolveSession, expression);
        JetScope jetScope = context.get(BindingContext.RESOLUTION_SCOPE, expression);

        if (jetScope == null || name.isEmpty()) {
            return Collections.emptyList();
        }

        Set<FunctionDescriptor> result = Sets.newHashSet();

        Collection<PsiMethod> topLevelFunctionPrototypes = JetFromJavaDescriptorHelper.getTopLevelFunctionPrototypesByName(name, project, scope);
        for (PsiMethod method : topLevelFunctionPrototypes) {
            FqName functionFQN = JetFromJavaDescriptorHelper.getJetTopLevelDeclarationFQN(method);
            if (functionFQN != null) {
                JetImportDirective importDirective = JetPsiFactory.createImportDirective(project, new ImportPath(functionFQN, false));
                Collection<? extends DeclarationDescriptor> declarationDescriptors = new QualifiedExpressionResolver().analyseImportReference(
                        importDirective, jetScope, new BindingTraceContext());
                for (DeclarationDescriptor declarationDescriptor : declarationDescriptors) {
                    if (declarationDescriptor instanceof FunctionDescriptor) {
                        result.add((FunctionDescriptor) declarationDescriptor);
                    }
                }
            }
        }

        Set<FqName> affectedPackages = Sets.newHashSet();
        Collection<JetNamedFunction> jetNamedFunctions = JetShortFunctionNameIndex.getInstance().get(name, project, scope);
        for (JetNamedFunction jetNamedFunction : jetNamedFunctions) {
            PsiFile containingFile = jetNamedFunction.getContainingFile();
            if (containingFile instanceof JetFile) {
                JetFile jetFile = (JetFile) containingFile;
                String packageName = jetFile.getPackageName();
                if (packageName != null) {
                    affectedPackages.add(new FqName(packageName));
                }
            }
        }

        Name referenceName = Name.identifier(name);

        for (FqName affectedPackage : affectedPackages) {
            NamespaceDescriptor packageDescriptor = resolveSession.getPackageDescriptorByFqName(affectedPackage);
            assert packageDescriptor != null : "There's a function in stub index with invalid package";
            JetScope memberScope = packageDescriptor.getMemberScope();
            result.addAll(memberScope.getFunctions(referenceName));
        }

        return result;
    }

    /**
     * Get jet extensions top-level function names. Method is allowed to give invalid names - all result should be
     * checked with getAllJetExtensionFunctionsByName().
     *
     * @return
     */
    @NotNull
    public Collection<String> getAllJetExtensionFunctionsNames(@NotNull GlobalSearchScope scope) {
        Set<String> extensionFunctionNames = new HashSet<String>();

        extensionFunctionNames.addAll(JetExtensionFunctionNameIndex.getInstance().getAllKeys(project));
        extensionFunctionNames.addAll(JetFromJavaDescriptorHelper.getTopExtensionFunctionNames(project, scope));

        return extensionFunctionNames;
    }

    public Collection<PsiElement> getJetExtensionFunctionsByName(@NotNull String name, @NotNull GlobalSearchScope scope) {
        HashSet<PsiElement> functions = new HashSet<PsiElement>();
        functions.addAll(JetExtensionFunctionNameIndex.getInstance().get(name, project, scope));
        functions.addAll(JetFromJavaDescriptorHelper.getTopExtensionFunctionPrototypesByName(name, project, scope));

        return functions;
    }

    // TODO: Make it work for properties
    public Collection<DeclarationDescriptor> getJetCallableExtensions(
            @NotNull Condition<String> acceptedNameCondition,
            @NotNull JetSimpleNameExpression expression,
            @NotNull ResolveSession resolveSession,
            @NotNull GlobalSearchScope searchScope
    ) {
        Collection<DeclarationDescriptor> resultDescriptors = new ArrayList<DeclarationDescriptor>();

        BindingContext context = ResolveSessionUtils.getExpressionBindingContext(resolveSession, expression);
        JetExpression receiverExpression = expression.getReceiverExpression();

        if (receiverExpression != null) {
            JetType expressionType = context.get(BindingContext.EXPRESSION_TYPE, receiverExpression);
            JetScope scope = context.get(BindingContext.RESOLUTION_SCOPE, receiverExpression);

            if (expressionType != null && scope != null) {
                Collection<String> extensionFunctionsNames = getAllJetExtensionFunctionsNames(searchScope);

                Set<FqName> functionFQNs = new java.util.HashSet<FqName>();

                // Collect all possible extension function qualified names
                for (String name : extensionFunctionsNames) {
                    if (acceptedNameCondition.value(name)) {
                        Collection<PsiElement> extensionFunctions = getJetExtensionFunctionsByName(name, searchScope);

                        for (PsiElement extensionFunction : extensionFunctions) {
                            if (extensionFunction instanceof JetNamedFunction) {
                                functionFQNs.add(JetPsiUtil.getFQName((JetNamedFunction) extensionFunction));
                            }
                            else if (extensionFunction instanceof PsiMethod) {
                                FqName functionFQN = JetFromJavaDescriptorHelper.getJetTopLevelDeclarationFQN((PsiMethod) extensionFunction);
                                if (functionFQN != null) {
                                    functionFQNs.add(functionFQN);
                                }
                            }
                        }
                    }
                }

                // Iterate through the function with attempt to resolve found functions
                for (FqName functionFQN : functionFQNs) {
                    for (CallableDescriptor functionDescriptor : ExpressionTypingUtils.canFindSuitableCall(
                            functionFQN, project, receiverExpression, expressionType, scope)) {

                        resultDescriptors.add(functionDescriptor);
                    }
                }
            }
        }

        return resultDescriptors;
    }

    public Collection<ClassDescriptor> getJetClassesDescriptors(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull ResolveSession resolveSession
    ) {
        Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

        for (String fqName : JetFullClassNameIndex.getInstance().getAllKeys(project)) {
            FqName classFQName = new FqName(fqName);
            if (acceptedShortNameCondition.value(classFQName.shortName().getName())) {
                classDescriptors.addAll(ResolveSessionUtils.getClassDescriptorsByFqName(resolveSession, classFQName));
            }
        }

        return classDescriptors;
    }

    @NotNull
    @Override
    public PsiMethod[] getMethodsByName(@NonNls @NotNull String name, @NotNull GlobalSearchScope scope) {
        return NO_METHODS;
    }

    @NotNull
    @Override
    public PsiMethod[] getMethodsByNameIfNotMoreThan(@NonNls @NotNull String name, @NotNull GlobalSearchScope scope, int maxCount) {
        return NO_METHODS;
    }

    @NotNull
    @Override
    public PsiField[] getFieldsByNameIfNotMoreThan(@NonNls @NotNull String s, @NotNull GlobalSearchScope scope, int i) {
        return NO_FIELDS;
    }

    @Override
    public boolean processMethodsWithName(@NonNls @NotNull String name, @NotNull GlobalSearchScope scope, @NotNull Processor<PsiMethod> processor) {
        return false;
    }

    @NotNull
    @Override
    public String[] getAllMethodNames() {
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public void getAllMethodNames(@NotNull HashSet<String> set) {
        set.addAll(JetShortFunctionNameIndex.getInstance().getAllKeys(project));
    }

    @NotNull
    @Override
    public PsiField[] getFieldsByName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope) {
        return NO_FIELDS;
    }

    @NotNull
    @Override
    public String[] getAllFieldNames() {
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public void getAllFieldNames(@NotNull HashSet<String> set) {
    }
}
