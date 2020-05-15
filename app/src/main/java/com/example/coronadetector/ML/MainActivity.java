package com.example.coronadetector.ML;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.coronadetector.R;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private int channelSize=3;
    private  int inputImageWidth=224;
    private  int inputImageHeight=224;
    private int modelInputSize=inputImageWidth*inputImageHeight*channelSize;
    private float[][] resultArray = new float[1][2];
    Interpreter interpreter;
    CircularProgressView progressView;
    LinearLayout progressContainer;

    ImageView inputIv;
    TextView covidTv,normalTv;
    public static int RESULT_LOAD_IMG=10;

    ArcGauge arcGauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ml);

        inputIv=findViewById(R.id.inputIv);
        covidTv=findViewById(R.id.covidRes);
        normalTv=findViewById(R.id.normalRes);



        interpreter=new Interpreter(loadModelFile());




        inputIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        progressView=findViewById(R.id.progress_view);
        progressContainer=findViewById(R.id.progressContainer);

        arcGauge=findViewById(R.id.arcGauge);
        initGauge();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);


                if(checkImageGrayScale(selectedImage))
                {
                    inputIv.setImageBitmap(selectedImage);

              /*  BitmapDrawable drawable = (BitmapDrawable) inputIv.getDrawable();
                Bitmap bitmap = drawable.getBitmap();*/


                    new recognizerTask().execute(selectedImage);

                }
                else {
                     showNoInternetDialog();
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked an X-Ray Image",Toast.LENGTH_LONG).show();
        }
    }
    public void showNoInternetDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please select a valid X-Ray Image !");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });

        alertDialogBuilder.setCancelable(false);


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private MappedByteBuffer loadModelFile()
    {
        try {
            final AssetManager assetMgr = getApplicationContext().getResources().getAssets();
            AssetFileDescriptor fileDescriptor=assetMgr.openFd("covid_tflite_model.tflite");
            FileInputStream inputStream= new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel=inputStream.getChannel();
            long startOffset=fileDescriptor.getStartOffset();
            long declaredLength=fileDescriptor.getDeclaredLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap)
    {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * this.modelInputSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[this.inputImageWidth * this.inputImageHeight];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;


        for(int i=0;i<inputImageWidth;i++)
        {
            for(int j=0;j<inputImageHeight;j++)
            {
                int pixelVal = pixels[pixel++];
                /*byteBuffer.putFloat((byte)(pixelVal >> 16 & 255));
                byteBuffer.putFloat((byte)(pixelVal >> 8 & 255));
                byteBuffer.putFloat((byte)(pixelVal & 255));
*/
                byteBuffer.putFloat( ((pixelVal >> 16) & 0xFF)* (1.f/255.f));
                byteBuffer.putFloat( ((pixelVal >> 8) & 0xFF)* (1.f/255.f));
                byteBuffer.putFloat( (pixelVal & 0xFF)* (1.f/255.f));
            }
        }
        return byteBuffer;
    }

    public void getRecognitionsResults(Bitmap bitmap)
    {
        Bitmap resizedBitmap=Bitmap.createScaledBitmap(bitmap,inputImageWidth,inputImageHeight,true);
        ByteBuffer modelInput=convertBitmapToByteBuffer(resizedBitmap);
        interpreter.run(modelInput,resultArray);


    }


    private class recognizerTask extends AsyncTask<Bitmap,Float, Float>
    {

        protected void onPreExecute(){

            progressContainer.setVisibility(View.VISIBLE);
            progressView.bringToFront();
            progressView.startAnimation();
        }

        @Override
        protected Float doInBackground(Bitmap... bitmaps) {

            getRecognitionsResults(bitmaps[0]);
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Float object) {
            super.onPostExecute(object);

            try {
                float covidProb= resultArray[0][0]*100;
                float normalProb=resultArray[0][1]*100;


                String covidRes=new DecimalFormat("##.##").format(covidProb)+"%";
                String normalRes=new DecimalFormat("##.##").format(normalProb)+"%";


                progressView.stopAnimation();
                progressContainer.setVisibility(View.GONE);

                covidTv.setText(covidRes);
                normalTv.setText(normalRes);


                String covStr=new DecimalFormat("##.##").format(covidProb);

                if(covStr.contains(","))
                {
                    covStr=covStr.replace(",",".");
                }


                System.out.println("Orig covid prob: "+covidProb);
                System.out.println(covStr);


                arcGauge.setValue(Double.parseDouble(covStr));

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }
    public void initGauge()
    {
        Range rg1 =new Range();
        rg1.setFrom(0);
        rg1.setTo(25);
        rg1.setColor(Color.GREEN);
        Range rg2 =new Range();
        rg2.setFrom(25);
        rg2.setTo(50);
        rg2.setColor(Color.YELLOW);
        Range rg3 =new Range();
        rg3.setFrom(50);
        rg3.setTo(75);
        rg3.setColor(Color.parseColor("#FFA500"));
        Range rg4 =new Range();
        rg4.setFrom(75);
        rg4.setTo(100);
        rg4.setColor(Color.RED);
        arcGauge.addRange(rg1);arcGauge.addRange(rg2);arcGauge.addRange(rg3);arcGauge.addRange(rg4);
        arcGauge.setUseRangeBGColor(true);


    }
    boolean checkImageGrayScale(Bitmap myImage)
    {
        int myHeight = myImage.getHeight();
        int myWidth  = myImage.getWidth();
        boolean isGrayscaleImage = true;  // assume it is grayscale until proven otherwise

        for(int i = 0; i < myWidth; i++){
            for(int j = 0; j < myHeight; j++){
                int currPixel = myImage.getPixel(i, j);

                if(!isGrayScalePixel(currPixel) ){
                    isGrayscaleImage = false;
                    break;
                }
            }
        }
        return isGrayscaleImage;
    }

    boolean isGrayScalePixel(int pixel){
        int alpha = (pixel & 0xFF000000) >> 24;
        int red   = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue  = (pixel & 0x000000FF);


        if( -1 == alpha && red == green && green == blue ) return true;
        else return false;

    }

}
