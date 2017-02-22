package com.parthdave.codegenerator.facebooklogin;

/**
 * Created by parth on 8/1/17.
 */

import com.parthdave.codegenerator.commons.AbortProcessingException;
import com.parthdave.codegenerator.commons.ErrorReporter;
import com.parthdave.codegenerator.commons.MethodsAndFields;
import com.parthdave.codegenerator.models.MethodModel;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class FacebookLoginCodeTemplateGenerator extends MethodsAndFields {

    ErrorReporter mErrorReporter;
    private String mainClassName, buttonName;

    public FacebookLoginCodeTemplateGenerator(String mainClassName, String buttonName) {
        this.mainClassName = mainClassName;
        this.buttonName = buttonName;
    }

    @Override
    public List<FieldSpec> getFields() {
        List<FieldSpec> fieldSpecsList = new ArrayList<>();
        fieldSpecsList.add(fieldSpecBuilder("com.facebook.CallbackManager", "callbackManager", PRIVATE));
        fieldSpecsList.add(fieldSpecBuilder("com.facebook.AccessTokenTracker", "accessTokenTracker", PRIVATE));
        fieldSpecsList.add(fieldSpecBuilder("com.facebook.ProfileTracker", "profileTracker", PRIVATE));
        fieldSpecsList.add(fieldSpecBuilder("com.facebook.AccessToken", "objAccessToken", PRIVATE));
        fieldSpecsList.add(fieldSpecBuilder("java.lang.String", "mUserId", PRIVATE));
        fieldSpecsList.add(fieldSpecBuilder("java.lang.String", "mAccessToken", PRIVATE));

        return fieldSpecsList;
    }

    @Override
    public MethodSpec onCreate(List<ParameterSpec> params, Modifier modifier) {
        return MethodSpecBuilder(params, "onCreate", modifier, getInitFacebookOnCreateCode(), superMethodBuilder(params, "onCreate"));
    }


    @Override
    public MethodSpec onCreate(List<ExecutableElement> executableElements) throws AbortProcessingException {
        MethodModel methodModel = findMethods(executableElements, "onCreate");

        if (methodModel != null)
            return MethodSpecBuilder(methodModel.getParams(), "onCreate", methodModel.getModifier(), getInitFacebookOnCreateCode(), superMethodBuilder(methodModel.getParams(), "onCreate"));
        return null;
    }


    @Override
    public MethodSpec onDestroy(List<ExecutableElement> executableElements) {
        MethodModel methodModel = findMethods(executableElements, "onDestroy");

        if (methodModel != null)
            return MethodSpecBuilder(methodModel.getParams(), "onDestroy", methodModel.getModifier(), getOnDestroy(), superMethodBuilder(methodModel.getParams(), "onDestroy"));
        else {
            return MethodSpecBuilder(methodOnDestroyBuilder(), "onDestroy", PROTECTED, getOnDestroy(), superMethodBuilder(methodOnDestroyBuilder(), "onDestroy"));
        }
    }

    @Override
    public MethodSpec onResume(List<ParameterSpec> params, Modifier modifier) {
        return null;
    }

    @Override
    public MethodSpec onPause(List<ParameterSpec> params, Modifier modifier) {
        return null;
    }

    @Override
    public MethodSpec onStart(List<ParameterSpec> params, Modifier modifier) {
        return null;
    }

    @Override
    public MethodSpec onActivityResult(List<ExecutableElement> executableElements) {
        MethodModel methodModel = findMethods(executableElements, "onActivityResult");

        if (methodModel != null)
            return MethodSpecBuilder(methodModel.getParams(), "onActivityResult", methodModel.getModifier(), getInitFacebookOnActivityResult(), superMethodBuilder(methodModel.getParams(), "onActivityResult"));
        else {
            return MethodSpecBuilder(methodOnActivityResultParamBuilder(), "onActivityResult", PROTECTED, getInitFacebookOnActivityResult(), superMethodBuilder(methodOnActivityResultParamBuilder(), "onActivityResult"));
        }
    }

    @Override
    public List<MethodSpec> extraMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        methodSpecs.add(MethodSpecBuilder(null, "initLogin", PUBLIC, getInitFacebookLogin(), null));
        methodSpecs.add(MethodSpecBuilder(null, "getAccessToken", PRIVATE, getAccessToken(), null));
        methodSpecs.add(MethodSpecBuilder(null, "getProfileDataUsingGraph", PRIVATE, getProfileDataUsingGraph(), null));
        System.out.println(methodSpecs);
        System.out.println("extraMethods:" + methodSpecs.size());
        return methodSpecs;
    }

    private String getOnDestroy() {
        return " if (accessTokenTracker != null)\n" +
                "            accessTokenTracker.stopTracking();\n" +
                "\n" +
                "        if (profileTracker != null)\n" +
                "            profileTracker.stopTracking();";
    }

    private String getInitFacebookOnActivityResult() {
        return "callbackManager.onActivityResult(requestCode, resultCode, data);";
    }

    private String getInitFacebookOnCreateCode() {
        return "//Facebook Initialize\n" + "\ncallbackManager = CallbackManager.Factory.create();" +
                "\ngetAccessToken();" +
                buttonName + ".setOnClickListener(new android.view.View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(android.view.View view) {\n" +
                "            com.facebook.login.LoginManager.getInstance().logOut();"+
                " android.util.Log.i(\""+mainClassName+"\", \""+buttonName+"Clicked\");"+
                "        \n initLogin();" +
                "            }\n" +
                "        })";
    }

    private String getProfileDataUsingGraph() {
        return " mAccessToken = objAccessToken.getToken();\n" +
                "        mUserId = objAccessToken.getUserId();\n" +
                "\n" +
                "        /*User Graph API to get user details */\n" +
                "        com.facebook.GraphRequest request = com.facebook.GraphRequest.newMeRequest(objAccessToken, new com.facebook.GraphRequest.GraphJSONObjectCallback() {\n" +
                "\n" +
                "            @Override\n" +
                "            public void onCompleted(org.json.JSONObject object, com.facebook.GraphResponse response) {\n" +
                "                android.util.Log.i(\"GplusLoginActivity\", response.toString());\n" +
                "                // Get facebook data from login\n" +
                "                parseFacebookData(object);\n" +
                "            }\n" +
                "        });\n" +
                "        Bundle parameters = new Bundle();\n" +
                "        parameters.putString(\"fields\", \"id, first_name, last_name, email,gender, birthday, location\");\n" +
                "        request.setParameters(parameters);\n" +
                "        request.executeAsync();";
    }


    private String getInitFacebookLogin() {
        return " " +
                "        if (objAccessToken == null) {\n" +
                "            /*Handle login callback */\n" +
                "            android.util.Log.i(\"facebook\", \"callbackManager\" + callbackManager);\n" +
                "            com.facebook.login.LoginManager.getInstance().logInWithReadPermissions(" + mainClassName + ".this, java.util.Arrays.asList(\"public_profile\", \"email\"));\n" +
                "            android.util.Log.i(\"facebook\", \"callbackManager logInWithReadPermissions\" + callbackManager);\n" +
                "\n" +
                "            com.facebook.login.LoginManager.getInstance().registerCallback(callbackManager,\n" +
                "                    new com.facebook.FacebookCallback<com.facebook.login.LoginResult>() {\n" +
                "                        @Override\n" +
                "                        public void onSuccess(com.facebook.login.LoginResult loginResult) {\n" +
                "                            android.util.Log.i(\"getAccessToken\", \"initLogin  CALL API \");\n" +
                "                            objAccessToken = loginResult.getAccessToken();\n" +
                "                        }\n" +
                "\n" +
                "                        @Override\n" +
                "                        public void onCancel() {\n" +
                "                            android.util.Log.i(\"initLogin\", \"onCancel\");\n" +
                "\n" +
                "                            updateUI();\n" +
                "                        }\n" +
                "\n" +
                "                        @Override\n" +
                "                        public void onError(com.facebook.FacebookException error) {\n" +
                "                            android.util.Log.i(\"initLogin\", \"onError\" + error.getMessage());\n" +
                "                            updateUI();\n" +
                "                        }\n" +
                "                    });\n" +
                "        }\n" +
                " ";
    }


    public static FacebookLoginCodeTemplateGenerator getInstance(String className, String buttonName) {
        return new FacebookLoginCodeTemplateGenerator(className, buttonName);
    }

    public String getAccessToken() {
        return " /*check already token available */\n" +
                "        objAccessToken = com.facebook.AccessToken.getCurrentAccessToken();\n" +
                "\n" +
                "        /*Token not available , get it first */\n" +
                "        accessTokenTracker = new AccessTokenTracker() {\n" +
                "            @Override\n" +
                "            protected void onCurrentAccessTokenChanged(com.facebook.AccessToken oldAccessToken, com.facebook.AccessToken currentAccessToken) {\n" +
                "                // Set the access token using\n" +
                "                // currentAccessToken when it's loaded or set.\n" +
                "                objAccessToken = currentAccessToken;\n" +
                "                if (objAccessToken != null) {\n" +
                "                    android.util.Log.i(\"getAccessToken\", \"onCurrentAccessTokenChanged  CALL API \");\n" +
                "                    mUserId = objAccessToken.getUserId();\n" +
                "                    //objUtils.getSessionManager().setBooleanDetail(Constant.IS_LOGIN_FB, true);\n" +
                "                    getProfileDataUsingGraph();\n" +
                "                }\n" +
                "            }\n" +
                "        };\n" +
                "\n" +
                "        if (objAccessToken != null) {\n" +
                "            android.util.Log.i(\"objAccessToken\", \"exist call api \");\n" +
                "            getProfileDataUsingGraph();\n" +
                "        }";
    }
}
