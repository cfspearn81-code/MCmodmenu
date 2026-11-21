import net.fastersprint.BridgeEdgeGuard;
public final class BridgeEdgeGuardTest {
    public static void main(String[] args) {
        for (int angle=0;angle<360;angle+=5) {
            double dx=2*Math.cos(Math.toRadians(angle)),dz=2*Math.sin(Math.toRadians(angle));
            double safe=BridgeEdgeGuard.fraction(2,true,t->supported(.5+dx*t,.5+dz*t));
            check(safe<1 && supported(.5+dx*safe,.5+dz*safe),"safe end at "+angle);
        }
        check(BridgeEdgeGuard.fraction(.1,true,t->supported(1.25-.1*t,.5))==1,"retreat from narrow margin");
        check(BridgeEdgeGuard.fraction(.1,true,t->supported(1.25+.1*t,.5))==0,"cannot move farther off edge");
        check(BridgeEdgeGuard.fraction(2,true,t->t<.2||t>.6)<.2,"sweep cannot skip a gap");
        check(BridgeEdgeGuard.fraction(.2,true,t->true)==1,"confirmed path unimpeded");
        check(BridgeEdgeGuard.fraction(.2,true,t->t<.5)<.5,"unconfirmed next block not trusted");
        System.out.println("PASS: 77 boundary/swept-angle checks");
    }
    static boolean supported(double x,double z){return x+.2>0&&x-.2<1&&z+.2>0&&z-.2<1;}
    static void check(boolean value,String name){if(!value)throw new AssertionError(name);}
}
