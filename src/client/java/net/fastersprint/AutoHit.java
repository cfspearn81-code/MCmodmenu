package net.fastersprint;

import net.fastersprint.mixin.MinecraftAttackInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class AutoHit {
    private static LocalPlayer actor;
    private static AttackCadence cadence = new AttackCadence();
    private AutoHit() { }

    public static void tick() {
        Minecraft game = Minecraft.getInstance();
        if (actor != game.player) {
            actor = game.player;
            cadence = new AttackCadence();
        }
        if (!PlayerDisplaySettings.autoHitEnabled() || actor == null || game.level == null
                || FreeCam.active() || game.gameMode == null || game.isPaused() || !game.isWindowActive()
                || game.gui.screen() != null || game.gui.overlay() != null || ClientMenu.isOpen()
                || !actor.isAlive() || actor.isSpectator() || actor.isUsingItem()
                || game.options.keyAttack.isDown() || game.options.keyUse.isDown()) return;
        if (!(game.hitResult instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof Player target) || target == actor
                || target.level() != game.level || !target.isAlive() || target.isRemoved()
                || target.isSpectator() || !target.isAttackable() || !actor.hasLineOfSight(target)) return;
        double reach = actor.entityInteractionRange();
        if (actor.getEyePosition().distanceToSqr(hit.getLocation()) > reach * reach) return;
        if (cadence.reserve(actor.tickCount, actor.getAttackStrengthScale(0),
                actor.getCurrentItemAttackStrengthDelay())) {
            // The same attack handler as a normal click, including vanilla restrictions and swing.
            ((MinecraftAttackInvoker) game).fastersprint$startAttack();
        }
    }
}
