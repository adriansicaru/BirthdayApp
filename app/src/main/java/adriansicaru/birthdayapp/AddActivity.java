package adriansicaru.birthdayapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

import static android.provider.CalendarContract.Events.CONTENT_URI;

/**
 * Created by adriansicaru on 3/12/18.
 */

public class AddActivity extends AppCompatActivity {

    Context mContext;
    EditText nameEdit;
    DatePicker birthdayView;
    Button addBtn, addCalBtn;
    CoordinatorLayout coordinatorLayout;
    BirthPerson birthPerson;
    FloatingActionButton speechBtn, speechCalendarBtn;
    private static final int NAME_CODE = 0;
    private static final int CALENDAR_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_layout);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add birthday");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        coordinatorLayout = findViewById(R.id.coordinator);
        nameEdit = findViewById(R.id.name_edit);
        birthdayView = findViewById(R.id.datePicker);
        addBtn = findViewById(R.id.add_btn);
        addCalBtn = findViewById(R.id.add_cal_btn);
        speechBtn = findViewById(R.id.speech_button);
        speechCalendarBtn = findViewById(R.id.speech_calendar_button);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEvent(false);
            }
        });

        addCalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEvent(true);
            }
        });

        speechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, NAME_CODE);
            }
        });

        speechCalendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, CALENDAR_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == NAME_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            nameEdit.setText(spokenText);
        } else if (requestCode == CALENDAR_CODE && resultCode == RESULT_OK){
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            Calendar calendar = parseSpeechToDate(results.get(0));
            if(calendar!=null) {
                birthdayView.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void clickEvent(boolean addToCalendar) {

        if(nameEdit.getText().toString().length()>0) {
            Calendar birthDate = Calendar.getInstance();
            birthDate.set(Calendar.HOUR_OF_DAY,0);
            birthDate.set(Calendar.MINUTE,0);
            birthDate.set(Calendar.SECOND,0);
            birthDate.set(Calendar.MILLISECOND,0);
            birthDate.set(Calendar.YEAR, birthdayView.getYear());
            birthDate.set(Calendar.MONTH, birthdayView.getMonth());
            birthDate.set(Calendar.DAY_OF_MONTH, birthdayView.getDayOfMonth());
            if(birthDate.getTimeInMillis()>Calendar.getInstance().getTimeInMillis()) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Birthday must be before the current date.", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                birthPerson = new BirthPerson();
                birthPerson.setName(nameEdit.getText().toString());
                birthPerson.setBirthdayStamp(birthDate.getTimeInMillis());
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.copyToRealm(birthPerson);
                realm.commitTransaction();
                if(addToCalendar) {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.putExtra(CalendarContract.Events.TITLE, birthPerson.getName()+"'s birthday");
                    intent.putExtra(CalendarContract.Events.ALL_DAY, true);
                    birthDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                    if(birthDate.before(Calendar.getInstance())) {
                        birthDate.add(Calendar.YEAR,1);
                    }
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, birthDate.getTimeInMillis());
                    mContext.startActivity(intent);
                    finish();
                } else {
                    onBackPressed();
                }
            }
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "You must enter a name.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private Calendar parseSpeechToDate(String text) {
        Calendar calendar = Calendar.getInstance();
        int year = -1;
        int day = 0;
        String month = "";
        int monthNumber;
        Matcher m;
        Pattern dayClassic = Pattern.compile("(^|\\s+)([0-9]{1}|[0-9]{2})(st|nd|rd|th)($|\\s+)");
        m = dayClassic.matcher(text);
        if(m.find()) {
            day = Integer.parseInt(m.group().toString().trim().replaceAll("(st|nd|rd|th)",""));
        } else {
            Pattern dayShort = Pattern.compile("(^|\\s+)(\\s*)([0-9]{1}|[0-9]{2})(\\s*)($|\\s+)");
            m = dayShort.matcher(text);
            if(m.find()) {
                day = Integer.parseInt(m.group().trim());
            }
        }

        Pattern yearPattern = Pattern.compile("(^|\\s)([0-9]{4})($|\\s)");
        m = yearPattern.matcher(text);
        if(m.find()) {
            year = Integer.parseInt(m.group().trim());
        }

        Pattern monthPattern = Pattern.compile("(^|\\s)(January|February|March|April|May|June|July|August|September|October|November|December)(^|\\s)",Pattern.CASE_INSENSITIVE);
        m = monthPattern.matcher(text);
        if(m.find()) {
            month = m.group().trim();
        }

        if(day>0 && year>0 && month.length()>0) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, returnCalendarMonthNumber(month));
            if(day<=calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                return null;
            }
        } else {
            return null;
        }
        return calendar;
    }

    public int returnCalendarMonthNumber(String month) {
        switch(month.trim().toUpperCase()) {
            case "JANUARY":
                return 0;
            case "FEBRUARY":
                return 1;
            case "MARCH":
                return 2;
            case "APRIL":
                return 3;
            case "MAY":
                return 4;
            case "JUNE":
                return 5;
            case "JULY":
                return 6;
            case "AUGUST":
                return 7;
            case "SEPTEMBER":
                return 8;
            case "OCTOBER":
                return 9;
            case "NOVEMBER":
                return 10;
            case "DECEMBER":
                return 11;
                default: return 0;
        }
    }

}