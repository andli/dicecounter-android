package com.andli826.dicecounter;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CaptureImageFragment.OnCaptureImageListener} interface
 * to handle interaction events.
 * Use the {@link CaptureImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CaptureImageFragment extends Fragment {
    private static final String TAG = "dclog::CaptureImageFragment";

    private static final Integer DICE_SIZE = 16; //16
    private static final Integer BLUR_FACTOR = 3;
    private static final Integer RED_LOW_THRESHOLD = 209;  //209
    private static final Integer MIN_PIP_AREA = 10; //10
    
    private static final int CAMERA_REQUEST = 1888;
    private Uri tempImageUri;
    private ImageView diceImageView;

    private OnCaptureImageListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CaptureImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaptureImageFragment newInstance(String param1, String param2) {
        CaptureImageFragment fragment = new CaptureImageFragment();
        return fragment;
    }
    public CaptureImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_capture_image, container, false);

        this.diceImageView = (ImageView) rootView.findViewById(R.id.imageViewDice);

        Button saveButton = (Button) rootView.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onImageCaptured();
                Log.d(TAG, "Get image");
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        File outputFile;
        Log.v(TAG, "Trying to create temp file & Uri");
        try
        {
            outputFile = new File(Environment.getExternalStorageDirectory(), "dicepic.jpg");
            tempImageUri = Uri.fromFile(outputFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);
            Log.v(TAG, "startActivityForResult");
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        catch(Exception e)
        {
            Log.v(TAG, "Can't create file to take picture!");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");

        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {

                    Bitmap photo = null;
                    try {
                        photo = MediaStore.Images.Media.getBitmap(
                                this.getActivity().getContentResolver(),
                                tempImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (OpenCVLoader.initDebug() && photo != null) {
                        Log.d(TAG, "OpenCV loaded!");

                        // Convert bitmap to Mat
                        Mat imgToProcess = new Mat();
                        Mat intermediateMat = new Mat();
                        Utils.bitmapToMat(photo, imgToProcess);
                        Log.d(TAG, "Original image: " + photo.getHeight() + "x" + photo.getWidth());
                        Log.d(TAG, "OpenCV Mat: " + imgToProcess.rows() + "x" + imgToProcess.cols());

                        // Threshold image
                        Imgproc.medianBlur(imgToProcess, intermediateMat, BLUR_FACTOR);

                        List<Mat> lRgb = new ArrayList<Mat>(3);
                        Core.split(intermediateMat, lRgb);
                        Mat red = lRgb.get(0);

                        // Fetch the dice contours using red threshold. invert.
                        Mat diceBlocks = new Mat();
                        Imgproc.threshold(red, diceBlocks, RED_LOW_THRESHOLD, 255, 1);  // 185 --> 235
                        Mat invDiceBlocks = new Mat(diceBlocks.rows(),diceBlocks.cols(), diceBlocks.type(), new Scalar(255,255,255));

                        Core.subtract(invDiceBlocks, diceBlocks, intermediateMat);
                        // Convert back to bitmap
                        Utils.matToBitmap(intermediateMat, photo);
                    }

                    this.diceImageView.setImageBitmap(photo);
                }
            return;
            default: // do nothing
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
/*    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCaptureImageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCaptureImageListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCaptureImageListener {
        // TODO: Update argument type and name
        public void onImageCaptured();
    }

}
