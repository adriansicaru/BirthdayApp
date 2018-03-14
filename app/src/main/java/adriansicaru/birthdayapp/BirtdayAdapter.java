package adriansicaru.birthdayapp;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by adriansicaru on 3/13/18.
 */

public class BirtdayAdapter extends RealmBaseAdapter<BirthPerson> implements ListAdapter {

    private static class Holder {
        TextView name;
        TextView birthday;
    }

    public BirtdayAdapter(RealmResults<BirthPerson> realmResults) {
        super(realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_layout, parent, false);
            holder = new Holder();
            holder.name = convertView.findViewById(R.id.name_tv);
            holder.birthday = convertView.findViewById(R.id.birthday_tv);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if (adapterData != null) {
            final BirthPerson birthPerson = adapterData.get(position);
            holder.name.setText(birthPerson.getName());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(birthPerson.getBirthdayStamp());
            holder.birthday.setText(calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())+" "+calendar.get(Calendar.YEAR));
        }
        return convertView;
    }

}
