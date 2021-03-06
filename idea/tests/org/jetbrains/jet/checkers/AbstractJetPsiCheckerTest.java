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

package org.jetbrains.jet.checkers;

import com.intellij.ProjectTopics;
import com.intellij.codeInsight.daemon.LightDaemonAnalyzerTestCase;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.impl.ModuleRootEventImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.plugin.PluginTestCaseBase;
import org.jetbrains.jet.test.generator.SimpleTestClassModel;
import org.jetbrains.jet.test.generator.TestGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author abreslav
 */
public abstract class AbstractJetPsiCheckerTest extends LightDaemonAnalyzerTestCase {

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        /*
         * TODO: remove this when fixed in IDEA
         * super.tearDown() calls cleanupForNextTest() on PsiManagerImpl, which invalidates a cache of view providers
         * this affects ExternalAnnotationsManagerImpl making XmlFile instances cached in it invalid
         * this results in external annotations being not found, unless we invalidate the cache in ExternalAnnotationsManagerImpl.
         * Currently, sending this funny event is the only way (apart from reflection) to invalidate that cache.
         * The problem will be fixed in IDEA soon (hopefully).
         */
        getProject().getMessageBus().syncPublisher(ProjectTopics.PROJECT_ROOTS).rootsChanged(new ModuleRootEventImpl(getProject(), true));
    }

    public void doTest(@NotNull String filePath) throws Exception {
        doTest(filePath, true, false);
    }

    public void doTestWithInfos(@NotNull String filePath) throws Exception {
        doTest(filePath, true, true);
    }

    @Override
    protected String getTestDataPath() {
        return "";
    }

    @Override
    protected Sdk getProjectJDK() {
        return PluginTestCaseBase.jdkFromIdeaHome();
    }

    public static void main(String[] args) throws IOException {
        Class<AbstractJetPsiCheckerTest> thisClass = AbstractJetPsiCheckerTest.class;
        String aPackage = thisClass.getPackage().getName();
        new TestGenerator(
                "idea/tests/",
                aPackage,
                "JetPsiCheckerTestGenerated",
                thisClass,
                Arrays.asList(
                        new SimpleTestClassModel(new File("idea/testData/checker"), false, "jet", "doTest"),
                        new SimpleTestClassModel(new File("idea/testData/checker/regression"), true, "jet", "doTest"),
                        new SimpleTestClassModel(new File("idea/testData/checker/infos"), true, "jet", "doTestWithInfos")
                ),
                thisClass
        ).generateAndSave();
    }
}
