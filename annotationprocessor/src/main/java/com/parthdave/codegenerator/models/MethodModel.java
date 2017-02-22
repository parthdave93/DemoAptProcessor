package com.parthdave.codegenerator.models;

import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;

/**
 * Created by Parth Dave(parth.dave93@gmail.com)-014 on 23/1/17.
 */

public class MethodModel {
    private List<ParameterSpec> params;
    private Set<Modifier> modifier;
    private String methodName;

    public List<ParameterSpec> getParams() {
        return params;
    }

    public void setParams(List<ParameterSpec> params) {
        this.params = params;
    }

    public Set<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(Set<javax.lang.model.element.Modifier> modifier) {
        this.modifier = modifier;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
