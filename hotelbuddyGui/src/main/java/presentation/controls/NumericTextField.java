package presentation.controls;

import javafx.scene.control.TextField;

/**
 * Created by Patrick on 07.07.2015.
 */
public class NumericTextField extends TextField
{
    private int maxlength;

    public NumericTextField()
    {
        this.maxlength = 10;
    }

    public void setMaxlength(int maxlength)
    {
        this.maxlength = maxlength;
    }

    @Override
    public void replaceText(int start, int end, String text)
    {
        if (validate(text))
        {
            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text)
    {
        if (validate(text))
        {
            super.replaceSelection(text);
        }
    }

    private boolean validate(String text)
    {
        return "".equals(text) || (text.matches("[0-9]") && getText().length() < maxlength);
    }
}