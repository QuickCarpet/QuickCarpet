package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

import java.util.Random;

public class SellItemFactory implements TradeOffers.Factory
{
    private final ItemStack sell;
    private final int price;
    private final int count;
    private final int maxUses;
    private final int experience;
    private final float multiplier;
    
    public SellItemFactory(Block block_1, int int_1, int int_2, int int_3, int int_4)
    {
        this(new ItemStack(block_1), int_1, int_2, int_3, int_4);
    }
    
    public SellItemFactory(Item item_1, int int_1, int int_2, int int_3)
    {
        this((ItemStack) (new ItemStack(item_1)), int_1, int_2, 6, int_3);
    }
    
    public SellItemFactory(Item item_1, int int_1, int int_2, int int_3, int int_4)
    {
        this(new ItemStack(item_1), int_1, int_2, int_3, int_4);
    }
    
    public SellItemFactory(ItemStack itemStack_1, int int_1, int int_2, int int_3, int int_4)
    {
        this(itemStack_1, int_1, int_2, int_3, int_4, 0.05F);
    }
    
    public SellItemFactory(ItemStack itemStack_1, int int_1, int int_2, int int_3, int int_4, float float_1)
    {
        this.sell = itemStack_1;
        this.price = int_1;
        this.count = int_2;
        this.maxUses = int_3;
        this.experience = int_4;
        this.multiplier = float_1;
    }
    
    public TradeOffer create(Entity entity_1, Random random_1)
    {
        return new TradeOffer(new ItemStack(Items.EMERALD, this.price), new ItemStack(this.sell.getItem(), this.count), this.maxUses, this.experience, this.multiplier);
    }
}