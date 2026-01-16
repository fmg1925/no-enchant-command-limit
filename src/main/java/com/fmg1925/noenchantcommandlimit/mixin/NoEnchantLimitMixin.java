package com.fmg1925.noenchantcommandlimit.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class NoEnchantLimitMixin {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.enchant.failed"));

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<Enchantment> enchantment, int level, CallbackInfoReturnable<Integer> ci) throws CommandSyntaxException {
        ci.cancel();
        if(level > 255) {
            level = 255;
            source.sendFeedback(() -> Text.of("Enchantment was leveled down to 255 as minecraft doesn't allow greater values"), false);
        }
        int i = 0;

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack itemStack = livingEntity.getMainHandStack();

                if (!itemStack.isEmpty()) {
                    ItemEnchantmentsComponent original = itemStack.getEnchantments();

                    if (original.getLevel(enchantment) <= 0 && level == 0) {
                        source.sendFeedback(() -> Text.of("Nothing was removed as the enchantment is not present"), false);
                        return;
                    }

                    ItemEnchantmentsComponent.Builder builder =
                            new ItemEnchantmentsComponent.Builder(original);

                    builder.remove(entry -> entry.equals(enchantment));

                    itemStack.set(
                            DataComponentTypes.ENCHANTMENTS,
                            builder.build()
                    );

                    if (level == 0) {
                        source.sendFeedback(() -> Text.literal("bruh (removed)"), false);
                        ci.setReturnValue(0);
                        return;
                    }

                    itemStack.addEnchantment(enchantment, level);
                    ++i;
                }
            }
        }

        if (i == 0) {
            throw FAILED_EXCEPTION.create();
        } else {
            int finalLevel = level;
            if (targets.size() == 1) {
                source.sendFeedback(() -> Text.translatable("commands.enchant.success.single", new Object[]{Enchantment.getName(enchantment, finalLevel), ((Entity) targets.iterator().next()).getDisplayName()}), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.enchant.success.multiple", new Object[]{Enchantment.getName(enchantment, finalLevel), targets.size()}), true);
            }
        }
        ci.setReturnValue(i);
    }
}
