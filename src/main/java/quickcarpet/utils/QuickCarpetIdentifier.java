package quickcarpet.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public final class QuickCarpetIdentifier {
    public static final Codec<Identifier> CODEC = Codec.STRING.comapFlatMap(QuickCarpetIdentifier::validate, Identifier::toString).stable();

    private QuickCarpetIdentifier() {}

    public static Identifier of(String id) {
        return id.contains(":") ? new Identifier(id) : new Identifier("quickcarpet", id);
    }

    public static String toString(Identifier id) {
        return "quickcarpet".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    public static DataResult<Identifier> validate(String id) {
        try {
            return DataResult.success(of(id));
        } catch (InvalidIdentifierException e) {
            return DataResult.error("Not a valid identifier: " + id + " " + e.getMessage());
        }
    }
}
