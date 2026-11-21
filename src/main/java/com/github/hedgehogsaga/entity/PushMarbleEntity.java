package com.github.hedgehogsaga.entity;

import com.github.hedgehogsaga.HedgehogSagaMod;
import com.github.hedgehogsaga.ModBlocks;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class PushMarbleEntity extends Entity {
    static final double CLICK_SPEED = 0.4;
    static final float CLICK_EXHAUSTION = 0.4f;
    static final double WALK_SPEED = 0.06;
    static final double LAVA_MAX_SPEED = 0.05;
    static final double LAVA_DRAG = 0.97;
    static final double FLOAT_DEPTH = 0.6;

    public static EntityType<PushMarbleEntity> TYPE;

    public PushMarbleEntity(EntityType<? extends PushMarbleEntity> type, World world) {
        super(type, world);
    }

    public static void register() {
        TYPE = Registry.register(Registries.ENTITY_TYPE, Identifier.of(HedgehogSagaMod.MOD_ID, "push_marble"),
                EntityType.Builder.<PushMarbleEntity>create(PushMarbleEntity::new, SpawnGroup.MISC)
                        .dimensions(0.98f, 0.98f)
                        .makeFireImmune()
                        .maxTrackingRange(10)
                        .trackingTickInterval(1)
                        .build("push_marble"));
        PayloadTypeRegistry.playC2S().register(WalkPush.ID, WalkPush.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WalkPush.ID,
                (payload, ctx) -> walkPush(ctx.player(), Direction.fromHorizontal(payload.dir())));
    }

    public static PushMarbleEntity fromBlock(World world, BlockPos pos) {
        world.removeBlock(pos, false);
        PushMarbleEntity e = new PushMarbleEntity(TYPE, world);
        e.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(e);
        return e;
    }

    public void clickPush(PlayerEntity player) {
        Direction d = player.getHorizontalFacing();
        setVelocity(d.getOffsetX() * CLICK_SPEED, getVelocity().y, d.getOffsetZ() * CLICK_SPEED);
        velocityDirty = true;
        player.addExhaustion(CLICK_EXHAUSTION);
        playSound(SoundEvents.BLOCK_STONE_HIT, 1.0f, 0.8f);
    }

    public static boolean walkPush(PlayerEntity player, Direction dir) {
        World world = player.getWorld();
        Box probe = player.getBoundingBox().contract(0, 0.1, 0).offset(dir.getOffsetX() * 0.4, 0, dir.getOffsetZ() * 0.4);
        PushMarbleEntity target = world.getEntitiesByClass(PushMarbleEntity.class, probe, e -> true).stream().findFirst().orElse(null);
        BlockPos blockPos = null;
        if (target == null) {
            for (BlockPos p : BlockPos.iterate(BlockPos.ofFloored(probe.minX, probe.minY, probe.minZ), BlockPos.ofFloored(probe.maxX, probe.maxY, probe.maxZ))) {
                if (world.getBlockState(p).isOf(ModBlocks.GREEN_PUSH_MARBLE)) { blockPos = p.toImmutable(); break; }
            }
            if (blockPos == null) return false;
        }
        if (world.isClient) return true;
        if (target == null) target = fromBlock(world, blockPos);

        Vec3d v = target.getVelocity();
        double along = v.x * dir.getOffsetX() + v.z * dir.getOffsetZ();
        if (along < WALK_SPEED) {
            target.setVelocity(v.add(dir.getOffsetX() * (WALK_SPEED - along), 0, dir.getOffsetZ() * (WALK_SPEED - along)));
            target.velocityDirty = true;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Box box = getBoundingBox();
        var riders = getWorld().getOtherEntities(this, new Box(box.minX, box.maxY - 0.05, box.minZ, box.maxX, box.maxY + 0.2, box.maxZ),
                e -> !e.noClip && e.getBoundingBox().minY > box.maxY - 0.1);
        Vec3d before = getPos();

        double drag = 0;
        if (getWorld().isClient) {
            if (lerpSteps > 0) {
                double f = 1.0 / lerpSteps--;
                setPosition(getX() + (lerpX - getX()) * f, getY() + (lerpY - getY()) * f, getZ() + (lerpZ - getZ()) * f);
            }
        } else {
            Vec3d v = getVelocity();
            double lava = getFluidHeight(FluidTags.LAVA);
            if (lava > 0) {
                v = new Vec3d(v.x, (lava - FLOAT_DEPTH) * 0.25, v.z);
                double h = v.horizontalLength();
                if (h > LAVA_MAX_SPEED) v = new Vec3d(v.x * LAVA_MAX_SPEED / h, v.y, v.z * LAVA_MAX_SPEED / h);
                drag = LAVA_DRAG;
            } else {
                v = v.add(0, -0.04, 0);
                drag = isOnGround() ? getWorld().getBlockState(getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98 : 0.98;
            }
            setVelocity(v);
            var others = getWorld().getEntitiesByClass(PushMarbleEntity.class, box.stretch(v).expand(0.01), e -> e != this)
                    .stream().map(e -> VoxelShapes.cuboid(e.getBoundingBox())).toList();
            move(MovementType.SELF, others.isEmpty() ? v : Entity.adjustMovementForCollisions(this, v, box, getWorld(), others));
        }
        Vec3d delta = getPos().subtract(before);
        if (delta.lengthSquared() > 1.0E-7) {
            for (Entity e : riders) {
                boolean ours = e instanceof PlayerEntity p ? p.isMainPlayer() : !getWorld().isClient;
                if (ours) e.move(MovementType.SHULKER, delta);
            }
        }

        if (!getWorld().isClient) {
            Vec3d after = getVelocity();
            setVelocity(after.x * drag, after.y * 0.98, after.z * drag);
        }
    }

    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpSteps = 3;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!getWorld().isClient) clickPush(player);
        return ActionResult.success(getWorld().isClient);
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (!getWorld().isClient && attacker instanceof PlayerEntity p) {
            if (p.isCreative()) {
                discard();
            } else if (p.getMainHandStack().isIn(ItemTags.PICKAXES)) {
                dropStack(new ItemStack(ModBlocks.GREEN_PUSH_MARBLE));
                discard();
            }
        }
        return true;
    }

    @Override public boolean isCollidable() { return getWorld().isClient; }
    @Override public boolean canHit() { return !isRemoved(); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluids() { return false; }
    @Override public ItemStack getPickBlockStack() { return new ItemStack(ModBlocks.GREEN_PUSH_MARBLE); }

    @Override protected void initDataTracker(DataTracker.Builder builder) {}
    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}

    public record WalkPush(int dir) implements CustomPayload {
        public static final Id<WalkPush> ID = new Id<>(Identifier.of(HedgehogSagaMod.MOD_ID, "walk_push"));
        public static final PacketCodec<RegistryByteBuf, WalkPush> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, WalkPush::dir, WalkPush::new);

        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }
}
