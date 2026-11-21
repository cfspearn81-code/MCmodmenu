package net.fastersprint.mixin;

import net.fastersprint.ClientMenu;
import net.fastersprint.PlayerDisplaySettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Unique
    private static final Identifier fastersprint$BOOST_ID =
        Identifier.fromNamespaceAndPath("fastersprint", "boost");

    @Inject(method = "tick", at = @At("HEAD"))
    private void fastersprint$updateMovementSpeed(CallbackInfo callback) {
        net.fastersprint.AutoBridge.tick();
        LocalPlayer player = (LocalPlayer) (Object) this;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean shouldBoost = ClientMenu.speedEnabled()
            && !net.fastersprint.AutoBridge.isProtecting()
            && player.isAlive()
            && !player.isSpectator()
            && !player.isPassenger()
            && !player.isSwimming()
            && !player.isFallFlying()
            && !player.getAbilities().flying;

        AttributeModifier current = speed.getModifier(fastersprint$BOOST_ID);
        double amount = PlayerDisplaySettings.speedMultiplier() - 1.0;
        if (shouldBoost && (current == null || Double.compare(current.amount(), amount) != 0)) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                fastersprint$BOOST_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (!shouldBoost && current != null) {
            speed.removeModifier(fastersprint$BOOST_ID);
        }
    }
}
