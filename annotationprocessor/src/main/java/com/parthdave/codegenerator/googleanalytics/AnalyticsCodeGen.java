package com.parthdave.codegenerator.googleanalytics;

/**
 * Created by parth on 8/1/17.
 */

import android.util.Log;
import android.view.animation.GridLayoutAnimationController;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.parthdave.annotations.AutoParcel;
import com.parthdave.annotations.GAanalytics;
import com.parthdave.codegenerator.commons.ErrorReporter;
import com.parthdave.codegenerator.commons.TypeUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
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

@SupportedAnnotationTypes("com.parthdave.annotations.AutoParcel")
@SupportedSourceVersion(RELEASE_7)
public final class AnalyticsCodeGen extends AbstractProcessor {

    ErrorReporter mErrorReporter;
    Types mTypeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mErrorReporter = new ErrorReporter(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Collection<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(GAanalytics.class);

        for (int index = 0; index < annotatedElements.size(); index++) {
            System.out.println(annotatedElements.toString());
        }

        List<TypeElement> types = new ImmutableList.Builder<TypeElement>().addAll(ElementFilter.typesIn(annotatedElements)).build();

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
        System.out.println("Codegen:" + "generateClass(type, className, \"\", true) = [" + generateClass(type, className, "", true) + "]");
        String source = generateClass(type, className, "", true);
        writeSourceFile(className, source, type);
    }


    private void writeSourceFile(String className, String text, TypeElement originatingType) {
        System.out.println("Codegen:" + "processType() called with: className = [" + className + "]"+"text = [" + text + "]" + "]"+"originatingType = [" + originatingType + "]");
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
        return generatedClassName(type, Strings.repeat("$", depth) + "AutoParcel_");
    }

    private String generatedClassName(TypeElement type, String prefix) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = TypeUtil.packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return pkg + dot + prefix + name;
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

     /*   List<VariableElement> nonPrivateFields = getParcelableFieldsOrError(type);
        if (nonPrivateFields.isEmpty()) {
            mErrorReporter.abortWithError("generateClass error, all fields are declared PRIVATE", type);
        }

        // get the properties
        ImmutableList<Property> properties = buildProperties(nonPrivateFields);

        // get the type adapters
        ImmutableMap<TypeMirror, FieldSpec> typeAdapters = getTypeAdapters(properties);*/

        // get the parcel version
        //noinspection ConstantConditions
        int version = 1;//type.getAnnotation(AutoParcel.class);

        // Generate the AutoParcel_??? class
        String pkg = TypeUtil.packageNameOf(type);
        TypeName classTypeName = ClassName.get(pkg, className);
        TypeSpec.Builder subClass = TypeSpec.classBuilder(className)
                // Add the version
                .addField(TypeName.INT, "version", PRIVATE)
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

        System.out.println("pkg:"+pkg);
        System.out.println("subClass.build():"+subClass.build());
        JavaFile javaFile = JavaFile.builder(pkg, subClass.build()).build();
        return javaFile.toString();
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
