package net.spawnnpc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;

/** A real client player entity for rendering/picking, with no network account or server entity. */
public final class TestNpc extends RemotePlayer {
    private final LocalPlayer appearance;
    public TestNpc(ClientLevel level, GameProfile profile, LocalPlayer appearance) {
        super(level, profile);
        this.appearance = appearance;
        setNoGravity(true);
    }
    @Override public PlayerSkin getSkin() { return appearance.getSkin(); }
    @Override public boolean isModelPartShown(PlayerModelPart part) { return appearance.isModelPartShown(part); }
    @Override public net.minecraft.world.entity.HumanoidArm getMainArm() { return appearance.getMainArm(); }
}
