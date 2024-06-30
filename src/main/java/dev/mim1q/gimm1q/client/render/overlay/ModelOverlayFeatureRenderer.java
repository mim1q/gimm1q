package dev.mim1q.gimm1q.client.render.overlay;

import dev.mim1q.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
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
 * A ready-to-use feature renderer that displays an overlay on top of the original entity model
 *
 * @param <E> Entity type
 * @param <M> Entity model type
 * @see ModelOverlayVertexConsumer
 */
public class ModelOverlayFeatureRenderer<E extends LivingEntity, M extends EntityModel<E>> extends FeatureRenderer<E, M> {
    private final BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker;
    private final Predicate<E> predicate;
    private final boolean affectsFeatures;

    public ModelOverlayFeatureRenderer(
        FeatureRendererContext<E, M> context,
        Predicate<E> predicate,
        BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        super(context);
        this.predicate = predicate;
        this.vertexConsumerPicker = vertexConsumerPicker;
        this.affectsFeatures = affectsFeatures;
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
        if (!predicate.test(entity)) {
            if (affectsFeatures) {
                ((EntityModelVertexConsumerOverrideAccessor) getContextModel()).gimm1q$setConsumerOverride(null, null);
            }
            return;
        }

        var consumer = vertexConsumerPicker.apply(entity, vertexConsumers);

        if (affectsFeatures) {
            //noinspection unchecked
            ((EntityModelVertexConsumerOverrideAccessor) getContextModel()).gimm1q$setConsumerOverride(
                (BiFunction<LivingEntity, VertexConsumerProvider, ModelOverlayVertexConsumer>) vertexConsumerPicker,
                entity
            );
        }

        if (consumer == null) return;
        getContextModel().render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<FeatureRendererContext<E, M>, FeatureRenderer<E, M>> of(
        Predicate<E> predicate,
        Function<VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, (e, v) -> vertexConsumerPicker.apply(v), affectsFeatures);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<FeatureRendererContext<E, M>, FeatureRenderer<E, M>> of(
        Predicate<E> predicate,
        BiFunction<E, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, vertexConsumerPicker, affectsFeatures);
    }
}
