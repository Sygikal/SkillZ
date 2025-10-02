package net.skillz.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.skillz.access.ClientPlayerAccess;
import net.skillz.access.ClientPlayerListAccess;
import net.minecraft.entity.Entity;
import net.skillz.util.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.init.ConfigInit;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    /*@Unique
    private AbstractClientPlayerEntity abstractClientPlayerEntity;

    @Inject(method = "renderLabelIfPresent", at = @At(value = "HEAD"))
    protected void renderLabelIfPresentMixin(AbstractClientPlayerEntity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
            CallbackInfo info) {
        this.abstractClientPlayerEntity = abstractClientPlayerEntity;
    }

    @ModifyArg(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    protected Text renderLabelIfPresentMixin(Text original) {
        if (ConfigInit.CONFIG.showLevel) {
            return Team.decorateName(abstractClientPlayerEntity.getScoreboardTeam(),
                    Text.translatable("text.levelz.scoreboard", ((PlayerListAccess) abstractClientPlayerEntity).getLevel(), abstractClientPlayerEntity.getName()));
        } else {
            return original;
        }
    }*/

    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void renderLabelIfPresentMixin(PlayerEntityRenderer instance, Entity abstractClientPlayerEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, Operation<Void> original) {
        if (!((ClientPlayerAccess) abstractClientPlayerEntity).shouldRenderClientName()) {
            return;
        }
        if (ConfigInit.CLIENT.showLevel) {
            original.call(instance, abstractClientPlayerEntity, Team.decorateName(abstractClientPlayerEntity.getScoreboardTeam(),
                    TextUtil.getGui("scoreboard", ((ClientPlayerListAccess) abstractClientPlayerEntity).getLevel(), abstractClientPlayerEntity.getName())), matrixStack, vertexConsumerProvider, i);
        } else {
            original.call(instance, abstractClientPlayerEntity, text, matrixStack, vertexConsumerProvider, i);
        }
    }
}
