package ar.rulosoft.mimanganu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class ActivityMisMangas extends ActionBarActivity {

	public static final String SERVER_ID = "server_id";
	public static final String MANGA_ID = "manga_id";
	public static final String MOSTRAR_EN_GALERIA = "mostrarengaleria";

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	FragmentMisMangas fragmentMisMangas;
	FragmentAddManga fragmentAddManga;
	SharedPreferences pm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_mis_mangas);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		fragmentAddManga = new FragmentAddManga();
		fragmentMisMangas = new FragmentMisMangas();

		mSectionsPagerAdapter.add(fragmentMisMangas);
		mSectionsPagerAdapter.add(fragmentAddManga);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		pm = PreferenceManager.getDefaultSharedPreferences(ActivityMisMangas.this);

		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.BLACK);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mis_mangas, menu);
		MenuItem menuMostrarGaleria = (MenuItem) menu.findItem(R.id.action_mostrar_en_galeria);
		boolean checked = pm.getInt(MOSTRAR_EN_GALERIA, 0) > 0;
		menuMostrarGaleria.setChecked(checked);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.buscarActualizaciones) {
			new BuscarNuevo().execute();
			return true;
		} else if (id == R.id.action_mostrar_en_galeria) {
			File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MiMangaNu/", ".nomedia");
			if (item.isChecked()) {
				item.setChecked(false);
				pm.edit().putInt(MOSTRAR_EN_GALERIA, 0).commit();
				if (f.exists())
					f.delete();
			} else {
				item.setChecked(true);
				pm.edit().putInt(MOSTRAR_EN_GALERIA, 1).commit();
				if (!f.exists())
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			return true;
		} else if (id == R.id.licencia) {
			Intent intent = new Intent(this, ActivityLicencia.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		int ci = mViewPager.getCurrentItem();
		if (ci != 0)
			mViewPager.setCurrentItem((ci - 1));
		else
			super.onBackPressed();
	}

	@SuppressLint("InlinedApi")
	public void lockOrintation() {
		int orientation = ActivityMisMangas.this.getRequestedOrientation();
		int rotation = ((WindowManager) ActivityMisMangas.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		if (android.os.Build.VERSION.SDK_INT > 8) {
			switch (rotation) {
			case Surface.ROTATION_0:
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			default:
				orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			}
			setRequestedOrientation(orientation);
		} else {
			setRequestedOrientation(getResources().getConfiguration().orientation);
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String tabs[] = new String[] { getResources().getString(R.string.mismangas), getResources().getString(R.string.masmangas) };

		List<Fragment> fragments;

		public void add(Fragment f) {
			fragments.add(f);
		}

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fragments = new ArrayList<Fragment>();
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public float getPageWidth(int position) {
			float size = 1f;
			if (position == 0)
				size = 0.9f;
			return size;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return tabs[position];
		}
	}

	public class BuscarNuevo extends AsyncTask<Void, String, Void> {

		ProgressDialog progreso = new ProgressDialog(ActivityMisMangas.this);

		@Override
		protected void onPreExecute() {
			setRequestedOrientation(getResources().getConfiguration().orientation);
			progreso.setTitle(getResources().getString(R.string.buscandonuevo));
			progreso.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			final String s = values[0];
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progreso.setMessage(s);
				}
			});
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Manga> mangas = Database.getMangasForUpdates(ActivityMisMangas.this);
			Database.removerCapitulosHuerfanos(ActivityMisMangas.this);
			for (int i = 0; i < mangas.size(); i++) {
				Manga manga = mangas.get(i);
				ServerBase s = ServerBase.getServer(manga.getServerId());
				try {
					onProgressUpdate(manga.getTitulo());
					s.cargarCapitulos(manga);
					int diff = s.buscarNuevosCapitulos(manga, ActivityMisMangas.this);
					if (0 < diff && fragmentMisMangas.adapter != null) {
						fragmentMisMangas.adapter.getItem(i).setNuevos(diff);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			if (fragmentMisMangas.adapter != null)
				fragmentMisMangas.adapter.notifyDataSetChanged();
			if (progreso != null && progreso.isShowing()) {
				try {
					progreso.dismiss();
				} catch (Exception e) {

				}
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			super.onPostExecute(result);
		}

	}

}
