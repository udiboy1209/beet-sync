package in.udiboy.beet_sync;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LibraryFragment extends Fragment implements Callback<List<Album>> {

    private OnListFragmentInteractionListener mListener;
    private LibraryRecyclerViewAdapter mLibraryAdapter;
    private String mBaseUrl;
    private BeetsyncInterface bsAPI;

    public LibraryFragment() {
    }

    public static LibraryFragment newInstance(String baseUrl) {
        LibraryFragment fragment = new LibraryFragment();
        fragment.mBaseUrl = baseUrl;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        bsAPI = retrofit.create(BeetsyncInterface.class);
        Call<List<Album>> albumCall = bsAPI.getAlbums();

        albumCall.enqueue(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mLibraryAdapter = new LibraryRecyclerViewAdapter(new ArrayList<LibraryItem>(), mListener,bsAPI);
            recyclerView.setAdapter(mLibraryAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResponse(Call<List<Album>> call, Response<List<Album>> response) {
        if(response.isSuccessful()) {
            List<Album> albums = response.body();

            mLibraryAdapter.mValues.clear();
            mLibraryAdapter.mValues.addAll(0,albums);
            mLibraryAdapter.notifyDataSetChanged();
        } else {
            Log.e("Retrofit",response.errorBody().toString());
        }
    }

    @Override
    public void onFailure(Call<List<Album>> call, Throwable t) {
        t.printStackTrace();
    }

    public interface OnListFragmentInteractionListener {
        void onSyncRequest(Song item);

        boolean isSynced(Song item);
    }
}