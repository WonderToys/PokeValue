package com.wondertoys.pokevalue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import com.wondertoys.pokevalue.calculator.LevelData;
import com.wondertoys.pokevalue.calculator.LevelDustCosts;
import com.wondertoys.pokevalue.calculator.Pokemon;
import com.wondertoys.pokevalue.utils.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class CalculateOverlayService extends Service implements View.OnClickListener, View.OnTouchListener,
        View.OnLongClickListener, ToolTipsManager.TipListener {

    //region - Classes -
    private class EvaluationData {
        public Pokemon pokemon;

        public int cp;
        public int hp;
        public int dust;

        public boolean powered;
    }
    //endregion

    //region - Fields -
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;

    private int currentShowingTooltip;

    private Boolean isMoving = false;
    private Boolean isMovable = false;

    private ArrayList<Pokemon> pokemonList;
    ArrayList<Pokemon.Potential> potentials;

    private Pokemon.Potential minPotential;
    private Pokemon.Potential maxPotential;
    private Pokemon.Potential perfectPotential;

    ToolTip statsToolTip;

    private View calculateOverlay;
    private View tableForm;
    private View tableResults;
    private View scrollViewMoreStats;

    private TableLayout tableMoreStats;
    private TableLayout tableMoveData;

    RelativeLayout rootView;

    private AutoCompleteTextView fieldPokemonName;
    private AutoCompleteTextView fieldDustCost;

    private EditText fieldCombatPower;
    private EditText fieldHitPoints;

    private CheckBox checkPoweredUp;

    private Button buttonMoreStats;

    private ArcProgress minPerfection;
    private ArcProgress maxPerfection;
    private ArcProgress perfectPerfection;

    private WindowManager windowManager;
    private ToolTipsManager toolTipsManager;
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

    private void shakeMe() {
        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        tableForm.startAnimation(shake);
    }

    @Nullable
    private EvaluationData getEvaluationData() {
        Boolean valid = true;

        Pokemon poke = getPokemonByName(fieldPokemonName.getText().toString());
        if ( poke == null ) {
            valid = false;

            fieldPokemonName.setError("Invalid Pokemon Name!");
        }

        String dustValue = fieldDustCost.getText().toString();
        if ( dustValue == null || dustValue.length() == 0 || !LevelDustCosts.validDustCost(Integer.parseInt(dustValue)) ) {
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
            EvaluationData ed = new EvaluationData();
            ed.pokemon = poke;
            ed.cp = Integer.parseInt(cpValue);
            ed.hp = Integer.parseInt(hpValue);
            ed.dust = Integer.parseInt(dustValue);
            ed.powered = checkPoweredUp.isChecked();

            return ed;
        }
        else {
            shakeMe();
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
        Point overlayLoc = Preferences.getCalculateOverlayLocation(this);

        // Get default X
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int screenWidthPx = metrics.widthPixels;
        int viewWidthPx = screenWidthPx - 50;
        int defaultX = ((screenWidthPx / 2) - (viewWidthPx / 2)) - 25;
        if ( overlayLoc.x == -1 ) {
            overlayLoc.set(defaultX, overlayLoc.y);
        }

        // Get LayoutParams
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.y = overlayLoc.y;
        params.x = overlayLoc.x;
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

        buttonMoreStats = (Button)calculateOverlay.findViewById(R.id.buttonMoreStats);
        buttonMoreStats.setClickable(true);
        buttonMoreStats.setLongClickable(true);
        buttonMoreStats.setOnClickListener(this);
        buttonMoreStats.setOnTouchListener(this);
        buttonMoreStats.setOnLongClickListener(this);

        // GetRootView
        rootView = (RelativeLayout)calculateOverlay.findViewById(R.id.rootCalculateLayout);

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
                android.R.layout.simple_dropdown_item_1line, LevelDustCosts.getDustCosts());

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

        // Arcs
        minPerfection = (ArcProgress)calculateOverlay.findViewById(R.id.minPerfection);
        minPerfection.setClickable(true);
        minPerfection.setOnClickListener(this);

        maxPerfection = (ArcProgress)calculateOverlay.findViewById(R.id.maxPerfection);
        maxPerfection.setClickable(true);
        maxPerfection.setOnClickListener(this);

        perfectPerfection = (ArcProgress)calculateOverlay.findViewById(R.id.perfectPerfection);
        perfectPerfection.setClickable(true);
        perfectPerfection.setOnClickListener(this);

        // More Stats Views
        scrollViewMoreStats = calculateOverlay.findViewById(R.id.scrollViewMoreStats);

        tableMoreStats = (TableLayout)calculateOverlay.findViewById(R.id.tableMoreStats);
        tableMoreStats.removeAllViews();

        tableMoveData = (TableLayout)calculateOverlay.findViewById(R.id.tableMoveData);
        tableMoveData.removeAllViews();

        // Add View
        windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(calculateOverlay, getViewParams());
    }

    private void hideToolTip() {
        toolTipsManager.findAndDismiss(minPerfection);
        toolTipsManager.findAndDismiss(maxPerfection);
        toolTipsManager.findAndDismiss(perfectPerfection);
    }

    private void showToolTip(View v, String text, int alignment) {
        hideToolTip();
        if ( currentShowingTooltip == v.getId() ) {
            currentShowingTooltip = -1;
            return;
        }

        // Create ToolTip
        ToolTip.Builder builder = new ToolTip.Builder(getApplicationContext(), v, rootView, text, ToolTip.POSITION_BELOW);
        builder.setAlign(alignment);
        builder.setOffsetY(-15);
        builder.setBackgroundColor(getResources().getColor(R.color.colorDeepPurple));

        statsToolTip = builder.build();

        toolTipsManager.show(statsToolTip);
        currentShowingTooltip = v.getId();
    }

    private View getMoreStatsHeaderRow(LayoutInflater inflater) {
        TableRow headerRow = (TableRow)inflater.inflate(R.layout.more_stats_row, null);

        TextView rowLevel = (TextView)headerRow.findViewById(R.id.rowLevel);
        rowLevel.setTypeface(null, Typeface.BOLD);
        rowLevel.setText("LVL");

        TextView rowAttack = (TextView)headerRow.findViewById(R.id.rowAttack);
        rowAttack.setTypeface(null, Typeface.BOLD);
        rowAttack.setText("ATK");

        TextView rowDefense = (TextView)headerRow.findViewById(R.id.rowDefense);
        rowDefense.setTypeface(null, Typeface.BOLD);
        rowDefense.setText("DEF");

        TextView rowStamina = (TextView)headerRow.findViewById(R.id.rowStamina);
        rowStamina.setTypeface(null, Typeface.BOLD);
        rowStamina.setText("STAM");

        TextView rowCombatPower = (TextView)headerRow.findViewById(R.id.rowCombatPower);
        rowCombatPower.setTypeface(null, Typeface.BOLD);
        rowCombatPower.setText("%");

        return headerRow;
    }

    private View getMoreStatsRow(LayoutInflater inflater, Pokemon.Potential potential) {
        View row = inflater.inflate(R.layout.more_stats_row, null);

        TextView rowLevel = (TextView)row.findViewById(R.id.rowLevel);
        rowLevel.setText(String.format("%.1f", potential.level));

        TextView rowAttack = (TextView)row.findViewById(R.id.rowAttack);
        rowAttack.setText(String.format("%d (%d)", potential.attack, potential.attackIV));

        TextView rowDefense = (TextView)row.findViewById(R.id.rowDefense);
        rowDefense.setText(String.format("%d (%d)", potential.defense, potential.defenseIV));

        TextView rowStamina = (TextView)row.findViewById(R.id.rowStamina);
        rowStamina.setText(String.format("%d (%d)", potential.stamina, potential.staminaIV));

        TextView rowCombatPower = (TextView)row.findViewById(R.id.rowCombatPower);
        rowCombatPower.setText(String.format("%.1f%%", potential.perfection));

        return row;
    }

    private void buildMoreStatsTable() {
        LayoutInflater inflater = LayoutInflater.from(this);

        tableMoreStats.removeAllViews();

        tableMoreStats.addView(getMoreStatsHeaderRow(inflater), new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));


        Collections.reverse(potentials);
        for ( Pokemon.Potential p : potentials ) {
            tableMoreStats.addView(getMoreStatsRow(inflater, p), new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
        }
        Collections.reverse(potentials);
    }

    private View getMoveDataHeaderRow(LayoutInflater inflater) {
        TableRow headerRow = (TableRow)inflater.inflate(R.layout.move_data_row, null);

        TextView rowLevel = (TextView)headerRow.findViewById(R.id.rowMoveType);
        rowLevel.setTypeface(null, Typeface.BOLD);
        rowLevel.setText("TYPE");

        TextView rowAttack = (TextView)headerRow.findViewById(R.id.rowMoveName);
        rowAttack.setTypeface(null, Typeface.BOLD);
        rowAttack.setText("NAME");

        TextView rowDefense = (TextView)headerRow.findViewById(R.id.rowMovePower);
        rowDefense.setTypeface(null, Typeface.BOLD);
        rowDefense.setText("POW");

        TextView rowStamina = (TextView)headerRow.findViewById(R.id.rowMoveEnergy);
        rowStamina.setTypeface(null, Typeface.BOLD);
        rowStamina.setText("ENG");

        TextView rowCombatPower = (TextView)headerRow.findViewById(R.id.rowMoveDPS);
        rowCombatPower.setTypeface(null, Typeface.BOLD);
        rowCombatPower.setText("DPS");

        return headerRow;
    }

    private View getMoveDataRow(LayoutInflater inflater, Pokemon.Move move, String type) {
        View row = inflater.inflate(R.layout.move_data_row, null);

        TextView rowLevel = (TextView)row.findViewById(R.id.rowMoveType);
        rowLevel.setText(type);

        TextView rowAttack = (TextView)row.findViewById(R.id.rowMoveName);
        rowAttack.setText(move.name);

        TextView rowDefense = (TextView)row.findViewById(R.id.rowMovePower);
        rowDefense.setText(String.format("%d", move.power));

        TextView rowStamina = (TextView)row.findViewById(R.id.rowMoveEnergy);
        rowStamina.setText(String.format("%d", Math.abs(move.energy)));

        TextView rowCombatPower = (TextView)row.findViewById(R.id.rowMoveDPS);
        rowCombatPower.setText(String.format("%.2f", move.dps));

        return row;
    }

    private View getMoveDataSeparatorRow(LayoutInflater inflater) {
        TableRow headerRow = (TableRow)inflater.inflate(R.layout.move_data_row, null);

        TextView rowLevel = (TextView)headerRow.findViewById(R.id.rowMoveType);
        rowLevel.setTypeface(null, Typeface.BOLD);
        rowLevel.setTextColor(Color.parseColor("#AAAAAA"));
        rowLevel.setText("-");

        TextView rowAttack = (TextView)headerRow.findViewById(R.id.rowMoveName);
        rowAttack.setTypeface(null, Typeface.BOLD);
        rowAttack.setTextColor(Color.parseColor("#AAAAAA"));
        rowAttack.setText("-");

        TextView rowDefense = (TextView)headerRow.findViewById(R.id.rowMovePower);
        rowDefense.setTypeface(null, Typeface.BOLD);
        rowDefense.setTextColor(Color.parseColor("#AAAAAA"));
        rowDefense.setText("-");

        TextView rowStamina = (TextView)headerRow.findViewById(R.id.rowMoveEnergy);
        rowStamina.setTypeface(null, Typeface.BOLD);
        rowStamina.setTextColor(Color.parseColor("#AAAAAA"));
        rowStamina.setText("-");

        TextView rowCombatPower = (TextView)headerRow.findViewById(R.id.rowMoveDPS);
        rowCombatPower.setTypeface(null, Typeface.BOLD);
        rowCombatPower.setTextColor(Color.parseColor("#AAAAAA"));
        rowCombatPower.setText("-");

        return headerRow;
    }

    private void buildMoveDataTable(Pokemon pokemon) {
        LayoutInflater inflater = LayoutInflater.from(this);

        tableMoveData.removeAllViews();

        tableMoveData.addView(getMoveDataHeaderRow(inflater), new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));


        for ( int i = 0; i < pokemon.fastMoves.size(); i++ ) {
            Pokemon.Move m = pokemon.fastMoves.get(i);
            TableRow row = (TableRow)getMoveDataRow(inflater, m, "FAST");

            if ( i == 0 ) {
                row.setBackgroundColor(Color.parseColor("#504caf50"));
            }

            tableMoveData.addView(row, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
        }

        tableMoveData.addView(getMoveDataSeparatorRow(inflater), new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));


        for ( int i = 0; i < pokemon.chargeMoves.size(); i++ ) {
            Pokemon.Move m = pokemon.chargeMoves.get(i);
            TableRow row = (TableRow)getMoveDataRow(inflater, m, "CHARGE");

            if ( i == 0 ) {
                row.setBackgroundColor(Color.parseColor("#504caf50"));
            }

            tableMoveData.addView(row, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            ));
        }
    }
    //endregion

    //region - Overrides -
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        toolTipsManager = new ToolTipsManager(this);

        pokemonList = Pokemon.loadAllPokemon(getAssets());
        LevelData.loadData(getAssets());
        LevelDustCosts.loadData(getAssets());

        createCalculateOverlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( calculateOverlay != null ) {
            windowManager.removeView(calculateOverlay);

            minPotential = null;
            maxPotential = null;

            pokemonList = null;
            potentials = null;

            fieldHitPoints = null;
            fieldDustCost = null;
            fieldCombatPower = null;
            fieldPokemonName = null;

            statsToolTip = null;

            checkPoweredUp = null;

            minPerfection = null;
            perfectPerfection = null;
            maxPerfection = null;

            tableForm = null;
            tableResults = null;
            tableMoreStats = null;
            tableMoveData = null;

            scrollViewMoreStats = null;

            buttonMoreStats = null;

            rootView = null;

            calculateOverlay = null;
            toolTipsManager = null;
        }
    }

    @Override
    public void onClick(View v) {
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        if ( vibrator.hasVibrator() ) {
            vibrator.vibrate(20);
        }

        if ( v.getId() == R.id.buttonClose ) {
            stopService(new Intent(this, CalculateOverlayService.class));

            Intent intent = new Intent(getApplicationContext(), ToggleOverlayService.class);
            startService(intent);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
        }
        else if ( v.getId() == R.id.buttonCalc ) {
            potentials = null;
            EvaluationData ed = getEvaluationData();

            if ( ed != null ) {
                potentials = ed.pokemon.evaluate(ed.cp, ed.hp, ed.dust, ed.powered);

                int potentialsSize = potentials.size();
                if ( potentialsSize == 0 ) {
                    fieldPokemonName.setError("No potentials found!");
                    fieldCombatPower.setError("No potentials found!");
                    fieldHitPoints.setError("No potentials found!");
                    fieldDustCost.setError("No potentials found!");

                    shakeMe();

                    return;
                }

                fieldPokemonName.setError(null);
                fieldCombatPower.setError(null);
                fieldHitPoints.setError(null);
                fieldDustCost.setError(null);

                minPotential = potentials.get(0);
                maxPotential = potentials.get(potentials.size() - 1);

                perfectPotential = ed.pokemon.getPotential(15, 15, 15, LevelData.getCpScalar(40.5));
                perfectPotential.level = 40.5;

                // Set Values
                minPerfection.setProgress((int)minPotential.perfection);
                maxPerfection.setProgress((int)maxPotential.perfection);

                perfectPerfection.setMax(perfectPotential.cp);
                perfectPerfection.setProgress(perfectPotential.cp);
                perfectPerfection.setSuffixText("");

                buildMoreStatsTable();
                buildMoveDataTable(ed.pokemon);

                Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
                Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

                tableForm.startAnimation(animFadeOut);
                tableResults.setAnimation(animFadeIn);

                tableForm.setVisibility(View.INVISIBLE);
                tableResults.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
            }
        }
        else if ( v.getId() == R.id.buttonBack ) {
            hideToolTip();
            currentShowingTooltip = -1;

            scrollViewMoreStats.setVisibility(View.GONE);

            tableMoreStats.removeAllViews();
            tableMoveData.removeAllViews();

            buttonMoreStats.setText("More");

            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

            tableResults.setAnimation(animFadeOut);
            tableForm.startAnimation(animFadeIn);

            tableResults.setVisibility(View.INVISIBLE);
            tableForm.setVisibility(View.VISIBLE);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(calculateOverlay.getWindowToken(), 0);
        }
        else if ( v.getId() == R.id.buttonMoreStats ) {
            if ( scrollViewMoreStats.getVisibility() == View.VISIBLE ) {
                scrollViewMoreStats.setVisibility(View.GONE);
                buttonMoreStats.setText("More");
            }
            else {
                scrollViewMoreStats.setVisibility(View.VISIBLE);
                ((ScrollView)scrollViewMoreStats).smoothScrollTo(0, 0);

                buttonMoreStats.setText("Less");
            }
        }
        else if ( v.getId() == R.id.minPerfection ) {
            if ( potentials != null ) {
                Pokemon.Potential potential = minPotential;
                String text = String.format(getString(R.string.potential_tooltip), potential.level,
                        potential.attack, potential.attackIV, potential.defense, potential.defenseIV, potential.stamina, potential.staminaIV);

                showToolTip(v, text, ToolTip.ALIGN_LEFT);
            }
        }
        else if ( v.getId() == R.id.maxPerfection ) {
            if ( potentials != null ) {
                Pokemon.Potential potential = maxPotential;
                String text = String.format(getString(R.string.potential_tooltip), potential.level,
                        potential.attack, potential.attackIV, potential.defense, potential.defenseIV, potential.stamina, potential.staminaIV);

                showToolTip(v, text, ToolTip.ALIGN_CENTER);
            }
        }
        else if ( v.getId() == R.id.perfectPerfection ) {
            if ( potentials != null ) {
                Pokemon.Potential potential = perfectPotential;
                String text = String.format(getString(R.string.potential_tooltip), potential.level,
                        potential.attack, potential.attackIV, potential.defense, potential.defenseIV, potential.stamina, potential.staminaIV);

                showToolTip(v, text, ToolTip.ALIGN_RIGHT);
            }
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

    @Override
    public void onTipDismissed(View view, int anchorViewId, boolean byUser) {
        statsToolTip = null;
        currentShowingTooltip = -1;
    }
    //endregion
}
