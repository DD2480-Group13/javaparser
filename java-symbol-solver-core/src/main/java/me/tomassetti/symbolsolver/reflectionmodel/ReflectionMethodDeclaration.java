package me.tomassetti.symbolsolver.reflectionmodel;

import com.github.javaparser.ast.Node;
import me.tomassetti.symbolsolver.model.declarations.MethodDeclaration;
import me.tomassetti.symbolsolver.model.declarations.ParameterDeclaration;
import me.tomassetti.symbolsolver.model.declarations.TypeDeclaration;
import me.tomassetti.symbolsolver.model.invokations.MethodUsage;
import me.tomassetti.symbolsolver.model.resolution.Context;
import me.tomassetti.symbolsolver.model.resolution.TypeParameter;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.model.typesystem.ReferenceTypeUsage;
import me.tomassetti.symbolsolver.model.typesystem.TypeUsage;
import me.tomassetti.symbolsolver.model.typesystem.WildcardUsage;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectionMethodDeclaration implements MethodDeclaration {

    // TODO
    // This class contains a huge portion of code just copied from JavaParserMethodDeclaration

    private Method method;
    private TypeSolver typeSolver;

    public ReflectionMethodDeclaration(Method method, TypeSolver typeSolver) {
        this.method = method;
        if (method.isSynthetic() || method.isBridge()) {
            throw new IllegalArgumentException();
        }
        this.typeSolver = typeSolver;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public String toString() {
        return "ReflectionMethodDeclaration{" +
                "method=" + method +
                '}';
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public TypeDeclaration declaringType() {
        if (method.getDeclaringClass().isInterface()) {
            return new ReflectionInterfaceDeclaration(method.getDeclaringClass(), typeSolver);
        } else {
            return new ReflectionClassDeclaration(method.getDeclaringClass(), typeSolver);
        }
    }

    @Override
    public TypeUsage getReturnType() {
        return ReflectionFactory.typeUsageFor(method.getGenericReturnType(), typeSolver);
    }

    @Override
    public int getNoParams() {
        return method.getParameterTypes().length;
    }

    @Override
    public ParameterDeclaration getParam(int i) {
        boolean variadic = false;
        if (method.isVarArgs()) {
            variadic = i == (method.getParameterCount() - 1);
        }
        return new ReflectionParameterDeclaration(method.getParameterTypes()[i], method.getGenericParameterTypes()[i], typeSolver, variadic);
    }

    public MethodUsage getUsage(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TypeParameter> getTypeParameters() {
        return Arrays.stream(method.getTypeParameters()).map((refTp) -> new ReflectionTypeParameter(refTp, false)).collect(Collectors.toList());
    }

    @Override
    public MethodUsage resolveTypeVariables(Context context, List<TypeUsage> parameterTypes) {
        //return new MethodUsage(new ReflectionMethodDeclaration(method, typeSolver), typeSolver);
        TypeUsage returnType = replaceTypeParams(new ReflectionMethodDeclaration(method, typeSolver).getReturnType(), typeSolver, context);
        List<TypeUsage> params = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            TypeUsage replaced = replaceTypeParams(new ReflectionMethodDeclaration(method, typeSolver).getParam(i).getType(), typeSolver, context);
            params.add(replaced);
        }

        // We now look at the type parameter for the method which we can derive from the parameter types
        // and then we replace them in the return type
        Map<String, TypeUsage> determinedTypeParameters = new HashMap<>();
        for (int i = 0; i < getNoParams(); i++) {
            TypeUsage formalParamType = getParam(i).getType();
            TypeUsage actualParamType = parameterTypes.get(i);
            determineTypeParameters(determinedTypeParameters, formalParamType, actualParamType, typeSolver);
        }

        for (String determinedParam : determinedTypeParameters.keySet()) {
            returnType = returnType.replaceParam(determinedParam, determinedTypeParameters.get(determinedParam));
        }

        return new MethodUsage(new ReflectionMethodDeclaration(method, typeSolver), params, returnType);
    }

    private void determineTypeParameters(Map<String, TypeUsage> determinedTypeParameters, TypeUsage formalParamType, TypeUsage actualParamType, TypeSolver typeSolver) {
        if (actualParamType.isNull()) {
            return;
        }
        if (actualParamType.isTypeVariable()) {
            return;
        }
        if (formalParamType.isTypeVariable()) {
            determinedTypeParameters.put(formalParamType.describe(), actualParamType);
            return;
        }
        if (formalParamType instanceof WildcardUsage) {
            return;
        }
        if (formalParamType.isArray() && actualParamType.isArray()) {
            determineTypeParameters(
                    determinedTypeParameters,
                    formalParamType.asArrayTypeUsage().getComponentType(),
                    actualParamType.asArrayTypeUsage().getComponentType(),
                    typeSolver);
            return;
        }
        if (formalParamType.isReferenceType() && actualParamType.isReferenceType()
                && !formalParamType.asReferenceTypeUsage().getQualifiedName().equals(actualParamType.asReferenceTypeUsage().getQualifiedName())) {
            List<ReferenceTypeUsage> ancestors = actualParamType.asReferenceTypeUsage().getAllAncestors();
            final String formalParamTypeQName = formalParamType.asReferenceTypeUsage().getQualifiedName();
            List<TypeUsage> correspondingFormalType = ancestors.stream().filter((a) -> a.getQualifiedName().equals(formalParamTypeQName)).collect(Collectors.toList());
            if (correspondingFormalType.isEmpty()) {
                throw new IllegalArgumentException();
            }
            actualParamType = correspondingFormalType.get(0);
        }
        if (formalParamType.isReferenceType() && actualParamType.isReferenceType()) {
            if (formalParamType.asReferenceTypeUsage().isRawType() || actualParamType.asReferenceTypeUsage().isRawType()) {
                return;
            }
            List<TypeUsage> formalTypeParams = formalParamType.asReferenceTypeUsage().parameters();
            List<TypeUsage> actualTypeParams = actualParamType.asReferenceTypeUsage().parameters();
            if (formalTypeParams.size() != actualTypeParams.size()) {
                throw new UnsupportedOperationException();
            }
            for (int i = 0; i < formalTypeParams.size(); i++) {
                determineTypeParameters(determinedTypeParameters, formalTypeParams.get(i), actualTypeParams.get(i), typeSolver);
            }
        }
    }

    private Optional<TypeUsage> typeParamByName(String name, TypeSolver typeSolver, Context context) {
        int i = 0;
        if (this.getTypeParameters() != null) {
            for (TypeParameter tp : this.getTypeParameters()) {
                if (tp.getName().equals(name)) {
                    TypeUsage typeUsage = this.getParam(i).getType();
                    return Optional.of(typeUsage);
                }
                i++;
            }
        }
        return Optional.empty();
    }

    private TypeUsage replaceTypeParams(TypeUsage typeUsage, TypeSolver typeSolver, Context context) {
        if (typeUsage.isTypeVariable()) {
            TypeParameter typeParameter = typeUsage.asTypeParameter();
            if (typeParameter.declaredOnClass()) {
                Optional<TypeUsage> typeParam = typeParamByName(typeParameter.getName(), typeSolver, context);
                if (typeParam.isPresent()) {
                    typeUsage = typeParam.get();
                }
            }
        }

        if (typeUsage.isReferenceType()) {
            for (int i = 0; i < typeUsage.asReferenceTypeUsage().parameters().size(); i++) {
                TypeUsage replaced = replaceTypeParams(typeUsage.asReferenceTypeUsage().parameters().get(i), typeSolver, context);
                // Identity comparison on purpose
                if (replaced != typeUsage.asReferenceTypeUsage().parameters().get(i)) {
                    typeUsage = typeUsage.asReferenceTypeUsage().replaceParam(i, replaced);
                }
            }
        }

        return typeUsage;
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(method.getModifiers());
    }

    @Override
    public boolean isPackageProtected() {
        return !Modifier.isPrivate(method.getModifiers()) && !Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers());
    }

    @Override
    public boolean isDefaultMethod() {
        return method.isDefault();
    }

}
