package com.chubbywolves;

import net.fabricmc.api.ClientModInitializer;

public class ChubbyWolvesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Nothing to do here yet — the chubby effect is applied entirely by
		// WolfRendererMixin, which Fabric Loader wires up automatically via
		// chubbywolves.client.mixins.json. This class just needs to exist so
		// the mod has a valid client entrypoint.
	}
}
