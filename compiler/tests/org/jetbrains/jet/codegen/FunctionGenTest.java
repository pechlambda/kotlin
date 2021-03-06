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

import org.jetbrains.jet.ConfigurationKind;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author alex.tkachman
 */
public class FunctionGenTest extends CodegenTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createEnvironmentWithMockJdkAndIdeaAnnotations(ConfigurationKind.JDK_ONLY);
    }

    public void testDefaultArgs() throws Exception {
        blackBoxFile("functions/defaultargs.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs1() throws Exception {
        blackBoxFile("functions/defaultargs1.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs2() throws Exception {
        blackBoxFile("functions/defaultargs2.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs3() throws Exception {
        blackBoxFile("functions/defaultargs3.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs4() throws Exception {
        blackBoxFile("functions/defaultargs4.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs5() throws Exception {
        blackBoxFile("functions/defaultargs5.jet");
        //        System.out.println(generateToText());
    }

    public void testDefaultArgs6() {
        blackBoxFile("functions/defaultargs6.kt");
    }

    public void testDefaultArgs7() {
        blackBoxFile("functions/defaultargs7.kt");
    }

    public void testNoThisNoClosure() throws Exception {
        blackBoxFile("functions/nothisnoclosure.jet");
        //        System.out.println(generateToText());
    }

    public void testAnyEqualsNullable() throws InvocationTargetException, IllegalAccessException {
        loadText("fun foo(x: Any?) = x.equals(\"lala\")");
        //        System.out.println(generateToText());
        Method foo = generateFunction();
        assertTrue((Boolean) foo.invoke(null, "lala"));
        assertFalse((Boolean) foo.invoke(null, "mama"));
    }

    public void testNoRefToOuter() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        loadText("class A() { fun f() : ()->String { val s = \"OK\"; return { -> s } } }");
        //        System.out.println(generateToText());
        Class foo = generateClass("A");
        final Object obj = foo.newInstance();
        final Method f = foo.getMethod("f");
        final Object closure = f.invoke(obj);
        final Class<? extends Object> aClass = closure.getClass();
        final Field[] fields = aClass.getFields();
        assertEquals(1, fields.length);
        assertEquals("$s", fields[0].getName());
    }

    public void testAnyEquals() throws InvocationTargetException, IllegalAccessException {
        loadText("fun foo(x: Any) = x.equals(\"lala\")");
        //        System.out.println(generateToText());
        Method foo = generateFunction();
        assertTrue((Boolean) foo.invoke(null, "lala"));
        assertFalse((Boolean) foo.invoke(null, "mama"));
    }

    public void testKt395() {
        blackBoxFile("regressions/kt395.jet");
    }

    public void testKt785() {
        blackBoxFile("regressions/kt785.jet");
    }

    public void testKt873() {
        blackBoxFile("regressions/kt873.kt");
    }

    public void testKt1413() {
        blackBoxFile("regressions/kt1413.kt");
    }

    public void testKt1199() {
        blackBoxFile("regressions/kt1199.kt");
        System.out.println(generateToText());
    }

    public void testFunction() throws InvocationTargetException, IllegalAccessException {
        blackBoxFile("functions/functionExpression.jet");
    }

    public void testLocalFunction() throws InvocationTargetException, IllegalAccessException {
        blackBoxFile("functions/localFunction.kt");
    }

    public void testInvoke() {
        blackBoxFile("functions/invoke.kt");
    }

    public void testKt2481() {
        blackBoxFile("regressions/kt2481.kt");
    }

    public void testKt2280() {
        blackBoxFile("regressions/kt2280.kt");
    }

    public void testKt1739() {
        blackBoxFile("regressions/kt1739.kt");
    }

    public void testKt2271() {
        blackBoxFile("regressions/kt2271.kt");
    }

    public void testKt2270() {
        blackBoxFile("regressions/kt2270.kt");
    }

    public void testK1649_1() {
        loadFile("regressions/kt1649_1.kt");
        generateToText();
    }

    public void testK1649_2() {
        loadFile("regressions/kt1649_2.kt");
        generateToText();
    }

    public void testKt1038() {
        blackBoxFile("regressions/kt1038.kt");
    }


    public void testReferencesStaticInnerClassMethod() throws Exception {
        blackBoxFileWithJava("functions/referencesStaticInnerClassMethod.kt");
    }

    public void testReferencesStaticInnerClassMethodTwoLevels() throws Exception {
        blackBoxFileWithJava("functions/referencesStaticInnerClassMethodL2.kt");
    }

    public void testRemoveInIterator() throws Exception {
        blackBoxFileWithJava("functions/removeInIterator.kt");
    }
}
