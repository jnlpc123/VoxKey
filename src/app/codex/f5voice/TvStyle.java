package app.codex.f5voice;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.view.View;
import android.widget.*;
final class TvStyle {
 static final int BG=0xFF19283F,TEXT=0xFFF5F6FA,MUTED=0xFFB1BDCF,ACCENT=Color.WHITE,INK=0xFF1B2332;
 static int dp(Context c,int x){return (int)(x*c.getResources().getDisplayMetrics().density+.5f);}
 static GradientDrawable shape(Context c,int color,boolean focused){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,22));return d;}
 static GradientDrawable pill(Context c,int color){GradientDrawable d=shape(c,color,false);d.setCornerRadius(dp(c,999));return d;}
 static ColorStateList ink(int normal,int focused){return new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{focused,focused,normal});}
 static StateListDrawable control(Context c){StateListDrawable d=new StateListDrawable();d.addState(new int[]{android.R.attr.state_focused},pill(c,0xFFF3F4F7));d.addState(new int[]{android.R.attr.state_pressed},pill(c,0xFFF3F4F7));d.addState(new int[]{},pill(c,0xFF35435A));return d;}
 static View scene(Context c,View content){FrameLayout frame=new FrameLayout(c);frame.setBackgroundColor(BG);frame.addView(content,new FrameLayout.LayoutParams(-1,-1));return frame;}
 static TextView text(Context c,String value,int size,int color){TextView t=new TextView(c);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));t.setPadding(0,dp(c,5),0,dp(c,5));return t;}
 static Button button(Context c,String label,Runnable action){Button b=new Button(c);b.setText(label);b.setTextSize(17);b.setTextColor(ink(TEXT,INK));b.setAllCaps(false);b.setBackground(control(c));b.setElevation(0);b.setPadding(dp(c,20),dp(c,8),dp(c,20),dp(c,8));b.setMinHeight(dp(c,48));b.setOnClickListener(v->action.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(c,54));p.setMargins(0,dp(c,5),0,dp(c,5));b.setLayoutParams(p);return b;}
 static EditText edit(Context c,String hint,boolean secret){EditText e=new EditText(c);e.setTextSize(19);e.setTextColor(ink(TEXT,INK));e.setHintTextColor(ink(MUTED,0xFF68758A));e.setHint(hint);e.setSingleLine(true);e.setBackground(control(c));e.setPadding(dp(c,22),dp(c,12),dp(c,22),dp(c,12));e.setInputType(secret?android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD:android.text.InputType.TYPE_CLASS_TEXT);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);return e;}
}
