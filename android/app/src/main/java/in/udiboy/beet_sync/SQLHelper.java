package in.udiboy.beet_sync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "beetsync-library.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SyncEntry.TABLE_NAME + " (" +
                    SyncEntry._ID + " INTEGER PRIMARY KEY," +
                    SyncEntry.COL_ID + " INTEGER," +
                    SyncEntry.COL_TITLE + " TEXT," +
                    SyncEntry.COL_ALBUM + " TEXT," +
                    SyncEntry.COL_ARTIST + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SyncEntry.TABLE_NAME;

    public SQLHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
