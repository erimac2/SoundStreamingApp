package com.erimac2.soundstreamingapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends ArrayAdapter<ArtistItem> implements Filterable {

    List<ArtistItem> objects;

    public ArtistAdapter(Context context, List<ArtistItem> objects) {
        super(context, R.layout.listitemdesign, objects);
        this.objects = new ArrayList<>(objects);
    }
    private Filter artistFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            List<ArtistItem> suggestions = new ArrayList<>();

            if (constraint == null && constraint.length() == 0)
            {
                suggestions.addAll(objects);
            }
            else
            {
                String filterPattern = constraint.toString().toLowerCase();

                for (ArtistItem item : objects)
                {
                    if (item.getTitle().toLowerCase().startsWith(filterPattern))
                    {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.artistitemdesign, null);
        }

        TextView title = view.findViewById(R.id.Artist_title);
        ImageView image = view.findViewById(R.id.Artist_image);

        ArtistItem item = getItem(position);

        title.setText(item.getTitle());
        Log.i("ION", item.getImageLink());
        Ion.with(image)
                .placeholder(android.R.drawable.btn_default_small)
                .error(android.R.drawable.ic_dialog_alert)
                .load(item.getImageLink());

        return view;
    }
    @Override
    public Filter getFilter()
    {
        return artistFilter;
    }
}

