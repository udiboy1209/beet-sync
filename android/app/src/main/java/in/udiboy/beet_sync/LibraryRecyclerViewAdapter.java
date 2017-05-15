package in.udiboy.beet_sync;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryRecyclerViewAdapter extends RecyclerView.Adapter<LibraryRecyclerViewAdapter.ViewHolder>
                        implements Callback<List<Song>> {

    public final List<LibraryItem> mValues;
    private final LibraryFragment.OnListFragmentInteractionListener mListener;
    private final BeetsyncInterface mAPI;

    public LibraryRecyclerViewAdapter(List<LibraryItem> items, LibraryFragment.OnListFragmentInteractionListener listener,
                                      BeetsyncInterface bsAPI) {
        mValues = items;
        mListener = listener;
        mAPI = bsAPI;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        ViewHolder vh;

        switch (viewType){
            case LibraryItem.TYPE_ARTIST:
            case LibraryItem.TYPE_ALBUM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_album, parent, false);
                vh = new AlbumViewHolder(view);
                break;
            case LibraryItem.TYPE_SONG:
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_song, parent, false);
                vh = new SongViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mName.setText(holder.mItem.name);
        switch (getItemViewType(position)){
            case LibraryItem.TYPE_ARTIST:
            case LibraryItem.TYPE_ALBUM:
                ((AlbumViewHolder)holder).mDropdownButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currState = ((Album) holder.mItem).display_state;
                        switch (currState){
                            case Album.DISPLAY_STATE_CLOSED:
                                onDropdownClick(holder.getAdapterPosition(), true);
                                break;
                            case Album.DISPLAY_STATE_OPENED:
                                onDropdownClick(holder.getAdapterPosition(), false);
                                break;
                            case Album.DISPLAY_STATE_LOADING:
                            default:
                                break;
                        }
                    }
                });

                holder.mName.setText(holder.mItem.name);
                ((AlbumViewHolder)holder).mArtist.setText(((Album)holder.mItem).artist);
                switch (((Album)holder.mItem).display_state){
                    case Album.DISPLAY_STATE_CLOSED:
                        ((AlbumViewHolder) holder).mDropdownButton.setImageResource(R.drawable.ic_add_circle_outline_black_24dp);
                        break;
                    case Album.DISPLAY_STATE_OPENED:
                        ((AlbumViewHolder) holder).mDropdownButton.setImageResource(R.drawable.ic_remove_circle_outline_black_24dp);
                        break;
                    case Album.DISPLAY_STATE_LOADING:
                    default:
                        ((AlbumViewHolder) holder).mDropdownButton.setImageResource(R.drawable.ic_sync_black_24dp);
                        break;
                }
                break;
            case LibraryItem.TYPE_SONG:
                ((SongViewHolder)holder).mPath.setText(holder.mItem.link);
                ((SongViewHolder)holder).mSyncButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mListener.onSyncRequest((Song)holder.mItem);
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                });
                if(mListener.isSynced((Song)holder.mItem)) {
                    ((SongViewHolder)holder).mSyncButton.setImageResource(R.drawable.ic_check_box_black_24dp);
                    ((SongViewHolder)holder).mSyncButton.setClickable(false);
                } else {
                    ((SongViewHolder)holder).mSyncButton.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
                    ((SongViewHolder)holder).mSyncButton.setClickable(true);
                }
                break;
        }
    }

    private void onDropdownClick(int position, boolean add) {
        Album album = (Album) mValues.get(position);

        if(add) {
            Call<List<Song>> songsCall = mAPI.getSongsInAlbum(album.artist, album.name);
            songsCall.enqueue(this);
            album.display_state = Album.DISPLAY_STATE_LOADING;
            notifyItemChanged(position);
        } else {
            int rmCount = 0, i=position+1;
            while(i<mValues.size()) {
                LibraryItem item = mValues.get(i);
                if (item instanceof Song){ // && (((Song) item).album.equals(album.name) || album.name.equals("Singles"))) {
                    mValues.remove(i);
                    rmCount++;
                } else {
                    break;
                }
            }
            album.display_state = Album.DISPLAY_STATE_CLOSED;
            notifyItemRangeRemoved(position+1,rmCount);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public int getItemViewType(int position){
        return mValues.get(position).type;
    }

    @Override
    public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
        if(response.isSuccessful()) {
            List<Song> songs = response.body();
            if (songs.size() > 0) {
                String album = songs.get(0).album;
                String artist = songs.get(0).artist;
                int i=0;

                for(i=0; i<mValues.size(); i++){
                    LibraryItem item = mValues.get(i);
                    if((item.name.equals(album) || (item.name.equals("Singles") && album.equals(""))) &&
                            ((Album)item).artist.equals(artist))
                        break;
                }
                ((Album)mValues.get(i)).display_state = Album.DISPLAY_STATE_OPENED;
                notifyItemChanged(i);
                mValues.addAll(i+1, songs);
                notifyItemRangeInserted(i+1,songs.size());
            }

        } else {
            Log.e("Retrofit",response.errorBody().toString());
        }
    }

    @Override
    public void onFailure(Call<List<Song>> call, Throwable t) {
        t.printStackTrace();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public LibraryItem mItem;
        public TextView mName;

        public ViewHolder(View view) {
            super(view);
            mName = (TextView) view.findViewById(R.id.item_name);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }

    public class AlbumViewHolder extends ViewHolder{
        public ImageView mDropdownButton;
        public TextView mArtist;

        public AlbumViewHolder(View view) {
            super(view);
            mArtist = (TextView) view.findViewById(R.id.item_artist);
            mDropdownButton = (ImageView) view.findViewById(R.id.album_dropdown);
        }
    }

    public class SongViewHolder extends ViewHolder {
        public final TextView mPath;
        public final ImageButton mSyncButton;

        public SongViewHolder(View view) {
            super(view);
            mPath = (TextView) view.findViewById(R.id.item_path);
            mSyncButton = (ImageButton) view.findViewById(R.id.button_sync);
        }
    }
}
