package net.just_s.hudmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/util/math/MatrixStack;)V"))
    public void onDraw(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        renderInventory(matrices, tickDelta);
    }

    private void renderInventory(MatrixStack matrix, float float_1) {
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity != null) {

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            this.client.getTextureManager().bindTexture(INVENTORY_TEX);
            int width = 16;
            int height = 16;

            // Smaller screen resolution
            Arm hand = playerEntity.getMainArm();
            int hotbarWidth = 182 + (hand == Arm.LEFT ? 29 : 0) * 2;
            if (this.scaledWidth < hotbarWidth + (width) * 2) {
                width /= 2;
                height /= 2;
            }

            int x = this.scaledWidth - width;
            int y = this.scaledHeight - height;

            int count = 0;
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 9; ++j) {
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

            ItemStack stack = new ItemStack(Item.byRawId(686), count);
            //stack.setCount(Item.getRawId(playerEntity.getInventory().main.get(0).getItem()));
            this.renderHotbarItem(x, y, float_1, playerEntity, stack, 1);
        }
    }

}
