package net.fastersprint;
public class QuickFallStateTest {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var s=new QuickFallState();check(!s.update(true,true,false,false,false,0),"must stay grounded");
  check(s.update(false,true,false,false,false,-.08),"walk-off did not activate");check(s.update(false,false,false,false,false,-8),"active fall stopped");check(!s.update(true,false,false,false,false,0),"landing did not stop");
  check(!s.update(false,false,false,false,false,-.08),"stationary support removal activated");
  s.update(true,true,false,false,false,0);check(!s.update(false,true,true,false,false,.4),"jump accelerated");check(!s.update(false,true,false,false,false,-.1),"jump descent accelerated");
  s.update(true,true,false,false,false,0);check(!s.update(false,true,false,false,true,-.08),"climb contact accelerated");check(s.update(false,true,false,false,false,-.2),"brief ledge contact lost walk-off");
  s.clear();s.update(true,true,false,false,false,0);for(int i=0;i<4;i++)check(!s.update(false,true,false,false,true,-.1),"climb accelerated");check(!s.update(false,true,false,false,false,-.2),"leaving ladder accelerated");
  s.update(true,true,false,false,false,0);check(!s.update(false,true,false,true,false,-.1),"sneak accelerated");
  s.update(true,true,false,false,false,0);s.clear();check(!s.update(false,true,false,false,false,-.1),"correction retained armed state");
  s.update(true,true,false,false,false,0);s.update(false,true,false,false,false,-.1);check(!s.update(false,true,false,false,false,1),"bounce/knockback overridden");
  System.out.println("PASS QuickFall walking departure, landing, jump, ladder grace, sneak, correction and bounce state");
 }
}
