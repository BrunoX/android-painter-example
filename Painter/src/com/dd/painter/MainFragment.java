package com.dd.painter;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainFragment extends Fragment {

	private DrawingView mDrawingView;
	private ViewGroup mBrushPanel;
	private ViewGroup mBrushColors;
	private SeekBar mBrushStroke;

	static int[] COLORS = { Color.rgb(255, 51, 255), // DARK PINK
			Color.rgb(255, 230, 102), // LIGHT YELLOW
			Color.rgb(148, 66, 50), // DARK MAROON
			Color.rgb(186, 123, 68), // LIGHT MAROON
			Color.rgb(252, 20, 20), // RED
			Color.rgb(102, 255, 255), // LIGHT BLUE

			Color.rgb(70, 78, 202), // DARK BLUE
			Color.rgb(190, 255, 91), // LIGHT GREEN
			Color.rgb(15, 230, 0), // DARK GREEN
			Color.rgb(123, 0, 230), // JAMBLI
			Color.rgb(255, 187, 50), // ORANGE
			Color.rgb(7, 5, 0), // BLACK

			Color.rgb(129, 128, 127), // GRAY
			Color.rgb(255, 4, 139), // PINK RED
			Color.rgb(51, 204, 255), // NAVY BLUE
			Color.rgb(102, 255, 204), // BRIGHT GREEN
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_mainfragment, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mDrawingView = (DrawingView) view.findViewById(R.id.drawing_view);

		mDrawingView.setDrawingColor(getResources().getColor(R.color.ab_color));

		mBrushPanel = (ViewGroup) view.findViewById(R.id.brush_panel);
		mBrushColors = (ViewGroup) view.findViewById(R.id.brush_colors);
		mBrushStroke = (SeekBar) view.findViewById(R.id.brush_stroke);

		mBrushStroke
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						mDrawingView.setDrawingStroke(progress);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}
				});
		mBrushStroke.setProgress(30);//

		mBrushPanel.getViewTreeObserver().addOnPreDrawListener(
				new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						mBrushPanel.getViewTreeObserver()
								.removeOnPreDrawListener(this);
						mBrushPanel
								.setTranslationY(isLandscape() ? -mBrushPanel
										.getHeight() : mBrushPanel.getHeight());
						return false;
					}
				});

		createBrushPanelContent();

	}

	@SuppressWarnings("null")
	private void createBrushPanelContent() {
		TableRow tableRow = null;
		final int rowLimit = isLandscape() ? 16 : 8;
		for (int i = 0; i < COLORS.length; i++) {
			if ((i % rowLimit) == 0) {
				tableRow = new TableRow(getActivity());
				mBrushColors.addView(tableRow, new TableLayout.LayoutParams(
						WRAP_CONTENT, WRAP_CONTENT));
			}
			tableRow.addView(createToolButton(tableRow,
					R.drawable.ic_paint_splot, i));
		}
	}

	private void showBrushPanel() {
		mBrushPanel.animate().translationY(0).start();
	}

	private void hideBrushPanel() {
		mBrushPanel
				.animate()
				.translationY(
						isLandscape() ? -mBrushPanel.getHeight() : mBrushPanel
								.getHeight()).start();
	}

	private boolean isLandscape() {
		return getResources().getBoolean(R.bool.is_landscape);
	}

	private ImageButton createToolButton(ViewGroup parent, int drawableResId,
			int index) {
		ImageButton button = (ImageButton) getActivity().getLayoutInflater()
				.inflate(R.layout.button_paint_spot, parent, false);
		button.setImageResource(drawableResId);
		button.setOnClickListener(mButtonClick);
		if (index != -1) {
			button.setTag(Integer.valueOf(index));
			button.setColorFilter(COLORS[index]);
		}
		return button;
	}

	private View.OnClickListener mButtonClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mDrawingView.setDrawingColor(COLORS[((Integer) v.getTag())
					.intValue()]);
			hideBrushPanel();
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_play, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_brush:
			if (mBrushPanel.getTranslationY() == 0) {
				hideBrushPanel();
			} else {
				showBrushPanel();
			}
			break;

		case R.id.action_eraser:
			mDrawingView.enableEraser();
			break;

		case R.id.action_undo:
			mDrawingView.undoOperation();
			break;

		case R.id.action_redo:
			mDrawingView.redoOperation();
			break;

		case R.id.action_save: {
			mDrawingView
					.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			mDrawingView.setDrawingCacheEnabled(true);
			mDrawingView.buildDrawingCache();
			Bitmap viewCache = mDrawingView.getDrawingCache();
			Bitmap bitmap = viewCache.copy(viewCache.getConfig(), false);
			mDrawingView.setDrawingCacheEnabled(false);
			new SaveTask().execute(bitmap);
		}
			break;

		case R.id.action_cancel:
			mDrawingView.clearDrawing();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private class SaveTask extends AsyncTask<Bitmap, Void, File> {
		private ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage(getString(R.string.saving));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(File result) {
			mProgressDialog.dismiss();
			if (result != null) {
				Toast.makeText(getActivity(),
						getString(R.string.saved_as) + result.getName(),
						Toast.LENGTH_LONG).show();
			}
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		protected File doInBackground(Bitmap... params) {
			String name = new SimpleDateFormat(
					"'Painter_'yyyy-MM-dd_HH-mm-ss.S'.png'").format(new Date());
			File result = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					name);

			FileOutputStream stream = null;
			try {
				try {
					stream = new FileOutputStream(result);
					if (params[0].compress(CompressFormat.PNG, 75, stream)) {
						getActivity().sendBroadcast(
								new Intent(
										Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
										Uri.fromFile(result)));
					} else {
						result = null;
					}
				} finally {
					if (stream != null) {
						stream.close();
					}
				}
			} catch (IOException e) {
				result = null;
			}
			return result;
		}
	}
}
