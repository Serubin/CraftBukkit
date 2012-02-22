package net.minecraft.server;


public class EntityDragonFireball extends EntitySmallFireball {

    public EntityDragonFireball(World world) {
        super(world);
        this.b(1F, 1F);
    }

    public EntityDragonFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
        this.b(0.3125F, 0.3125F);
        
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        this.motX = d0 / d3 * 1;
        this.motY = d1 / d3 * 1;
        this.motZ = d2 / d3 * 1;
    }
}
