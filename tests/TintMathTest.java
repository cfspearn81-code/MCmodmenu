package net.fastersprint;
public class TintMathTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  check(ColorMath.rgb(0,1,1)==0xFFFF0000,"red");check(ColorMath.rgb(1F/3,1,1)==0xFF00FF00,"green");check(ColorMath.rgb(2F/3,1,1)==0xFF0000FF,"blue");check(ColorMath.rgb(1,1,1)==0xFFFF0000,"hue seam");
  for(int h=0;h<=360;h++){check(ColorMath.rgb(h/360F,0,1)==0xFFFFFFFF,"white");check(ColorMath.rgb(h/360F,1,0)==0xFF000000,"black");}
  check(ColorMath.rgb(.5F,1,.5F)==0xFF008080,"brightness");
  System.out.println("PASS sky hue primaries/seam, grayscale and brightness");
 }
}
