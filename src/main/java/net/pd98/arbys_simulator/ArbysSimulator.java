package net.pd98.arbys_simulator;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArbysSimulator implements ModInitializer{
	public static final String MOD_ID = "arbys-simulator";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		InstabilityManager.intialize();

		InstabilityManager.tickInventory();
	}

	public static int getEquipmentSlotIndex(EquipmentSlot slot, PlayerEntity player){
		return switch (slot) {
			case HEAD  -> 39;
			case CHEST -> 38;
			case LEGS  -> 37;
			case FEET  -> 36;
			case MAINHAND -> {
				LOGGER.info("MAINHAND!");
				LOGGER.info(String.valueOf(player.getInventory().getSelectedSlot()));
				yield player.getInventory().getSelectedSlot();
			}
			case OFFHAND -> 40;
            default -> {
                LOGGER.error("Non-player equipment slot attempted to convert to index!");
				yield -1;
            }
        };
	}

}