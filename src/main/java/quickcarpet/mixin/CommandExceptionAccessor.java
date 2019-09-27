package quickcarpet.mixin;

import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandException.class)
public interface CommandExceptionAccessor {
    @Accessor("message")
    Text getMessageText();
}
