package com.example.coronadetector;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.coronadetector.apropos.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {
   Button button;
    ImageView imageView;
    TextView textView;
    Animation animation,animation1,animation2;
    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_about, container, false);
        button=v.findViewById(R.id.start);
        imageView=v.findViewById(R.id.imgview);
        animation= AnimationUtils.loadAnimation(getContext(),R.anim.img_initial_fragment);
        imageView.startAnimation(animation);
        textView=v.findViewById(R.id.txt);
        animation1= AnimationUtils.loadAnimation(getContext(),R.anim.txt_initial_fragment);
        textView.startAnimation(animation1);
        animation2=AnimationUtils.loadAnimation(getContext(),R.anim.btnanim);
        button.startAnimation(animation2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), MainActivity.class);
                startActivity(i);
            }
        });
        return v;
    }

}
