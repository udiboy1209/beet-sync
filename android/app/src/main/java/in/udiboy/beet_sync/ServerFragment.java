package in.udiboy.beet_sync;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ServerFragment extends Fragment implements NsdHelper.OnServiceChangedListener {
    private OnListFragmentInteractionListener mListener;
    private ServerRecyclerViewAdapter mServerAdapter;
    private NsdHelper mNsdHelper;

    public ServerFragment() {
    }

    public static ServerFragment newInstance(NsdHelper nsdHelper) {
        ServerFragment fragment = new ServerFragment();
        fragment.mNsdHelper = nsdHelper;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNsdHelper.registerServiceChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            mServerAdapter = new ServerRecyclerViewAdapter(new ArrayList<NsdServiceInfo>(), mListener);
            recyclerView.setAdapter(mServerAdapter);
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
        mNsdHelper.unregisterServiceChangedListener(this);
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onServiceAdded(NsdServiceInfo service) {
        mServerAdapter.add(service);
    }

    @Override
    public void onServiceRemoved(NsdServiceInfo service) {
        Log.d("Log", "running on service removed");
        for(int i=0; i<mServerAdapter.getItemCount(); i++){
            if(mServerAdapter.get(i).getServiceName().equals(service.getServiceName())){
                mServerAdapter.remove(i);
                break;
            }
        }
    }

    public interface OnListFragmentInteractionListener {
        void onServerConnect(NsdServiceInfo item);
    }
}
