package net.fastersprint;

/** Arms only on a walking departure, never on the descending half of a jump. */
final class QuickFallState {
    private boolean grounded,moved,dropping;
    private int climbGrace;
    void clear(){grounded=false;moved=false;dropping=false;climbGrace=0;}
    boolean update(boolean onGround,boolean moving,boolean jumping,boolean sneaking,boolean climbing,double velocityY){
        if(onGround){grounded=true;moved=moving;dropping=false;climbGrace=0;return false;}
        if(jumping||sneaking||velocityY>0){clear();return false;}
        if(climbing){dropping=false;if(++climbGrace>2)clear();return false;}
        if(grounded&&(moved||moving))dropping=true;
        grounded=false;moved=false;climbGrace=0;return dropping;
    }
}
