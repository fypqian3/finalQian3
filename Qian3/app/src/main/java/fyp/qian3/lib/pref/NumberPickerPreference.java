package fyp.qian3.lib.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

    private static final String androidns="http://schemas.android.com/apk/res/android";
    private static final String qian3ns="http://schemas.android.com/apk/fyp.qian3.lib.pref.SeekBarPreference";

    private NumberPicker nPicker;
    private int mValue, mMin, mMax, mDefault;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
        mMin = attrs.getAttributeIntValue(qian3ns,"min", 0);
        mMax = attrs.getAttributeIntValue(androidns,"max", 500);

    }

    @Override
    protected View onCreateDialogView() {
        nPicker = new NumberPicker(getContext());
        nPicker.setMinValue(mMin);
        nPicker.setMaxValue(mMax);
        nPicker.setValue(mValue);

        return nPicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        nPicker.clearFocus();
        if (positiveResult) {
            syncValue();
            Log.d("NumberPickerPreference", "mValue : " + mValue);
            Log.d("NumberPickerPreference", "NumberPickerValue : " + nPicker.getValue());
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
        int value = nPicker.getValue();
        if (value != mValue) {
            if (callChangeListener(value)) {
                setValue(value, false);
            } else {
                nPicker.setValue(mValue);
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
        myState.min = mMin;
        myState.max = mMax;
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
        mMin = myState.min;
        mMax = myState.max;
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
        int min, max;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            min = source.readInt();
            max = source.readInt();
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(min);
            dest.writeInt(max);
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
