package com.pisen.ott.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

public class TestActivity extends Activity {

	public static final String[] sCheeseStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi", "Acorn", "Adelost" };

	GridView gridView1;
	ListView listView1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main111);

		gridView1 = (GridView) findViewById(R.id.grdAppManage);
		listView1 = (ListView) findViewById(R.id.listView1);

		gridView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sCheeseStrings));
		listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sCheeseStrings));
	}

}
