package net.tsukers.wisteria;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wisteria implements ModInitializer {
	public static final String MOD_ID = "wisteria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello from Tsukers!");
	}
}