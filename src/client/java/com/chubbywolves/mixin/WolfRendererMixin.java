package com.chubbywolves.mixin;

import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bumps up the render scale of tamed wolves so they look chubby/chonky.
 *
 * This targets WolfRenderer#extractRenderState(Wolf, WolfRenderState, float),
 * the method Minecraft calls once per frame to copy live entity data into the
 * (thread-safe) WolfRenderState object that actually gets rendered. Writing to
 * WolfRenderState.scale here is the same mechanism the game itself uses for
 * baby-animal scaling, so it plays nicely with everything else — including
 * hitbox/eye height, which stay based on the real (unscaled) entity, so this
 * is purely visual.
 *
 * Verified against the official 1.21.11 Mojang mappings. If a future game
 * update renames extractRenderState() or the `scale` field, your IDE will
 * show a "cannot find symbol" error right on this line — use "Go to
 * Declaration" on WolfRenderer/WolfRenderState to find the new name.
 */
@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {

	// How much bigger a tamed wolf renders. 1.0 = normal size.
	private static final float CHUBBY_SCALE = 1.45f;

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void chubbywolves$makeChubby(Wolf wolf, WolfRenderState state, float partialTick, CallbackInfo ci) {
		if (wolf.isTame()) {
			state.scale *= CHUBBY_SCALE;
		}
	}
}
