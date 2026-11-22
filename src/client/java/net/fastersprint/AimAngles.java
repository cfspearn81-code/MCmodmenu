package net.fastersprint;

/** Minecraft look angles, choosing the equivalent yaw nearest the current view. */
public final class AimAngles {
    private AimAngles() { }
    public record Look(float yaw,float pitch) { }
    public static Look toward(double dx,double dy,double dz,float currentYaw){
        double horizontal=Math.hypot(dx,dz);
        float desired=horizontal<1e-9?currentYaw:(float)(Math.toDegrees(Math.atan2(dz,dx))-90);
        float difference=(desired-currentYaw)%360;
        if(difference>=180)difference-=360;if(difference< -180)difference+=360;
        return new Look(currentYaw+difference,(float)Math.clamp(-Math.toDegrees(Math.atan2(dy,horizontal)),-90,90));
    }
}
