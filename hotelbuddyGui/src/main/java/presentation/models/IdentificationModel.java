package presentation.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Patrick on 24.06.2015.
 */
public class IdentificationModel
{
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty birthDate = new SimpleStringProperty("");

    public String getName()
    {
        return name.get();
    }

    public StringProperty nameProperty()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name.set(name);
    }

    public String getBirthDate()
    {
        return birthDate.get();
    }

    public StringProperty birthDateProperty()
    {
        return birthDate;
    }

    public void setBirthDate(String birthDate)
    {
        this.birthDate.set(birthDate);
    }
}
