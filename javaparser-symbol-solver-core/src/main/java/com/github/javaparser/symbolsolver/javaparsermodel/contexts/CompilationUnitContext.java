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

package com.github.javaparser.symbolsolver.javaparsermodel.contexts;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.coveragetool.CoverageTool;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserAnnotationDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;

import java.util.List;

/**
 * @author Federico Tomassetti
 */
public class CompilationUnitContext extends AbstractJavaParserContext<CompilationUnit> {

    ///
    /// Static methods
    ///

    private static boolean isQualifiedName(String name) {
        return name.contains(".");
    }

    ///
    /// Constructors
    ///

    public CompilationUnitContext(CompilationUnit wrappedNode, TypeSolver typeSolver) {
        super(wrappedNode, typeSolver);
    }

    ///
    /// Public methods
    ///

    @Override
    public SymbolReference<? extends ResolvedValueDeclaration> solveSymbol(String name, TypeSolver typeSolver) {

        // solve absolute references
        String itName = name;
        while (itName.contains(".")) {
            String typeName = getType(itName);
            String memberName = getMember(itName);
            SymbolReference<ResolvedTypeDeclaration> type = this.solveType(typeName, typeSolver);
            if (type.isSolved()) {
                return new SymbolSolver(typeSolver).solveSymbolInType(type.getCorrespondingDeclaration(), memberName);
            } else {
                itName = typeName;
            }
        }

        // Look among statically imported values
        if (wrappedNode.getImports() != null) {
            for (ImportDeclaration importDecl : wrappedNode.getImports()) {
                if(importDecl.isStatic()){
                    if(importDecl.isAsterisk()) {
                        String qName = importDecl.getNameAsString();
                        ResolvedTypeDeclaration importedType = typeSolver.solveType(qName);
                        SymbolReference<? extends ResolvedValueDeclaration> ref = new SymbolSolver(typeSolver).solveSymbolInType(importedType, name);
                        if (ref.isSolved()) {
                            return ref;
                        }
                    } else{
                        String whole = importDecl.getNameAsString();

                        // split in field/method name and type name
                        String memberName = getMember(whole);
                        String typeName = getType(whole);

                        if (memberName.equals(name)) {
                            ResolvedTypeDeclaration importedType = typeSolver.solveType(typeName);
                            return new SymbolSolver(typeSolver).solveSymbolInType(importedType, memberName);
                        }
                    }
                }
            }
        }

        return SymbolReference.unsolved(ResolvedValueDeclaration.class);
    }

    @Override
    public SymbolReference<ResolvedTypeDeclaration> solveType(String name, TypeSolver typeSolver) {
        CoverageTool.makeCovered("solveType 1");
        if (wrappedNode.getTypes() != null) {
            CoverageTool.makeCovered("solveType 2");
            for (TypeDeclaration<?> type : wrappedNode.getTypes()) {
                CoverageTool.makeCovered("solveType 3");
                if (type.getName().getId().equals(name)) {
                    CoverageTool.makeCovered("solveType 4");
                    if (type instanceof ClassOrInterfaceDeclaration) {
                        CoverageTool.makeCovered("solveType 5");
                        return SymbolReference.solved(JavaParserFacade.get(typeSolver).getTypeDeclaration((ClassOrInterfaceDeclaration) type));
                    } else if (type instanceof AnnotationDeclaration) {
                        CoverageTool.makeCovered("solveType 6");
                        return SymbolReference.solved(new JavaParserAnnotationDeclaration((AnnotationDeclaration) type, typeSolver));
                    } else if (type instanceof EnumDeclaration) {
                        CoverageTool.makeCovered("solveType 7");
                        return SymbolReference.solved(new JavaParserEnumDeclaration((EnumDeclaration) type, typeSolver));
                    } else {
                        CoverageTool.makeCovered("solveType 8");
                        throw new UnsupportedOperationException(type.getClass().getCanonicalName());
                    }
                }
            }
        }

        if (wrappedNode.getImports() != null) {
            CoverageTool.makeCovered("solveType 9");
            int dotPos = name.indexOf('.');
            String prefix = null;
            if (dotPos > -1) {
                CoverageTool.makeCovered("solveType 10");
                prefix = name.substring(0, dotPos);
            }
            // look into type imports
            for (ImportDeclaration importDecl : wrappedNode.getImports()) {
                CoverageTool.makeCovered("solveType 11");
                if (!importDecl.isAsterisk()) {
                    CoverageTool.makeCovered("solveType 12");
                    String qName = importDecl.getNameAsString();
                    boolean defaultPackage = !importDecl.getName().getQualifier().isPresent();
                    boolean found = !defaultPackage && importDecl.getName().getIdentifier().equals(name);
                    if (!found) {
                        CoverageTool.makeCovered("solveType 13");
                        if (prefix != null) {
                            CoverageTool.makeCovered("solveType 14");
                            found = qName.endsWith("." + prefix);
                            if (found) {
                                CoverageTool.makeCovered("solveType 15");
                                qName = qName + name.substring(dotPos);
                            }
                        }
                    }
                    if (found) {
                        CoverageTool.makeCovered("solveType 16");
                        SymbolReference<ResolvedReferenceTypeDeclaration> ref = typeSolver.tryToSolveType(qName);
                        if (ref.isSolved()) {
                            CoverageTool.makeCovered("solveType 17");
                            return SymbolReference.adapt(ref, ResolvedTypeDeclaration.class);
                        }
                    }
                }
            }
            // look into type imports on demand
            for (ImportDeclaration importDecl : wrappedNode.getImports()) {
                CoverageTool.makeCovered("solveType 18");
                if (importDecl.isAsterisk()) {
                    CoverageTool.makeCovered("solveType 19");
                    String qName = importDecl.getNameAsString() + "." + name;
                    SymbolReference<ResolvedReferenceTypeDeclaration> ref = typeSolver.tryToSolveType(qName);
                    if (ref.isSolved()) {
                        CoverageTool.makeCovered("solveType 20");
                        return SymbolReference.adapt(ref, ResolvedTypeDeclaration.class);
                    }
                }
            }
        }

        // Look in current package
        if (this.wrappedNode.getPackageDeclaration().isPresent()) {
            CoverageTool.makeCovered("solveType 21");
            String qName = this.wrappedNode.getPackageDeclaration().get().getName().toString() + "." + name;
            SymbolReference<ResolvedReferenceTypeDeclaration> ref = typeSolver.tryToSolveType(qName);
            if (ref.isSolved()) {
                CoverageTool.makeCovered("solveType 22");
                return SymbolReference.adapt(ref, ResolvedTypeDeclaration.class);
            }
        } else {
            CoverageTool.makeCovered("solveType 23");
            // look for classes in the default package
            String qName = name;
            SymbolReference<ResolvedReferenceTypeDeclaration> ref = typeSolver.tryToSolveType(qName);
            if (ref.isSolved()) {
                CoverageTool.makeCovered("solveType 24");
                return SymbolReference.adapt(ref, ResolvedTypeDeclaration.class);
            }
        }

        // Look in the java.lang package
        SymbolReference<ResolvedReferenceTypeDeclaration> ref = typeSolver.tryToSolveType("java.lang." + name);
        if (ref.isSolved()) {
            CoverageTool.makeCovered("solveType 25");
            return SymbolReference.adapt(ref, ResolvedTypeDeclaration.class);
        }

        // DO NOT look for absolute name if this name is not qualified: you cannot import classes from the default package
        if (isQualifiedName(name)) {
            CoverageTool.makeCovered("solveType 26");
            return SymbolReference.adapt(typeSolver.tryToSolveType(name), ResolvedTypeDeclaration.class);
        } else {
            CoverageTool.makeCovered("solveType 27");
            return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
        }
    }

