import net.fastersprint.AttackCadence;
public final class AttackCadenceTest {
 public static void main(String[] args) {
  var c=new AttackCadence();
  check(!c.reserve(0,.9f,5),"partially charged swing refused");
  check(c.reserve(0,1,5),"charged swing accepted");
  check(!c.reserve(0,1,5)&&!c.reserve(4,1,5),"no duplicate/burst");
  check(c.reserve(5,1,5),"next empty-hand cooldown");
  c=new AttackCadence();int hits=0;for(int t=0;t<100;t++)if(c.reserve(t,1,12.5f))hits++;
  check(hits==8,"fractional sword cooldown rounds up to full ticks");
  check(!c.reserve(200,Float.NaN,5),"invalid strength refused");
  c=new AttackCadence();check(c.reserve(0,1,Float.NaN)&&!c.reserve(19,1,5),"invalid delay has bounded fallback");
  System.out.println("PASS: 7 attack cadence checks");
 }
 static void check(boolean x,String name){if(!x)throw new AssertionError(name);}
}
