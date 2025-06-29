package dev.mim1q.gimm1q.particle.electric;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ElectricityParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private final Vec3d direction;
    private final float pitch;
    private final float yaw;
    private final float roll;
    private final int length;
    private final int color;
    private final float size;
    private final boolean isMainBranch;

    private ElectricityParticle(
        SpriteProvider spriteProvider,
        ClientWorld clientWorld,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ,
        Vec3d direction,
        int length,
        int color,
        float size,
        boolean isMainBranch
    ) {
        super(clientWorld, x, y, z);
        this.spriteProvider = spriteProvider;
        this.direction = direction.normalize();
        this.length = length;
        this.color = color;
        this.maxAge = 5 + length;
        this.size = size;
        this.isMainBranch = isMainBranch;

        this.yaw = (float) Math.asin(-this.direction.y);
        // Math checks out but IDEA throws a naming warning ;P
        // noinspection SuspiciousNameCombination
        this.pitch = (float) Math.atan2(this.direction.x, this.direction.z);
        this.roll = clientWorld.random.nextFloat() * 2 * MathHelper.PI;

        this.setVelocity(velocityX, velocityY, velocityZ);
        this.setSprite(spriteProvider);

        if (length > 0) {
            for (int i = 0; i < 3; i++) {
                if (world.random.nextFloat() < 0.7f) this.addSideBranch();
            }
            this.addNextMainBranch();
        }
    }

    @Override
    public void tick() {
        this.setPos(this.x + this.velocityX, this.y + this.velocityY, this.z + this.velocityZ);
        if (age % 2 == 0) {
            this.setSprite(spriteProvider);
        }

        if (this.age++ >= this.maxAge) {
            this.markDead();
        }
    }

    private void addSideBranch() {
        final var delta = world.random.nextFloat();
        final var newPos = new Vec3d(this.x, this.y, this.z).add(this.direction.multiply(delta * size));
        final var randomDirection = this.direction.addRandom(world.random, 2.0f);
        final var randomLength = Math.min(this.length - 1, world.random.nextInt(3));

        final var particle = new ElectricityParticleEffect(randomDirection, randomLength, this.color, this.size * 0.5f, false);

        world.addParticle(
            particle,
            newPos.x,
            newPos.y,
            newPos.z,
            velocityX,
            velocityY,
            velocityZ
        );
    }

    private void addNextMainBranch() {
        final var newDirection = this.direction.addRandom(world.getRandom(), isMainBranch ? 0.1f : 2.0f);

        final var newPos = new Vec3d(this.x, this.y, this.z).add(this.direction.multiply(size));


        world.addParticle(
            new ElectricityParticleEffect(newDirection, this.length - 1, this.color, this.size, this.isMainBranch),
            newPos.x,
            newPos.y,
            newPos.z,
            velocityX,
            velocityY,
            velocityZ
        );
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        final var lifetimeDelta = (this.age + tickDelta) / this.maxAge;
        var alpha = 255;

        if (lifetimeDelta < 0.2) {
            alpha = (int) (lifetimeDelta / 0.2 * 255);
        } else if (lifetimeDelta > 0.8) {
            alpha = (int) ((1 - lifetimeDelta) / 0.2 * 255);
        }

        alpha = MathHelper.clamp(alpha, 0, 255);

        final var vector3fs = getParticleQuad(camera, tickDelta);

        float k = this.getMinU();
        float l = this.getMaxU();
        float m = this.getMinV();
        float n = this.getMaxV();

        final var u = new float[]{l, l, k, k};
        final var v = new float[]{n, m, m, n};

        final int o = 0xF000F0;

        final var r = this.color >> 16 & 0xFF;
        final var g = this.color >> 8 & 0xFF;
        final var b = this.color & 0xFF;

        for (int i = 0; i <= 3; ++i) {
            vertexConsumer.vertex(vector3fs[i].x(), vector3fs[i].y(), vector3fs[i].z()).texture(u[i], v[i]).color(r, g, b, alpha).light(o);
        }
        for (int i = 3; i >= 0; --i) {
            vertexConsumer.vertex(vector3fs[i].x(), vector3fs[i].y(), vector3fs[i].z()).texture(u[i], v[i]).color(r, g, b, alpha).light(o);
        }

    }

    private Vector3f[] getParticleQuad(Camera camera, float tickDelta) {
        final var px = (float) (MathHelper.lerp(tickDelta, prevPosX, x) - camera.getPos().x);
        final var py = (float) (MathHelper.lerp(tickDelta, prevPosY, y) - camera.getPos().y);
        final var pz = (float) (MathHelper.lerp(tickDelta, prevPosZ, z) - camera.getPos().z);

        final var quaternionf = new Quaternionf();
        quaternionf.rotateY(pitch);
        quaternionf.rotateX(yaw);
        quaternionf.rotateZ(roll);

        final var quad = new Vector3f[]{
            new Vector3f(-0.5f, 0f, 0.0f),
            new Vector3f(-0.5f, 0f, 1.0f),
            new Vector3f(0.5f, 0f, 1.0f),
            new Vector3f(0.5f, 0f, 0.0f)
        };

        for (int j = 0; j < 4; ++j) {
            final var vector3f = quad[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(size);
            vector3f.add(px, py, pz);
        }

        return quad;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(
        SpriteProvider spriteProvider
    ) implements ParticleFactory<ElectricityParticleEffect> {
        @Override
        public Particle createParticle(
            ElectricityParticleEffect parameters,
            ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ
        ) {
            return new ElectricityParticle(
                spriteProvider,
                world,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                parameters.direction(),
                parameters.length(),
                parameters.color(),
                parameters.size(),
                parameters.isMainBranch()
            );
        }
    }
}
