package quickcarpet.api.settings;

import java.util.List;

/**
 * @since 1.2.0
 */
@FunctionalInterface
public interface RuleCreator {
    <T> ParsedRule<T> create(
        String name,
        FieldAccessor<T> field,
        List<RuleCategory> categories,
        List<String> options,
        Validator<T> validator,
        ChangeListener<T> changeListener,
        boolean deprecated
    );
}
