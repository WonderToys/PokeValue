package com.wondertoys.pokevalue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.wondertoys.pokevalue.utils.EvaluationData;
import com.wondertoys.pokevalue.utils.LevelUpData;
import com.wondertoys.pokevalue.utils.Pokemon;

import java.util.ArrayList;


public class CalculateOverlayService extends Service implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {
    //region - Fields -
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;

    private Boolean isMoving = false;
    private Boolean isMovable = false;

    private ArrayList<Pokemon> pokemonList;
    private ArrayList<LevelUpData> levelUpDataList;
    private ArrayList<Integer> dustCosts;

    private View calculateOverlay;
    private View tableForm;
    private View tableResults;

    private AutoCompleteTextView fieldPokemonName;
    private AutoCompleteTextView fieldDustCost;

    private EditText fieldCombatPower;
    private EditText fieldHitPoints;

    private CheckBox checkPoweredUp;

    private WindowManager windowManager;
    //endregion

    //region - Private Methods -
    private Pokemon getPokemonByName(String name) {
        Pokemon poke = null;

        for ( Pokemon p : pokemonList ) {
            if ( p.name.equals(name) ) {
                poke = p;
                break;
            }
        }

        return poke;
    }

    private EvaluationData getEvaluationData() {
        Boolean valid = true;

        Pokemon poke = getPokemonByName(fieldPokemonName.getText().toString());
        if ( poke == null ) {
            valid = false;

            fieldPokemonName.setError("Invalid Pokemon Name!");
        }

        String dustValue = fieldDustCost.getText().toString();
        if ( dustValue == null || dustValue.length() == 0 || !dustCosts.contains(Integer.parseInt(dustValue)) ) {
            valid = false;
            fieldDustCost.setError("Invalid Dust Cost!");
        }

        String cpValue = fieldCombatPower.getText().toString();
        if ( cpValue == null || cpValue.length() == 0 || Integer.parseInt(cpValue) <= 0 ) {
            valid = false;

            fieldCombatPower.setError("Invalid Combat Power!");
        }

        String hpValue = fieldHitPoints.getText().toString();
        if ( hpValue == null || hpValue.length() == 0 || Integer.parseInt(hpValue) <= 0 ) {
            valid = false;

            fieldHitPoints.setError("Invalid Hit Points!");
        }

        if ( valid == true ) {
            return new EvaluationData(poke, Integer.parseInt(cpValue), Integer.parseInt(hpValue), Integer.parseInt(dustValue), checkPoweredUp.isChecked());
        }

        return null;
    }

    private String[] getPokemonNames() {
        String[] names = new String[pokemonList.size()];

        for ( int i = 0; i <= pokemonList.size() - 1; i++ ) {
            Pokemon p = pokemonList.get(i);
            names[i] = p.name;
        }

        return names;
    }

    private WindowManager.LayoutParams getViewParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int screenWidthPx = metrics.widthPixels;
        int viewWidthPx = screenWidthPx - 50;
        int viewX = ((screenWidthPx / 2) - (viewWidthPx / 2)) - 25;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.y = 250;
        params.x = viewX;
        params.gravity = Gravity.TOP;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = viewWidthPx;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;

