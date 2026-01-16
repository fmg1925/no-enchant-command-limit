package com.fmg1925.noenchantcommandlimit;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoEnchantCommandLimit implements ModInitializer {
	public static final String MOD_ID = "no-enchant-command-limit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("no enchant command limit loaded");
	}
}