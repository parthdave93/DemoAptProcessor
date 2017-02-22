DemoAptProcessor
-------------

Apt-processor is a conveyor to reduce boilerplate codes like facebook logins, alert dialogues, request permission etc.

1. Own Annotations
--------

First job is to create your own annotations which will have different properties according to your requirement like
If you want to method reference then <br/>

```
@Target(ElementType.METHOD)
```
If you want fields like butterknife do then

```
@Target(ElementType.FIELD)
```

now annotation file will be something like below:
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FacebookInit {
    boolean isDebugabble() default false;
}
```
If we pay more attention we can see <kbd>@Target(ElementType.TYPE)</kbd> this tells the compiler that this annotation can have multipurpose. 
If we have requirement like to define only on fields then we can have <kbd>@Target(ElementType.FIELD)</kbd>

There are various Element type which will be used in different cases for more info check <a href="http://www.oracle.com/technetwork/articles/java/javareflection-1536171.html">Java Reflections</a>

If you want extra parameters like
```
@ViewById(R.id.buttonName)
```

The <kbd>R.id.buttonName</kbd> is what we called as property or fields of your annotations in above example I do have <kbd>isDebugabble</kbd> property which will be false by default.

If you do have multiple property then it will look like below:
```
@Target(ElementType.TYPE)<br/>
@Retention(RetentionPolicy.SOURCE)<br/>
public @interface FacebookLogin {<br/>
    String[] permissions() default {"public_profile", "email"};<br/>
    String[] fieldsFromGraph() default {"fields", "id, first_name, last_name, email,gender, location,picture.width(512)"};<br/>
}<br/>
```

Now that annotation created, we need to get the actions. by actions i mean when build.gradle starts before the java compiler starts we need to create our own class. as we can not alter the java class by apt-processor we need to create extra file for our code.

2. Annotation Processor Compile callback
-------------


let's create annotation callback (by callback i mean at compile time this file will give you the references of the files which uses the our annotation)

```
@SupportedAnnotationTypes("com.parthdave.annotations.FacebookInit")
@SupportedSourceVersion(RELEASE_7)
public final class FacebookInitCodegen extends AbstractProcessor {

    public final String TAG = "FacebookInitCodegen";
    ErrorReporter mErrorReporter;
    Types mTypeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
     ....
    }
}
```

Here <kbd>@SupportedAnnotationTypes("com.parthdave.annotations.FacebookInit")</kbd> this defines that when FacebookInit which is mine annotation if any files using this annotation please let me know here.
and the classes which is using this annotation we will get it from process method

```
Collection<? extends Element> facebookInitAnnotatedElements = roundEnvironment.getElementsAnnotatedWith(FacebookInit.class);
```

once we get the file we can process it.
check <a href="https://github.com/parthdave93/DemoAptProcessor/blob/master/annotationprocessor/src/main/java/com/parthdave/codegenerator/facebooklogin/FacebookInitCodegen.java">FacebookLoginCodeGen</a> 

2. Register this annotation processor
---------
<img src="https://github.com/parthdave93/DemoAptProcessor/blob/master/captures/image1.png
></img>


Goto <kbd>main > resources > create directory META-INF.services</kbd>
now create file <kbd>javax.annotation.processing.Processor</kbd>

in which write reference path of your annotation processor like below:
```
com.parthdave.codegenerator.facebooklogin.FacebookLoginCodeGen
```

Now we can use our processor
Use project or jar in your project any thing you like to have

I have used like this:
```
provided project(":annotationadapter")
apt project(":annotationprocessor")
```

And
```
@FacebookInit
public class ApplicationUtils extends Application {

    public boolean isDebug = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isDebug = true;
        Log.d("asdasd","asdasd");
    }
}
```

So generated java class looks like this:
```
public final class ApplicationUtils_ extends ApplicationUtils {
  private int version;

  public void onCreate() {
    super.onCreate();
     //Facebook Initialize
                com.facebook.FacebookSdk.sdkInitialize(this);
                com.facebook.FacebookSdk.setIsDebugEnabled(false);
  }
}
```

Viola, We had created reduntdent code just by adding one annotation.
So what we can archive from this is like:
Create services in background to get locations (BaseCode)
Create Facebook,Gplus or any other logins from just annotation.
RuntimePermissions like <a href="https://github.com/hotchemi/PermissionsDispatcher">Permission Dispacher</a>
Pushnotifications GCM or FCM or Amazon
Api calls failed popups etc.

I will re-structure the code for this and make some of above mentioned topics.
Until then if you do want to contribute go ahead and solve the basic problems of android devops.

You can connect me on <a href="https://in.linkedin.com/in/parth-dave-907b8177">linkdin</a> or to my email parth.dave93@gmail.com










































































































































































































































































































































1
1











































































































