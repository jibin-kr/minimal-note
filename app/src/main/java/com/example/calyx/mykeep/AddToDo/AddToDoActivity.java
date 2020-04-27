package com.example.calyx.mykeep.AddToDo;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.calyx.mykeep.AppDefault.AppDefaultActivity;
import com.example.calyx.mykeep.R;

public class AddToDoActivity extends AppDefaultActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int contentViewLayoutRes() {
        return R.layout.activity_add_to_do;
    }

    @NonNull
    @Override
    protected Fragment createInitialFragment() {
        return AddToDoFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}

