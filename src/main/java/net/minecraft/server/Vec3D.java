package net.minecraft.server;

public class Vec3D
{
  private static final ThreadLocal d = new Vec3DPoolThreadLocal();
  public double a;
  public double b;
  public double c;

  public static Vec3D a(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    return new Vec3D(paramDouble1, paramDouble2, paramDouble3);
  }

  public static Vec3DPool a() {
    return (Vec3DPool)d.get();
  }

  protected Vec3D(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    if (paramDouble1 == -0.0D) paramDouble1 = 0.0D;
    if (paramDouble2 == -0.0D) paramDouble2 = 0.0D;
    if (paramDouble3 == -0.0D) paramDouble3 = 0.0D;
    this.a = paramDouble1;
    this.b = paramDouble2;
    this.c = paramDouble3;
  }

  protected Vec3D b(double paramDouble1, double paramDouble2, double paramDouble3) {
    this.a = paramDouble1;
    this.b = paramDouble2;
    this.c = paramDouble3;
    return this;
  }

  public Vec3D b()
  {
    double d1 = MathHelper.sqrt(this.a * this.a + this.b * this.b + this.c * this.c);
    if (d1 < 0.0001D) return a().create(0.0D, 0.0D, 0.0D);
    return a().create(this.a / d1, this.b / d1, this.c / d1);
  }

  public double b(Vec3D paramVec3D) {
    return this.a * paramVec3D.a + this.b * paramVec3D.b + this.c * paramVec3D.c;
  }

  public Vec3D add(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    return a().create(this.a + paramDouble1, this.b + paramDouble2, this.c + paramDouble3);
  }

  public double d(Vec3D paramVec3D) {
    double d1 = paramVec3D.a - this.a;
    double d2 = paramVec3D.b - this.b;
    double d3 = paramVec3D.c - this.c;
    return MathHelper.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
  }

  public double distanceSquared(Vec3D paramVec3D) {
    double d1 = paramVec3D.a - this.a;
    double d2 = paramVec3D.b - this.b;
    double d3 = paramVec3D.c - this.c;
    return d1 * d1 + d2 * d2 + d3 * d3;
  }

  public double d(double paramDouble1, double paramDouble2, double paramDouble3) {
    double d1 = paramDouble1 - this.a;
    double d2 = paramDouble2 - this.b;
    double d3 = paramDouble3 - this.c;
    return d1 * d1 + d2 * d2 + d3 * d3;
  }

  public double c()
  {
    return MathHelper.sqrt(this.a * this.a + this.b * this.b + this.c * this.c);
  }

  public Vec3D b(Vec3D paramVec3D, double paramDouble) {
    double d1 = paramVec3D.a - this.a;
    double d2 = paramVec3D.b - this.b;
    double d3 = paramVec3D.c - this.c;

    if (d1 * d1 < 1.000000011686097E-07D) return null;

    double d4 = (paramDouble - this.a) / d1;
    if ((d4 < 0.0D) || (d4 > 1.0D)) return null;
    return a().create(this.a + d1 * d4, this.b + d2 * d4, this.c + d3 * d4);
  }

  public Vec3D c(Vec3D paramVec3D, double paramDouble) {
    double d1 = paramVec3D.a - this.a;
    double d2 = paramVec3D.b - this.b;
    double d3 = paramVec3D.c - this.c;

    if (d2 * d2 < 1.000000011686097E-07D) return null;

    double d4 = (paramDouble - this.b) / d2;
    if ((d4 < 0.0D) || (d4 > 1.0D)) return null;
    return a().create(this.a + d1 * d4, this.b + d2 * d4, this.c + d3 * d4);
  }

  public Vec3D d(Vec3D paramVec3D, double paramDouble) {
    double d1 = paramVec3D.a - this.a;
    double d2 = paramVec3D.b - this.b;
    double d3 = paramVec3D.c - this.c;

    if (d3 * d3 < 1.000000011686097E-07D) return null;

    double d4 = (paramDouble - this.c) / d3;
    if ((d4 < 0.0D) || (d4 > 1.0D)) return null;
    return a().create(this.a + d1 * d4, this.b + d2 * d4, this.c + d3 * d4);
  }

  public String toString() {
    return "(" + this.a + ", " + this.b + ", " + this.c + ")";
  }

  public void a(float paramFloat)
  {
    float f1 = MathHelper.cos(paramFloat);
    float f2 = MathHelper.sin(paramFloat);

    double d1 = this.a;
    double d2 = this.b * f1 + this.c * f2;
    double d3 = this.c * f1 - this.b * f2;

    this.a = d1;
    this.b = d2;
    this.c = d3;
  }

  public void b(float paramFloat) {
    float f1 = MathHelper.cos(paramFloat);
    float f2 = MathHelper.sin(paramFloat);

    double d1 = this.a * f1 + this.c * f2;
    double d2 = this.b;
    double d3 = this.c * f1 - this.a * f2;

    this.a = d1;
    this.b = d2;
    this.c = d3;
  }
}