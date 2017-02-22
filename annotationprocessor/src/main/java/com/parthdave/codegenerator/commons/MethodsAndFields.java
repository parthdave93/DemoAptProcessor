package com.parthdave.codegenerator.commons;

import android.content.Intent;
import android.util.Log;

import com.google.common.collect.Lists;
import com.parthdave.codegenerator.models.MethodModel;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;

/**
 * Created by Parth Dave(parth.dave93@gmail.com)-014 on 16/1/17.
 */

public abstract class MethodsAndFields {
    public abstract List<FieldSpec> getFields();

    public abstract MethodSpec onCreate(List<ParameterSpec> params, Modifier modifier);

    public abstract MethodSpec onCreate(List<ExecutableElement> methodModel) throws AbortProcessingException;

    public abstract MethodSpec onDestroy(List<ExecutableElement> executableElements);

    public abstract MethodSpec onResume(List<ParameterSpec> params, Modifier modifier);

    public abstract MethodSpec onPause(List<ParameterSpec> params, Modifier modifier);

    public abstract MethodSpec onStart(List<ParameterSpec> params, Modifier modifier);

    public abstract MethodSpec onActivityResult(List<ExecutableElement> executableElements);

    public abstract List<MethodSpec> extraMethods();


    protected MethodSpec MethodSpecBuilder(List<ParameterSpec> params, String methodName, Set<Modifier> modifier, String statements, String superStatement) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName).addModifiers(modifier).addParameters(params);//.addParameters(params);

        if (superStatement != null && !superStatement.isEmpty())
            builder.addStatement(superStatement);
        builder.addStatement(statements);

        return builder.build();
    }

    protected MethodSpec MethodSpecBuilder(List<ParameterSpec> params, String methodName, Modifier modifier, String statements, String superStatement) {
        System.out.println("MethodSpecBuilder() called with: params = [" + params + "], methodName = [" + methodName + "], modifier = [" + modifier + "], statements = [" + statements + "], superStatement = [" + superStatement + "]");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);//.addParameters(params);

        if(modifier!=null)
            builder.addModifiers(modifier);
        if (params != null)
            builder.addParameters(params);
        if (superStatement != null && !superStatement.isEmpty())
            builder.addStatement(superStatement);
        builder.addStatement(statements);
        return builder.build();
    }

    protected String superMethodBuilder(List<ParameterSpec> params, String methodName) {
        System.out.println("superMethodBuilder() called with: params = [" + params + "], methodName = [" + methodName + "]");
        String superString = "super." + methodName + "(";
        if (params != null && !params.isEmpty()) {
            for (int index = 0; index < params.size(); index++) {
                superString += params.get(index).name + ",";
            }
            superString = superString.substring(0, superString.length() - 1);
        }
        superString += ")";
        return superString;
    }

    protected FieldSpec fieldSpecBuilder(String className, String variableName, Modifier modifier) {
        FieldSpec.Builder builder = FieldSpec.builder(ClassName.get(className.substring(0, className.lastIndexOf(".")), className.substring(className.lastIndexOf(".") + 1)), variableName, modifier);

        return builder.build();
    }


    protected ParameterSpec paramSpecBuilder(String className, String variableName, Modifier modifier) {
        ParameterSpec.Builder builder = ParameterSpec.builder(ClassName.get(className.substring(0, className.lastIndexOf(".")), className.substring(className.lastIndexOf(".") + 1)), variableName, modifier);
        return builder.build();
    }

    protected ParameterSpec methodParamSpecBuilder(TypeName className, String variableName) {
        ParameterSpec.Builder builder = ParameterSpec.builder(className, variableName);
        return builder.build();
    }

    protected ParameterSpec methodParamSpecBuilder(String className, String variableName) {
        if (className.contains("."))
            return ParameterSpec.builder(ClassName.get(className.substring(0, className.lastIndexOf(".")), className.substring(className.lastIndexOf(".") + 1)), variableName).build();
        else
            return ParameterSpec.builder(getUserDefinedTypes(className), variableName).build();
    }

    private TypeName getUserDefinedTypes(String className) {
        switch (className) {
            case "int":
                return TypeName.INT;
            case "double":
                return TypeName.DOUBLE;
            case "char":
                return TypeName.CHAR;
            case "boolean":
                return TypeName.BOOLEAN;
            case "byte":
                return TypeName.BYTE;
            case "float":
                return TypeName.FLOAT;
            case "long":
                return TypeName.LONG;
            case "Object":
                return TypeName.OBJECT;
            case "short":
                return TypeName.SHORT;
            case "void":
                return TypeName.VOID;
        }
        return null;
    }

    protected List<ParameterSpec> methodOnActivityResultParamBuilder() {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        parameterSpecs.add(methodParamSpecBuilder(TypeName.INT, "requestCode"));
        parameterSpecs.add(methodParamSpecBuilder(TypeName.INT, "resultCode"));
        parameterSpecs.add(methodParamSpecBuilder("android.content.Intent", "data"));
        return parameterSpecs;
    }

    protected List<ParameterSpec> methodOnDestroyBuilder() {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        return parameterSpecs;
    }


    protected MethodModel findMethods(List<ExecutableElement> executableElements, String onCreate) {
        MethodModel methodModel = null;
        for (int index = 0; index < executableElements.size(); index++) {
            if (executableElements.get(index).getSimpleName().toString().equals(onCreate)) {
                ExecutableElement methodElement = executableElements.get(index);

                List<ParameterSpec> params = null;
                if (methodElement.getParameters() != null) {
                    params = Lists.newArrayListWithCapacity(methodElement.getParameters().size());
                    System.out.println("params:" + methodElement.getParameters());
                    for (int paramIndex = 0; paramIndex < methodElement.getParameters().size(); paramIndex++) {
                        List<? extends VariableElement> variableElement = methodElement.getParameters();
                        System.out.println("asType:" + variableElement.get(paramIndex).asType());
                        params.add(methodParamSpecBuilder(variableElement.get(paramIndex).asType().toString(), variableElement.get(paramIndex).getSimpleName().toString()));//getModifier(variableElement.get(paramIndex).getModifiers())
                    }
                }

                methodModel = new MethodModel();
                methodModel.setParams(params);
                methodModel.setModifier(methodElement.getModifiers());
                methodModel.setMethodName(onCreate);

                return methodModel;

            }
        }
        return null;
    }

    private Modifier getModifier(Set<Modifier> modifiers) {
        if (modifiers.size() == 0)
            return PROTECTED;
        else if (modifiers.size() > 0) {
            return (Modifier) modifiers.toArray()[0];
        }
        return PROTECTED;
    }
}
