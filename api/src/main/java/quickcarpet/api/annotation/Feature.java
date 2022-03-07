package quickcarpet.api.annotation;

import java.lang.annotation.*;

@SuppressWarnings("ALL")
@Deprecated(forRemoval = true)
@Retention(RetentionPolicy.CLASS)
@Repeatable(Features.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Feature {
    String[] value();
    String description() default "";
    BugFix[] bug() default {};
}
