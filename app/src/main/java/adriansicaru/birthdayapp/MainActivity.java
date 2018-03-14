package adriansicaru.birthdayapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    Realm realm;
    RealmResults<BirthPerson> persons;
    ListView birthdayListView;
    BirtdayAdapter birtdayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Realm.init(mContext);
        realm = Realm.getDefaultInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Birthdays");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        birthdayListView = findViewById(R.id.list_view);
        birthdayListView.setEmptyView(findViewById(R.id.empty_view));
        persons = realm.where(BirthPerson.class).findAll().sort("birthdayStamp", Sort.DESCENDING);
        birtdayAdapter = new BirtdayAdapter(persons);
        birthdayListView.setAdapter(birtdayAdapter);
        registerForContextMenu(birthdayListView);

        birthdayListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                
                return false;
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AddActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        persons = realm.where(BirthPerson.class).findAll().sort("birthdayStamp", Sort.DESCENDING);
        Log.e("NUMBER OF PERSONS",persons.size()+"");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                BirthPerson personToDelete = persons.get(info.position);
                realm.beginTransaction();
                realm.where(BirthPerson.class).equalTo("name", personToDelete.getName())
                        .equalTo("birthdayStamp", personToDelete.getBirthdayStamp()).findAll().deleteAllFromRealm();
                realm.commitTransaction();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}
