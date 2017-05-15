package in.udiboy.beet_sync;

/**
 * Created by udiboy on 10/5/17.
 */
public class Album extends LibraryItem{
    public static final int DISPLAY_STATE_OPENED = 1;
    public static final int DISPLAY_STATE_LOADING = 2;
    public static final int DISPLAY_STATE_CLOSED = 3;

    public String artist;
    public int display_state = DISPLAY_STATE_CLOSED;

    public Album(){
        type = LibraryItem.TYPE_ALBUM;
    }
}
