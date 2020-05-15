package com.example.coronadetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;


import com.example.coronadetector.stat.controller.ExampleService;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Fragment f;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v3);
        drawerLayout=findViewById(R.id.drawer);
        navigationView=findViewById(R.id.navigationview);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.Open,R.string.Close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);





        if (savedInstanceState==null){
            InitialFragment initialFragment= new InitialFragment();
            FragmentManager fm=getSupportFragmentManager();
            FragmentTransaction ft1=fm.beginTransaction();
            ft1.replace(R.id.fragment_container,initialFragment);
            ft1.commit();}

        f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if(!isMyServiceRunning(ExampleService.class))
        {
            startService(new Intent(this, ExampleService.class));

        }

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id=menuItem.getItemId();



        switch (id){
            case R.id.worldmap:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof WorldFragment))
                {
                    WorldFragment worldFragment= new WorldFragment();
                    FragmentManager fm=getSupportFragmentManager();
                    FragmentTransaction ft1=fm.beginTransaction();
                    ft1.setCustomAnimations(R.anim.slide_out_left,0);

                    ft1.replace(R.id.fragment_container,worldFragment,"worldFrag");
                    ft1.addToBackStack(null);

                    ft1.commit();

                }
                drawerLayout.closeDrawer(Gravity.LEFT);

                break;

            case R.id.xray:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof XrayFragment)) {



                XrayFragment xrayFragment=new XrayFragment();
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction ft=fragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_out_left,0);

                    ft.replace(R.id.fragment_container,xrayFragment,"xrayFrag");
                    ft.addToBackStack(null);

                    ft.commit(); }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.temp:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof TempFragment)) {



                TempFragment tempFragment=new TempFragment();
                FragmentManager fmf=getSupportFragmentManager();
                FragmentTransaction ftf=fmf.beginTransaction();
                    ftf.setCustomAnimations(R.anim.slide_out_left,0);

                    ftf.replace(R.id.fragment_container,tempFragment,"tempFrag");
                    ftf.addToBackStack(null);

                    ftf.commit(); }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;

            case R.id.stat:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof WorldStaticFragment)) {



                WorldStaticFragment worldStaticFragment=new WorldStaticFragment();
                FragmentManager fms=getSupportFragmentManager();
                FragmentTransaction fts=fms.beginTransaction();
                    fts.setCustomAnimations(R.anim.slide_out_left,0);

                    fts.replace(R.id.fragment_container,worldStaticFragment,"statFrag");
                    fts.addToBackStack(null);

                    fts.commit();  }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.form:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof FormFragment)) {



                FormFragment formFragment=new FormFragment();
                FragmentManager fmfo=getSupportFragmentManager();
                FragmentTransaction ftfo=fmfo.beginTransaction();
                    ftfo.setCustomAnimations(R.anim.slide_out_left,0);

                    ftfo.replace(R.id.fragment_container,formFragment,"formFrag");
                    ftfo.addToBackStack(null);

                    ftfo.commit();   }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;

            case R.id.about:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof AboutFragment)) {



                AboutFragment forumFragment=new AboutFragment();
                FragmentManager fmfa=getSupportFragmentManager();
                FragmentTransaction ftfa=fmfa.beginTransaction();
                    ftfa.setCustomAnimations(R.anim.slide_out_left,0);

                    ftfa.replace(R.id.fragment_container,forumFragment,"aboutFrag");
                    ftfa.addToBackStack(null);

                    ftfa.commit();  }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.homepage:
                 f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if(!(f instanceof InitialFragment)) {



                InitialFragment initialFragment=new InitialFragment();
                FragmentManager fmfi=getSupportFragmentManager();
                FragmentTransaction ftfi=fmfi.beginTransaction();
                    ftfi.setCustomAnimations(R.anim.slide_out_left,0);

                    ftfi.replace(R.id.fragment_container,initialFragment,"homeFrag");
                ftfi.addToBackStack(null);
                ftfi.commit();
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;

        }

        return true;
    }
    @Override
    public void onBackPressed()
    {

        //  startActivity(new Intent(MainActivity.this, com.example.coronadetector.MainActivity.class), ActivityOptions.makeSceneTransitionAnimation(thisAct).toBundle());

        //  finish();

        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStackImmediate();
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
           // super.onBackPressed();
            moveTaskToBack(true);
        }
    }
}
