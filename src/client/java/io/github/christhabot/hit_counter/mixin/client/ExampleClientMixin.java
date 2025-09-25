package io.github.christhabot.hit_counter.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.christhabot.hit_counter.HitCounterClient;
import net.minecraft.entity.Entity;

@Mixin(MinecraftClient.class)
class ExampleClientMixin {

	@Inject(method = "doAttack", at = @At("RETURN"))
	private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null) return;

			try {
				if (client.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() != null) {
					Entity ent = ehr.getEntity();
					if (ent instanceof LivingEntity target) {

						if (!HitCounterClient.getConfig().onlyPlayers || target instanceof PlayerEntity) {
							try {
								HitCounterClient.onHitDealt(2);
							} catch (Throwable inner) {
								System.err.println("[HitCounter] Error computing estimated damage: " + inner);
							}
						}
					}
				}
			} catch (Throwable inner2) {
				System.err.println("[HitCounter] Error inspecting crosshairTarget in doAttack: " + inner2);
			}

		} catch (Throwable t) {
			System.err.println("[HitCounter] Unexpected error in doAttack mixin: " + t);
		}
	}
}


@Mixin(ClientPlayerEntity.class)
class ClientPlayerEntityMixin {

	@Unique
	private float lastHealth = -1f;

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null) {
				lastHealth = -1f;
				return;
			}

			ClientPlayerEntity player = client.player;
			if (player == null) {
				lastHealth = -1f;
				return;
			}

			if (!player.isAlive()) {
				lastHealth = -1f;
				return;
			}

			float currentHealth;
			try {
				currentHealth = player.getHealth();
			} catch (Throwable t) {

				System.err.println("[HitCounter] Failed to read player health: " + t);
				return;
			}

			if (lastHealth < 0f) {
				lastHealth = currentHealth;
				return;
			}

			if (currentHealth < lastHealth) {
				boolean counted = false;

				try {

					DamageSource lastDamageSource = player.getRecentDamageSource();

					if (lastDamageSource != null) {
						try {

							Object attackerObj = lastDamageSource.getAttacker();
							if (attackerObj instanceof LivingEntity attacker) {

								if (!HitCounterClient.getConfig().onlyPlayers || attacker instanceof PlayerEntity) {
									counted = true;
								}
							}
						} catch (Throwable innerAttacker) {

							System.err.println("[HitCounter] Error inspecting attacker from DamageSource: " + innerAttacker);
						}
					}
				} catch (Throwable dsEx) {

					System.err.println("[HitCounter] Exception calling getRecentDamageSource(): " + dsEx);
				}

				if (counted) {
					try {
						HitCounterClient.onHitTaken(lastHealth - currentHealth);
					} catch (Throwable t) {
						System.err.println("[HitCounter] Error calling onHitTaken(): " + t);
					}
				}
			}

			lastHealth = currentHealth;
			if (player.isDead() || player.getHealth() <= 0f) {
				lastHealth = player.getMaxHealth();
			}

		} catch (Throwable t) {

			System.err.println("[HitCounter] Error in tick mixin: " + t);
		}
	}
}