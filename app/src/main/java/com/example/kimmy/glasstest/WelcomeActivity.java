package com.example.kimmy.glasstest;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

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
public class WelcomeActivity extends Activity {

    private GestureDetector mGestureDetector;
    Intent graphingIntent;
    Intent helpIntent;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // Requests a voice menu on this activity. As for any other
        // window feature, be sure to request this before
        // setContentView() is called
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK , "My Tag");
        wakeLock.acquire(2140000000);

        graphingIntent = new Intent(this, GraphingActivity.class);
        graphingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        graphingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        helpIntent = new Intent(this, HelpActivity.class);

        mGestureDetector = createGestureDetector(this);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_welcome);
    }
    private GestureDetector createGestureDetector(Context context) {
        System.out.println("Gestures 2 activated");

        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setAlwaysConsumeEvents(true);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // do something on tap
                    System.out.println("Tap dat DPAD");
                    startActivity(graphingIntent);
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap

                    System.exit(0);
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {

                    // do something on right (forward) swipe
                    System.out.println("I swiped right");


                    finish();
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    System.out.println("To da left");
                    startActivity(graphingIntent);
                    finish();
                    // do something on left (backwards) swipe
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
            getMenuInflater().inflate(R.menu.welcome_menu, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            System.out.println("Registered voice");
            switch (item.getItemId()) {
                case R.id.beginGraphing:
                    System.out.println("Voice activated for second activity");
                    startActivity(graphingIntent);

//                    onPause();

                    break;
                case R.id.goToHelp:
                    startActivity(helpIntent);
                    break;
                case R.id.exit:
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

