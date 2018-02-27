package com.github.javaparser.symbolsolver.javaparsermodel.declarations;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.ContextTest;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class JavaParserFacadeTest {
    private TypeSolver typeSolver = new CombinedTypeSolver(new MemoryTypeSolver(), new ReflectionTypeSolver());

    private CompilationUnit parseSample(String sampleName) {
        InputStream is = ContextTest.class.getClassLoader().getResourceAsStream(sampleName + ".java.txt");
        return JavaParser.parse(is);
    }

    private Context emptyContext = new Context() {
        @Override
        public Context getParent() {
            return null;
        }

        @Override
        public SymbolReference<? extends ResolvedValueDeclaration> solveSymbol(String name, TypeSolver typeSolver) {
            return null;
        }

        @Override
        public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly, TypeSolver typeSolver) {
            return null;
        }
    };

    @Test
    public void testNullContextInConvertToUsage() {
        //Contract: the context should not be null
        try {
            JavaParserFacade.get(typeSolver).convert(null, (Context) null);
            Assert.fail("No exception thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Context should not be null", e.getMessage());
        }
    }

    @Test
    public void testRefIsNotSolvedInConvertToUsage() {
        //Contract: if the type is instanceof ClassOrInterfaceType, its reference should be solved
        // meaning the parent of the context is not null
        CompilationUnit cu = parseSample("NavigatorSimplified");
        com.github.javaparser.ast.body.ClassOrInterfaceDeclaration clazz = Navigator.demandClass(cu, "Navigator");
        MethodDeclaration method = Navigator.demandMethod(clazz, "foo");
        com.github.javaparser.ast.type.Type streamJavaParserType = method.getParameters().get(0).getType();

        TypeSolver typeSolver = new ReflectionTypeSolver();
        try {
            JavaParserFacade.get(typeSolver).convert(streamJavaParserType, emptyContext);
            Assert.fail("No exception thrown");
        } catch (UnsolvedSymbolException e) {
            Assert.assertEquals(new UnsolvedSymbolException(((ClassOrInterfaceType) streamJavaParserType).getName().getId()).getMessage(), e.getMessage());
        }
    }

    @Test
    public void testInexistingOperationInConvertToUsage() {
        //Contract: Should throw an exception when the type of operation does not exist/is not supported
        try {
            JavaParserFacade.get(typeSolver).convert(new com.github.javaparser.ast.type.TypeParameter(), emptyContext);
            Assert.fail("No exception thrown");
        }catch(UnsupportedOperationException e){
            Assert.assertEquals("com.github.javaparser.ast.type.TypeParameter", e.getMessage());
        }
    }

    @Test
    public void testVarTypeParentOfTypeVariableDeclarationInConvertToUsage(){
        //Contract: if the type is instanceof VarType, its parentNode should be of none other type than VariableDeclaration
        Type type = new VarType();
        type.setParentNode(new Expression() {
            @Override
            public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
                return null;
            }

            @Override
            public <A> void accept(VoidVisitor<A> v, A arg) {

            }
        });
        try {
            JavaParserFacade.get(typeSolver).convert(type, emptyContext);
            Assert.fail("No exception thrown");
        }catch (IllegalStateException e){
            Assert.assertEquals("Trying to resolve a `var` which is not in a variable declaration.", e.getMessage());
        }
    }
}