    private String qName(ClassOrInterfaceType type) {
        if (type.getScope().isPresent()) {
            return qName(type.getScope().get()) + "." + type.getName().getId();
        } else {
            return type.getName().getId();
        }
    }

    private String qName(Name name) {
        if (name.getQualifier().isPresent()) {
            return qName(name.getQualifier().get()) + "." + name.getId();
        } else {
            return name.getId();
        }
    }

    private String toSimpleName(String qName) {
        String[] parts = qName.split("\\.");
        return parts[parts.length - 1];
    }

    private String packageName(String qName) {
        int lastDot = qName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new UnsupportedOperationException();
        } else {
            return qName.substring(0, lastDot);
        }
    }

    @Override
    public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly, TypeSolver typeSolver) {
        for (ImportDeclaration importDecl : wrappedNode.getImports()) {
            if(importDecl.isStatic()){
                if(importDecl.isAsterisk()){
                    String importString = importDecl.getNameAsString();

                    if (this.wrappedNode.getPackageDeclaration().isPresent()
                            && this.wrappedNode.getPackageDeclaration().get().getName().getIdentifier().equals(packageName(importString))
                            && this.wrappedNode.getTypes().stream().anyMatch(it -> it.getName().getIdentifier().equals(toSimpleName(importString)))) {
                        // We are using a static import on a type defined in this file. It means the value was not found at
                        // a lower level so this will fail
                        return SymbolReference.unsolved(ResolvedMethodDeclaration.class);
                    }

                    ResolvedTypeDeclaration ref = typeSolver.solveType(importString);
                    SymbolReference<ResolvedMethodDeclaration> method = MethodResolutionLogic.solveMethodInType(ref, name, argumentsTypes, true, typeSolver);

                    if (method.isSolved()) {
                        return method;
                    }
                } else{
                    String qName = importDecl.getNameAsString();

                    if (qName.equals(name) || qName.endsWith("." + name)) {
                        String typeName = getType(qName);
                        ResolvedTypeDeclaration ref = typeSolver.solveType(typeName);
                        SymbolReference<ResolvedMethodDeclaration> method = MethodResolutionLogic.solveMethodInType(ref, name, argumentsTypes, true, typeSolver);
                        if (method.isSolved()) {
                            return method;
                        }
                    }
                }
            }
        }
        return SymbolReference.unsolved(ResolvedMethodDeclaration.class);
    }

    ///
    /// Private methods
    ///

    private String getType(String qName) {
        int index = qName.lastIndexOf('.');
        if (index == -1) {
            throw new UnsupportedOperationException();
        }
        String typeName = qName.substring(0, index);
        return typeName;
    }

    private String getMember(String qName) {
        int index = qName.lastIndexOf('.');
        if (index == -1) {
            throw new UnsupportedOperationException();
        }
        String memberName = qName.substring(index + 1);
        return memberName;
    }
}
