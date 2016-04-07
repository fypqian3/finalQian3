package fyp.qian3.lib.srv;

public class PedoEvent {

    // Parameters for PedoEventListener
    private onPedoEventListener mOnPedoEventListener;

    public PedoEvent() {
        this.mOnPedoEventListener = null;
    }

    public void setOnPedoEventListener(onPedoEventListener listener) {
        mOnPedoEventListener = listener;
    }

    protected void callChangeListener() {
        if (mOnPedoEventListener != null) {
            mOnPedoEventListener.onPedoDetected();
        }
    }
}