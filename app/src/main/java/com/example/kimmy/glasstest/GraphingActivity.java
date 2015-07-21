package com.example.kimmy.glasstest;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The graphing_menu content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class GraphingActivity extends Activity {
    int totalSignals = 4;
    int numActiveSignals = 0;
    int currentSignal = 0;
    TextView title;
    GraphView graph;
    double maxYvalue;
    double minYvalue;
    private GestureDetector mGestureDetector;
    Intent goBackHome;
    Runnable realTimePlotter;
    Runnable tcpConnection;
    LineGraphSeries<DataPoint>[] series;
    double[] xValues1;
    double[] xValues2;
    double[] xValues3;
    double[] xValues4;
    double[] yValues1;
    double[] yValues2;
    double[] yValues3;
    double[] yValues4;
    double lastXValue = 0;
    private final Handler mHandler = new Handler();
    private final Handler timeoutHandler = new Handler();
    Random random = new Random();
    Socket[] socket = new Socket[totalSignals];
    boolean[] socketActive = new boolean[totalSignals];
    int signalCount = 0;
    Semaphore initialize = new Semaphore(-1);  //Ensures that TCP connections are made before running the graph
    DataInputStream[] in = new DataInputStream[totalSignals];
    DataOutputStream[] out = new DataOutputStream[totalSignals];
    Integer[] graphTitleArray = new Integer[totalSignals];
    int readValue = 0;
    boolean isRunning = true;
    static int untilTimeout = 1;
//    TaskCanceller taskCanceller;                //This names the object of the TaskCanceller Runnable thread
    boolean tcpRunning = false;
    boolean isConnected = false;
    boolean discreteGraphMode; // This variable sets whether or not the program will be in discrete mode.
    /*
    * onCreate
    *
    * This method creates the intents and gesture objects.
    * It also creates the graph objects.
    * Requests a voice menu on this activity. As for any other
    * window feature, be sure to request this before
    * setContentView() is called
    */
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        goBackHome = new Intent(this, WelcomeActivity.class);
        goBackHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        goBackHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        taskCanceller = new TaskCanceller();
        System.out.println("Beginning thread for creating tcp connection");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK , "My Tag");
        wakeLock.acquire(2140000000);

        tcpConnection = new Runnable(){

            @Override
            public void run() {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectNetwork() // or .detectAll() for all detectable problems
                        .penaltyDialog()  //show a dialog
                                //.permitNetwork() //permit Network access
                        .build());
                while(isConnected == false) {
                    try {
                        InetAddress serverAddr = InetAddress.getByName("192.168.137.1");
                        //here you must put your computer's IP address.
                        Log.e("TCP Client", "C: Connecting...");
                        //create a socket0 to make the connection with the server
                        System.out.println("Before entering socket0 creation.");
                        int portNumberStart = 10000;
                        for(int i = 0; i < totalSignals; i++){
                            //Connect Socket
                            socket[i] = new Socket(serverAddr, portNumberStart);
                            System.out.println("Connected to port " + portNumberStart);
                            //Connect Output Stream
//                            out[i] = new DataOutputStream((OutputStream) socket[i].getOutputStream());
//                            System.out.println("Connected OutputStream " + i + " to socket" + i);

//                            //Connect Input Stream
                            in[i] = new DataInputStream((InputStream) socket[i].getInputStream());
                            System.out.println("Connected InputStream " + i + " to socket" + i);


                            portNumberStart++;
                        }
                        System.out.println("Passed the first loop");
                        for(int i = 0; i<totalSignals; i++){
                            //Check if signal active
                            if(testIfSignalAvailable(in[i]) > 0) {
                                graphTitleArray[numActiveSignals] = socket[i].getPort();
                                numActiveSignals++;
                                System.out.println("Socket " + 10000+i + " is active.");
                                socketActive[i] = true;

                            }

                        }
                        System.out.println("Number of active signals: " + numActiveSignals);
                        Log.e("TCP Client", "C: Connected...");
                        System.out.println("HI I INITIATED IN AND OUT");
//                        tcpRunning = true;
//                        System.out.println("tcpRunning status: " + tcpRunning);
                        isConnected = true;
//6
//                    initialize.release(); //The initializer semaphore value will be 1 now
                    } catch (Throwable t) {
                        System.out.println("Inside catch " + untilTimeout + " time(s)");
                        t.printStackTrace();
//                        t.printStackTrace();
//                        System.out.println("Done printing stacktrace");
                        isConnected = false;
                        untilTimeout++;
                        if (untilTimeout == 10) {
                            System.out.println("Could not establish a connection. System will now exit.");
                            System.exit(0);
                            finish();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };


        //Thread thread = new Thread(tcpConnection);
        //thread.start();
        tcpConnection.run();

        System.out.println("Before running timeoutHandler");
//        timeoutHandler.postDelayed(taskCanceller, 4000);


        System.out.println("BEGAN thread for creating tcp connection");

        mGestureDetector = createGestureDetector(this);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_graphing);
        graph = (GraphView) findViewById(R.id.graph);

        //Create Series only for active signals
        series = new LineGraphSeries[numActiveSignals];

        //Set Graph Title
        title = (TextView) findViewById(R.id.graphTitle);
        switch(graphTitleArray[0]) {
            case(10000):
                title.setText("Oscilloscope: 1");
                break;
            case(10001):
                title.setText("Oscilloscope: 2");
                break;
            case(10002):
                title.setText("Arbitrary Signal: 1");
                break;
            case(10003):
                title.setText("Arbitrary Signal: 2");
                break;
        }
//        title.setText("Oscilloscope: 1");

        //
        for(int i = 0; i < numActiveSignals; i++) {
            series[i] = new LineGraphSeries<DataPoint>();
//            series[i].setTitle("Graph " + (i+1));
        }

        //Discrete Testing.
//        for(int i = 0; i<100; i++){
//            series[0].appendData(new DataPoint(lastXValue, random.nextDouble()*10), true, 100);
//            lastXValue+=1d;
//        }

        if(numActiveSignals >= 0) {
            graph.addSeries(series[0]);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            if(discreteGraphMode == false) {
                graph.getViewport().setMaxX(50);
            }
            else if(discreteGraphMode == true){
                graph.getViewport().setMaxX(100);
            }
            graph.getViewport().setMaxY(1);
            graph.getViewport().setMinY(0);

        }

    }

    /*
    * onResume()
    *
    * This creates the runnable threads that populates the graph.
    * The update interval is 10 frames per second
    */

    @Override
    protected void onResume() {
        super.onResume();
        realTimePlotter = new Runnable() {
            @Override
            public void run() {

                lastXValue+=1;
                try {
                    getYValue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(this, 0);
            }
        };

        realTimePlotter.run();

        System.out.println("Running realTimePlotter");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //This pauses the updates so that the user can see
        //the  graph when it's not updating
        mHandler.removeCallbacks(realTimePlotter);
    }

    /*
    *getYValue
    *
    * This grabs the information from TCP ports to display onto the glass
    *
     */
    public Double testIfSignalAvailable(DataInputStream in) throws IOException {
        System.out.println("Entered signal testing");
        Double value = Double.parseDouble(in.readLine());
        System.out.println("Read value as " + value);
        if(value != -1){
            Double mode = Double.parseDouble(in.readLine());
//        System.out.println("Data bit value is: " + value);
            System.out.println("Mode is: " + mode);
            if(mode == 1){
                discreteGraphMode = false;
            }
            else if(mode == 2){
                discreteGraphMode = true;
            }
        }
        return value;
    }

    private void getYValue() throws IOException {
        //Random Number Generating if LabVIEW not present. Testing purposes only
//        value = random.nextDouble()*30;
        int signalNum = 0;
        for(int x = 0; x < totalSignals; x++){
            if(socketActive[x])
            {

                try {
                    Double currValue = Double.parseDouble(in[x].readLine()) / 1000.0;
                    if(discreteGraphMode == false) {
                        series[signalNum].appendData(new DataPoint(lastXValue, currValue), true, 50);
                    }
                    else if(discreteGraphMode == true){
                        series[signalNum].appendData(new DataPoint(lastXValue, currValue), true, 100);
                    }
                    updateAxis(currValue);
                    signalNum++;
                }
                catch(NullPointerException e){
                    socketActive[x] = false;
//                    series[signalNum].appendData(series[signalNum].getValues(0,series[signalNum].getDataPointsRadius()),true,(int) series[signalNum].getDataPointsRadius());

                    e.printStackTrace();
                }
            }
        }

//        value = Double.parseDouble(String.valueOf(in.read())); //Returns a Y value as a double
    }

    private void updateAxis(double dataPoint) {


        if(dataPoint > maxYvalue) {
            maxYvalue = dataPoint;
            graph.getViewport().setMaxY(maxYvalue);

        }
        else if(dataPoint < minYvalue) {
            minYvalue = dataPoint;
            graph.getViewport().setMinY(minYvalue);
        }
    }

    /*
    * GestureDetector
    *
    * This embedded class handles the gestures. Tap, swiping left,
    * swiping right, and tapping with two fingers are all possible
    * gestures Glass will recognize.
     */
    private GestureDetector createGestureDetector(Context context) {
        System.out.println("Gestures 1 activated");

        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setAlwaysConsumeEvents(true);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // do something on tap
                    System.out.println("HOLD IT");
                    if(!isRunning) {
                        onResume();
                        isRunning = true;
                    }
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap

                    System.exit(0);
                    return true;

                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    //Increments current signal
                    System.out.println("I swiped right");
                    graph.removeAllSeries();
                    currentSignal++;

                    //Check array out of bounds, loop around.
                    if(currentSignal >= numActiveSignals)
                        currentSignal -= (numActiveSignals);
                    System.out.println("Port number: " + socket[currentSignal].getPort() + " for Signal: " + currentSignal);
                    //Add new series to GraphView
//                    if(socket[) {
//                        title.setText("Oscilloscope: " + (currentSignal+1));
//
//                    }
//                    else if(currentSignal == 2 | currentSignal == 3){
//                        title.setText("Arbitrary Signal: " + (currentSignal-1));
//                    }
                    switch(graphTitleArray[currentSignal]){
                        case(10000):
                            title.setText("Oscilloscope: 1");
                            break;
                        case(10001):
                            title.setText("Oscilloscope: 2");
                            break;
                        case(10002):
                            title.setText("Arbitrary Signal: 1");
                            break;
                        case(10003):
                            title.setText("Arbitrary Signal: 2");
                            break;
                    }
                    graph.addSeries(series[currentSignal]);

                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    //Decrements current signal
                    System.out.println("To da left");
                    graph.removeAllSeries();
                    currentSignal--;

                    //Check array out of bounds, loop around.
                    if(currentSignal < 0)
                        currentSignal = numActiveSignals-1;

                    //Add new series to GraphView
                    switch(graphTitleArray[currentSignal]){
                        case(10000):
                            title.setText("Oscilloscope: 1");
                            break;
                        case(10001):
                            title.setText("Oscilloscope: 2");
                            break;
                        case(10002):
                            title.setText("Arbitrary Signal: 1");
                            break;
                        case(10003):
                            title.setText("Arbitrary Signal: 2");
                            break;
                    }
                    graph.addSeries(series[currentSignal]);
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
                // do something on finger count changes
            }
        });
