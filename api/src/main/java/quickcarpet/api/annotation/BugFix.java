package quickcarpet.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BugFix {
    String value();
    String fixVersion() default "";
    String status() default "";
}
