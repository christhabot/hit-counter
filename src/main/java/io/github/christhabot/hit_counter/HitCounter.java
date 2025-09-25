package io.github.christhabot.hit_counter;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class HitCounter implements ModInitializer {
	public static HitCounterConfig CONFIG;
	public static final Logger LOGGER = LoggerFactory.getLogger("hit_counter");
	public static final String MOD_ID = "hit_counter";

	@Override
	public void onInitialize() {
		try {
			AutoConfig.register(HitCounterConfig.class, GsonConfigSerializer::new);
			CONFIG = AutoConfig.getConfigHolder(HitCounterConfig.class).getConfig();
			LOGGER.info("Hit Counter initialized with config!");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize Hit Counter config", e);
			CONFIG = new HitCounterConfig();
		}
	}
}