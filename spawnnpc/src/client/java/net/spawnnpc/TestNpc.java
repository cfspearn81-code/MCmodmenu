package net.spawnnpc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;

/** A real client player entity for rendering/picking, with no network account or server entity. */
public final class TestNpc extends RemotePlayer {
    private final LocalPlayer appearance;
    private final boolean knockbackEnabled;
    public TestNpc(ClientLevel level, GameProfile profile, LocalPlayer appearance) {
        this(level, profile, appearance, false);
    }
    public TestNpc(ClientLevel level, GameProfile profile, LocalPlayer appearance, boolean knockbackEnabled) {
        super(level, profile);
        this.appearance = appearance;
        this.knockbackEnabled = knockbackEnabled;
        setNoGravity(!knockbackEnabled);
        if (knockbackEnabled) noPhysics = false;
    }
    public boolean hasKnockback() { return knockbackEnabled; }
    public void reactToHit(Player attacker) {
        animateHurt(attacker.getYRot());
        if (knockbackEnabled) knockback(.4, attacker.getX() - getX(), attacker.getZ() - getZ(),
            attacker.damageSources().playerAttack(attacker), 0);
    }
    @Override public void aiStep() {
        super.aiStep();
        // RemotePlayer normally only interpolates server movement. These local dummies need one native physics step.
        if (knockbackEnabled) {
            travel(Vec3.ZERO);
            // Remote players do not clear downward velocity on client-side floor contact by themselves.
            Vec3 motion = getDeltaMovement();
            if (onGround() && motion.y < 0) setDeltaMovement(motion.x, 0, motion.z);
        }
    }
    @Override public PlayerSkin getSkin() { return appearance.getSkin(); }
    @Override public boolean isModelPartShown(PlayerModelPart part) { return appearance.isModelPartShown(part); }
    @Override public net.minecraft.world.entity.HumanoidArm getMainArm() { return appearance.getMainArm(); }
}
