package net.cs50.recipes;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getActionBar().setDisplayShowTitleEnabled(false);
	}
	
}
