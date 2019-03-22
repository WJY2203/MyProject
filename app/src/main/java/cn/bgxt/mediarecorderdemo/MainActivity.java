package cn.bgxt.mediarecorderdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;

import java.io.File;
import android.app.Activity;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends Activity {

    private Button btn_RecordStart, btn_RecordStop;
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private File file= new File("/sdcard/mediarecorder.amr");      /* 存储路径 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btn_RecordStart = (Button) findViewById(R.id.btn_RecordStart);
        btn_RecordStop = (Button) findViewById(R.id.btn_RecordStop);

        btn_RecordStop.setEnabled(false);

        btn_RecordStart.setOnClickListener(click);
        btn_RecordStop.setOnClickListener(click);
    }

    private View.OnClickListener click = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_RecordStart:
                    startRecord();
                    break;
                case R.id.btn_RecordStop:
                    stopRecord();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开始录音
     */
    protected void startRecord() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            if (file.exists()) {                                             /* 检测文件是否存在 */
                file.delete();
            }
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);        /* 设置麦克风 */
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setAudioChannels(1);                                  /* 单声道采样 */
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);   /* 设置输出格式 */
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);   /* 设置采样波形 */
            mediaRecorder.setAudioEncodingBitRate(16000);
            mediaRecorder.setOutputFile(file.getAbsolutePath());             /* 存储路径 */
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();     /* 开始录音 */
                isRecording = false;
                btn_RecordStart.setEnabled(false);
                btn_RecordStop.setEnabled(true);
                Toast.makeText(MainActivity.this, "Begin to input voice", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*protected void start() {
        if (mediaRecorder==null) {
            mediaRecorder = new MediaRecorder();// 设置音频录入源
        }
        try {
            //File file = new File("/sdcard/mediarecorder.amr");
            if (file.exists()) {
                // 如果文件存在，删除它，演示代码保证设备上只有一个录音文件
                file.delete();
            }


            mediaRecorder = new MediaRecorder();
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置录制音频的输出格式
            mediaRecorder.setAudioSamplingRate(8000);
            mediaRecorder.setAudioChannels(1);
            单声道采样
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            // 设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置录制音频文件输出文件路径
            mediaRecorder.setAudioEncodingBitRate(16000);
            mediaRecorder.setOutputFile(file.getAbsolutePath());

            mediaRecorder.setOnErrorListener(new OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    isRecording = false;
                    btn_RecordStart.setEnabled(true);
                    btn_RecordStop.setEnabled(false);
                    Toast.makeText(MainActivity.this, "录音发生错误", Toast.LENGTH_SHORT).show();
                }
            });
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();     /* 开始录音 */
                /*isRecording = true;
                btn_RecordStart.setEnabled(false);
                btn_RecordStop.setEnabled(true);
                Toast.makeText(MainActivity.this, "Begin to input voice", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    /**
     * 录音结束
     */
    protected void stopRecord() {
        if (file != null && file.exists()) {
            mediaRecorder.stop();         /* 停止录音 */
            mediaRecorder.release();      /* 释放资源 */
            mediaRecorder = null;
            isRecording = false;
            btn_RecordStart.setEnabled(true);
            btn_RecordStop.setEnabled(false);
            Toast.makeText(MainActivity.this, "Stop Inputting voice", Toast.LENGTH_SHORT).show();
                /* 开始识别 */
            try {
                getToken();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*protected void stop() {
        if (isRecording) {
            // 如果正在录音，停止并释放资源
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording=false;
            btn_RecordStart.setEnabled(true);
            btn_RecordStop.setEnabled(false);

            Toast.makeText(MainActivity.this, "录音结束", Toast.LENGTH_SHORT).show();
        }try {
            getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    private static final String serverURL = "http://vop.baidu.com/server_api";   //语音识别网关
    private static String token = null;
    private static final String apiKey = "98C36KDSvikCvnW43M9Eqtbj";             // API Key 
    private static final String secretKey = "yLMHBfjK2M41XmS8qKN6r9F3iTtO5wUA";  // Secret Key
    private static final String cuid = "862217035490850";            //唯一表示码

    private void getToken(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" +"&client_id=" + apiKey + "&client_secret=" + secretKey;
                try {
                    connection = (HttpURLConnection) new URL(getTokenURL).openConnection();
                    token = new JSONObject(printResponse(connection)).getString("access_token");
                    SpeechRecognition();    //开始语音识别
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(connection!= null) connection.disconnect();
                }
            }
        }).start();
    }
    private void SpeechRecognition(){
        file = new File("/sdcard/mediarecorder.amr");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strc;
                try {
                    File pcmFile = new File(file.getAbsolutePath());
                    HttpURLConnection conn = (HttpURLConnection) new URL(serverURL+ "?cuid=" + cuid + "&token=" + token).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "audio/amr; rate=16000");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(loadFile(pcmFile));
                    wr.flush();
                    wr.close();
                    strc=printResponse(conn);
                    Message message = new Message();
                    message.what = 0x02;
                    message.obj =strc;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //字符处理
    private  String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        Message message = new Message();
        message.what = 0x01;
        message.obj = new JSONObject(response.toString()).toString(4);
        handler.sendMessage(message);
        return response.toString();
    }
    //文件加载
    private byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            String response=(String)msg.obj;
            String strc=null;
            switch (msg.what){
                case 0x01:
                    Log.e("return:",response);            //得到返回的所有结果
                    break;
                case 0x02:
                    strc=getRectstr(response,"[","]");    //得到返回语音内容
                    Log.d("return:",strc);
                    TextView text_curtime = (TextView) this.findViewById(R.id.record_voice);

                    break;
                default:
                    break;
            }
        }
    };

    private String getRectstr(String str,String strStart, String strEnd){
        if (str.indexOf(strStart) < 0 || str.indexOf(strEnd) < 0) return "";
        return str.substring(str.indexOf(strStart) + strStart.length()+1, str.indexOf(strEnd)-2);
    }
}