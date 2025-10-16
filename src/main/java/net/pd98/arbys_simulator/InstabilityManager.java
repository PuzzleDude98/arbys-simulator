package net.pd98.arbys_simulator;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

import static net.pd98.arbys_simulator.ArbysSimulator.LOGGER;

public class InstabilityManager {

    private static final double A = 4.091482860085613e-7;
    private static final double B = 0.1088444479051065;
    private static final double g_base = 0.002;
    private static final double s = 0.0002;

    public static final ComponentType<Integer> INSTABILITY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(ArbysSimulator.MOD_ID, "instability"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );
    protected static void intialize() { }

    public static void tickInventory() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            LOGGER.info(hand.getEquipmentSlot().asString());
            nukeItem(player, ArbysSimulator.getEquipmentSlotIndex(hand.getEquipmentSlot(),player));
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                Random random = player.getRandom();
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack item = player.getInventory().getStack(i);
                    try {
                        int instability = item.get(InstabilityManager.INSTABILITY);

                        double nuke_probability = A * Math.pow(Math.E,(B*instability));
                        if (random.nextDouble() <= nuke_probability) {
                            nukeItem(player, i);
                        }

                        double growth_probability = g_base + s * instability;
                        if (random.nextDouble() <= growth_probability) {
                            instability++;
                            item.set(InstabilityManager.INSTABILITY, instability);
                        }

                        setInstability(item, instability);

                    } catch (Exception ignored) {
                        item.set(InstabilityManager.INSTABILITY, 0);
                    }
                }
            }
        });
    }

    public static void nukeItem(PlayerEntity player, int slot) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Random random = world.getRandom();
        ItemStack item = player.getInventory().getStack(slot);
        if (player.getInventory().getStack(slot).isEmpty()) { return; }
        world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, item),
                player.getX(), player.getY() + 1, player.getZ(), 50, 0, 0, 0, 1);
        world.playSound(null, player.getBlockPos(), SoundEvent.of(Identifier.of(ArbysSimulator.MOD_ID,"blast")), SoundCategory.PLAYERS);
        player.getInventory().removeStack(slot);
        player.damage(world, world.getDamageSources().explosion(player, player), 1);
    }

    public static void setInstability(ItemStack stack, int instability) {
        // Get the existing lore if it exists
        LoreComponent lore = stack.get(DataComponentTypes.LORE);

        Text instability_text;
        if (instability <= 30) {
            instability_text = Text.literal("Low").formatted(Formatting.GREEN);
        } else if (instability <= 60) {
            instability_text = Text.literal("Medium").formatted(Formatting.YELLOW);
        } else {
            instability_text = Text.literal("High").formatted(Formatting.RED);
        }

        List<Text> lines = lore != null
                ? new ArrayList<>(lore.lines())
                : new ArrayList<>();

        // If the lore is empty, add a new line for instability
        if (lines.isEmpty()) {
            lines.add(Text.literal("Instability: ").append(instability_text));
        } else {
            boolean found = false;
            // Try to find an existing instability line to update
            for (int i = 0; i < lines.size(); i++) {
                String raw = lines.get(i).getString();
                if (raw.startsWith("Instability:")) {
                    lines.set(i, Text.literal("Instability: ").append(instability_text));
                    found = true;
                }

            }

            // If not found, append a new one
            if (!found) {
                lines.add(Text.literal("Instability: ").append(instability_text));
            }
        }

        // Apply the modified lore
        stack.set(DataComponentTypes.LORE, new LoreComponent(lines));
    }

}
