package com.parthdave.codegenerator.facebooklogin;

/**
 * Created by parth on 8/1/17.
 */

import com.parthdave.codegenerator.commons.AbortProcessingException;
import com.parthdave.codegenerator.commons.MethodsAndFields;
import com.parthdave.codegenerator.models.MethodModel;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public final class FacebookInitCodeTemplateGenerator extends MethodsAndFields {
    public boolean isDebug = false;

    public FacebookInitCodeTemplateGenerator(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public List<FieldSpec> getFields() throws AbortProcessingException {
        return null;
    }

    @Override
    public MethodSpec onCreate(List<ParameterSpec> params, Modifier modifier) throws AbortProcessingException {
        return MethodSpecBuilder(params, "onCreate", modifier, " //Facebook Initialize\n" +
                "        com.facebook.FacebookSdk.sdkInitialize(this);\n" +
                "        com.facebook.FacebookSdk.setIsDebugEnabled(" + (isDebug ? true : false) + ")", superMethodBuilder(params, "onCreate"));
    }

    @Override
    public MethodSpec onCreate(List<ExecutableElement> executableElements) throws AbortProcessingException {
        MethodModel methodModel = findMethods(executableElements, "onCreate");
        return MethodSpecBuilder(methodModel.getParams(), "onCreate", methodModel.getModifier(), " //Facebook Initialize\n" +
                "        com.facebook.FacebookSdk.sdkInitialize(this);\n" +
                "        com.facebook.FacebookSdk.setIsDebugEnabled(" + (isDebug ? true : false) + ")", superMethodBuilder(methodModel.getParams(), "onCreate"));
    }

    @Override
    public MethodSpec onDestroy(List<ExecutableElement> executableElements) throws AbortProcessingException {
        return null;
    }

    @Override
    public MethodSpec onResume(List<ParameterSpec> params, Modifier modifier) throws AbortProcessingException {
        return null;
    }

    @Override
    public MethodSpec onPause(List<ParameterSpec> params, Modifier modifier) throws AbortProcessingException {
        return null;
    }

    @Override
    public MethodSpec onStart(List<ParameterSpec> params, Modifier modifier) throws AbortProcessingException {
        return null;
    }

    @Override
    public MethodSpec onActivityResult(List<ExecutableElement> executableElements) {
        return null;
    }

    @Override
    public List<MethodSpec> extraMethods() {
        return null;
    }

    public static FacebookInitCodeTemplateGenerator getInstance(boolean b) {
        return new FacebookInitCodeTemplateGenerator(b);
    }
}
