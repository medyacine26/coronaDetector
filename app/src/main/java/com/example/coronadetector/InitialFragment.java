package com.example.coronadetector;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class InitialFragment extends Fragment {
  ImageView imageView;
    Animation animation;
    Animation animationTv;
  TextView textView;
    public InitialFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_initial, container, false);
        imageView=v.findViewById(R.id.imageview);
        textView=v.findViewById(R.id.initTv);
        animation= AnimationUtils.loadAnimation(getContext(),R.anim.img_initial_fragment);
        animationTv= AnimationUtils.loadAnimation(getContext(),R.anim.txt_initial_fragment);
        imageView.startAnimation(animation);
        textView.startAnimation(animationTv);
        return v;
    }

}
