package in.udiboy.beet_sync;


public class LibraryItem {
    public static final int TYPE_ARTIST = 1;
    public static final int TYPE_ALBUM = 2;
    public static final int TYPE_SONG = 3;

    public int type;
    public String name;
    public String link;

    public LibraryItem(){
    }
}
