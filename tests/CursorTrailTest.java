import net.fastersprint.CursorTrail;
public final class CursorTrailTest {
 public static void main(String[] args) {
  var trail=new CursorTrail();trail.update(0,0,0);
  check(trail.snapshot(0).isEmpty(),"opening has no stale tail");
  trail.update(10,0,10_000_000);
  var points=trail.snapshot(10_000_000);
  check(points.size()==5,"movement interpolated densely");
  check(points.getLast().x()==10 && points.getLast().y()==0,"tail follows cursor");
  check(points.getFirst().remaining(10_000_000)>points.getFirst().remaining(60_000_000),"time-based fading");
  int count=points.size();trail.update(10,0,20_000_000);
  check(trail.snapshot(20_000_000).size()==count,"stationary cursor does not accumulate");
  check(trail.snapshot(200_000_000).isEmpty(),"stationary trail expires");
  for(int i=0;i<200;i++)trail.update(i,0,201_000_000+i*1000L);
  check(trail.snapshot(202_000_000).size()<=96,"bounded particle count");
  trail.update(1000,1000,210_000_000);check(trail.snapshot(210_000_000).isEmpty(),"no cross-screen teleport streak");
  trail.clear();trail.update(1,1,220_000_000);check(trail.snapshot(220_000_000).isEmpty(),"reopening resets history");
  System.out.println("PASS: 9 cursor lifecycle/fade checks");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
