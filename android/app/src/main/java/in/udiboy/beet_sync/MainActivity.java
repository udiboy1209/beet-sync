package in.udiboy.beet_sync;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ServerFragment.OnListFragmentInteractionListener, LibraryFragment.OnListFragmentInteractionListener {

    private NsdHelper mNsdHelper;
    private String mBaseUrl;

    private List<Song> mCurrentLibrary;
    private SQLHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        switchContent(ServerFragment.newInstance(mNsdHelper));
        //mNsdHelper.discoverServices();

        mDbHelper = new SQLHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                SyncEntry.COL_ID,
                SyncEntry.COL_TITLE,
                SyncEntry.COL_ALBUM,
                SyncEntry.COL_ARTIST,
        };

        Cursor cursor = db.query(
                SyncEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        mCurrentLibrary = new ArrayList<>(cursor.getCount());
        Log.i("Database","Column count: " + cursor.getColumnCount());
        for(int i=0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            Song song = new Song();
            song.name = cursor.getString(1);
            song.album = cursor.getString(2);
            song.artist = cursor.getString(3);
            song.id = String.valueOf(cursor.getInt(0));

            mCurrentLibrary.add(song);
        }

        cursor.close();
    }

    private void switchContent(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_fragment, fragment);
        ft.commit();
    }


    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            Log.i("NSD","Service Discovery started");
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onServerConnect(NsdServiceInfo item) {
        mBaseUrl = "http://" + item.getHost().getHostAddress() + ":" + String.valueOf(item.getPort());
        LibraryFragment frag = LibraryFragment.newInstance(mBaseUrl);
        switchContent(frag);
    }

    @Override
    public void onSyncRequest(Song item) {
        mCurrentLibrary.add(item);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SyncEntry.COL_ID, Integer.valueOf(item.id));
        values.put(SyncEntry.COL_TITLE, item.name);
        values.put(SyncEntry.COL_ALBUM, item.album);
        values.put(SyncEntry.COL_ARTIST, item.artist);

        long newRowId = db.insert(SyncEntry.TABLE_NAME, null, values);

        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra("filepath",item.link);
        intent.putExtra("baseUrl", mBaseUrl);

        startService(intent);
    }

    @Override
    public boolean isSynced(Song item){
        for(Song s: mCurrentLibrary){
            if(item.id.equals(s.id) &&
               item.name.equals(s.name) &&
               item.album.equals(s.album) &&
               item.artist.equals(s.artist))
                return true;
        }
        return false;
    }
}
