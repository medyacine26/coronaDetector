package com.example.coronadetector.formulaire;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.coronadetector.R;


public class Main6Activity extends AppCompatActivity {


    public int cumule;
    HalfGauge hg;
    private TextView result;
    Button menuBtn;
    Activity thisAct=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            // set an exit transition
            getWindow().setExitTransition(new Fade());
        }


        super.onCreate(savedInstanceState);

        setContentView(R.layout.formulaire6);
        final Intent in = getIntent();
        cumule = in.getIntExtra("cumule",0);
        result=findViewById(R.id.result);
        hg = findViewById(R.id.halfGauge);
        Range rg1 =new Range();
        rg1.setFrom(0);
        rg1.setTo(25);
        rg1.setColor(Color.GREEN);
        Range rg2 =new Range();
        rg2.setFrom(25);
        rg2.setTo(50);
        rg2.setColor(Color.YELLOW);
        Range rg3 =new Range();
        rg3.setFrom(50);
        rg3.setTo(75);
        rg3.setColor(Color.parseColor("#FFA500"));
        Range rg4 =new Range();
        rg4.setFrom(75);
        rg4.setTo(100);
        rg4.setColor(Color.RED);
        hg.addRange(rg1);hg.addRange(rg2);hg.addRange(rg3);hg.addRange(rg4);
        hg.setUseRangeBGColor(true);

        hg.setEnableNeedleShadow(true);

        double score = (double)cumule/(double)36;


        if((int)(score*100)<25){
            hg.animate();

            hg.setValue((int)(score*100));


            result.setText("Your test result is negative . Your chance to have the COVID-19 according to this test is: "+(int)(score*100)+"%. Stay at home and be safe.");
        }
        else if( (int)(score*100)>=25 && (int)(score*100)<=50){
            hg.animate();
            hg.setValue((int)(score*100));

            result.setText("Your test result is not well known . Your chance to have the COVID-19 according to this test is: "+(int)(score*100)+"%. You need to do a test.");
        }
        else {
            hg.animate();
            hg.setValue((int)(score*100));

            result.setText("Your test result is positive . Your chance to have the COVID-19 according to this test is: "+(int)(score*100)+"%. you have to call the emergency !");
        }

        menuBtn=findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), com.example.coronadetector.MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(thisAct).toBundle());

            }
        });

    }
}
