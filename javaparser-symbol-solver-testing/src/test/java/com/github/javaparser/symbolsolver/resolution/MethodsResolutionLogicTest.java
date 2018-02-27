/*
 * Copyright 2016 Federico Tomassetti
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

package com.github.javaparser.symbolsolver.resolution;

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionFactory;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MethodsResolutionLogicTest extends AbstractResolutionTest {

    private TypeSolver typeSolver;

    @Before
    public void setup() {
        File srcNewCode = adaptPath(new File("src/test/test_sourcecode/javaparser_new_src/javaparser-core"));
        CombinedTypeSolver combinedTypeSolverNewCode = new CombinedTypeSolver();
        combinedTypeSolverNewCode.add(new ReflectionTypeSolver());
        combinedTypeSolverNewCode.add(new JavaParserTypeSolver(srcNewCode));
        combinedTypeSolverNewCode.add(new JavaParserTypeSolver(adaptPath(new File("src/test/test_sourcecode/javaparser_new_src/javaparser-generated-sources"))));
        typeSolver = combinedTypeSolverNewCode;
    }

    @Test
    public void compatibilityShouldConsiderAlsoTypeVariablesNegative() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("com.github.javaparser.ast.body.ConstructorDeclaration");

        ResolvedReferenceType stringType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(String.class, typeSolver);
        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
        assertEquals(true, rawClassType.isRawType());
        ResolvedReferenceType classOfStringType = (ResolvedReferenceType) rawClassType.replaceTypeVariables(rawClassType.getTypeDeclaration().getTypeParameters().get(0), stringType);
        MethodUsage mu = constructorDeclaration.getAllMethods().stream().filter(m -> m.getDeclaration().getSignature().equals("isThrows(java.lang.Class<? extends java.lang.Throwable>)")).findFirst().get();

        assertEquals(false, MethodResolutionLogic.isApplicable(mu, "isThrows", ImmutableList.of(classOfStringType), typeSolver));
    }

    @Test
    public void compatibilityShouldConsiderAlsoTypeVariablesRaw() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("com.github.javaparser.ast.body.ConstructorDeclaration");

        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
        MethodUsage mu = constructorDeclaration.getAllMethods().stream().filter(m -> m.getDeclaration().getSignature().equals("isThrows(java.lang.Class<? extends java.lang.Throwable>)")).findFirst().get();

        assertEquals(true, MethodResolutionLogic.isApplicable(mu, "isThrows", ImmutableList.of(rawClassType), typeSolver));
    }

    @Test
    public void compatibilityShouldConsiderAlsoTypeVariablesPositive() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("com.github.javaparser.ast.body.ConstructorDeclaration");

        ResolvedReferenceType runtimeException = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(RuntimeException.class, typeSolver);
        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
        ResolvedReferenceType classOfRuntimeType = (ResolvedReferenceType) rawClassType.replaceTypeVariables(rawClassType.getTypeDeclaration().getTypeParameters().get(0), runtimeException);
        MethodUsage mu = constructorDeclaration.getAllMethods().stream().filter(m -> m.getDeclaration().getSignature().equals("isThrows(java.lang.Class<? extends java.lang.Throwable>)")).findFirst().get();

        assertEquals(true, MethodResolutionLogic.isApplicable(mu, "isThrows", ImmutableList.of(classOfRuntimeType), typeSolver));
    }

    @Test(expected = IllegalStateException.class)
    public void unsupportedOperationException() throws Throwable {

        Class[] args = new Class[1];
        args[0] = ResolvedReferenceType.class;

        Method method = ResolvedReferenceType.class.getDeclaredMethod("compareConsideringTypeParameters", args);
        method.setAccessible(true);

        String mock = "mock";

        ResolvedReferenceType resolved1 = Mockito.mock(ResolvedReferenceType.class);
        Mockito.when(resolved1.getQualifiedName()).thenReturn(mock);
        Mockito.when(resolved1.compareConsideringTypeParameters(Mockito.any(ResolvedReferenceType.class))).thenCallRealMethod();

        List mockList1 = Mockito.mock(ArrayList.class);
        Mockito.when(mockList1.size()).thenReturn((Integer)9);
        Mockito.when(resolved1.typeParametersValues()).thenReturn(mockList1);

        ResolvedReferenceType resolved2 = Mockito.mock(ResolvedReferenceType.class);
        Mockito.when(resolved2.getQualifiedName()).thenReturn(mock);

        List mockList2 = Mockito.mock(ArrayList.class);
        Mockito.when(mockList2.size()).thenReturn((Integer)10);
        Mockito.when(resolved2.typeParametersValues()).thenReturn(mockList2);

        resolved1.compareConsideringTypeParameters(resolved2);
    }
}
