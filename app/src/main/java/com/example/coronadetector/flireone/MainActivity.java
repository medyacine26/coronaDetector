/*
 * ******************************************************************
 * @title FLIR THERMAL SDK
 * @file MainActivity.java
 * @Author FLIR Systems AB
 *
 * @brief  Main UI of test application
 *
 * Copyright 2019:    FLIR Systems
 * ******************************************************************/
package com.example.coronadetector.flireone;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coronadetector.BuildConfig;
import com.example.coronadetector.R;
import com.example.coronadetector.map.Activity.MapsMarkerActivity;
import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.log.ThermalLog;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sample application for scanning a FLIR ONE or a built in emulator
 * <p>
 * See the {@link CameraHandler} for how to preform discovery of a FLIR ONE camera, connecting to it and start streaming images
 * <p>
 * The MainActivity is primarily focused to "glue" different helper classes together and updating the UI components
 * <p/>
 * Please note, this is <b>NOT</b> production quality code, error handling has been kept to a minimum to keep the code as clear and concise as possible
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Handles Android permission for eg Network
    private PermissionHandler permissionHandler;

    //Handles network camera operations
    private CameraHandler cameraHandler;
    private Identity connectedIdentity = null;

    private ImageView photoImage;
    private FaceDetector detector;
    Bitmap editedMsxBitmap;
    ProcessImage pi;
    Boolean isThreadWorking=false;
    ImageView plusSign;
    TextView tempTv;
    ToggleButton tgbtn;
    float dip = 30f;
    int plusSignSize=0;
    boolean isManuel=true;
    private DecimalFormat df = new DecimalFormat("0.0");

    private LinkedBlockingQueue<FrameDataHolder> framesBuffer = new LinkedBlockingQueue(21);
    private UsbPermissionHandler usbPermissionHandler = new UsbPermissionHandler();
    Dialog dangerDialog;
    Dialog gifDialog;
    TextView dialogTv;
    ImageView popupDissBtn;
    ImageView popupDissGifBtn;
    Button popupGotBtn;
    TextView temperatureTv;
    Activity thisAct=this;

    /**
     * Show message on the screen
     */
    public interface ShowMessage {
        void show(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main_flir);

        ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE;


        detector = new FaceDetector.Builder(getApplicationContext())
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(false) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.NO_LANDMARKS)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .setMinFaceSize(0.35f)
                .build();
         ;
        //ThermalSdkAndroid has to be initiated from a Activity with the Application Context to prevent leaking Context,
        // and before ANY using any ThermalSdkAndroid functions
        //ThermalLog will show log from the Thermal SDK in standards android log framework
        ThermalSdkAndroid.init(getApplicationContext(), enableLoggingInDebug);

        permissionHandler = new PermissionHandler(showMessage, MainActivity.this);

        cameraHandler = new CameraHandler();
        plusSign=findViewById(R.id.plusSign);
        tempTv=findViewById(R.id.tempTv);
        plusSign.bringToFront();
       // tgbtn=findViewById(R.id.toggleButton);

        cameraHandler.initViews(this);

        setupViews();

        Resources r = getResources();
        plusSignSize = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        ));
        startDiscovery();
        connect(cameraHandler.getFlirOne());
        initDialog();
        initDialogDemo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_contact, menu);
        super.onCreateOptionsMenu(menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.connect_camera:
                startDiscovery();
                connect(cameraHandler.getFlirOne());
                return true;



        }
        return super.onOptionsItemSelected(item);
    }
    public void startDiscovery(View view) {
        startDiscovery();
    }

    public void stopDiscovery(View view) {
        stopDiscovery();
    }


    public void connectFlirOne(View view) {
        connect(cameraHandler.getFlirOne());
    }

    public void connectSimulatorOne(View view) {
        connect(cameraHandler.getCppEmulator());
    }

    public void connectSimulatorTwo(View view) {
        connect(cameraHandler.getFlirOneEmulator());
    }

    public void disconnect(View view) {
        disconnect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopDiscovery();

    }

    public class ProcessImage extends Thread {


        Bitmap bitmap;
        Bitmap msxBitmap;
        ImageView imageView;
        Paint paint;
        float scale;
        Frame frame;
        SparseArray<Face> faces;


        public ProcessImage() {
        }

        public void setData(Bitmap bitmap, Bitmap msxBitmap, ImageView imageView) {
            this.bitmap = bitmap;
            this.msxBitmap = msxBitmap;
            this.imageView = imageView;

            scale = getResources().getDisplayMetrics().density;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setTextSize((int) (16 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
        }

        public void run() {
            if (detector.isOperational() && bitmap != null) {
                try {





//                                t=new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//
//                                        frame = new Frame.Builder().setBitmap(bitmap).build();
//                                        faces = detector.detect(frame);
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                imageView.setImageBitmap(msxBitmap);
//                                                plusSign.setVisibility(View.GONE);
//
//
//
//                                            }
//                                        });
//                                    }
//                                });
//                                t.start();
//
//
//
//                            waitForThreadComplete(t);

                    frame = new Frame.Builder().setBitmap(bitmap).build();
                    faces = detector.detect(frame);


//                   thread.start();
                    editedMsxBitmap = Bitmap.createBitmap(msxBitmap.getWidth(), msxBitmap
                            .getHeight(), msxBitmap.getConfig());

                    Canvas canvas = new Canvas(editedMsxBitmap);
                    canvas.drawBitmap(msxBitmap, 0, 0, paint);


                    if(faces!=null && faces.size()!=0)
                    {


                       // for (int index = 0; index < faces.size(); ++index) {
                            Face face = faces.valueAt(0);


                            canvas.drawRect(
                                    face.getPosition().x,
                                    face.getPosition().y,
                                    face.getPosition().x + face.getWidth(),
                                    face.getPosition().y + face.getHeight(), paint);

                       // }

                        float xCenter=(face.getPosition().x*2+face.getWidth())/2;
                        float yCenter=(face.getPosition().y*2+face.getHeight())/2;


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(editedMsxBitmap);
                                plusSign.setVisibility(View.VISIBLE);

                                plusSign.setX(xCenter);
                                plusSign.setY(yCenter);

                                cameraHandler.getHeatAtPlus(plusSign,xCenter,yCenter);

                            }
                        });
                    }



                } catch (Exception ex) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            plusSign.setVisibility(View.GONE);

                        }
                    });
                }


            }
            isThreadWorking = false;


        }
    }





    /**
     * Handle Android permission request response for Bluetooth permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Connect to a Camera
     */
    private void connect(Identity identity) {
        //We don't have to stop a discovery but it's nice to do if we have found the camera that we are looking for
        cameraHandler.stopDiscovery(discoveryStatusListener);

        if (connectedIdentity != null) {
            Log.d(TAG, "connect(), in *this* code sample we only support one camera connection at the time");
          //  showMessage.show("connect(), in *this* code sample we only support one camera connection at the time");

            connectedIdentity = null;
            return;
        }

        if (identity == null) {
            Log.d(TAG, "connect(), can't connect, no camera available");
            //showMessage.show("connect(), can't connect, no camera available");
            return;
        }

        connectedIdentity = identity;

       // updateConnectionText(identity, ConnectionStatus.CONNECTING);
        //IF your using "USB_DEVICE_ATTACHED" and "usb-device vendor-id" in the Android Manifest
        // you don't need to request permission, see documentation for more information
        if (UsbPermissionHandler.isFlirOne(identity)) {
            usbPermissionHandler.requestFlirOnePermisson(identity, this, permissionListener);
        } else {
            cameraHandler.connect(identity, connectionStatusListener);
        }

    }

    private UsbPermissionHandler.UsbPermissionListener permissionListener = new UsbPermissionHandler.UsbPermissionListener() {
        @Override
        public void permissionGranted(Identity identity) {
            cameraHandler.connect(identity, connectionStatusListener);
        }

        @Override
        public void permissionDenied(Identity identity) {
            MainActivity.this.showMessage.show("Permission was denied for identity ");
        }

        @Override
        public void error(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, final Identity identity) {
            MainActivity.this.showMessage.show("Error when asking for permission for FLIR ONE, error:"+errorType+ " identity:" +identity);
        }
    };

    /**
     * Disconnect to a camera
     */
    private void disconnect() {
        connectedIdentity = null;
        Log.d(TAG, "disconnect() called with: connectedIdentity = [" + connectedIdentity + "]");
        cameraHandler.disconnect();
    }

    /**
     * Update the UI text for connection status
     */
    private void updateConnectionText(Identity identity, ConnectionStatus status) {
        String deviceId = identity != null ? identity.deviceId : "";
    }

    /**
     * Start camera discovery
     */
    private void startDiscovery() {
        cameraHandler.startDiscovery(cameraDiscoveryListener, discoveryStatusListener);
    }

    /**
     * Stop camera discovery
     */
    private void stopDiscovery() {
        cameraHandler.stopDiscovery(discoveryStatusListener);
    }

    /**
     * Callback for discovery status, using it to update UI
     */
    private CameraHandler.DiscoveryStatus discoveryStatusListener = new CameraHandler.DiscoveryStatus() {
        @Override
        public void started() {
        }

        @Override
        public void stopped() {
        }
    };

    /**
     * Camera connecting state thermalImageStreamListener, keeps track of if the camera is connected or not
     * <p>
     * Note that callbacks are received on a non-ui thread so have to eg use {@link #runOnUiThread(Runnable)} to interact view UI components
     */
    private ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void onConnectionStatusChanged(@NotNull ConnectionStatus connectionStatus, @org.jetbrains.annotations.Nullable ErrorCode errorCode) {
            Log.d(TAG, "onConnectionStatusChanged connectionStatus:" + connectionStatus + " errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectionText(connectedIdentity, connectionStatus);

                    switch (connectionStatus) {
                        case CONNECTING: break;
                        case CONNECTED: {
                            cameraHandler.startStream(streamDataListener);
                        }
                        break;
                        case DISCONNECTING: break;
                        case DISCONNECTED: break;
                    }
                }
            });
        }
    };

    private final CameraHandler.StreamDataListener streamDataListener = new CameraHandler.StreamDataListener() {

        @Override
        public void images(FrameDataHolder dataHolder) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //msxImage.setImageBitmap(dataHolder.msxBitmap);
                  //  photoImage.setImageBitmap(dataHolder.dcBitmap);
//                    if(!isThreadWorking) {
//                        isThreadWorking = true;
//                        waitForFdetThreadComplete();
//                        pi = new ProcessImage();
//                        pi.setData(dataHolder.dcBitmap, dataHolder.msxBitmap, photoImage);
//                        pi.start();
//                    }
                }
            });
        }

        @Override
        public void images(Bitmap test, Bitmap dcBitmap) {

            try {
                framesBuffer.put(new FrameDataHolder(test,dcBitmap));
            } catch (InterruptedException e) {
                //if interrupted while waiting for adding a new item in the queue
                Log.e(TAG,"images(), unable to add incoming images to frames buffer, exception:"+e);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    FrameDataHolder poll = framesBuffer.poll();

                    if(isManuel)
                    {


                     /*   FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(plusSignSize,plusSignSize);
                        params.gravity = Gravity.CENTER;


                        //plusSign.setForegroundGravity(Gravity.CENTER);


                        plusSign.setLayoutParams(params);
*/
                        plusSign.setVisibility(View.VISIBLE);


                        photoImage.setImageBitmap(poll.msxBitmap);


//                        float xCenter=(plusSign.getX()*2+plusSign.getWidth())/2;
//                        float yCenter=(plusSign.getY()*2+plusSign.getHeight())/2;
//
                        float xCenter=poll.msxBitmap.getWidth()/2;
                        float yCenter=poll.msxBitmap.getHeight()/2;
                        cameraHandler.manualHeatAtPlus(xCenter,yCenter);

                        double temp=cameraHandler.getCurrTemp();
                        if(temp>=38.5f)
                        {
                            if(!dangerDialog.isShowing())
                            {
                                dialogTv.setText(df.format(temp)+" °C");
                                dangerDialog.show();
                            }
                        }



                    }
                    else
                    {

//                        if(!isThreadWorking)
//                        {
//                            isThreadWorking = true;
                           // waitForFdetThreadComplete();
                            pi=new ProcessImage();
                            pi.setData(poll.dcBitmap,poll.msxBitmap,photoImage);
                            pi.start();
                            // msxImage.setImageBitmap(poll.msxBitmap);
                            // photoImage.setImageBitmap(poll.msxBitmap);

                      //  }



                    }



                }
            });

        }
    };

    public void initDialog()
    {
        dangerDialog=new Dialog(this);
        dangerDialog.setContentView(R.layout.negative_popup);

        popupDissBtn=dangerDialog.findViewById(R.id.popupDissBtn);
        popupGotBtn=dangerDialog.findViewById(R.id.popupGotBtn);
        dialogTv=dangerDialog.findViewById(R.id.dialogTv);


        popupDissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dangerDialog.dismiss();
            }
        });
        popupGotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dangerDialog.dismiss();
            }
        });

        dangerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        temperatureTv=findViewById(R.id.tempTv);



    }
    public void initDialogDemo()
    {
        gifDialog=new Dialog(this);
        gifDialog.setContentView(R.layout.flir_gif_popup);

        popupDissGifBtn=gifDialog.findViewById(R.id.popupDissGifBtn);


        popupDissGifBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifDialog.dismiss();
            }
        });


        gifDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));



        gifDialog.show();


    }
    private void waitForFdetThreadComplete() {
        if (pi == null) {
            return;
        }

        if (pi.isAlive()) {
            try {
                pi.join();
                pi = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void waitForThreadComplete(Thread thread) {
        if (thread == null) {
            return;
        }

        if (thread.isAlive()) {
            try {
                thread.join();
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * Camera Discovery thermalImageStreamListener, is notified if a new camera was found during a active discovery phase
     * <p>
     * Note that callbacks are received on a non-ui thread so have to eg use {@link #runOnUiThread(Runnable)} to interact view UI components
     */
    private DiscoveryEventListener cameraDiscoveryListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            Log.d(TAG, "onCameraFound identity:" + identity);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraHandler.add(identity);
                }
            });
        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.d(TAG, "onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopDiscovery();
                    MainActivity.this.showMessage.show("onDiscoveryError communicationInterface:" + communicationInterface + " errorCode:" + errorCode);
                }
            });
        }
    };

    private ShowMessage showMessage = new ShowMessage() {
        @Override
        public void show(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };



    private void setupViews() {


        photoImage = findViewById(R.id.photo_image);
    }





}
