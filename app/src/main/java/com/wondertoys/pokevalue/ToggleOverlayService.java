package com.wondertoys.pokevalue;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ToggleOverlayService extends Service implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    //region - Fields -
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;

    private Boolean isMoving = false;
    private Boolean isOpen = false;
    private Boolean isMovable = false;

    private WindowManager windowManager;

    private View topRightLayout;
    private View toggleLayout;
    //endregion

    //region - Private Functions -
    private void createToggleWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        toggleLayout = inflater.inflate(R.layout.toggle_overlay, null);

        toggleLayout.setClickable(true);
        toggleLayout.setLongClickable(true);
        toggleLayout.setOnLongClickListener(this);
        toggleLayout.setOnTouchListener(this);

        View imageMore = toggleLayout.findViewById(R.id.imageMore);
        imageMore.setClickable(true);
        imageMore.setLongClickable(true);
        imageMore.setOnClickListener(this);
        imageMore.setOnTouchListener(this);
        imageMore.setOnLongClickListener(this);

        View imageCalc = toggleLayout.findViewById(R.id.imageCalc);
        imageCalc.setClickable(true);
        imageCalc.setLongClickable(true);
        imageCalc.setOnClickListener(this);
        imageCalc.setOnTouchListener(this);
        imageCalc.setOnLongClickListener(this);

        View imageExit = toggleLayout.findViewById(R.id.imageExit);
        imageExit.setClickable(true);
        imageExit.setLongClickable(true);
        imageExit.setOnClickListener(this);
        imageExit.setOnTouchListener(this);
        imageExit.setOnLongClickListener(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.y = 250;
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics());
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;

        windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(toggleLayout, params);

        topRightLayout = new View(this);
        WindowManager.LayoutParams topRightParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        topRightParams.gravity = Gravity.TOP | Gravity.RIGHT;
        topRightParams.width = 0;
        topRightParams.height = 0;
        windowManager.addView(topRightLayout, topRightParams);
    }
    //endregion

    //region - Overrides -
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        createToggleWindow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( toggleLayout != null ) {
            windowManager.removeView(toggleLayout);
            windowManager.removeView(topRightLayout);
            toggleLayout = null;
            topRightLayout = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ( !isMovable ) return false;

        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            float x = event.getRawX();
            float y = event.getRawY();

            isMoving = false;

            int[] location = new int[2];
            toggleLayout.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;
        } else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
            int[] topRightLocationOnScreen = new int[2];
            topRightLayout.getLocationOnScreen(topRightLocationOnScreen);


            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams)toggleLayout.getLayoutParams();

            int newX = (int)(offsetX + x);
            int newY = (int)(offsetY + y);

            if ( Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !isMoving ) {
                return false;
            }

            params.x = newX - (topRightLocationOnScreen[0]);
            params.y = newY - (topRightLocationOnScreen[1]);

            windowManager.updateViewLayout(toggleLayout, params);
            isMoving = true;
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            if ( isMoving ) {
                toggleLayout.getBackground().setAlpha(255);
                isMovable = false;

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onLongClick(View view) {
        toggleLayout.getBackground().setAlpha(128);
        isMovable = true;

        return true;
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.imageMore ) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams)toggleLayout.getLayoutParams();
            if ( isOpen ) {
                params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, getResources().getDisplayMetrics());
            }
            else {
                params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 116, getResources().getDisplayMetrics());
            }

            windowManager.updateViewLayout(toggleLayout, params);
            isOpen = !isOpen;
        }
        else if ( v.getId() == R.id.imageExit ) {
            final Context ctx = this;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getApplicationContext());
            builder.setTitle("Close Tray?")
                    .setMessage("Are you sure you want to close the PokeValue tray?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            stopService(new Intent(ctx, ToggleOverlayService.class));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Noop
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();
        }
        else if ( v.getId() == R.id.imageCalc ) {
            stopService(new Intent(this, ToggleOverlayService.class));

            Intent intent = new Intent(getApplicationContext(), CalculateOverlayService.class);
            startService(intent);
        }
    }
    //endregion
}