        return params;
    }

    private void createCalculateOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        calculateOverlay = inflater.inflate(R.layout.calculate_overlay, null);

        calculateOverlay.setClickable(true);
        calculateOverlay.setLongClickable(true);
        calculateOverlay.setOnTouchListener(this);
        calculateOverlay.setOnLongClickListener(this);

        // Button Handlers
        Button buttonClose = (Button)calculateOverlay.findViewById(R.id.buttonClose);
        buttonClose.setClickable(true);
        buttonClose.setLongClickable(true);
        buttonClose.setOnClickListener(this);
        buttonClose.setOnTouchListener(this);
        buttonClose.setOnLongClickListener(this);

        Button buttonCalc = (Button)calculateOverlay.findViewById(R.id.buttonCalc);
        buttonCalc.setClickable(true);
        buttonCalc.setLongClickable(true);
        buttonCalc.setOnClickListener(this);
        buttonCalc.setOnTouchListener(this);
        buttonCalc.setOnLongClickListener(this);

        Button buttonBack = (Button)calculateOverlay.findViewById(R.id.buttonBack);
        buttonBack.setClickable(true);
        buttonBack.setLongClickable(true);
        buttonBack.setOnClickListener(this);
        buttonBack.setOnTouchListener(this);
        buttonBack.setOnLongClickListener(this);

        // AutoCompletes
        ArrayAdapter<String> pokemonNamesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, getPokemonNames());

        fieldPokemonName = (AutoCompleteTextView)calculateOverlay.findViewById(R.id.fieldPokemonName);
        fieldPokemonName.setThreshold(2);
        fieldPokemonName.setAdapter(pokemonNamesAdapter);
        fieldPokemonName.setLongClickable(true);
        fieldPokemonName.setOnTouchListener(this);
        fieldPokemonName.setOnLongClickListener(this);

        ArrayAdapter<Integer> combatPowersAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_dropdown_item_1line, dustCosts.toArray(new Integer[dustCosts.size()]));

        fieldDustCost = (AutoCompleteTextView)calculateOverlay.findViewById(R.id.fieldDustCost);
        fieldDustCost.setThreshold(1);
        fieldDustCost.setAdapter(combatPowersAdapter);
        fieldDustCost.setLongClickable(true);
        fieldDustCost.setOnTouchListener(this);
        fieldDustCost.setOnLongClickListener(this);

        // EditTexts
        fieldCombatPower = (EditText)calculateOverlay.findViewById(R.id.fieldCombatPower);
        fieldCombatPower.setLongClickable(true);
        fieldCombatPower.setOnTouchListener(this);
        fieldCombatPower.setOnLongClickListener(this);

        fieldHitPoints = (EditText)calculateOverlay.findViewById(R.id.fieldHitPoints);
        fieldHitPoints.setLongClickable(true);
        fieldHitPoints.setOnTouchListener(this);
        fieldHitPoints.setOnLongClickListener(this);

        // CheckBoxes
        checkPoweredUp = (CheckBox)calculateOverlay.findViewById(R.id.checkPowered);
        checkPoweredUp.setLongClickable(true);
        checkPoweredUp.setOnTouchListener(this);
        checkPoweredUp.setOnLongClickListener(this);

        // Tables
        tableForm = calculateOverlay.findViewById(R.id.tableForm);
        tableResults = calculateOverlay.findViewById(R.id.tableResult);

        // Add View
        windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(calculateOverlay, getViewParams());
    }
    //endregion

    //region - Overrides -
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Integer[] values = new Integer[] { 200, 400, 600, 800, 1000, 1300, 1600, 1900, 2200, 2500, 3000, 3500,
                4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000 };

        dustCosts = new ArrayList<>();
        for ( Integer value : values ) {
            dustCosts.add(value);
        }

        pokemonList = Pokemon.loadAllPokemon(getAssets());
        levelUpDataList = LevelUpData.loadAllLevelUpData(getAssets());

        createCalculateOverlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( calculateOverlay != null ) {
            windowManager.removeView(calculateOverlay);

            pokemonList = null;
            levelUpDataList = null;
            dustCosts = null;

            fieldHitPoints = null;
            fieldDustCost = null;
            fieldCombatPower = null;
            fieldPokemonName = null;
            checkPoweredUp = null;

            tableForm = null;
            tableResults = null;

            calculateOverlay = null;
        }
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.buttonClose ) {
            stopService(new Intent(this, CalculateOverlayService.class));

            Intent intent = new Intent(getApplicationContext(), ToggleOverlayService.class);
            startService(intent);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
        }
        else if ( v.getId() == R.id.buttonCalc ) {
            EvaluationData ed = getEvaluationData();
            if ( ed != null ) {
                ed.pokemon.evaluate(ed, levelUpDataList);

                if ( ed.pokemon.potentialIVs.size() > 0 ) {
                    Pokemon.PossibleIV maxPossible = ed.pokemon.potentialIVs.get(ed.pokemon.potentialIVs.size() - 1);

                    int min = (int) ed.pokemon.potentialIVs.get(0).perfection;
                    int max = (int) maxPossible.perfection;
                    int average = 0;

                    int total = 0;
                    for (Pokemon.PossibleIV piv : ed.pokemon.potentialIVs) {
                        total += piv.perfection;
                    }

                    average = (total / ed.pokemon.potentialIVs.size());

                    ((TextView) calculateOverlay.findViewById(R.id.textMinIV)).setText(Integer.toString(min) + "%");
                    ((TextView) calculateOverlay.findViewById(R.id.textAvgIV)).setText(Integer.toString(average) + "%");
                    ((TextView) calculateOverlay.findViewById(R.id.textMaxIV)).setText(Integer.toString(max) + "%");

                    ((TextView) calculateOverlay.findViewById(R.id.textAttackIV)).setText(Integer.toString(maxPossible.attackIV));
                    ((TextView) calculateOverlay.findViewById(R.id.textDefenseIV)).setText(Integer.toString(maxPossible.defenseIV));
                    ((TextView) calculateOverlay.findViewById(R.id.textStaminaIV)).setText(Integer.toString(maxPossible.staminaIV));

                    tableForm.setVisibility(View.INVISIBLE);
                    tableResults.setVisibility(View.VISIBLE);

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
                }
            }
        }
        else if ( v.getId() == R.id.buttonBack ) {
            tableResults.setVisibility(View.INVISIBLE);
            tableForm.setVisibility(View.VISIBLE);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
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
            calculateOverlay.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;
        } else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams)calculateOverlay.getLayoutParams();

            int newX = (int)(offsetX + x);
            int newY = (int)(offsetY + y);

            if ( Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !isMoving ) {
                return false;
            }

            params.x = newX;
            params.y = newY;

            windowManager.updateViewLayout(calculateOverlay, params);
            isMoving = true;
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            if ( isMoving ) {
                calculateOverlay.getBackground().setAlpha(255);
                isMovable = false;

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onLongClick(View view) {
        calculateOverlay.getBackground().setAlpha(128);
        isMovable = true;

        return true;
    }
    //endregion
}
