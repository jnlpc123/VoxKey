package app.codex.f5voice;
import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;

import android.view.*;
/** Aerial-style solid dock; measured as one panel with protected bottom spacing. */
final class KeyboardPanel extends View {
 interface Action{void perform(int index);}
 static final String[] LABELS={"删除","空格","光标左移","光标右移","清空输入","搜索 / 确认","本地 / 云端","设置","切换输入法","收起键盘"};
 final ToolbarNavigation navigation=new ToolbarNavigation();
 private final TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
 private final Action action;
 private final RectF[] cells=new RectF[10];
 private final RectF dock=new RectF(),iconBounds=new RectF();
 private final Path iconPath=new Path();
 private String status="",mode="本地离线";
 KeyboardPanel(Context c,Action action){super(c);this.action=action;setFocusable(true);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));setContentDescription("VoxKey工具栏");for(int i=0;i<10;i++)cells[i]=new RectF();}
 private float dp(float x){return x*getResources().getDisplayMetrics().density;}
 private float sp(float x){return x*getResources().getDisplayMetrics().scaledDensity;}
 void status(String text,boolean cloud){status=text;mode=cloud?"百炼云端":"本地离线";setContentDescription(mode+"，"+status+"，当前按钮："+LABELS[navigation.selected]);invalidate();}
 void move(int dx,int dy){navigation.move(dx,dy);status(status,mode.equals("百炼云端"));}
 void click(){action.perform(navigation.selected);}
 @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),resolveSize((int)Math.min(dp(164),getResources().getDisplayMetrics().heightPixels*.32f),h));}
 private void text(Canvas c,String value,float size,int color,float x,float y){paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setTextSize(sp(size));c.drawText(value,x,y,paint);}
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);c.drawColor(TvStyle.BG);paint.setStyle(Paint.Style.FILL);
  float left=Math.max(dp(26),getWidth()*.035f),right=getWidth()-left,bottom=dp(14);WindowInsets insets=getRootWindowInsets();if(insets!=null)bottom=Math.max(bottom,insets.getStableInsetBottom()+dp(8));
  
  
  dock.set(left-dp(10),dp(4),right+dp(10),getHeight()-bottom+dp(6));paint.setColor(0xFF24344C);c.drawRoundRect(dock,dp(25),dp(25),paint);
  float top=dp(12),gap=dp(8),cellH=(getHeight()-bottom-top-gap)/2;
  for(int i=0;i<10;i++){
   int row=i/5,col=i%5;float cellW=(right-left-4*gap)/5;
   RectF r=cells[i];float x=left+col*(cellW+gap),y=top+row*(cellH+gap);r.set(x,y,x+cellW,y+cellH);boolean focused=i==navigation.selected;
   paint.setStyle(Paint.Style.FILL);paint.setColor(focused?0xFFF3F4F7:0xFF35435A);c.drawRoundRect(r,cellH/2,cellH/2,paint);
   icon(c,i,r.centerX(),r.centerY()-dp(10),dp(9),focused);
   String label=i==6?(mode.equals("百炼云端")?"切到本地":"切到云端"):LABELS[i];paint.setTextSize(sp(15));float tw=paint.measureText(label);text(c,label,15,focused?TvStyle.INK:Color.WHITE,r.centerX()-tw/2,r.centerY()+dp(18));
  }
  
 }
 private void icon(Canvas c,int index,float x,float y,float s,boolean focused){
  paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1.7f));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setColor(focused?TvStyle.INK:0xF2FFFFFF);Path p=iconPath;p.rewind();
  if(index==0){p.moveTo(x-s,y-s*.7f);p.lineTo(x+s,y-s*.7f);p.lineTo(x+s,y+s*.7f);p.lineTo(x-s,y+s*.7f);p.lineTo(x-s*1.6f,y);p.close();c.drawPath(p,paint);c.drawLine(x-s*.2f,y-s*.3f,x+s*.4f,y+s*.3f,paint);c.drawLine(x-s*.2f,y+s*.3f,x+s*.4f,y-s*.3f,paint);}
  else if(index==1){p.moveTo(x-s,y-s*.4f);p.lineTo(x-s,y+s*.5f);p.lineTo(x+s,y+s*.5f);p.lineTo(x+s,y-s*.4f);c.drawPath(p,paint);}
  else if(index==2||index==3){float direction=index==2?-1:1;c.drawLine(x-s,y,x+s,y,paint);c.drawLine(x+direction*s,y,x+direction*s*.2f,y-s*.7f,paint);c.drawLine(x+direction*s,y,x+direction*s*.2f,y+s*.7f,paint);}
  else if(index==4){iconBounds.set(x-s*.65f,y-s*.5f,x+s*.65f,y+s);c.drawRoundRect(iconBounds,dp(2),dp(2),paint);c.drawLine(x-s,y-s*.7f,x+s,y-s*.7f,paint);c.drawLine(x-s*.3f,y-s,x+s*.3f,y-s,paint);}
  else if(index==5){c.drawCircle(x-s*.2f,y-s*.2f,s*.75f,paint);c.drawLine(x+s*.35f,y+s*.35f,x+s*1.1f,y+s*1.1f,paint);}
  else if(index==6){p.moveTo(x-s,y+s*.5f);p.cubicTo(x-s*1.8f,y-s*.2f,x-s*.4f,y-s*1.3f,x+s*.15f,y-s*.5f);p.cubicTo(x+s*.8f,y-s*.9f,x+s*1.7f,y+s*.4f,x+s,y+s*.5f);p.close();c.drawPath(p,paint);}
  else if(index==7){c.drawCircle(x,y,s*.65f,paint);for(int j=0;j<8;j++){double a=j*Math.PI/4;c.drawLine(x+(float)Math.cos(a)*s*.9f,y+(float)Math.sin(a)*s*.9f,x+(float)Math.cos(a)*s*1.2f,y+(float)Math.sin(a)*s*1.2f,paint);}}
  if(index==8){iconBounds.set(x-s*1.4f,y-s*.8f,x+s*1.4f,y+s*.8f);c.drawRoundRect(iconBounds,dp(2),dp(2),paint);for(int row=0;row<2;row++)for(int col=0;col<4;col++)c.drawPoint(x-s*.9f+col*s*.6f,y-s*.4f+row*s*.45f,paint);c.drawLine(x-s*.6f,y+s*.5f,x+s*.6f,y+s*.5f,paint);}
  if(index==9){c.drawLine(x-s,y-s*.4f,x,y+s*.5f,paint);c.drawLine(x,y+s*.5f,x+s,y-s*.4f,paint);}
  paint.setStyle(Paint.Style.FILL);paint.setStrokeCap(Paint.Cap.BUTT);
 }
 @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){for(int i=0;i<10;i++)if(cells[i].contains(e.getX(),e.getY())){navigation.selected=i;click();invalidate();return true;}}return true;}
 @Override public boolean performClick(){super.performClick();click();return true;}
}



