package com.tuputech.mvp.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tuputech.mvp.R;


public class MainActivity extends AppCompatActivity {


    MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainFragment = MainFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mMainFragment)
                .commitAllowingStateLoss();
    }



}
