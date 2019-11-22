package air.kanna.mystorage.sync.service;

import android.util.Log;

import java.io.File;
import java.net.Socket;

import air.kanna.mystorage.sync.model.ConnectParam;
import air.kanna.mystorage.sync.process.LocalFileReceiveSyncProcess;

public class SyncClient{
    private Socket socket;
    private LocalFileReceiveSyncProcess receiveProcess;

    public void start(ConnectParam param, File file){
        try {
            receiveProcess = new LocalFileReceiveSyncProcess(param, file);
            socket = new Socket(param.getIp(), param.getPort());
            Log.i(getClass().getName(), "Connect to: " + param.getIp());
            
            receiveProcess.start(socket);
            
        }catch(Exception e) {
            if(receiveProcess != null && receiveProcess.isFinish()) {
                
            }else {
                Log.e(getClass().getName(), "error", e);
            }
        }finally {
            Log.i(getClass().getName(), "Client finish");
            if(receiveProcess != null && !receiveProcess.isFinish()) {
                try {
                    receiveProcess.finish();
                } catch (Exception e) {
                    Log.e(getClass().getName(), "finish error", e);
                }
            }
        }
    }
}
