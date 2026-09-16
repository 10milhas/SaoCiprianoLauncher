package br.com.saocipriano.launcher;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, apps;
    TextView clock;
    final int GOLD = Color.rgb(190,150,75);

    int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density + .5f); }

    TextView text(String s, float size, int color, boolean bold){
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTypeface(android.graphics.Typeface.DEFAULT, bold ? 1 : 0);
        t.setPadding(dp(8),dp(4),dp(8),dp(4));
        return t;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(8,9,13));
        getWindow().setNavigationBarColor(Color.rgb(5,6,9));
        build();
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
    }

    void build(){
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(10),dp(12),dp(8));
        root.setBackgroundResource(br.com.saocipriano.launcher.R.drawable.bg);

        LinearLayout.LayoutParams top = new LinearLayout.LayoutParams(-1,0,1);
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        clock = text("--:--", 42, Color.WHITE, true);
        clock.setGravity(Gravity.CENTER);
        content.addView(clock, new LinearLayout.LayoutParams(-1,dp(75)));

        TextView subtitle = text("SÃO CIPRIANO  •  PROTEÇÃO • PROSPERIDADE • SABEDORIA", 11, GOLD, true);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle, new LinearLayout.LayoutParams(-1,dp(40)));

        TextView symbols = text("✦   ☽   ⚿   ✧   ☉   ✦", 26, GOLD, false);
        symbols.setGravity(Gravity.CENTER);
        content.addView(symbols, new LinearLayout.LayoutParams(-1,dp(52)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"PROTEÇÃO","PROSPERIDADE","SAÚDE","SABEDORIA"};
        for(String s:labels){
            TextView x=text(s,9,Color.WHITE,true); x.setGravity(Gravity.CENTER);
            x.setBackgroundResource(R.drawable.button);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(52),1);
            p.setMargins(dp(3),0,dp(3),dp(8)); actions.addView(x,p);
            x.setOnClickListener(v->notifyMessage(s));
        }
        content.addView(actions);

        TextView title=text("APLICATIVOS",16,Color.WHITE,true);
        title.setPadding(dp(8),dp(14),dp(8),dp(8));
        content.addView(title);

        apps=new LinearLayout(this); apps.setOrientation(LinearLayout.VERTICAL);
        content.addView(apps);

        Button settings = new Button(this);
        settings.setText("⚙  Configurações do Launcher");
        settings.setTextColor(Color.WHITE); settings.setBackgroundResource(R.drawable.button);
        settings.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_SETTINGS)));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(50)); sp.setMargins(0,dp(10),0,dp(10));
        content.addView(settings,sp);

        scroll.addView(content);
        root.addView(scroll, top);
        setContentView(root);
        loadApps();
        updateClock();
    }

    void updateClock(){
        if(clock==null) return;
        clock.setText(new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        clock.postDelayed(this::updateClock, 30000);
    }

    void loadApps(){
        apps.removeAllViews();
        PackageManager pm=getPackageManager();
        Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list=pm.queryIntentActivities(i,0);
        Collections.sort(list,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
        for(ResolveInfo r:list){
            if(r.activityInfo.packageName.equals(getPackageName())) continue;
            TextView row=text("  "+r.loadLabel(pm),16,Color.WHITE,false);
            row.setBackgroundResource(R.drawable.card);
            row.setCompoundDrawablesWithIntrinsicBounds(r.loadIcon(pm),null,null,null);
            row.setCompoundDrawablePadding(dp(12));
            row.setPadding(dp(12),dp(8),dp(12),dp(8));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58)); p.setMargins(0,dp(3),0,dp(3));
            apps.addView(row,p);
            row.setOnClickListener(v->{
                try{ startActivity(new Intent().setComponent(new ComponentName(r.activityInfo.packageName,r.activityInfo.name))); }
                catch(Exception ignored){}
            });
        }
    }

    void notifyMessage(String kind){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel("sc","São Cipriano",NotificationManager.IMPORTANCE_DEFAULT));
        Notification.Builder n=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"sc"):new Notification.Builder(this);
        n.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("São Cipriano Launcher")
         .setContentText(kind+" • intenção registrada no launcher").setAutoCancel(true);
        if(Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)
            nm.notify((int)System.currentTimeMillis(),n.build());
    }
}
