package com.parthdave.codegenerator.facebooklogin;

/**
 * Created by parth on 8/1/17.
 */

import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.parthdave.annotations.FacebookInit;
import com.parthdave.annotations.FacebookLogin;
import com.parthdave.codegenerator.commons.ErrorReporter;
import com.parthdave.codegenerator.commons.TypeUtil;
import com.parthdave.codegenerator.parcable.Codegen;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.lang.model.SourceVersion.RELEASE_7;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;

@SupportedAnnotationTypes("com.parthdave.annotations.FacebookInit")
@SupportedSourceVersion(RELEASE_7)
public final class FacebookInitCodegen extends AbstractProcessor {

    public final String TAG = "FacebookInitCodegen";
    ErrorReporter mErrorReporter;
    Types mTypeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        Log.d(TAG,"init");
        mErrorReporter = new ErrorReporter(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Collection<? extends Element> facebookInitAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(FacebookInit.class);

        for (int index = 0; index < facebookInitAnnotatedElements.size(); index++) {
            System.out.println(facebookInitAnnotatedElements.toString());
        }

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>().addAll(ElementFilter.typesIn(facebookInitAnnotatedElements)).build();

        for (int index = 0; index < types.size(); index++) {
            System.out.println(types.get(index));
        }

        for (TypeElement type : types) {
            processType(type);
        }


        return true;
    }


    private void processType(TypeElement type) {
        System.out.println("Codegen:" + "processType() called with: type = [" + type + "]");
        System.out.println("Codegen:" + "generatedSubclassName() = [" + generatedSubclassName(type, 0) + "]");
        String className = generatedSubclassName(type, 0);
        String source = generateClass(type, className, generatedSubClassName(type), true);
        System.out.println("Codegen:" + "generateClass(type, className, \"\", true) = [" + source + "]");
        writeSourceFile(className, source, type);
    }


    private void writeSourceFile(String className, String text, TypeElement originatingType) {
        System.out.println("Codegen:" + "processType() called with: className = [" + className + "]" + "text = [" + text + "]" + "]" + "originatingType = [" + originatingType + "]");
        try {
            JavaFileObject sourceFile = processingEnv.getFiler().
                    createSourceFile(className, originatingType);

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
        System.out.println("Codegen:" + "generatedSubclassName() called with: type = [" + type + "], depth = [" + depth + "]");
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


    private String generateClass(TypeElement type, String className, String classToExtend, boolean isFinal) {
        if (type == null) {
            Log.d("generateClass", "generateClass() called with: type = [" + type + "], className = [" + className + "], classToExtend = [" + classToExtend + "], isFinal = [" + isFinal + "]");
            mErrorReporter.abortWithError("generateClass was invoked with null type", type);
        }
        if (className == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null class name", type);
        }
        if (classToExtend == null) {
            mErrorReporter.abortWithError("generateClass was invoked with null parent class", type);
        }

        System.out.println("generateClass() called with: type = [" + type + "], className = [" + className + "], classToExtend = [" + classToExtend + "], isFinal = [" + isFinal + "]");


        List<VariableElement> nonPrivateFields = getParcelableFieldsOrError(type);
        //        if (nonPrivateFields.isEmpty()) {
        //            mErrorReporter.abortWithError("generateClass error, all fields are declared PRIVATE", type);
        //        }

        // get the properties
        ImmutableList<Property> properties = buildProperties(nonPrivateFields);


        for (int index = 0; index < properties.size(); index++) {
            System.out.println("properties: " + properties.toString());
        }

        getListOfFields(type, "onCreate");

        // get the type adapters
        //        ImmutableMap<TypeMirror, FieldSpec> typeAdapters = getTypeAdapters(properties);

        // get the parcel version
        //noinspection ConstantConditions
        int version = 1;//type.getAnnotation(AutoParcel.class);

        // Generate the AutoParcel_??? class
        String pkg = TypeUtil.packageNameOf(type);
        TypeName classTypeName = ClassName.get(pkg, className);
        TypeName classToExtendTypeName = ClassName.get(pkg, classToExtend);
        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                // Add the version
                .superclass(classToExtendTypeName).addField(TypeName.INT, "version", PRIVATE)
//                .addMethod(FacebookInitCodeGen.getInstance(false).onCreate())
                // Class must be always final
                .addModifiers(FINAL);
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
        JavaFile javaFile = JavaFile.builder(pkg, subClass.build()).build();
        return javaFile.toString();
    }


    private Element getListOfFields(TypeElement properties, String methodType) {

        ImmutableList.Builder<Property> property = new ImmutableList.Builder<Property>();

        for (int index = 0; index < properties.getEnclosedElements().size(); index++) {
            System.out.println("getSimpleName Name:" + properties.getEnclosedElements().get(index).getSimpleName());
            System.out.println("getModifiers Name:" + properties.getEnclosedElements().get(index).getModifiers());
            System.out.println("getKind Name:" + properties.getEnclosedElements().get(index).getKind());
            System.out.println("getEnclosedElements Name:" + properties.getEnclosedElements().get(index).getEnclosedElements());

            if (properties.getEnclosedElements().get(index).getSimpleName().toString().equals(methodType)) {
                return properties.getEnclosedElements().get(index);
            }
        }
        return null;
    }

    private ImmutableList<Property> buildProperties(List<VariableElement> elements) {
        ImmutableList.Builder<Property> builder = ImmutableList.builder();
        for (VariableElement element : elements) {
            builder.add(new Property(element.getSimpleName().toString(), element));
        }

        return builder.build();
    }

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
