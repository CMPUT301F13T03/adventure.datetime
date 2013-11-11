/*
 *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
 * 	Evan DeGraff
 *
 * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
 * 	this software and associated documentation files (the "Software"), to deal in
 * 	the Software without restriction, including without limitation the rights to
 * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * 	the Software, and to permit persons to whom the Software is furnished to do so,
 * 	subject to the following conditions:
 *
 * 	The above copyright notice and this permission notice shall be included in all
 * 	copies or substantial portions of the Software.
 *
 * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.cmput301f13t03.adventure_datetime.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.cmput301f13t03.adventure_datetime.R;
import ca.cmput301f13t03.adventure_datetime.model.Choice;
import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentFragmentListener;
import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * View Accessed via MainView > Continue > ~Select Item~ or via MainView > BrowseView > StoryDescription > ~Play /
 * Continue item ~
 * <p/>
 * Holds Horizontal filmstrip containing illustrations at top of page, story fragment text in the view, and an actions
 * buttons at the bottom of the page.
 *
 * @author James Finlay
 */
public class FragmentView extends Activity implements ICurrentFragmentListener {
	private static final String TAG = "FragmentView";

	private HorizontalScrollView _filmstrip;
	private TextView _content;
	private LinearLayout _filmLayout;
	private Button _choices;

	private StoryFragment _fragment;

	@Override
	public void OnCurrentFragmentChange(StoryFragment newFragment) {
		_fragment = newFragment;
		setUpView();
	}

	@Override
	public void onResume() {
		Locator.getPresenter().Subscribe(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		Locator.getPresenter().Unsubscribe(this);
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_view);
		setUpView();
	}
	public void setUpView() {
		if (_fragment == null) return;

		/** Layout items **/
		_filmLayout = (LinearLayout) findViewById(R.id.filmstrip);
		_filmstrip = (HorizontalScrollView) findViewById(R.id.filmstrip_wrapper);
		_choices = (Button) findViewById(R.id.choices);
		_content = (TextView) findViewById(R.id.content);

		if (_fragment.getStoryMedia() == null)
			_fragment.setStoryMedia(new ArrayList<String>());
		
		/** Programmatically set filmstrip height **/
		// TODO::JF Unshitify this, aka not static value
		if (_fragment.getStoryMedia().size() > 0)
			_filmstrip.getLayoutParams().height = 300;


		_content.setText(_fragment.getStoryText());


		// 1) Create new ImageView and add to the LinearLayout
		// 2) Set appropriate Layout Params to ImageView
		// 3) Give onClickListener for going to fullscreen
		LinearLayout.LayoutParams lp;
		for (int i = 0; i < _fragment.getStoryMedia().size(); i++) {
			// TODO::JF Get images from fragment
			/*	ImageView li = new ImageView(this);
			li.setScaleType(ScaleType.CENTER_INSIDE);
			li.setImageResource(R.drawable.grumpy_cat2);
			_filmLayout.addView(li);

			lp = (LinearLayout.LayoutParams) li.getLayoutParams();
			lp.setMargins(10, 10, 10, 10);
			lp.width = LayoutParams.WRAP_CONTENT;
			lp.gravity = Gravity.CENTER_VERTICAL;
			li.setLayoutParams(lp);

			li.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO: Open image in fullscreen
				}
			});*/
		}


		/** Choices **/
		final List<String> choices = new ArrayList<String>();
		for (Choice choice : _fragment.getChoices())
			choices.add(choice.getText());
		
		_choices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(v.getContext())
				.setTitle("Actions")
				.setCancelable(true)
				.setItems(choices.toArray(new String[choices.size()]), 
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Iterator<Choice> ite = _fragment.getChoices().iterator();
						Choice choice = null;

						for (int i=0; i<=which; i++)
							choice = ite.next();
						Locator.getUserController().MakeChoice(choice);
					}
				})
				.create().show();
			}
		});


	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.storydesc, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_comment:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
