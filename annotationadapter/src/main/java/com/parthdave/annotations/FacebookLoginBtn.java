package com.parthdave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Parth Dave(parth.dave93@gmail.com)-014 on 16/1/17.
 */


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface FacebookLoginBtn {
    String[] permissions() default {"public_profile", "email"};
    String[] fieldsFromGraph() default {"fields", "id, first_name, last_name, email,gender, location,picture.width(512)"};
}
