package com.parthdave.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Parth Dave(parth.dave93@gmail.com)-014 on 11/1/17.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GAanalytics {
    String analyticsId() default "";
}
