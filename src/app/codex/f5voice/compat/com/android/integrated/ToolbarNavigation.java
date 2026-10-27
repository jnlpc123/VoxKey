package app.codex.f5voice.compat.com.android.integrated;
final class ToolbarNavigation {
 int selected=0;
 void move(int dx,int dy){int row=selected/5,col=selected%5;row=Math.max(0,Math.min(1,row+dy));col=Math.max(0,Math.min(4,col+dx));selected=row*5+col;}
 boolean repeatable(){return selected==0;}
}
