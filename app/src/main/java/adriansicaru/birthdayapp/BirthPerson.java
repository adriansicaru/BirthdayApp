package adriansicaru.birthdayapp;

import io.realm.RealmObject;

/**
 * Created by adriansicaru on 3/12/18.
 */

public class BirthPerson extends RealmObject {
    private String name;
    private long birthdayStamp;

    public long getBirthdayStamp() {
        return birthdayStamp;
    }

    public void setBirthdayStamp(long birthdayStamp) {
        this.birthdayStamp = birthdayStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
