package com.parthdave.codegenerator.facebooklogin;

/**
 * Created by parth on 8/1/17.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.parthdave.annotations.FacebookLogin;
import com.parthdave.annotations.FacebookLoginBtn;
import com.parthdave.codegenerator.commons.ErrorReporter;
import com.parthdave.codegenerator.commons.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;


import static javax.lang.model.SourceVersion.RELEASE_7;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

@SupportedAnnotationTypes("com.parthdave.annotations.FacebookLogin")
@SupportedSourceVersion(RELEASE_7)
public final class FacebookLoginCodeGen extends AbstractProcessor {

    public final String TAG = "FacebookLoginCodeGen";
    ErrorReporter mErrorReporter;
    Types mTypeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println(TAG + "init");
        mErrorReporter = new ErrorReporter(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> facebookLoginAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(FacebookLogin.class);
        handleElementsCodeGeneration(facebookLoginAnnotatedElements);
        return true;
    }

    public void handleElementsCodeGeneration(Collection<? extends Element> elements) {
        List<TypeElement> types = new ImmutableList.Builder<TypeElement>().addAll(ElementFilter.typesIn(elements)).build();

        for (TypeElement type : types) {
            System.out.println("Kind:"+type.getKind());
            if (type.getEnclosedElements() != null)
                processType(type, ElementFilter.methodsIn(type.getEnclosedElements()), ElementFilter.fieldsIn(type.getEnclosedElements()));
            else
                processType(type, null, ElementFilter.fieldsIn(type.getEnclosedElements()));
        }
    }


    private void processType(TypeElement type, List<ExecutableElement> executableElements, List<VariableElement> variableElements) {
        String className = generatedSubclassName(type, 0);
        String source = generateClass(type, className, generatedSubClassName(type), executableElements,variableElements, true);
        writeSourceFile(className, source, type);
    }


    private void writeSourceFile(String className, String text, TypeElement originatingType) {
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(className, originatingType);
            Writer writer = sourceFile.openWriter();
            try {
                writer.write(text);
            } finally {
                writer.close();
            }
        } catch (IOException e) {// silent}
            e.printStackTrace();
        }
    }

    private String generatedSubclassName(TypeElement type, int depth) {
        return generatedClassName(type, Strings.repeat("$", depth) + "_");
    }

    private String generatedSubClassName(TypeElement type) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = TypeUtil.packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return name;
    }

    private String generatedClassName(TypeElement type, String postFix) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = TypeUtil.packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return name + postFix;
    }


    private String generateClass(TypeElement type, String className, String classToExtend, List<ExecutableElement> executableElements,List<VariableElement> variableElements, boolean isFinal) {
        if (type == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null type", type);
        }
        if (className == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null class name", type);
        }
        if (classToExtend == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null parent class", type);
        }

        List<VariableElement> nonPrivateFields = getParcelableFieldsOrError(type);
        //        if (nonPrivateFields.isEmpty()) {
        //            mErrorReporter.abortWithError("generateClass error, all fields are declared PRIVATE", type);
        //        }


        // get the properties
        ImmutableList<Property> properties = buildProperties(nonPrivateFields);


        for (int index = 0; index < properties.size(); index++) {
            System.out.println("properties: " + properties.get(index).element);
        }

        // get the type adapters
        //        ImmutableMap<TypeMirror, FieldSpec> typeAdapters = getTypeAdapters(properties);

        // get the parcel version
        //noinspection ConstantConditions
        int version = 1;//type.getAnnotation(AutoParcel.class);

        VariableElement loginButtonView = null;
        for (int index = 0; index < variableElements.size(); index++) {
            for (int annotationsIndex = 0; annotationsIndex < variableElements.get(index).getAnnotationMirrors().size(); annotationsIndex++) {
                if(variableElements.get(index).getAnnotationMirrors().get(annotationsIndex).getAnnotationType().toString().equals("com.parthdave.annotations.FacebookLoginBtn")){
                    loginButtonView = variableElements.get(index);
                }
            }
        }


        // Generate the AutoParcel_??? class
        String pkg = TypeUtil.packageNameOf(type);
        TypeName classTypeName = ClassName.get(pkg, className);
        TypeName classToExtendTypeName = ClassName.get(pkg, classToExtend);

        FacebookLoginCodeTemplateGenerator facebookLoginCodeTemplateGenerator = FacebookLoginCodeTemplateGenerator.getInstance(className,loginButtonView.getSimpleName().toString());

        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                // Add the version
                .addModifiers(FINAL)
                .addModifiers(PUBLIC)
                .addFields(facebookLoginCodeTemplateGenerator.getFields())
                .superclass(classToExtendTypeName).addField(TypeName.INT, "version", PRIVATE)
                .addMethod(facebookLoginCodeTemplateGenerator.onCreate(executableElements))
                .addMethod(facebookLoginCodeTemplateGenerator.onActivityResult(executableElements))
                .addMethod(facebookLoginCodeTemplateGenerator.onDestroy(executableElements))
                .addMethods(facebookLoginCodeTemplateGenerator.extraMethods());

        // extends from original abstract class
        //                .superclass(ClassName.get(pkg, classToExtend));
        // Add the DEFAULT constructor
        //                .addMethod(generateConstructor(properties));
        // Add the private constructor
        //                .addMethod(generateConstructorFromParcel(processingEnv, properties, typeAdapters))
        //                 overrides describeContents()
        //                .addMethod(generateDescribeContents())
        //                 static final CREATOR
        //                .addField(generateCreator(processingEnv, properties, classTypeName, typeAdapters))
        //                 overrides writeToParcel()
        //                .addMethod(generateWriteToParcel(version, processingEnv, properties, typeAdapters)); // generate writeToParcel()

       /* if (!ancestoIsParcelable(processingEnv, type)) {
            // Implement android.os.Parcelable if the ancestor does not do it.
            subClass.addSuperinterface(ClassName.get("android.os", "Parcelable"));
        }*/

       /* if (!typeAdapters.isEmpty()) {
            typeAdapters.values().forEach(subClass::addField);
        }*/

        System.out.println("pkg:" + pkg);
        System.out.println("subClass.build():" + subClass.build());
        JavaFile javaFile = JavaFile.builder(pkg, subClass.build()).addFileComment("Generated By : Parth Dave").build();
        return javaFile.toString();
    }




    private Element getListOfFields(TypeElement properties, String methodType) {

        /*ImmutableList.Builder<Property> property = new ImmutableList.Builder<Property>();

        for (int index = 0; index < properties.getEnclosedElements().size(); index++) {
            Element element = properties.getEnclosedElements().get(index);
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            //get method name and modifier
            System.out.println("getSimpleName Name:" + element.getSimpleName());
            System.out.println("getModifiers Name:" + element.getModifiers());
            //            System.out.println("getKind Name:" + element.getKind());
            //            System.out.println("getEnclosedElements Name:" + element.getEnclosedElements());
            System.out.println("getEnclosingElement Name:" + element.getEnclosingElement().getEnclosedElements());

            List<? extends Element> classElements = element.getEnclosingElement().getEnclosedElements();

            for (int classElementsIndex = 0; classElementsIndex < classElements.size(); classElementsIndex++) {
                Element classInnerElements = classElements.get(classElementsIndex);
                System.out.println("getSimpleName Name:" + classInnerElements.getSimpleName());
                System.out.println("getModifiers Name:" + classInnerElements.getModifiers());

                System.out.println("classInnerElements getEnclosedElements:" + classInnerElements.getEnclosedElements());
                System.out.println("classInnerElements getAnnotationMirrors:" + classInnerElements.getAnnotationMirrors());
                System.out.println("classInnerElements getModifiers:" + classInnerElements.getModifiers());

                System.out.println("classInnerElements asType:" + classInnerElements.asType());
                System.out.println("classInnerElements getEnclosingElement:" + classInnerElements.getEnclosingElement());
                TypeMirror typeMirror = classInnerElements.asType();

                Element methodElement = mTypeUtils.asElement(typeMirror);
                if(methodElement!=null) {
                    System.out.println("mTypeUtils.asElement( Name:" + methodElement.getSimpleName());
                    System.out.println("mTypeUtils.asElement( Name:" + methodElement.getModifiers());
                }

                //                System.out.println("getKind Name:" + classInnerElements.getKind());
                //                System.out.println("getEnclosedElements Name:" + classInnerElements.getEnclosedElements());
            }

            System.out.println("getDeclaredMethod properties.getEnclosedElements().get(index).getClass().getDeclaredMethods().length:" + element.getAnnotationMirrors());

            //get annotations over method
            for (int annotationMirrors = 0; annotationMirrors < element.getAnnotationMirrors().size(); annotationMirrors++) {
                AnnotationMirror annotationMirror = element.getAnnotationMirrors().get(annotationMirrors);
                System.out.println("getSimpleName Name:" + annotationMirror.getAnnotationType());
                System.out.println("getModifiers Name:" + annotationMirror.getElementValues());
            }


           *//* for (int declaredMethodsIndex = 0; declaredMethodsIndex < element.getClass().getDeclaredMethods().length; declaredMethodsIndex++) {
                Method declaredMethod = element.getClass().getDeclaredMethods()[declaredMethodsIndex];
                System.out.println("getDeclaredMethods Name:" + declaredMethod.getName());
            }*//*

            for (int innerEnclosedElements = 0; innerEnclosedElements < element.getEnclosedElements().size(); innerEnclosedElements++) {
                System.out.println("getSimpleName Name:" + element.getEnclosedElements().get(innerEnclosedElements).getSimpleName());
                System.out.println("getModifiers Name:" + element.getEnclosedElements().get(innerEnclosedElements).getModifiers());
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            *//*for (int methodIndex = 0; methodIndex < properties.getEnclosedElements().get(index).getClass().getDeclaredMethods().length; methodIndex++) {
                Method method = properties.getEnclosedElements().get(index).getClass().getDeclaredMethods()[methodIndex];
                System.out.println("Method Name:" + method.getName());
                System.out.println("Method getModifiers Name:" + (method.getModifiers() == 0 ? "public" : method.getModifiers() == 2 ? "private" : method.getModifiers() == 1 ? "protected" : "default"));
                System.out.println("Method getReturnType Name:" + method.getReturnType());
                for (int parameterIndex = 0; parameterIndex < method.getTypeParameters().length; parameterIndex++) {
                    System.out.println("Method Name:" + method.getTypeParameters()[parameterIndex].getName());
                }
                System.out.println("getTypeParameters:" + method.getTypeParameters());

                System.out.println("method.toString():" + method.toString());

                System.out.println("method.toString():" + method.getParameterCount());


                for (int typeParameterIndex = 0; typeParameterIndex < method.getTypeParameters().length; typeParameterIndex++) {
                    TypeVariable<Method> typeParameterMethod = method.getTypeParameters()[typeParameterIndex];
                    System.out.println("getTypeParameters getName:" + typeParameterMethod.getName());
                }

               *//**//* for (int annotationMirrorsIndex = 0; annotationMirrorsIndex < properties.getEnclosedElements().get(index).getAnnotationMirrors().size(); annotationMirrorsIndex++) {
                    System.out.println("getAnnotationMirrors Name:" + properties.getEnclosedElements().get(index).getAnnotationMirrors().get(annotationMirrorsIndex).getAnnotationType());
                    System.out.println("getAnnotationMirrors getElementValues:" + properties.getEnclosedElements().get(index).getAnnotationMirrors().get(annotationMirrorsIndex).getElementValues());
                }*//**//*
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            for (int innerElement = 0; innerElement < properties.getEnclosedElements().get(innerElement).getEnclosedElements().size(); innerElement++) {
                System.out.println("getSimpleName Name:" + properties.getEnclosedElements().get(innerElement).getEnclosedElements().get(index).getSimpleName());
                System.out.println("getModifiers Name:" + properties.getEnclosedElements().get(innerElement).getEnclosedElements().get(index).getModifiers());
                System.out.println("getKind Name:" + properties.getEnclosedElements().get(innerElement).getEnclosedElements().get(index).getKind());
                System.out.println("getEnclosedElements Name:" + properties.getEnclosedElements().get(innerElement).getEnclosedElements().get(index).getEnclosedElements());
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            if (properties.getEnclosedElements().get(index).getSimpleName().toString().equals(methodType)) {
                return properties.getEnclosedElements().get(index);
            }*//*
        }*/
        return null;
    }

    private ImmutableList<Property> buildProperties(List<VariableElement> elements) {
        ImmutableList.Builder<Property> builder = ImmutableList.builder();
        for (VariableElement element : elements) {
            builder.add(new Property(element.getSimpleName().toString(), element));
        }

        return builder.build();
    }


   /* private MethodSpec getOnCreateMethod(Element element, MethodsAndFields methodsAndFields) {
        System.out.println("getOnCreateMethod() called with: element = [" + element + "], methodsAndFields = [" + methodsAndFields + "]");
        List<ParameterSpec> params = Lists.newArrayListWithCapacity(1);
        params.add(ParameterSpec.builder(TypeName.VOID, String.valueOf(element.getSimpleName()), PUBLIC).build());

        MethodSpec.Builder builder = MethodSpec.methodBuilder(String.valueOf(element.getSimpleName())).addModifiers(PUBLIC);//.addParameters(params);

       *//* for (ParameterSpec param : params) {
            builder.addStatement("this.$N = $N", param.name, param.name);
        }*//*
        builder.addStatement("super.onCreate()");
        builder.addStatement(methodsAndFields.onCreate(params,PUBLIC));


        return builder.build();
    }*/


   /* private MethodSpec getOnCreateMethod(ImmutableList<Property> properties, MethodsAndFields methodsAndFields) {
        List<ParameterSpec> params = Lists.newArrayListWithCapacity(properties.size());
        for (Property property : properties) {
            params.add(ParameterSpec.builder(property.typeName, property.fieldName).build());
        }

        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addParameters(params);

        for (ParameterSpec param : params) {
            builder.addStatement("this.$N = $N", param.name, param.name);
        }

        builder.addStatement(methodsAndFields.onCreate());

        return builder.build();
    }*/

    private MethodSpec generateConstructor(ImmutableList<Property> properties) {

        List<ParameterSpec> params = Lists.newArrayListWithCapacity(properties.size());
        for (Property property : properties) {
            params.add(ParameterSpec.builder(property.typeName, property.fieldName).build());
        }

        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addParameters(params);

        for (ParameterSpec param : params) {
            builder.addStatement("this.$N = $N", param.name, param.name);
        }

        return builder.build();
    }

    /**
     * This method returns a list of all non private fields. If any <code>private</code> fields is
     * found, the method errors out
     *
     * @param type
     *         element
     *
     * @return list of all non-<code>private</code> fields
     */
    private List<VariableElement> getParcelableFieldsOrError(TypeElement type) {
        List<VariableElement> allFields = ElementFilter.fieldsIn(type.getEnclosedElements());
        List<VariableElement> nonPrivateFields = new ArrayList<>();

        for (VariableElement field : allFields) {
            if (!field.getModifiers().contains(PRIVATE)) {
                nonPrivateFields.add(field);
            } else {
                // return error, PRIVATE fields are not allowed
                mErrorReporter.abortWithError("getFieldsError error, PRIVATE fields not allowed", type);
            }
        }

        return nonPrivateFields;
    }


    static final class Property {
        final String fieldName;
        final VariableElement element;
        final TypeName typeName;
        final ImmutableSet<String> annotations;
        final int version;
        TypeMirror typeAdapter;

        Property(String fieldName, VariableElement element) {
            this.fieldName = fieldName;
            this.element = element;
            this.typeName = TypeName.get(element.asType());
            this.annotations = getAnnotations(element);
            this.version = 1;
/*
            // get the parcel adapter if any
            ParcelAdapter parcelAdapter = element.getAnnotation(ParcelAdapter.class);
            if (parcelAdapter != null) {
                try {
                    parcelAdapter.value();
                } catch (MirroredTypeException e) {
                    this.typeAdapter = e.getTypeMirror();
                }
            }

            // get the element version, default 0
            ParcelVersion parcelVersion = element.getAnnotation(ParcelVersion.class);
            this.version = parcelVersion == null ? 0 : parcelVersion.from();*/
        }

        public boolean isNullable() {
            return this.annotations.contains("Nullable");
        }

        public int version() {
            return this.version;
        }

        private ImmutableSet<String> getAnnotations(VariableElement element) {
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                builder.add(annotation.getAnnotationType().asElement().getSimpleName().toString());
            }

            return builder.build();
        }
    }
}
