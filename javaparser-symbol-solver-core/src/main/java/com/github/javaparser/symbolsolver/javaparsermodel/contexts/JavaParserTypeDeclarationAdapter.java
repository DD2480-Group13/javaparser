package com.github.javaparser.symbolsolver.javaparsermodel.contexts;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.coveragetool.CoverageTool;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserTypeParameter;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.reflectionmodel.*;
import com.github.javaparser.symbolsolver.resolution.ConstructorResolutionLogic;
import com.github.javaparser.symbolsolver.resolution.MethodResolutionLogic;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Federico Tomassetti
 */
public class
JavaParserTypeDeclarationAdapter {

    private com.github.javaparser.ast.body.TypeDeclaration<?> wrappedNode;
    private TypeSolver typeSolver;
    private Context context;
    private ResolvedReferenceTypeDeclaration typeDeclaration;

    public JavaParserTypeDeclarationAdapter(com.github.javaparser.ast.body.TypeDeclaration<?> wrappedNode, TypeSolver typeSolver,
                                            ResolvedReferenceTypeDeclaration typeDeclaration,
                                            Context context) {
        this.wrappedNode = wrappedNode;
        this.typeSolver = typeSolver;
        this.typeDeclaration = typeDeclaration;
        this.context = context;
    }

    public SymbolReference<ResolvedTypeDeclaration> solveType(String name, TypeSolver typeSolver) {
        CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 0");
        if (this.wrappedNode.getName().getId().equals(name)) {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 1");
            return SymbolReference.solved(JavaParserFacade.get(typeSolver).getTypeDeclaration(wrappedNode));
        } else {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 2");
        }
        // Internal classes
        for (BodyDeclaration<?> member : this.wrappedNode.getMembers()) {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 3");
            if (member instanceof com.github.javaparser.ast.body.TypeDeclaration) {
                CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 4");
                com.github.javaparser.ast.body.TypeDeclaration<?> internalType = (com.github.javaparser.ast.body.TypeDeclaration<?>) member;
                if (internalType.getName().getId().equals(name)) {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 5");
                    return SymbolReference.solved(JavaParserFacade.get(typeSolver).getTypeDeclaration(internalType));
                } else if (name.startsWith(String.format("%s.%s", wrappedNode.getName(), internalType.getName()))) {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 6");
                    return JavaParserFactory.getContext(internalType, typeSolver).solveType(name.substring(wrappedNode.getName().getId().length() + 1), typeSolver);
                } else if (name.startsWith(String.format("%s.", internalType.getName()))) {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 7");
                    return JavaParserFactory.getContext(internalType, typeSolver).solveType(name.substring(internalType.getName().getId().length() + 1), typeSolver);
                } else {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 8");
                }
            } else {
                CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 9");
            }
        }

        if (wrappedNode instanceof NodeWithTypeParameters) {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 10");
            NodeWithTypeParameters<?> nodeWithTypeParameters = (NodeWithTypeParameters<?>) wrappedNode;
            for (TypeParameter astTpRaw : nodeWithTypeParameters.getTypeParameters()) {
                CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 11");
                TypeParameter astTp = astTpRaw;
                if (astTp.getName().getId().equals(name)) {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 12");
                    return SymbolReference.solved(new JavaParserTypeParameter(astTp, typeSolver));
                } else {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 13");
                }
            }
        } else {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 14");
        }

        // Look into extended classes and implemented interfaces
        for (ResolvedReferenceType ancestor : this.typeDeclaration.getAncestors()) {
            CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 15");
        	try {
	            for (ResolvedTypeDeclaration internalTypeDeclaration : ancestor.getTypeDeclaration().internalTypes()) {
                    CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 16");
	                if (internalTypeDeclaration.getName().equals(name)) {
                        CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 17");
	                    return SymbolReference.solved(internalTypeDeclaration);
	                } else {
                        CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 18");
                    }
	            }
        	} catch (UnsupportedOperationException e) {
                CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 19");
	            // just continue using the next ancestor
            }
        }
        CoverageTool.makeCovered("JavaParserTypeDeclarationAdapter.solveType 20");
        return context.getParent().solveType(name, typeSolver);
    }

    public SymbolReference<ResolvedMethodDeclaration> solveMethod(String name, List<ResolvedType> argumentsTypes, boolean staticOnly, TypeSolver typeSolver) {
        List<ResolvedMethodDeclaration> candidateMethods = typeDeclaration.getDeclaredMethods().stream()
                .filter(m -> m.getName().equals(name))
                .filter(m -> !staticOnly || (staticOnly &&  m.isStatic()))
                .collect(Collectors.toList());
        // We want to avoid infinite recursion in case of Object having Object as ancestor
        if (!Object.class.getCanonicalName().equals(typeDeclaration.getQualifiedName())) {
            for (ResolvedReferenceType ancestor : typeDeclaration.getAncestors()) {
		// Avoid recursion on self
                if (typeDeclaration != ancestor.getTypeDeclaration()) {
                    SymbolReference<ResolvedMethodDeclaration> res = MethodResolutionLogic
                            .solveMethodInType(ancestor.getTypeDeclaration(), name, argumentsTypes, staticOnly, typeSolver);
                    // consider methods from superclasses and only default methods from interfaces :
                    // not true, we should keep abstract as a valid candidate
                    // abstract are removed in MethodResolutionLogic.isApplicable is necessary
                    if (res.isSolved()) {
                        candidateMethods.add(res.getCorrespondingDeclaration());
                    }
		}
            }
        }
        // We want to avoid infinite recursion when a class is using its own method
        // see issue #75
        if (candidateMethods.isEmpty()) {
            SymbolReference<ResolvedMethodDeclaration> parentSolution = context.getParent().solveMethod(name, argumentsTypes, staticOnly, typeSolver);
            if (parentSolution.isSolved()) {
                candidateMethods.add(parentSolution.getCorrespondingDeclaration());
            }
        }

        // if is interface and candidate method list is empty, we should check the Object Methods
        if (candidateMethods.isEmpty() && typeDeclaration.isInterface()) {
            SymbolReference<ResolvedMethodDeclaration> res = MethodResolutionLogic.solveMethodInType(new ReflectionClassDeclaration(Object.class, typeSolver), name, argumentsTypes, false, typeSolver);
            if (res.isSolved()) {
                candidateMethods.add(res.getCorrespondingDeclaration());
            }
        }

        return MethodResolutionLogic.findMostApplicable(candidateMethods, name, argumentsTypes, typeSolver);
    }

    public SymbolReference<ResolvedConstructorDeclaration> solveConstructor(List<ResolvedType> argumentsTypes, TypeSolver typeSolver) {
        if (typeDeclaration instanceof ResolvedClassDeclaration) {
            return ConstructorResolutionLogic.findMostApplicable(((ResolvedClassDeclaration) typeDeclaration).getConstructors(), argumentsTypes, typeSolver);
        }
        return SymbolReference.unsolved(ResolvedConstructorDeclaration.class);
    }
}
