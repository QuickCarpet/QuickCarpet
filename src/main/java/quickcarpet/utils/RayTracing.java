package quickcarpet.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Predicate;

public class RayTracing {
    public static HitResult rayTrace(Entity source, float partialTicks, double reach, boolean fluids) {
        BlockHitResult blockHit = rayTraceBlocks(source, partialTicks, reach, fluids);
        double maxSqDist = reach * reach;
        if (blockHit != null) {
            maxSqDist = blockHit.getPos().squaredDistanceTo(source.getCameraPosVec(partialTicks));
        }
        EntityHitResult entityHit = rayTraceEntities(source, partialTicks, reach, maxSqDist);
        return entityHit == null ? blockHit : entityHit;
    }

    public static BlockHitResult rayTraceBlocks(Entity source, float partialTicks, double reach, boolean fluids) {
        Vec3d pos = source.getCameraPosVec(partialTicks);
        Vec3d reachEnd = getEndOfReach(pos, source.getRotationVec(partialTicks), reach);
        return source.world.raycast(new RaycastContext(pos, reachEnd, RaycastContext.ShapeType.OUTLINE, fluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, source));
    }

    public static EntityHitResult rayTraceEntities(Entity source, float partialTicks, double reach, double maxSqDist) {
        Vec3d pos = source.getCameraPosVec(partialTicks);
        Vec3d reachVec = source.getRotationVec(partialTicks).multiply(reach);
        Box box = source.getBoundingBox().stretch(reachVec).expand(1);
        return rayTraceEntities(source, pos, pos.add(reachVec), box, e -> !e.isSpectator() && e.canHit(), maxSqDist);
    }

    public static EntityHitResult rayTraceEntities(Entity source, Vec3d start, Vec3d end, Box box, Predicate<Entity> predicate, double maxSqDistance) {
        World world = source.world;
        double targetDistance = maxSqDistance;
        Entity target = null;
        Vec3d targetHitPos = null;
        for (Entity current : world.getOtherEntities(source, box, predicate)) {
            Box currentBox = current.getBoundingBox().expand(current.getTargetingMargin());
            Optional<Vec3d> currentHit = currentBox.raycast(start, end);
            if (currentBox.contains(start)) {
                if (targetDistance >= 0) {
                    target = current;
                    targetHitPos = currentHit.orElse(start);
                    targetDistance = 0;
                }
            } else if (currentHit.isPresent()) {
                Vec3d currentHitPos = currentHit.get();
                double currentDistance = start.squaredDistanceTo(currentHitPos);
                if (currentDistance < targetDistance || targetDistance == 0) {
                    if (current.getRootVehicle() == source.getRootVehicle()) {
                        if (targetDistance == 0) {
                            target = current;
                            targetHitPos = currentHitPos;
                        }
                    } else {
                        target = current;
                        targetHitPos = currentHitPos;
                        targetDistance = currentDistance;
                    }
                }
            }
        }
        if (target == null) return null;
        return new EntityHitResult(target, targetHitPos);
    }

    public static Vec3d getEndOfReach(Vec3d pos, Vec3d rotation, double reach) {
        return pos.add(rotation.x * reach, rotation.y * reach, rotation.z * reach);
    }
}
