package ir.javadroid.sqlite_sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


class AdapterUser extends RecyclerView.Adapter<AdapterUser.MainHolder> {

    private List<ModelUser> apps;
    delegate mDelegate;
    Context context;

    public AdapterUser(Context context, List<ModelUser> apps, delegate delegate) {
        this.apps = apps;
        this.context = context;
        mDelegate = delegate;
    }


    @Override
    public int getItemCount() {
        return apps.size();
    }

    public interface delegate {
        void onClick(ModelUser user, int position);
        void onDelete(ModelUser user, int position);
    }

    @Override
    public void onBindViewHolder(final MainHolder mainHolder, @SuppressLint("RecyclerView") final int position) {
        final ModelUser user = apps.get(position);
        mainHolder.linData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.onClick(user, position);
            }
        });
        mainHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.onDelete(user, position);
            }
        });

        mainHolder.tvName.setText("#" + user.id + "-" + user.name);
        mainHolder.tvAddress.setText(user.address);


    }


    @Override
    public MainHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.adapter_user, viewGroup, false);
        return new MainHolder(itemView);
    }

    static class MainHolder extends RecyclerView.ViewHolder {


        TextView tvName;
        TextView tvDelete;
        TextView tvAddress;
        RelativeLayout linData;


        public MainHolder(View rootView) {
            super(rootView);
            tvName = rootView.findViewById(R.id.tvName);
            tvDelete = rootView.findViewById(R.id.tvDelete);
            tvAddress = rootView.findViewById(R.id.tvAddress);
            linData = rootView.findViewById(R.id.linData);
        }


    }
}
