package com.erimac2.soundstreamingapp.Lab3;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.erimac2.soundstreamingapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lab3Activity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Sensor accelerometer;
    private Sensor orientation;

    private Button startAndStop;

    private TextView xValue;
    private TextView yValue;
    private TextView zValue;
    private TextView coordinates;
    private TextView ori;

    boolean flag = false;

    private boolean InformationObtained;

    private boolean sos_sent = false;

    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private boolean photo_took = false;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSession;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    double oldX;
    double oldY;
    double oldZ;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab3design);

        InformationObtained = false;

        startAndStop = findViewById(R.id.start_and_stop);
        startAndStop.setOnClickListener(StartAndStopButtonListener);

        xValue = findViewById(R.id.x_value);
        yValue = findViewById(R.id.y_value);
        zValue = findViewById(R.id.z_value);
        coordinates = findViewById(R.id.coordinates);
        ori = findViewById(R.id.orient);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if(orientation != null){
            sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
        }

        textureView = findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        takePictureButton = findViewById(R.id.take_photo);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takePicture();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Log.i("Requesting_permission", "asd");
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        }

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(Lab3Activity.this, "Saved" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread()
    {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    protected void stopBackgroundThread()
    {
        backgroundThread.quitSafely();
        try
        {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    protected void takePicture()
    {
        if(null == cameraDevice)
        {
            Log.e(TAG, "cameraDevice is null");
            return;
        }

        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try
        {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
            {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            }
            int width = 640;

            int height = 480;

            if(jpegSizes != null && 0 < jpegSizes.length)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(reader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;

                    try
                    {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        save(bytes);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        if(image != null)
                        {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException
                {
                    OutputStream outputStream = null;

                    try
                    {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }
                    finally
                    {
                        if(outputStream != null)
                        {
                            outputStream.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, backgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(Lab3Activity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    try
                    {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    }
                    catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, backgroundHandler);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }
    protected void createCameraPreview()
    {
        try
        {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if(cameraDevice == null)
                    {
                        return;
                    }

                    cameraCaptureSession = session;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(Lab3Activity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }
    private void openCamera()
    {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");

        try
        {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(Lab3Activity.this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview()
    {
        if(cameraDevice == null)
        {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try
        {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }
    private void closeCamera()
    {
        if(cameraDevice != null)
        {
            cameraDevice.close();
            cameraDevice = null;
        }
        if(imageReader != null)
        {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(Lab3Activity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    View.OnClickListener StartAndStopButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(accelerometer == null)
            {
                Toast.makeText(Lab3Activity.this, "No sensor", Toast.LENGTH_LONG).show();
                return;
            }

            if(InformationObtained)
            {
                startAndStop.setText("Start");
                sensorManager.unregisterListener(Lab3Activity.this, accelerometer);
                InformationObtained = false;
            }
            else
            {
                sensorManager.registerListener(Lab3Activity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                startAndStop.setText("Stop");
                InformationObtained = true;
            }
        }
    };
    public void SOS()
    {
        final long short_flash_time = 150;
        final long long_flash_time = 300;
        final long between_flash_time = 150;
        final long short_flash_wait_time = short_flash_time + between_flash_time;
        final long long_flash_wait_time = long_flash_time + between_flash_time;

        final Handler handler = new Handler();
        final Runnable shortFlash = new Runnable() {
            @Override
            public void run() {
                flashOn();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flashOff();
                    }
                }, short_flash_time);

            }
        };
        final Runnable longFlash = new Runnable() {
            @Override
            public void run() {
                flashOn();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flashOff();
                    }
                }, long_flash_time);
            }
        };

        Runnable short_triple_flash = new Runnable() {
            @Override
            public void run() {
                handler.post(shortFlash);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(shortFlash);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handler.post(shortFlash);
                            }
                        }, short_flash_wait_time);
                    }
                }, short_flash_wait_time);
            }
        };
        Runnable long_triple_flash = new Runnable() {
            @Override
            public void run() {
                handler.post(longFlash);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(longFlash);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handler.post(longFlash);
                            }
                        }, long_flash_wait_time);
                    }
                }, long_flash_wait_time);
            }
        };

        handler.postDelayed(short_triple_flash, 0);
        handler.postDelayed(long_triple_flash,
                short_flash_wait_time * 3 + between_flash_time);
        handler.postDelayed(short_triple_flash,
                short_flash_wait_time * 3 + long_flash_wait_time * 3 + between_flash_time * 2);
        handler.postDelayed(() -> sos_sent = false,
                short_flash_wait_time * 3 + long_flash_wait_time * 3 + between_flash_time * 2);
    }
    private void flashOn()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        Log.i("Flashlight", "turning on");

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flag = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void flashOff()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        Log.i("Flashlight", "turning off");
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flag = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        Sensor mySensor = event.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(Math.abs(oldX - x) > 0.1)
            {
                oldX = x;
            }
            if(Math.abs(oldY - y) > 0.1)
            {
                oldX = y;
            }
            if(Math.abs(oldZ - z) > 0.1)
            {
                oldX = z;
            }

                xValue.setText(String.valueOf(x));
                yValue.setText(String.valueOf(y));
                zValue.setText(String.valueOf(z));

            if(x > 7)
            {
                ori.setText("Left");
            }
            if(x < -7)
            {
                ori.setText("Right");

            }
            if(z > 7)
            {
                ori.setText("Up");
            }
            if(z < -7)
            {
                ori.setText("Down");
                moveTaskToBack(true);

            }
            if(y > 7)
            {
                ori.setText("Rightside up");

            }
            if(y < -7)
            {
                ori.setText("Upside down");

            }
        }
        if(mySensor.getType() == Sensor.TYPE_ORIENTATION)
        {
            float degree = Math.round(event.values[0]);
            float degreeY = event.values[1] % 90;
            Log.i("Camera_photo", photo_took + " " + degree);
            if(!photo_took && (degree > 350 || degree < 1))
            {
                Toast.makeText(this, "Taking photo", Toast.LENGTH_SHORT).show();
                takePicture();
                photo_took = true;
            }
            else if(degree < 358 && degree > 2)
            {
                photo_took = false;
            }
            if(!sos_sent && (degree > 179 && degree < 181))
            {
                closeCamera();
                SOS();
                sos_sent = true;
            }
            int brightness = (int)Math.round(Math.abs(degreeY) / 90.0 * 255.0);
            if(Math.abs(event.values[1]) > 90)
                brightness = 255 - brightness;
            boolean writeAllowed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(this))
                    writeAllowed = true;
            }else
                writeAllowed = true;
            if(writeAllowed) {
                Settings.System.putInt(this.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }
    @Override
    protected void onPause()
    {
        super.onPause();
        if(accelerometer != null)
        {
            sensorManager.unregisterListener(Lab3Activity.this, accelerometer);
        }
        if(orientation != null)
        {
            sensorManager.unregisterListener(Lab3Activity.this, orientation);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        this.locationManager.removeUpdates(this);
        stopBackgroundThread();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if(accelerometer != null && InformationObtained)
        {
            sensorManager.registerListener(Lab3Activity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);

        startBackgroundThread();

        if(textureView.isAvailable())
        {
            openCamera();
        }
        else
        {
            textureView.setSurfaceTextureListener(textureListener);
        }
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onLocationChanged(Location location)
    {
        if(location != null)
        {
            coordinates.setText("Latitude:" + " " + location.getLatitude() + " " + "Longitude:" + " " + location.getLongitude());
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }
    @Override
    public void onProviderEnabled(String provider)
    {

    }
    @Override
    public void onProviderDisabled(String provider)
    {

    }
}
