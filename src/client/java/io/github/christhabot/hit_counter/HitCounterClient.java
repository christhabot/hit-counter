package io.github.christhabot.hit_counter;

import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import me.shedaniel.autoconfig.AutoConfig;

public class HitCounterClient implements ClientModInitializer {
	public static HitCounterConfig getConfig() {
		return AutoConfig.getConfigHolder(HitCounterConfig.class).getConfig();
	}



	private static final AtomicInteger HITS_DEALT = new AtomicInteger(0);
	private static final AtomicInteger HITS_TAKEN = new AtomicInteger(0);
	private static final AtomicInteger HP_DEALT = new AtomicInteger(0);
	private static final AtomicInteger HP_TAKEN = new AtomicInteger(0);

	private static volatile long lastActionTime = 0L;
	private static long getTimeoutMs() {
		return getConfig().disappearAfter * 1000;
	}


	private static volatile int lastRecordedLastAttackedTime = 0;

	private enum ActionType { NONE, DEALT, TAKEN }
	private static volatile ActionType lastActionType = ActionType.NONE;

	public static void onHitDealt(float hp) {
		HP_DEALT.addAndGet((int) Math.floor(hp));
		HITS_DEALT.incrementAndGet();
		lastActionTime = System.currentTimeMillis();
		lastActionType = ActionType.DEALT;
	}

	public static void onHitTaken(float hp) {
		HP_TAKEN.addAndGet((int) Math.floor(hp));
		HITS_TAKEN.incrementAndGet();
		lastActionTime = System.currentTimeMillis();
		lastActionType = ActionType.TAKEN;
	}

	public static boolean isActive() {
		long t = lastActionTime;
		return t != 0L && (System.currentTimeMillis() - t) < getTimeoutMs();
	}

	public static void reset() {
		HITS_DEALT.set(0);
		HITS_TAKEN.set(0);
		lastActionTime = 0L;
		lastActionType = ActionType.NONE;
	}
	private float lastRecordedHp = -1;

	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(this::onHudRender);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client == null || client.player == null) return;

			try {
				int lastAttacked = client.player.getLastAttackedTime();
				float currentHp = client.player.getHealth();

				
				if (lastRecordedHp < 0) {
					lastRecordedHp = currentHp;
				}

				if (lastAttacked > lastRecordedLastAttackedTime) {
					float hpLost = lastRecordedHp - currentHp;
					if (hpLost > 0) {
						client.execute(() -> HitCounterClient.onHitTaken(hpLost));
					}
				}

				lastRecordedHp = currentHp;
				lastRecordedLastAttackedTime = lastAttacked;
			} catch (Throwable t) {
				System.err.println("[HitCounter] Error in client tick: " + t.getMessage());
			}
		});
	}

	private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
		try {
			if (!isActive()) {
				if (lastActionTime != 0L) reset();
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null || client.textRenderer == null) return;

			int x = HitCounterClient.getConfig().guiX;
			int y = HitCounterClient.getConfig().guiY;
			String dealt;
			String taken;
			String damageDealt;
			String damageTaken;
			if (HitCounterClient.getConfig().showText) {
				dealt = "Hits Dealt: " + HITS_DEALT.get();
				taken = "Hits Taken: " + HITS_TAKEN.get();
				damageTaken = "Damage Taken: " + HP_TAKEN.get()/2;
				damageDealt = "Damage Dealt: " + HP_DEALT.get()/2;
			} else {
				dealt = "" + HITS_DEALT.get();
				taken = "" + HITS_TAKEN.get();
				damageTaken = "" + HP_TAKEN.get()/2;
				damageDealt = "" + HP_DEALT.get()/2;
			}
			int dealtColor = 0xAAAAAA;
			int takenColor = 0xAAAAAA;
			if(HitCounterClient.getConfig().ColorText)
			{
				dealtColor = Integer.parseInt(HitCounterClient.getConfig().defaultHex.substring(1), 16);
				takenColor = Integer.parseInt(HitCounterClient.getConfig().defaultHex.substring(1), 16);
			}
			else if (HitCounterClient.getConfig().ColorOnBothText) {
				if (lastActionType == ActionType.DEALT) {
					dealtColor = Integer.parseInt(HitCounterClient.getConfig().didHitHex.substring(1), 16);
					takenColor = Integer.parseInt(HitCounterClient.getConfig().didHitHex.substring(1), 16);
				} else if (lastActionType == ActionType.TAKEN) {
					takenColor = Integer.parseInt(HitCounterClient.getConfig().gotHitHex.substring(1), 16);
					dealtColor = Integer.parseInt(HitCounterClient.getConfig().gotHitHex.substring(1), 16);
				}
			} else {
				dealtColor = Integer.parseInt(HitCounterClient.getConfig().defaultHex.substring(1), 16);
				takenColor = Integer.parseInt(HitCounterClient.getConfig().defaultHex.substring(1), 16);

				if (lastActionType == ActionType.DEALT) {
					dealtColor = Integer.parseInt(HitCounterClient.getConfig().didHitHex.substring(1), 16);
				} else if (lastActionType == ActionType.TAKEN) {
					takenColor = Integer.parseInt(HitCounterClient.getConfig().gotHitHex.substring(1), 16);
				}
			}
			try {
				int curY = y;
				String[] options = {
						HitCounterClient.getConfig().configA.name(),
						HitCounterClient.getConfig().configB.name(),
						HitCounterClient.getConfig().configC.name(),
						HitCounterClient.getConfig().configD.name()
				};
				for(String option: options)
				{
                    switch (option) {
                        case "NONE" -> {
                            continue;
                        }
                        case "HITSTAKEN" ->
								context.drawTextWithShadow(client.textRenderer, taken, x, curY, takenColor);
                        case "HITSDEALT" ->
								context.drawTextWithShadow(client.textRenderer, dealt, x, curY, dealtColor);
                        case "DAMAGETAKEN" ->
                                context.drawTextWithShadow(client.textRenderer, damageTaken, x, curY, takenColor);
                        case "DAMAGEDEALT" ->
                                context.drawTextWithShadow(client.textRenderer, damageDealt, x, curY, dealtColor);
                    }
					curY += 10;
				}
			} catch (Exception e) {
				System.err.println("[HitCounter] Error rendering HUD: " + e.getMessage());
			}
		} catch (Exception e) {
			System.err.println("[HitCounter] Render error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}