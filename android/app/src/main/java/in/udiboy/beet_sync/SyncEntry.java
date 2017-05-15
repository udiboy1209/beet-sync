package in.udiboy.beet_sync;

import android.provider.BaseColumns;

/**
 * Created by udiboy on 13/5/17.
 */
public class SyncEntry implements BaseColumns{
    public static final String TABLE_NAME = "sync";
    public static final String COL_ID = "beets_id";
    public static final String COL_TITLE = "title";
    public static final String COL_ALBUM = "album";
    public static final String COL_ARTIST = "artist";

}
