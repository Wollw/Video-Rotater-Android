package org.wollw.videorotater;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Deque;

enum Mode {
	MODE_SLIT, MODE_ROTATE
}

public class VideoRotaterMainActivity extends Activity implements
		CvCameraViewListener {

	private String TAG = "VideoRotater";

	private CameraBridgeViewBase mOpenCvCameraView;

	private Deque<Mat> frameList = new LinkedList<Mat>(); 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_video_rotater_main);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	int mWidth = 0;
	int mHeight = 0;
	public void onCameraViewStarted(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public void onCameraViewStopped() {
	}

	Mode mode = Mode.MODE_ROTATE;
	int delta = 1;
	int rotateRow = 0;
	public Mat onCameraFrame(Mat inputFrame) {

		int y = 0;
		switch (mode) {
		case MODE_SLIT:
			while (frameList.size() >= mHeight)
				frameList.removeFirst().release();
			frameList.addLast(inputFrame.clone());
		
			y = 0;
			for (Iterator<Mat> i = frameList.iterator(); i.hasNext();) {
				Mat m = i.next();
				m.row(y).copyTo(inputFrame.row(y));
				y++;
			}
			
			return inputFrame;
		
		case MODE_ROTATE:
			while (frameList.size() >= mHeight)
				frameList.removeFirst().release();
			frameList.addLast(inputFrame.clone());
			
			y = 0;
			for (Iterator<Mat> i = frameList.iterator(); i.hasNext();) {
				Mat m = i.next();
				m.row(rotateRow).copyTo(inputFrame.row(y));
				y++;
			}
			
			if (rotateRow == mHeight - 1) {
				delta = -1;
			} else if (rotateRow == 0) {
				delta = 1;
			}
			rotateRow += delta;

			return inputFrame;
		
		default:
			return inputFrame;
		}
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				mLoaderCallback);
	}

	@Override
	public void onTrimMemory(int level) {
		Log.d(TAG,"Memory Level: "+level);
	}
}
