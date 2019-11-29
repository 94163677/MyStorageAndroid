package air.kanna.mystorage.android;

import android.widget.ProgressBar;
import android.widget.TextView;

import air.kanna.mystorage.sync.process.ProcessListener;
import air.kanna.mystorage.util.StringUtil;

public class AndroidProcessListener implements ProcessListener {
    private int current = 0;
    private String name = "";

    private TextView processMsg = null;
    private ProgressBar processBar = null;

    public AndroidProcessListener(TextView processMsg, ProgressBar processBar){
        if(processMsg == null || processBar == null){
            throw new NullPointerException("TextView or ProgressBar is null");
        }
        this.processMsg = processMsg;
        this.processBar = processBar;
        this.processBar.setMin(0);
    }

    @Override
    public void setMax(int max) {
        if(max <= 0){
            throw new IllegalArgumentException("ProcessBar's max must > 0");
        }
        current = 0;
        processBar.setMax(max);
    }

    @Override
    public void next(String message) {
        if(processBar.getProgress() >= processBar.getMax()){
            finish(message);
        }else{
            setPosition(processBar.getProgress() + 1, message);
        }
    }

    @Override
    public void setPosition(int current, String message) {
        if(current < 0){
            current = 0;
        }
        if(current >= processBar.getMax()){
            finish(message);
        }
        processMsg.setText(
                new StringBuilder()
                        .append(name).append('(').append(current)
                        .append('/').append(processBar.getMax()).append(')')
                        .toString());
        processBar.setProgress(current);
    }

    @Override
    public void finish(String message) {
        processMsg.setText("");
        processBar.setProgress(processBar.getMax());
    }
}
