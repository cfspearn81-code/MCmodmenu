package net.fastersprint.mixin;

import net.fastersprint.AutoBridge;
import net.fastersprint.BridgeControls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class AutoBridgeInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("RETURN"))
    private void fastersprint$protectBridgeControls(CallbackInfo callback) {
        Minecraft game = Minecraft.getInstance();
        if (game.player == null || game.player.input != (Object) this) return;
        if (net.fastersprint.FreeCam.active() || net.fastersprint.ClientMenu.capturesMovement()) {
            keyPresses = Input.EMPTY; moveVector = Vec2.ZERO; return;
        }
        Input raw = keyPresses;
        AutoBridge.updateControls(moveVector, raw.jump());
        boolean protecting = AutoBridge.isProtecting();
        boolean stop = protecting && AutoBridge.shouldStopInput(moveVector, raw.jump(), raw.sprint());
        var requested = new BridgeControls.Keys(raw.forward(), raw.backward(), raw.left(), raw.right(),
            raw.jump(), raw.shift(), raw.sprint());
        var guarded = BridgeControls.protect(requested, protecting, stop);
        keyPresses = new Input(guarded.forward(), guarded.backward(), guarded.left(), guarded.right(),
            guarded.jump(), guarded.sneak(), guarded.sprint());
        if (stop) moveVector = Vec2.ZERO;
        // LocalPlayer consumes these same controls for physics and vanilla ServerboundPlayerInputPacket.
        // No custom movement/rotation packets and no post-physics velocity or position changes.
    }
}
