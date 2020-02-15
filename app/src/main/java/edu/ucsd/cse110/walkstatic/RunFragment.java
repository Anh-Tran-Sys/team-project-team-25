package edu.ucsd.cse110.walkstatic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static android.content.Context.MODE_PRIVATE;

import edu.ucsd.cse110.walkstatic.fitness.DefaultBlueprints;
import edu.ucsd.cse110.walkstatic.fitness.FitnessListener;
import edu.ucsd.cse110.walkstatic.fitness.FitnessService;
import edu.ucsd.cse110.walkstatic.fitness.FitnessServiceFactory;
import edu.ucsd.cse110.walkstatic.fitness.GoogleFitAdapter;
import edu.ucsd.cse110.walkstatic.runs.Run;
import edu.ucsd.cse110.walkstatic.time.TimeMachine;


public class RunFragment extends Fragment {

    private DistanceTracker stepTracker;
    private FitnessService fitnessService;
    private SecondTimer timer;
    private Chronometer chronometer;

    LocalTime now;
    Clock clock;
    private Run run;



    private static final String TAG = "StepCountActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_run, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        initStepCount();
        Button startButton = getActivity().findViewById(R.id.startButton);

        chronometer = getActivity().findViewById(R.id.chronometer);
        chronometer.setText("00:00:00");
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                Instant startInstant = Instant.ofEpochMilli(run.getStartTime());
                LocalDateTime startTime = LocalDateTime.ofInstant(startInstant,ZoneId.systemDefault());
                LocalDateTime currentTime = TimeMachine.now();
                Duration runTime = Duration.between(startTime, currentTime);
                long time = runTime.toMillis();
                int hours = (int)(time /3600000);
                int minutes = (int)(time - hours * 3600000) / 60000;
                int seconds = (int)(time - hours * 3600000 - minutes * 60000) / 1000;
                String t = (hours < 10 ? "0"+hours: hours)+":"+(minutes < 10 ? "0"+minutes: minutes)+":"+ (seconds < 10 ? "0"+seconds: seconds);
                chronometer.setText(t);
            }
        });

        Button stopButton = getActivity().findViewById(R.id.stopButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepTracker.setStartPressed(true);
                chronometer.setBase(SystemClock.elapsedRealtime());
                run.setStartTime(TimeMachine.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                run.setInitialSteps(stepTracker.getStepTotal());
                saveCurrentRun();
                chronometer.start();
                startButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepTracker.setStartPressed(false);
                stopButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                addNewRun();
                deleteCurrentRun();
            }
        });
        loadCurrentRun();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//       If authentication was required during google fit setup, this will be called after the user authenticates
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fitnessService.getRequestCode()) {
                fitnessService.updateStepCount();
            }
        } else {
            Log.e(TAG, "ERROR, google fit result code: " + resultCode);
        }
    }


    private void initStepCount(){

        TextView textSteps = getActivity().findViewById(R.id.steps_today);
        textSteps.setText("--");
        this.fitnessService = FitnessServiceFactory.create(this.getActivity());
        this.fitnessService.setup();
        this.stepTracker = new DistanceTracker(this.fitnessService);

        Handler handler = new Handler();

        int secondDelay = 1000; //TODO make constant
        this.timer = new SecondTimer(secondDelay, handler);
        handler.postDelayed(this.timer, secondDelay);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(this.timer != null){
            this.timer.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Run Fragment", "Pausing");
        if(this.timer != null){
            this.timer.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Run Fragment", "Resuming");
        if(this.timer != null){
            this.timer.resume();
        }
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }

    private void updateStepCount(){
        //for day
        this.stepTracker.update();
        TextView textSteps = getActivity().findViewById(R.id.steps_today);
        long steps = this.stepTracker.getStepTotal();
        textSteps.setText(Long.toString(steps));
        if(stepTracker.isStartPressed() == true) { // for current run
            TextView textRunSteps = getActivity().findViewById(R.id.stepRunCount);
            textRunSteps.setText(Long.toString(this.run.calculateNewSteps(steps)));
        }
    }

    private void updateMilesCount(){
        TextView textMiles = getActivity().findViewById(R.id.miles_today);
        SharedPreferences sharedPreferences = (SharedPreferences) getActivity().getSharedPreferences("userHeight", MODE_PRIVATE);

        String uHeight = sharedPreferences.getString("height","-1");
        double miles = this.stepTracker.getMilesCount(uHeight, false);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String displayMiles = decimalFormat.format(miles);
        textMiles.setText(displayMiles);

        if(stepTracker.isStartPressed() == true) { // for current run
            TextView textRunSteps = getActivity().findViewById(R.id.mileRunCount);
            miles = this.stepTracker.getMilesCount(uHeight, true);
            displayMiles = decimalFormat.format(miles);
            textRunSteps.setText(displayMiles);
        }
    }

    private void saveCurrentRun() {
        String preferencesName = this.getResources().getString(R.string.current_run);
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(
                preferencesName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(preferencesName, this.run.toJSON()).apply();
    }

    private void deleteCurrentRun() {
        String preferencesName = this.getResources().getString(R.string.current_run);
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(
                preferencesName, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }
    private void loadCurrentRun() {
        Run currentRun;
        String preferencesName = this.getResources().getString(R.string.current_run);
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(
                preferencesName, Context.MODE_PRIVATE);
        String runName = sharedPreferences.getString(preferencesName, null );
        if(runName == null){
             currentRun = new Run();
            Button stopButton = getActivity().findViewById(R.id.stopButton);
            stopButton.setVisibility(View.GONE);
        } else {
             currentRun = new Run(runName);
            Button startButton = getActivity().findViewById(R.id.startButton);
            startButton.setVisibility(View.GONE);
            this.stepTracker.setStartPressed(true);
            this.chronometer.start();
        }
        this.run = currentRun;
    }

    private class SecondTimer implements Runnable{
        int delay;
        Handler timer;
        boolean stop;
        public SecondTimer(int delay, Handler timer){
            this.delay = delay;
            this.timer = timer;
            this.stop = false;
        }

        @Override
        public void run(){
            if(this.stop){
                return;
            }
            updateStepCount();
            updateMilesCount();
            timer.postDelayed(this, this.delay);
        }

        void resume(){
            this.stop = false;
            this.run();
        }

        void stop(){
            this.stop = true;
        }
    }

    private void addNewRun(){
        Navigation.findNavController(this.getActivity(), this.getId()).navigate(R.id.action_runActivityFragment_to_editRunFragment);
    }


}
