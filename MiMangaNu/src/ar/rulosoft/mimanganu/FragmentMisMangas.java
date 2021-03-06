package ar.rulosoft.mimanganu;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import ar.rulosoft.mimanganu.adapters.MisMangasAdaptes;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.R;

public class FragmentMisMangas extends Fragment {

	GridView grilla;
	MisMangasAdaptes adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rView = inflater.inflate(R.layout.fragment_mis_mangas, container, false);
		grilla = (GridView) rView.findViewById(R.id.grilla_mis_mangas);
		return rView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = getResources().getDisplayMetrics().density;
		float dpWidth = outMetrics.widthPixels / density;
		int columnas = (int) (dpWidth / 150);

		if (columnas < 2)
			columnas = 2;
		else if (columnas > 6)
			columnas = 6;
		grilla.setNumColumns(columnas);

		grilla.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Manga m = (Manga) grilla.getAdapter().getItem(position);
				Intent intent = new Intent(getActivity(), ActivityCapitulos.class);
				intent.putExtra(ActivityMisMangas.MANGA_ID, m.getId());
				getActivity().startActivity(intent);
			}
		});

		registerForContextMenu(grilla);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = getActivity().getMenuInflater();

		if (v.getId() == R.id.grilla_mis_mangas)
			;
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(grilla.getAdapter().getItem(info.position).toString());
		inflater.inflate(R.menu.gridview_mismangas, menu);
		MenuItem m = (MenuItem) menu.findItem(R.id.noupdate);
		if (((Manga) grilla.getAdapter().getItem(info.position)).isFinalizado()) {
			m.setTitle(getResources().getString(R.string.buscarupdates));
		} else {
			m.setTitle(getResources().getString(R.string.nobuscarupdate));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (item.getItemId() == R.id.borrar) {
			Manga m = (Manga) grilla.getAdapter().getItem(info.position);

			ServerBase s = ServerBase.getServer(m.getServerId());
			String ruta = ColaDeDescarga.generarRutaBase(s, m);
			DeleteRecursive(new File(ruta));
			Database.BorrarManga(getActivity(), m.getId());
			adapter.remove(m);
			adapter.notifyDataSetChanged();
		} else if (item.getItemId() == R.id.noupdate) {
			Manga m = (Manga) grilla.getAdapter().getItem(info.position);
			if (m.isFinalizado()) {
				m.setFinalizado(false);
				Database.setUpgrable(getActivity(), m.getId(), false);
			} else {
				m.setFinalizado(true);
				Database.setUpgrable(getActivity(), m.getId(), true);
			}

		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onResume() {
		adapter = new MisMangasAdaptes(getActivity(), Database.getMangas(getActivity()));
		grilla.setAdapter(adapter);
		super.onResume();
	}

	public static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);
		fileOrDirectory.delete();
	}

}
