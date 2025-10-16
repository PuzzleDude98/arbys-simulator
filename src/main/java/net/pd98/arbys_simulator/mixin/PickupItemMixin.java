package net.pd98.arbys_simulator.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class PickupItemMixin extends Entity {

	public PickupItemMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow public abstract ItemStack getStack();
	@Shadow private int pickupDelay;
	@Shadow @Nullable private UUID owner;

	@Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
	private void init(PlayerEntity player, CallbackInfo ci) {
		if (!this.getEntityWorld().isClient()) {
			ItemStack itemStack = this.getStack();
			Item item = itemStack.getItem();
			int i = itemStack.getCount();
			if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUuid()))) {

				///  INJECTED
				if (!itemStack.contains(InstabilityManager.INSTABILITY)) {
					for (int j = 0; j < player.getInventory().size(); j++) {
						ItemStack invStack = player.getInventory().getStack(j);
						if (invStack.getItem() == item && invStack.contains(InstabilityManager.INSTABILITY)) {
							int instability = invStack.get(InstabilityManager.INSTABILITY);
							ItemStack testStack = itemStack.copy();
							testStack.set(InstabilityManager.INSTABILITY, instability);
							testStack.set(DataComponentTypes.LORE, invStack.get(DataComponentTypes.LORE));
							if (ItemStack.areItemsAndComponentsEqual(invStack.copyWithCount(1), testStack.copyWithCount(1))) {
								itemStack = testStack;
							}
							break;
						}
					}
				}
				///

				if (player.getInventory().insertStack(itemStack)) {
					player.sendPickup((ItemEntity) (Object) this, i);
					if (itemStack.isEmpty()) {
						this.discard();
						itemStack.setCount(i);
					}

					player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), i);
					player.triggerItemPickedUpByEntityCriteria((ItemEntity) (Object) this);
				}
			}

		}
		ci.cancel();
	}
}