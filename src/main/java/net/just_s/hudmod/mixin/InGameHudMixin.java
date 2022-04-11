package net.just_s.hudmod.mixin;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;

import static net.minecraft.client.gui.widget.ClickableWidget.WIDGETS_TEXTURE;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    private static final Identifier INVENTORY_TEX = new Identifier("textures/gui/container/inventory.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;

    @Shadow
    private PlayerEntity getCameraPlayer() {
        return null; // method content ignored
    }

    @Shadow
    private void renderHotbarItem(int int_1, int int_2, float float_1, PlayerEntity playerEntity_1, ItemStack itemStack_1, int int_3) {
        // method content ignored
    }

    @Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    public void onDraw(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        renderInventory(matrices, tickDelta);
        ci.cancel();
    }

    private void renderInventory(MatrixStack matrices, float tickDelta) {
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            ItemStack itemStack = playerEntity.getOffHandStack();
            Arm arm = playerEntity.getMainArm().getOpposite();
            int i = this.scaledWidth / 2;
            int j = this.getZOffset();
            boolean k = true;
            boolean l = true;
            this.setZOffset(-90);
            this.drawTexture(matrices, i - 91, this.scaledHeight - 22, 0, 0, 182, 22);
            this.drawTexture(matrices, i - 91 - 1 + playerEntity.getInventory().selectedSlot * 20, this.scaledHeight - 22 - 1, 0, 22, 24, 22);
            if (!itemStack.isEmpty()) {
                if (arm == Arm.LEFT) {
                    this.drawTexture(matrices, i - 91 - 29, this.scaledHeight - 23, 24, 22, 29, 24);
                } else {
                    this.drawTexture(matrices, i + 91, this.scaledHeight - 23, 53, 22, 29, 24);
                }
            }

            this.setZOffset(j);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int m = 1;

            int n;
            int o;
            int p;
            for(n = 0; n < 9; ++n) {
                o = i - 90 + n * 20 + 2;
                p = this.scaledHeight - 16 - 3;
                this.renderHotbarItem(o, p, tickDelta, playerEntity, (ItemStack)playerEntity.getInventory().main.get(n), m++);
            }

            if (!itemStack.isEmpty()) {
                n = this.scaledHeight - 16 - 3;
                if (arm == Arm.LEFT) {
                    this.renderHotbarItem(i - 91 - 26, n, tickDelta, playerEntity, itemStack, m++);
                } else {
                    this.renderHotbarItem(i + 91 + 10, n, tickDelta, playerEntity, itemStack, m++);
                }
            }

            int width = 16;
            int height = 16;
            int x = this.scaledWidth - width;
            int y = 0;
            boolean hasBeneficialEffect = false;
            boolean hasVainEffect = false;
            Collection effects = this.client.player.getStatusEffects();
            Iterator iterator = Ordering.natural().reverse().sortedCopy(effects).iterator();
            while(iterator.hasNext()) {
                StatusEffectInstance statusEffectInstance = (StatusEffectInstance)iterator.next();
                StatusEffect statusEffect = statusEffectInstance.getEffectType();
                if (statusEffectInstance.shouldShowIcon()) {
                    if (statusEffect.isBeneficial()) {
                        hasBeneficialEffect = true;
                    } else {
                        hasVainEffect = true;
                    }
                }
                if (hasVainEffect && hasBeneficialEffect) {break;}
            }
            if (hasBeneficialEffect) {
                if (hasVainEffect) {y += 51;}
                else {y += 24;}
            }
            int count = 0;
            for (i = 0; i < 4; ++i) {
                for (j = 0; j < 9; ++j) {
                    ItemStack slot = playerEntity.getInventory().main.get(i * 9 + j);
                    // Diamonds
                    if (Item.getRawId(slot.getItem()) == 686) {
                        count += slot.getCount();
                    }
                    // Diamond Blocks
                    if (Item.getRawId(slot.getItem()) == 68) {
                        count += slot.getCount() * 9;
                    }
                }
            }

            this.renderHotbarItem(x, y, tickDelta, playerEntity, new ItemStack(Item.byRawId(686), count), m++);

            if (this.client.options.attackIndicator == AttackIndicator.HOTBAR) {
                float z = this.client.player.getAttackCooldownProgress(0.0F);
                if (z < 1.0F) {
                    o = this.scaledHeight - 20;
                    p = i + 91 + 6;
                    if (arm == Arm.RIGHT) {
                        p = i - 91 - 22;
                    }

                    RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
                    int q = (int)(z * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexture(matrices, p, o, 0, 94, 18, 18);
                    this.drawTexture(matrices, p, o + 18 - q, 18, 112 - q, 18, q);
                }
            }

            RenderSystem.disableBlend();
        }
    }

//    private void renderInventory(MatrixStack matrix, float float_1) {
//        PlayerEntity playerEntity = this.getCameraPlayer();
//        if (playerEntity != null) {
//
//            //RenderSystem.setShaderColor(0.0F, 1.0F, 1.0F, 1.0F);
//            //RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            //RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
//            //RenderSystem.enableBlend();
//            //RenderSystem.defaultBlendFunc();
//
//            //this.client.getTextureManager().bindTexture(INVENTORY_TEX);
//            int width = 16;
//            int height = 16;
//
//            // Smaller screen resolution
//            Arm hand = playerEntity.getMainArm();
//            int hotbarWidth = 182 + (hand == Arm.LEFT ? 29 : 0) * 2;
//            if (this.scaledWidth < hotbarWidth + (width) * 2) {
//                width /= 2;
//                height /= 2;
//            }
//
//            int x = this.scaledWidth - width;
//            int y = this.scaledHeight - height;
//
//            int count = 0;
//            for (int i = 0; i < 4; ++i) {
//                for (int j = 0; j < 9; ++j) {
//                    ItemStack slot = playerEntity.getInventory().main.get(i * 9 + j);
//                    // Diamonds
//                    if (Item.getRawId(slot.getItem()) == 686) {
//                        count += slot.getCount();
//                    }
//                    // Diamond Blocks
//                    if (Item.getRawId(slot.getItem()) == 68) {
//                        count += slot.getCount() * 9;
//                    }
//                }
//            }
//
//            ItemStack stack = new ItemStack(Item.byRawId(686), count);
//            //stack.setCount(Item.getRawId(playerEntity.getInventory().main.get(0).getItem()));
//            this.renderHotbarItem(x, 0, float_1, playerEntity, stack, 1);
//        }
//    }

}
