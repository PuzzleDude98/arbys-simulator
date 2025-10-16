package net.pd98.arbys_simulator.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import net.pd98.arbys_simulator.ArbysSimulator;
import net.pd98.arbys_simulator.InstabilityManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerInventory.class)
public abstract class PickupItemMixin {

	@Inject(method = "insertStack*", at = @At("HEAD"))
	private void init(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		PlayerInventory self = (PlayerInventory) (Object) this;

		if (!stack.contains(InstabilityManager.INSTABILITY)) {
			for (int j = 0; j < self.size(); j++) {
				ItemStack invStack = self.getStack(j);
				if (invStack.getItem() == stack.getItem() && invStack.contains(InstabilityManager.INSTABILITY)) {
					int instability = invStack.get(InstabilityManager.INSTABILITY);
					ItemStack testStack = stack.copy();
					testStack.set(InstabilityManager.INSTABILITY, instability);
					testStack.set(DataComponentTypes.LORE, invStack.get(DataComponentTypes.LORE));
					if (ItemStack.areItemsAndComponentsEqual(invStack.copyWithCount(1), testStack.copyWithCount(1))) {
						stack.set(InstabilityManager.INSTABILITY, instability);
						stack.set(DataComponentTypes.LORE, invStack.get(DataComponentTypes.LORE));
					}
					break;
				}
			}
		}
	}
}