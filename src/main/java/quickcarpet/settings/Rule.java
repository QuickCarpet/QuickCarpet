package quickcarpet.settings;

import quickcarpet.annotation.BugFix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any field in this class annotated with this class is interpreted as a carpet rule.
 * The field must be static and have a type of one of:
 * - boolean
 * - int
 * - double
 * - String
 * - a subclass of Enum
 * The default value of the rule will be the initial value of the field.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    /**
     * The rule name, by default the same as the field name
     */
    String name() default ""; // default same as field name

    /**
     * A list of categories the rule is in
     */
    RuleCategory[] category();

    /**
     * Options to select in menu and in carpet client.
     * Inferred for booleans and enums. Otherwise, must be present.
     */
    String[] options() default {};

    /**
     * The class of the validator checked when the rule is changed.
     */
    Class<? extends Validator> validator() default Validator.AlwaysTrue.class;

    /**
     * The class of the listener called when the rule is changed.
     */
    Class<? extends ChangeListener> onChange() default ChangeListener.Empty.class;

    /**
     * List of Minecraft bugs this rule fixes
     */
    BugFix[] bug() default {};

    String deprecated() default "";
}
