/*******************************************************************
 * @title FLIR THERMAL SDK
 * @file CameraHandler.java
 * @Author FLIR Systems AB
 *
 * @brief Helper class that encapsulates *most* interactions with a FLIR ONE camera
 *
 * Copyright 2019:    FLIR Systems
 ********************************************************************/
package com.example.coronadetector.flireone;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coronadetector.R;
import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.Point;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.PaletteManager;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the handling of a FLIR ONE camera or built in emulator, discovery, connecting and start receiving images.
 * All listeners are called from Thermal SDK on a non-ui thread
 * <p/>
 * Usage:
 * <pre>
 * Start discovery of FLIR FLIR ONE cameras or built in FLIR ONE cameras emulators
 * {@linkplain #startDiscovery(DiscoveryEventListener, DiscoveryStatus)}
 * Use a discovered Camera {@linkplain Identity} and connect to the Camera
 * {@linkplain #connect(Identity, ConnectionStatusListener)}
 * Once connected to a camera
 * {@linkplain #startStream(StreamDataListener)}
 * </pre>
 * <p/>
 * You don't *have* to specify your application to listen or USB intents but it might be beneficial for you application,
 * we are enumerating the USB devices during the discovery process which eliminates the need to listen for USB intents.
 * See the Android documentation about USB Host mode for more information
 * <p/>
 * Please note, this is <b>NOT</b> production quality code, error handling has been kept to a minimum to keep the code as clear and concise as possible
 */
class CameraHandler {

    private static final String TAG = "CameraHandler";
    private DecimalFormat df = new DecimalFormat("0.0");
    private StreamDataListener streamDataListener;

    public interface StreamDataListener {
        void images(FrameDataHolder dataHolder);
        void images(Bitmap msxBitmap, Bitmap dcBitmap);
    }

    //Discovered FLIR cameras
    LinkedList<Identity> foundCameraIdentities = new LinkedList<>();

    //A FLIR Camera
    private Camera camera;


    Point plusPoint;
    Point plusPointtl;
    Point plusPointtm;
    Point plusPointtr;
    Point plusPointrm;
    Point plusPointrb;
    Point plusPointbm;
    Point plusPointbl;
    Point plusPointlm;


    TextView temperatureTv;


    Activity activity;

    private double currTemp=0;




    public interface DiscoveryStatus {
        void started();
        void stopped();
    }

    public CameraHandler() {


    }

    public double getCurrTemp()
    {
        return currTemp;
    }
    /**
     * Start discovery of USB and Emulators
     */
    public void startDiscovery(DiscoveryEventListener cameraDiscoveryListener, DiscoveryStatus discoveryStatus) {
        DiscoveryFactory.getInstance().scan(cameraDiscoveryListener, CommunicationInterface.EMULATOR, CommunicationInterface.USB);
        discoveryStatus.started();

    }

    /**
     * Stop discovery of USB and Emulators
     */
    public void stopDiscovery(DiscoveryStatus discoveryStatus) {
        DiscoveryFactory.getInstance().stop(CommunicationInterface.EMULATOR, CommunicationInterface.USB);
        discoveryStatus.stopped();
    }

    public void connect(Identity identity, ConnectionStatusListener connectionStatusListener) {
        camera = new Camera();
        camera.connect(identity, connectionStatusListener);
    }

    public void disconnect() {
        if (camera == null) {
            return;

        }
        if (camera.isGrabbing()) {
            camera.unsubscribeAllStreams();
        }
        camera.disconnect();
    }

    /**
     * Start a stream of {@link ThermalImage}s images from a FLIR ONE or emulator
     */
    public void startStream(StreamDataListener listener) {
        this.streamDataListener = listener;
        camera.subscribeStream(thermalImageStreamListener);
    }

    /**
     * Stop a stream of {@link ThermalImage}s images from a FLIR ONE or emulator
     */
    public void stopStream(ThermalImageStreamListener listener) {
        camera.unsubscribeStream(listener);
    }

    /**
     * Add a found camera to the list of known cameras
     */
    public void add(Identity identity) {
        foundCameraIdentities.add(identity);
    }

    @Nullable
    public Identity get(int i) {
        return foundCameraIdentities.get(i);
    }

    /**
     * Get a read only list of all found cameras
     */
    @Nullable
    public List<Identity> getCameraList() {
        return Collections.unmodifiableList(foundCameraIdentities);
    }

    /**
     * Clear all known network cameras
     */
    public void clear() {
        foundCameraIdentities.clear();
    }

    @Nullable
    public Identity getCppEmulator() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            if (foundCameraIdentity.deviceId.contains("C++ Emulator")) {
                return foundCameraIdentity;
            }
        }
        return null;
    }

    @Nullable
    public Identity getFlirOneEmulator() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            if (foundCameraIdentity.deviceId.contains("EMULATED FLIR ONE")) {
                return foundCameraIdentity;
            }
        }
        return null;
    }

    @Nullable
    public Identity getFlirOne() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            boolean isFlirOneEmulator = foundCameraIdentity.deviceId.contains("EMULATED FLIR ONE");
            boolean isCppEmulator = foundCameraIdentity.deviceId.contains("C++ Emulator");
            if (!isFlirOneEmulator && !isCppEmulator) {
                return foundCameraIdentity;
            }
        }

        return null;
    }

    private void withImage(ThermalImageStreamListener listener, Camera.Consumer<ThermalImage> functionToRun) {
        camera.withImage(listener, functionToRun);
    }


    /**
     * Called whenever there is a new Thermal Image available, should be used in conjunction with {@link Camera.Consumer}
     */
    private final ThermalImageStreamListener thermalImageStreamListener = new ThermalImageStreamListener() {
        @Override
        public void onImageReceived() {
            //Will be called on a non-ui thread
            Log.d(TAG, "onImageReceived(), we got another ThermalImage");

            withImage(this, handleIncomingImage);
        }
    };

    /**
     * Function to process a Thermal Image and update UI
     */
    private final Camera.Consumer<ThermalImage> handleIncomingImage = new Camera.Consumer<ThermalImage>() {
        @Override
        public void accept(ThermalImage thermalImage) {
            Log.d(TAG, "accept() called with: thermalImage = [" + thermalImage.getDescription() + "]");
            //Will be called on a non-ui thread,
            // extract information on the background thread and send the specific information to the UI thread

            //Get a bitmap with only IR data
            Bitmap msxBitmap;
            {
                thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
                thermalImage.setPalette(PaletteManager.getDefaultPalettes().get(0));
                msxBitmap = BitmapAndroid.createBitmap(thermalImage.getImage()).getBitMap();


            }

            try {
                if(plusPoint!=null)
                {



                    double temp= thermalImage.getValueAt(plusPoint);
                    double c= temp- 273.15;
                    double temp2= thermalImage.getValueAt(plusPointtl);
                    double c2= temp2- 273.15;
                    double temp3= thermalImage.getValueAt(plusPointtm);
                    double c3= temp3- 273.15;
                    double temp4= thermalImage.getValueAt(plusPointtr);
                    double c4= temp4- 273.15;
                    double temp5= thermalImage.getValueAt(plusPointrm);
                    double c5= temp5- 273.15;
                    double temp6= thermalImage.getValueAt(plusPointrb);
                    double c6= temp6- 273.15;
                    double temp7= thermalImage.getValueAt(plusPointbm);
                    double c7= temp7- 273.15;
                    double temp8= thermalImage.getValueAt(plusPointbl);
                    double c8= temp8- 273.15;
                    double temp9= thermalImage.getValueAt(plusPointlm);
                    double c9= temp9- 273.15;



                    List<Double> temps = Arrays.asList(c,c2,c3,c4,c5,c6,c7,c8,c9);

                   currTemp=manageHeats(temps);






                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temperatureTv.setText(df.format(currTemp)+" °C");




                        }
                    });

                }
            }
            catch (Exception ex)
            {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        temperatureTv.setText(ex.getMessage());

                    }
                });
            }


            //Get a bitmap with the visual image, it might have different dimensions then the bitmap from THERMAL_ONLY
            Bitmap dcBitmap = BitmapAndroid.createBitmap(thermalImage.getFusion().getPhoto()).getBitMap();

            streamDataListener.images(msxBitmap,dcBitmap);
        }
    };
    public void initViews(Activity activity)
    {
        temperatureTv=activity.findViewById(R.id.tempTv);

        this.activity=activity;
    }
    public void getHeatAtPlus(ImageView plusSign, float xCenter, float yCenter)
    {
        try
        {

                Point plusP=new Point( Math.round(xCenter), Math.round(yCenter));

                plusPoint=plusP;
//                plusSign.setX(plusP.x);
//                plusSign.setY(plusP.y);




        }
        catch (Exception ex)
        {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    temperatureTv.setText(ex.getMessage());

                }
            });        }




    }

    public double manageHeats(List<Double> temps)
    {
        int moy=1;

        double moyRes=temps.get(0);

        float diff=.2f;


        for(int i=1;i<temps.size();i++)
        {
            if(temps.get(0)-diff<temps.get(i) ||temps.get(0)+diff>temps.get(i))
            {
                moy++;
                moyRes+=temps.get(i);


            }
        }


        return moyRes/moy;
    }

    public void manualHeatAtPlus(float xPos,float yPos)
    {
        try
        {


            Point plusP=new Point( Math.round(xPos), Math.round(yPos));

            Point plusPtl=new Point( Math.round(xPos-1), Math.round(yPos+1));
            Point plusPtm=new Point( Math.round(xPos), Math.round(yPos+1));
            Point plusPtr=new Point( Math.round(xPos+1), Math.round(yPos+1));
            Point plusPrm=new Point( Math.round(xPos+1), Math.round(yPos));
            Point plusPrb=new Point( Math.round(xPos+1), Math.round(yPos-1));
            Point plusPbm=new Point( Math.round(xPos), Math.round(yPos-1));
            Point plusPbl=new Point( Math.round(xPos-1), Math.round(yPos-1));
            Point plusPml=new Point( Math.round(xPos-1), Math.round(yPos));

            plusPoint=plusP;

            plusPointtl=plusPtl;
            plusPointtm=plusPtm;
            plusPointtr=plusPtr;
            plusPointrm=plusPrm;
            plusPointrb=plusPrb;
            plusPointbm=plusPbm;
            plusPointbl=plusPbl;
            plusPointlm=plusPml;

        }
        catch (Exception ex)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    temperatureTv.setText(ex.getMessage());

                }
            });
        }


    }


}
