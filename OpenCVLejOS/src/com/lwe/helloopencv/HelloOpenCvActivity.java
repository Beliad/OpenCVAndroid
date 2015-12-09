package com.lwe.helloopencv;


import java.io.IOException;

import lejos.hardware.Audio;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.RegulatedMotor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



public class HelloOpenCvActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
	
	private RemoteRequestEV3 ev3;
    private RegulatedMotor left, right;
    private Audio audio;
	private Mat mRgba, mHSVMat;

	private int estado; //0detenido 1 izquierde 2 derecha
    
    private Scalar colorRed, colorGreen, colorWhite;
	
	private CameraBridgeViewBase mOpenCvCameraView;
	
	private SeekBar h;
	private SeekBar s;
	private SeekBar v;

	private SeekBar h2;
	private SeekBar s2;
	private SeekBar v2;
	
	private TextView lh_text;
	private TextView ls_text;
	private TextView lv_text;
	private TextView hh_text;
	private TextView hs_text;
	private TextView hv_text;
	
	private LinearLayout menu;
	
	int iLastX = -1;
	int iLastY = -1;
	int  iLineThickness = 2;
	
	int width=0;
	int rango=0;
	int height=0;
	int vista=0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    mOpenCvCameraView.enableView();
                }break;
                default:{
                    super.onManagerConnected(status);
                }
            }

        }
    };
    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }
    @Override
    public void onPause(){
        super.onPause();
        if(mOpenCvCameraView!=null){
        	mOpenCvCameraView.disableView();
        	releaseMats();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView!=null){
        	mOpenCvCameraView.disableView();
        	releaseMats();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("oncreate", "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hello_open_cv);
        
        estado=0;
        
        if (ev3 == null) {
            new Control().execute("connect","192.168.43.99");
            Log.i("lejos","connect");
        }
        else {
            new Control().execute("disconnect");
            Log.i("lejos","disconect");
        }
        
        h=(SeekBar) findViewById(R.id.h);
        s=(SeekBar) findViewById(R.id.s);
        v=(SeekBar) findViewById(R.id.v);
        h2=(SeekBar) findViewById(R.id.h2);
        s2=(SeekBar) findViewById(R.id.s2);
        v2=(SeekBar) findViewById(R.id.v2);
        menu=(LinearLayout) findViewById(R.id.menu);
        
        lh_text= (TextView) findViewById(R.id.lh_text);
        ls_text= (TextView) findViewById(R.id.ls_text);
        lv_text= (TextView) findViewById(R.id.lv_text);
        hh_text= (TextView) findViewById(R.id.hh_text);
        hs_text= (TextView) findViewById(R.id.hs_text);
        hv_text= (TextView) findViewById(R.id.hv_text);
        
        h.setProgress(56);
    	s.setProgress(57);
    	v.setProgress(60); //rojo 66 168 39
    	h2.setProgress(78);
    	s2.setProgress(255);
    	v2.setProgress(255);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.helloOpen);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        

    }
  

    @Override
    public void onCameraViewStarted(int width, int height) {
    	colorRed = new Scalar(255, 0, 0, 255);
        colorGreen = new Scalar(0, 255, 0, 255);
        colorWhite = new Scalar(255, 255, 255, 255);
        mHSVMat = new Mat();
        mRgba = new Mat();
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        
        this.width=width;
        this.height=height;
        this.rango=(int) ((int) width*0.30);
        
        
        }

    @Override
    public void onCameraViewStopped() {
    	releaseMats();
    }
    
    
    public void releaseMats () {
    	mRgba.release();
        mHSVMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    	mRgba=inputFrame.rgba();
    	
    	
    	
    	Core.line(mRgba, new Point(0,height/2), new Point(width,height/2), colorWhite, iLineThickness-2);
    	Core.line(mRgba, new Point(width/2,0), new Point(width/2,height), colorWhite, iLineThickness-2);
    	
    	Scalar lower=new Scalar(h.getProgress(),s.getProgress(),v.getProgress());
    	Scalar upper=new Scalar(h2.getProgress(),s2.getProgress(),v2.getProgress());
    	
    	Imgproc.cvtColor(mRgba,mHSVMat,Imgproc.COLOR_BGR2HSV);
    	Core.inRange(mHSVMat, lower, upper, mHSVMat);
    	Imgproc.erode(mHSVMat, mHSVMat, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10,10)));
    	Imgproc.dilate(mHSVMat, mHSVMat, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10,10)));
    	Imgproc.erode(mHSVMat, mHSVMat, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10,10)));
    	Imgproc.dilate(mHSVMat, mHSVMat, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(10,10)));
    	
    	Moments oMoments= Imgproc.moments(mHSVMat);
    	double dM01= oMoments.get_m01();
    	double dM10= oMoments.get_m10();
    	double dArea= oMoments.get_m00();
    	
    	if (dArea > 10000){
			int posX = (int) (dM10 / dArea);
			int posY = (int) (dM01 / dArea);
			if (iLastX >= 0 && iLastY >= 0 && posX >= 0 && posY >= 0){
				if(posX>width/2-rango&&posX<width/2+rango){
					Core.circle(mRgba, new Point(posX,posY),(int) dArea/100000, colorGreen, iLineThickness+10);
					if(estado!=3&&ev3!=null){
						left.forward();
		                right.forward();
			    		estado=3;
					}
					
				}
				else{
					if(posX<width/2-rango){
						Core.circle(mRgba, new Point(posX,posY),(int) dArea/100000, colorWhite, iLineThickness+10);
						if(estado!=2&&ev3!=null){
							left.backward();
			                right.forward();
			                
			                estado=2;
			                	//new Control().execute("rotate right");
			                }
						}
					else if(posX>width/2+rango){
						Core.circle(mRgba, new Point(posX,posY),(int) dArea/100000, colorRed, iLineThickness+10);
						if(estado!=1&&ev3!=null){
							left.forward();
			                right.backward();
			                estado=1;
								//new Control().execute("rotate left");
			                }
						}
				}
			}
			iLastX = posX;
			iLastY = posY;
		}
    	if(estado!=0&&ev3!=null){
    		//new Control().execute("stop");
    		left.stop(true);
    		right.stop(true);
    		estado=0;
    	}
    	
    	Core.line(mRgba, new Point(width/2-rango,height/2-rango), new Point(width/2+rango,height/2-rango) , colorGreen);
		Core.line(mRgba, new Point(width/2-rango,height/2-rango), new Point(width/2-rango,height/2+rango) , colorGreen);
		Core.line(mRgba, new Point(width/2-rango,height/2+rango), new Point(width/2+rango,height/2+rango) , colorGreen);
		Core.line(mRgba, new Point(width/2+rango,height/2-rango), new Point(width/2+rango,height/2+rango) , colorGreen);
    	
		if(vista==0){
    		return mRgba;
    	}else{
    		return mHSVMat;
    	}
    }
    
    public void abrirMenu(View view){
    	if(menu.getVisibility()==0){
    		menu.setVisibility(4);
    	}else{
    		lh_text.setText("LH: "+ h.getProgress());
        	ls_text.setText("LS: "+ s.getProgress());
        	lv_text.setText("LV: "+ v.getProgress());
        	hh_text.setText("HH: "+ h2.getProgress());
        	hs_text.setText("HS: "+ s2.getProgress());
        	hv_text.setText("HV: "+ v2.getProgress());
    		menu.setVisibility(0);
    	}
    }
    
    public void cambiarVista(View v){
    	if(vista==0){
    		vista=1;
    	}else{
    		vista=0;
    	}
    }
    
    public void conectarLego(View v){
    	
    }
    
    private class Control extends AsyncTask<String, Integer, Long> {
    	 
        protected Long doInBackground(String... cmd) {

            if (cmd[0].equals("connect")) {
               try {
                   ev3 = new RemoteRequestEV3(cmd[1]);
                   left = ev3.createRegulatedMotor("C", 'L');
                   right = ev3.createRegulatedMotor("B", 'L');
                   audio = ev3.getAudio();
                   right.setSpeed(150);
                   left.setSpeed(150);
                   audio.systemSound(3);
                   return 0l;
               } catch (IOException e) {
                   return 1l;
               }
            } else if (cmd[0].equals("disconnect") && ev3 != null) {
                audio.systemSound(2);
                left.close();
                right.close();
                ev3.disConnect();
                ev3 = null;
                return 0l;
            } 

            if (ev3 == null) return 2l;

            ev3.getAudio().systemSound(1);

            /*if (cmd[0].equals("stop")) {
                	left.setSpeed(200);
                	left.setSpeed(100);
                	left.setSpeed(0);
                	right.setSpeed(200);
                	right.setSpeed(100);
                	right.setSpeed(0);
                	
                	//left.stop(true);
                    //right.stop(true);
                    //estado=0;
                    Log.i("stop",""+estado);
            } else if (cmd[0].equals("forward")) {
                left.forward();
                right.forward();
            } else if (cmd[0].equals("backward")) {
                left.backward();
                right.backward();
            } *//*else if (cmd[0].equals("rotate left")) {
                	//left.setSpeed(100);
                    //right.setSpeed(100);
	            	left.backward();
	                right.forward();
	                //estado=1;
	                Log.i("left",""+estado);
            } else if (cmd[0].equals("rotate right")) {
                
                	//left.setSpeed(100);
                    //right.setSpeed(100);
                	left.forward();
                    right.backward();
                    //estado=2;
                    Log.i("rigth",""+estado);
            }*/

            return 0l;
        }

        protected void onPostExecute(Long result) {
            if (result == 1l) Toast.makeText(HelloOpenCvActivity.this, "Could not connect to EV3", Toast.LENGTH_LONG).show();
            else if (result == 2l) Toast.makeText(HelloOpenCvActivity.this, "Not connected", Toast.LENGTH_LONG).show();
        }
   }
}
