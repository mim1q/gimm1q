package dev.mim1q.gimm1q.client.render.overlay;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A feature renderer that displays an overlay on top of the original entity model
 *
 * @param <E> Entity type
 * @param <M> Entity model type
 * @see ModelOverlayVertexConsumer
 */
public class ModelOverlayFeatureRenderer<E extends LivingEntity, M extends EntityModel<E>> extends FeatureRenderer<E, M> {
    private final BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker;
    private final Predicate<E> predicate;

    public ModelOverlayFeatureRenderer(
        FeatureRendererContext<E, M> context,
        Predicate<E> predicate,
        BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker
    ) {
        super(context);
        this.predicate = predicate;
        this.vertexConsumerPicker = vertexConsumerPicker;
    }

    @Override
    public void render(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        E entity,
        float limbAngle,
        float limbDistance,
        float tickDelta,
        float animationProgress,
        float headYaw,
        float headPitch
    ) {
        if (!predicate.test(entity)) return;

        var consumer = vertexConsumerPicker.apply(entity, vertexConsumers);
        if (consumer == null) return;

        getContextModel().render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<FeatureRendererContext<E, M>, FeatureRenderer<E, M>> of(
        Predicate<E> predicate,
        Function<VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, (e, v) -> vertexConsumerPicker.apply(v));
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<FeatureRendererContext<E, M>, FeatureRenderer<E, M>> of(
        Predicate<E> predicate,
        BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, vertexConsumerPicker);
    }
}
