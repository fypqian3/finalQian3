package com.counter.app.lib.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerPreference extends DialogPreference {

    private static final String androidns="http://schemas.android.com/apk/res/android";

    private Context mContext;
    private AttributeSet mAttrs;
    private DatePicker nPicker;
    private int mValue, mDefault;

    public DatePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 19950001);
    }

    @Override
    protected View onCreateDialogView() {
        nPicker = new DatePicker(mContext);
        nPicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
        nPicker.updateDate(mValue / 10000, (mValue / 100) % 100, mValue % 100);
        //nPicker.setSpinnersShown(false);

        return nPicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        nPicker.clearFocus();
        if (positiveResult) {
            syncValue();
            Log.d("DatePickerPreference", "mValue : " + mValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setInitValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }

    public void setInitValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mValue) {
            mValue = value;
            notifyChanged();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, mDefault);
    }

    private void setValue (int value, boolean notifyChanged) {
        if (value != mValue) {
            mValue = value;
            persistInt(value);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    void syncValue() {
        int value = nPicker.getYear()*10000 + nPicker.getMonth()*100 + nPicker.getDayOfMonth();
        if (value != mValue) {
            if (callChangeListener(value)) {
                setValue(value, false);
            } else {
                nPicker.updateDate(mValue/10000, (mValue/100)%100, mValue%100);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */

        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.value = mValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mValue = myState.value;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
