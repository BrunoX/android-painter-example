package com.dd.painter;

import android.app.Activity;
import android.os.Bundle;

public class PlayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setBackgroundDrawable(
				Utils.createCheckerBoard(getResources(), 16));

		loadMainFragment(new MainFragment());
	}

	private void loadMainFragment(MainFragment fragment) {
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment).commit();
	}
}
