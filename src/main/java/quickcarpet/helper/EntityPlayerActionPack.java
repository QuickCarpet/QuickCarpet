package quickcarpet.helper;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EntityPlayerActionPack
{
    private ServerPlayerEntity player;
    
    private boolean doesAttack;
    private int attackInterval;
    private int attackCooldown;
    
    private boolean doesUse;
    private int useInterval;
    private int useCooldown;
    
    private boolean doesJump;
    private int jumpInterval;
    private int jumpCooldown;
    
    private BlockPos currentBlock = new BlockPos(-1, -1, -1);
    private int blockHitDelay;
    private boolean isHittingBlock;
    private float curBlockDamageMP;
    
    private boolean sneaking;
    private boolean sprinting;
    private float forward;
    private float strafing;
    
    public EntityPlayerActionPack(ServerPlayerEntity playerIn)
    {
        player = playerIn;
        stop();
    }
    
    public void copyFrom(EntityPlayerActionPack other)
    {
        doesAttack = other.doesAttack;
        attackInterval = other.attackInterval;
        attackCooldown = other.attackCooldown;
        
        doesUse = other.doesUse;
        useInterval = other.useInterval;
        useCooldown = other.useCooldown;
        
        doesJump = other.doesJump;
        jumpInterval = other.jumpInterval;
        jumpCooldown = other.jumpCooldown;
        
        
        currentBlock = other.currentBlock;
        blockHitDelay = other.blockHitDelay;
        isHittingBlock = other.isHittingBlock;
        curBlockDamageMP = other.curBlockDamageMP;
        
        sneaking = other.sneaking;
        sprinting = other.sprinting;
        forward = other.forward;
        strafing = other.strafing;
    }
    
    public EntityPlayerActionPack setSneaking(boolean doSneak)
    {
        sneaking = doSneak;
        player.setSneaking(doSneak);
        if (sprinting && sneaking)
            setSprinting(false);
        return this;
    }
    public EntityPlayerActionPack setSprinting(boolean doSprint)
    {
        sprinting = doSprint;
        player.setSprinting(doSprint);
        if (sneaking && sprinting)
            setSneaking(false);
        return this;
    }

    public EntityPlayerActionPack stop()
    {
        this.doesUse = false;
        this.doesAttack = false;
        this.doesJump = false;
        resetBlockRemoving(false);
        setSneaking(false);
        setSprinting(false);
        forward = 0.0F;
        strafing = 0.0F;
        player.setJumping(false);


        return this;
    }
    
    public void onUpdate()
    {
        if (doesJump)
        {
            if (--jumpCooldown==0)
            {
                jumpCooldown = jumpInterval;
                //jumpOnce();
                player.setJumping(true);
            }
            else
            {
                player.setJumping(false);
            }
        }
        
        boolean used = false;
        
        /*
        if (doesUse && (--useCooldown)==0)
        {
            useCooldown = useInterval;
            used  = useOnce();
        }
        if (doesAttack)
        {
            if ((--attackCooldown) == 0)
            {
                attackCooldown = attackInterval;
                if (!(used)) attackOnce();
            }
        }
        if (forward != 0.0F)
        {
            player.moveForward = forward*(sneaking?0.3F:1.0F);
        }
        if (strafing != 0.0F)
        {
            player.moveStrafing = strafing*(sneaking?0.3F:1.0F);
        }
        */
    }

    public void resetBlockRemoving(boolean force)
    {
        if (this.isHittingBlock || force)
        {
            player.networkHandler.onPlayerAction(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.currentBlock, Direction.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            player.getEntityWorld().setBlockBreakingProgress(player.getEntityId(), this.currentBlock, -1);
            player.resetLastAttackedTicks();
            blockHitDelay = 0;
            this.currentBlock = new BlockPos(-1,-1,-1);
        }
    }
    

    /*
    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos stack, EnumFacing pos, Vec3d facing, EnumHand vec)
    {
        this.syncCurrentPlayItem();
        ItemStack itemstack = player.getHeldItem(vec);
        float f = (float)(facing.xCoord - (double)stack.getX());
        float f1 = (float)(facing.yCoord - (double)stack.getY());
        float f2 = (float)(facing.zCoord - (double)stack.getZ());
        boolean flag = false;

        if (!this.mc.world.getWorldBorder().contains(stack))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            if (this.currentGameType != GameType.SPECTATOR)
            {
                IBlockState iblockstate = worldIn.getBlockState(stack);

                if ((!player.isSneaking() || player.getHeldItemMainhand().func_190926_b() && player.getHeldItemOffhand().func_190926_b()) && iblockstate.getBlock().onBlockActivated(worldIn, stack, iblockstate, player, vec, pos, f, f1, f2))
                {
                    flag = true;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock)
                {
                    ItemBlock itemblock = (ItemBlock)itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, stack, pos, player, itemstack))
                    {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(stack, pos, vec, f, f1, f2));

            if (!flag && this.currentGameType != GameType.SPECTATOR)
            {
                if (itemstack.func_190926_b())
                {
                    return EnumActionResult.PASS;
                }
                else if (player.getCooldownTracker().hasCooldown(itemstack.getItem()))
                {
                    return EnumActionResult.PASS;
                }
                else
                {
                    if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock())
                    {
                        Block block = ((ItemBlock)itemstack.getItem()).getBlock();

                        if (block instanceof BlockCommandBlock || block instanceof BlockStructure)
                        {
                            return EnumActionResult.FAIL;
                        }
                    }

                    if (this.currentGameType.isCreative())
                    {
                        int i = itemstack.getMetadata();
                        int j = itemstack.func_190916_E();
                        EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                        itemstack.setItemDamage(i);
                        itemstack.func_190920_e(j);
                        return enumactionresult;
                    }
                    else
                    {
                        return itemstack.onItemUse(player, worldIn, stack, vec, pos, f, f1, f2);
                    }
                }
            }
            else
            {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    public EnumActionResult processRightClick(EntityPlayer player, World worldIn, EnumHand stack)
    {
        if (this.currentGameType == GameType.SPECTATOR)
        {
            return EnumActionResult.PASS;
        }
        else
        {
            this.syncCurrentPlayItem();
            this.connection.sendPacket(new CPacketPlayerTryUseItem(stack));
            ItemStack itemstack = player.getHeldItem(stack);

            if (player.getCooldownTracker().hasCooldown(itemstack.getItem()))
            {
                return EnumActionResult.PASS;
            }
            else
            {
                int i = itemstack.func_190916_E();
                ActionResult<ItemStack> actionresult = itemstack.useItemRightClick(worldIn, player, stack);
                ItemStack itemstack1 = actionresult.getResult();

                if (itemstack1 != itemstack || itemstack1.func_190916_E() != i)
                {
                    player.setHeldItem(stack, itemstack1);
                }

                return actionresult.getType();
            }
        }
    }

    public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, EnumHand heldItem)
    {
        this.syncCurrentPlayItem();
        this.connection.sendPacket(new CPacketUseEntity(target, heldItem));
        return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : player.func_190775_a(target, heldItem);
    }

    /
     * Handles right clicking an entity from the entities side, sends a packet to the server.
     *
    public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, RayTraceResult raytrace, EnumHand heldItem)
    {
        this.syncCurrentPlayItem();
        Vec3d vec3d = new Vec3d(raytrace.hitVec.xCoord - target.posX, raytrace.hitVec.yCoord - target.posY, raytrace.hitVec.zCoord - target.posZ);
        this.connection.sendPacket(new CPacketUseEntity(target, heldItem, vec3d));
        return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : target.applyPlayerInteraction(player, vec3d, heldItem);
    }
*/

}