//        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
//            @Override
//            public boolean onScroll(float displacement, float delta, float velocity) {
//                // do something on scrolling
//            }
//        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.graphing_menu, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graphing_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            System.out.println("Registered voice");
            switch (item.getItemId()) {
                case R.id.pause:
                    System.out.println("Voice activated for pausing");
                    if(isRunning == false){
                        break;
                    }
                    else{
                        onPause();
                        isRunning = false;
                        break;
                    }
                case R.id.resume:
                    System.out.println("Voice activated for resuming");
                    if(isRunning == true){
                        break;
                    }
                    else{
                        onResume();
                        isRunning = true;
                        break;
                    }
                case R.id.nextGraph:
                    System.out.println("Voice activated for next graph");
                    graph.removeAllSeries();
                    currentSignal ++;

                    //Check array out of bounds, loop around.
                    if(currentSignal >= numActiveSignals)
                        currentSignal -= (numActiveSignals);

                    //Add new series to GraphView
                    switch(graphTitleArray[currentSignal]){
                        case(10000):
                            title.setText("Oscilloscope: 1");
                            break;
                        case(10001):
                            title.setText("Oscilloscope: 2");
                            break;
                        case(10002):
                            title.setText("Arbitrary Signal: 1");
                            break;
                        case(10003):
                            title.setText("Arbitrary Signal: 2");
                            break;
                    }

                    graph.addSeries(series[currentSignal]);
                    break;
                case R.id.previousGraph:
                    System.out.println("Voice activated for previous graph");
                    graph.removeAllSeries();
                    currentSignal--;

                    //Check array out of bounds, loop around.
                    if(currentSignal < 0)
                        currentSignal = numActiveSignals-1;



                    //Add new series to GraphView
                    switch(graphTitleArray[currentSignal]){
                        case(10000):
                            title.setText("Oscilloscope: 1");
                            break;
                        case(10001):
                            title.setText("Oscilloscope: 2");
                            break;
                        case(10002):
                            title.setText("Arbitrary Signal: 1");
                            break;
                        case(10003):
                            title.setText("Arbitrary Signal: 2");
                            break;
                    }

                    graph.addSeries(series[currentSignal]);
                    break;
                case R.id.returnHome:
                    System.out.println("Voice activated for go home");
                    graph.removeAllSeries();
                    startActivity(goBackHome);
                    for(int i = 0; i < numActiveSignals; i++){
                        try {
                            socket[i].close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    finish();
                    break;
                case R.id.exitFromGraph:
                    for(int i = 0; i < numActiveSignals; i++){
                        try {
                            socket[i].close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                    finish();

                default:
                    return true;
            }
//            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }

//    class TaskCanceller implements Runnable{
//        @Override
//        public void run() {
//            System.out.println("INSIDE TASKCANCELLER. INSIDE RUN METHOD");
//            if (tcpRunning == false){  //This checks to see if displayContent is still running
//                System.out.println("INSIDE TASKCANCELLER. Inside IF statement ");
//                System.out.println("Program taking too long to find a connection. Terminating");  //This turns on the boolean value inside the AsyncTask for cancelling to be true
//                System.exit(0);  //This causes the text to display this string
//            }
//        }
//    }
}
//

