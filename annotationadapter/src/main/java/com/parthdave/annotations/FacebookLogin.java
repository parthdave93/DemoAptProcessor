package com.parthdave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sotsys-014 on 16/1/17.
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface FacebookLogin {
    String[] permissions() default {"public_profile", "email"};
    String[] fieldsFromGraph() default {"fields", "id, first_name, last_name, email,gender, location,picture.width(512)"};
}
