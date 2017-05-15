package in.udiboy.beet_sync;

import android.net.nsd.NsdServiceInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ServerRecyclerViewAdapter extends RecyclerView.Adapter<ServerRecyclerViewAdapter.ViewHolder> {

    private final List<NsdServiceInfo> mValues;
    private final ServerFragment.OnListFragmentInteractionListener mListener;

    public ServerRecyclerViewAdapter(List<NsdServiceInfo> items, ServerFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_server, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String ip = holder.mItem.getHost().getHostAddress() + ":" + String.valueOf(holder.mItem.getPort());
//        String hostname = holder.mItem.getHost().getHostName();
        holder.mIpView.setText(ip);
        holder.mHostnameView.setText(holder.mItem.getServiceName());

        holder.mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onServerConnect(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public NsdServiceInfo get(int i){
        return mValues.get(i);
    }

    public void add(NsdServiceInfo info){
        mValues.add(info);
        notifyDataSetChanged();
    }

    public NsdServiceInfo remove(int i){
        return mValues.remove(i);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIpView;
        public final TextView mHostnameView;
        public final Button mConnectButton;
        public NsdServiceInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIpView = (TextView) view.findViewById(R.id.ip);
            mHostnameView = (TextView) view.findViewById(R.id.hostname);
            mConnectButton = (Button) view.findViewById(R.id.connect);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mHostnameView.getText() + "@" + mIpView.getText() + "'";
        }
    }
}
