package in.udiboy.beet_sync;

public class Song extends LibraryItem{
    String album,artist,id;
    boolean sync_state = false;

    public Song(){
        type = LibraryItem.TYPE_SONG;
    }
}
