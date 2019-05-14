package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

public class Reflection {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final Class<? extends TradeOffers.Factory> SELL_ITEM_FACTORY_CLASS = TradeOffers.WANDERING_TRADER_TRADES.get(1)[0].getClass();
    private static final MethodType SELL_ITEM_FACTORY_CONSTRUCTOR_TYPE = MethodType.methodType(void.class, ItemStack.class, int.class, int.class, int.class, int.class, float.class);
    private static final MethodHandle newSellItemFactoryHandle = getNewSellItemFactoryHandle();

    private static MethodHandle getNewSellItemFactoryHandle() {
        try {
            return LOOKUP.findConstructor(SELL_ITEM_FACTORY_CLASS, SELL_ITEM_FACTORY_CONSTRUCTOR_TYPE);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls {@link TradeOffers.SellItemFactory#SellItemFactory(ItemStack, int, int, int, int, float)}
     * @param sell The item to sell (amount is ignored)
     * @param price The price in emeralds
     * @param count The number of items to sell
     * @param maxUses Maximum number of uses before locking
     * @param experience Experience for leveling up the trader
     * @param multiplier Price multiplier for adjustments
     * @return A {@link TradeOffers.Factory} representing this trade
     */
    public static TradeOffers.Factory newSellItemFactory(ItemStack sell, int price, int count, int maxUses, int experience, float multiplier) {
        try {
            return (TradeOffers.Factory) newSellItemFactoryHandle.invoke(sell, price, count, maxUses, experience, multiplier);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Calls {@link BlockEntityType.Builder#create(Supplier)} (1.14) or {@link BlockEntityType.Builder#create(Supplier, Block...)} (1.14.1)
     * @param supplier Supplier/constructor of the block entity
     * @param blocks Blocks this block entity is used for
     * @param <T> Type of the block entity
     * @return a BlockEntityType.Builder
     */
    public static <T extends BlockEntity> BlockEntityType.Builder<T> newBlockEntityTypeBuilder(Supplier<T> supplier, Block... blocks) {
        try {
            return BlockEntityType.Builder.create(supplier); // 1.14
        } catch (NoSuchMethodError e) {
            Method m = Arrays.stream(BlockEntityType.Builder.class.getMethods()).filter(x -> x.getReturnType() == BlockEntityType.Builder.class).findFirst().get();
            try {
                return (BlockEntityType.Builder<T>) m.invoke(null, supplier, blocks); // 1.14.1
            } catch (ReflectiveOperationException e1) {
                e1.addSuppressed(e);
                throw new RuntimeException(e1);
            }
        }
    }

    public static <T> T callPrivateConstructor(Class<T> cls) {
        try {
            Constructor<T> constr = cls.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
