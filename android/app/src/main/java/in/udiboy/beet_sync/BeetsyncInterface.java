package in.udiboy.beet_sync;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Created by udiboy on 10/5/17.
 */
public interface BeetsyncInterface {
    @GET("/albums")
    Call<List<Album>> getAlbums();

    @GET("/album/{artist}/{album}")
    Call<List<Song>> getSongsInAlbum(@Path("artist") String artist, @Path("album") String album);

    @GET("/download/{path}")
    @Streaming
    Call<ResponseBody> downloadSong(@Path(value="path", encoded=true) String filepath);

//    @GET("/song/{id}")
//    Call<SongDetail> getSongDetail(@Path("id") int id);
}
