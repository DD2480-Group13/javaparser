package com.github.javaparser.symbolsolver;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaParserFacadeTest {

    @Ignore
    @Test(expected = NullPointerException.class)
    public void convertToUsageNullPointerException() {

        JavaParserFacade javaParserFacade = JavaParserFacade.get(new ReflectionTypeSolver());
        javaParserFacade.convertToUsage(null);
    }

    @Ignore
    @Test(expected = UnsolvedSymbolException.class)
    public void classOrInterfaceTypeUnsolvedSymbolException() throws Throwable {

        Class[] args = new Class[2];
        args[0] = com.github.javaparser.ast.type.Type.class;
        args[1] = Context.class;

        Method method = JavaParserFacade.class.getDeclaredMethod("convertToUsage", args);
        method.setAccessible(true);

        SymbolReference<ResolvedTypeDeclaration> ref = Mockito.mock(SymbolReference.class);
        Mockito.when(ref.isSolved()).thenReturn(false);

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.solveType(ArgumentMatchers.<String>any(), ArgumentMatchers.<TypeSolver>any())).thenReturn(ref);

        try {
            JavaParserFacade javaParserFacade = JavaParserFacade.get(new ReflectionTypeSolver());
            ClassOrInterfaceType classOrInterfaceType = new ClassOrInterfaceType();
            method.invoke(javaParserFacade, classOrInterfaceType, context);
        }catch(InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unsupportedOperationException() throws Throwable {

        Class[] args = new Class[2];
        args[0] = com.github.javaparser.ast.type.Type.class;
        args[1] = Context.class;

        Method method = JavaParserFacade.class.getDeclaredMethod("convertToUsage", args);
        method.setAccessible(true);

        JavaParserFacade javaParserFacade = JavaParserFacade.get(new ReflectionTypeSolver());

        try {
            method.invoke(javaParserFacade, Mockito.mock(com.github.javaparser.ast.type.Type.class), Mockito.mock(Context.class));
        }catch(InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
