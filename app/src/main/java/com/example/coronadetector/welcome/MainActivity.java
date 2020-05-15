package com.example.coronadetector.welcome;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coronadetector.R;

public class MainActivity extends AppCompatActivity {
ImageView imageView;
TextView welcome,desc;
Button button;
Animation img,btn;

Activity thisAct=this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            // set an exit transition
            getWindow().setExitTransition(new Fade());
        }


        setContentView(R.layout.activity_main_welcome);
        imageView=findViewById(R.id.image);
        welcome=findViewById(R.id.welcome);
        desc=findViewById(R.id.desctxt);
        button=findViewById(R.id.getstarted);
        img= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.imganim);
        btn=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.btnanim);
        imageView.startAnimation(img);
        welcome.startAnimation(btn);
        button.startAnimation(btn);
        desc.startAnimation(btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, com.example.coronadetector.MainActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(thisAct).toBundle());
            }
        });
    }


}
