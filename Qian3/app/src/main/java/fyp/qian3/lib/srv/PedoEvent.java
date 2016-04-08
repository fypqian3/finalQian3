package fyp.qian3.lib.srv;

public class PedoEvent {
    // Parameters for PedoEventListener
    private onPedoEventListener mOnPedoEventListener;

    public interface onPedoEventListener {
        void onPedoDetected();
    }

    public PedoEvent(onPedoEventListener listener) {
        this.mOnPedoEventListener = listener;
    }

    public void setOnPedoEventListener(onPedoEventListener listener) {
        mOnPedoEventListener = listener;
    }

    public void callChangeListener() {
        if (mOnPedoEventListener != null) {
            mOnPedoEventListener.onPedoDetected();
        }
    }
}