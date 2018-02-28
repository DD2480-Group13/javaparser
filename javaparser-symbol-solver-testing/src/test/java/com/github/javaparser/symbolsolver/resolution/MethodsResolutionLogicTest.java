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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
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

    /**
     * Covers the first return statement in MethodResolutionLogic.isApplicable(MethodUsage method, ...) ~line 275
     * also covers the first return statement in MethodResolutionLogic.isApplicable(ResolvedMethodDeclaration method, ...) ~line 71
     * and its corresponding helper function, isApplicable(ResolvedMethodDeclaration method, String name, List<ResolvedType> argumentsTypes, TypeSolver typeSolver).
     *
     * The return statement in question checks that parameter method.getName() is equal to parameter name, else simply returns false.
     * Method coverage before: 91,3%
     * Method coverage after: 95.7%
     * Branch coverage before: 27/34 = 79.4%
     * Branch coverage after: ??
     */
    @Test
    public void differingMethodNamesReturnsFalse() {
        JavaParserClassDeclaration constructorDeclaration = (JavaParserClassDeclaration) typeSolver.solveType("com.github.javaparser.ast.body.ConstructorDeclaration");

        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
        MethodUsage mu = constructorDeclaration.getAllMethods().stream().filter(m -> m.getDeclaration().getSignature().equals("isThrows(java.lang.Class<? extends java.lang.Throwable>)")).findFirst().get();

        //This .isApplicable call is to another method declared on line ~275, taking MethodUsage method as parameter.
        //Makes sure that the name check branch is reached in that method.
        assertEquals(false, MethodResolutionLogic.isApplicable(mu, "isntThrows", ImmutableList.of(rawClassType), typeSolver));

        //Generating a ResolvedMethodDeclaration method that differs from mu above.
        CompilationUnit cu = parseSample("Issue338");
        ClassOrInterfaceDeclaration clazz = Navigator.demandClass(cu, "TypePromotions");
        MethodDeclaration method = Navigator.demandMethod(clazz, "callingLong");
        MethodCallExpr expression = method.getBody().get().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr();
        SymbolReference<ResolvedMethodDeclaration> reference = JavaParserFacade.get(new ReflectionTypeSolver()).solve(expression);
        ResolvedMethodDeclaration typeDecl = reference.getCorrespondingDeclaration();

        // Validates that branch of isApplicable method in question
        assertEquals(false, MethodResolutionLogic.isApplicable(typeDecl, "isThrows", ImmutableList.of(rawClassType), typeSolver));

    }

    /**
     * Code reaching this is found in AnalyseNewJavaParserTest.parseAllOtherNodes, and  parseModifier
     * Test case reaching if (pos > argumentsTypes.size()) condition in MethodResolutionLogic.java :: isApplicable(ResolvedMethodDeclaration method,...)
     * This is done using mocked objects, as all examples found on how to construct objects reaching this code are found in tests such as
     * AnalyseNewJavaParserTest.parseAllOtherNodes, and  parseModifier, which parses actual files and therefore gets quite complicated.
     *
     * The conditions required to be met in order to reach the tested condition is;
     *  1) the methods name is equal to the name parameter.
     *  2) method returns true for method.hasVariadicParameter()
     *  3) method.getNumberOfParams is not equal to argumentsTypes.size(), which is the third parameter, List<ResolvedType>
     *  4) method.getNumberOfParams() - 1 must be bigger than argumentsTypes.size().
     *  If all these are fulfilled, the method should return false.
     *
     * Branch coverage before: 27/34 = 79.4%
     * Branch coverage after: ???
     */
    @Test
    public void variadicParametersPosBiggerThanArgumentTypesReturnsFalse() {
        ResolvedReferenceType rawClassType = (ResolvedReferenceType) ReflectionFactory.typeUsageFor(Class.class, typeSolver);
        String name = "isThrows";
        ResolvedMethodDeclaration mocked_method = Mockito.mock(ResolvedMethodDeclaration.class);
        Mockito.when(mocked_method.getName()).thenReturn(name);
        Mockito.when(mocked_method.hasVariadicParameter()).thenReturn(true);
        Mockito.when(mocked_method.getNumberOfParams()).thenReturn(5);

        assertEquals(false, MethodResolutionLogic.isApplicable(mocked_method, name, ImmutableList.of(rawClassType), typeSolver));
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
