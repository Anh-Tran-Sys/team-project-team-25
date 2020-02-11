package edu.ucsd.cse110.walkstatic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import edu.ucsd.cse110.walkstatic.runs.Run;
import edu.ucsd.cse110.walkstatic.speech.SpeechListener;
import edu.ucsd.cse110.walkstatic.speech.VoiceDictationFactory;
import edu.ucsd.cse110.walkstatic.speech.VoiceDictation;

public class EditRunFragment extends Fragment implements SpeechListener {
    private enum RunElement {
        NAME(R.id.dictate_name),
        STARTING_POINT(R.id.dictate_starting_point);

        private int buttonId;
        RunElement(int buttonId){
            this.buttonId = buttonId;
        }

        public int getButtonId(){
            return this.buttonId;
        }
    }
    private static String TYPE_KEY = "runKey";

    private VoiceDictation voiceDictation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        this.voiceDictation = VoiceDictationFactory.getVoiceDictation(this.getActivity());
        this.voiceDictation.setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_run, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        this.addSpeechListeners();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.done_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_save){
            this.saveRun();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView(){
        this.voiceDictation.cancel();
        super.onDestroyView();
    }

    private void saveRun(){
        EditText runName = this.getActivity().findViewById(R.id.run_name_text);
        EditText startingPoint = this.getActivity().findViewById(R.id.starting_point_text);
        String runText = runName.getText().toString() + startingPoint.getText();

        RunViewModel runViewModel = new ViewModelProvider(this.getActivity()).get(RunViewModel.class);
        runViewModel.setRun(new Run(this.safeGetUUID(), runName.getText().toString()));
        runName.clearFocus();
        startingPoint.clearFocus();
        Navigation.findNavController(this.getView()).navigateUp();
    }

    private int safeGetUUID(){
        if(getArguments() == null){
            return 0;
        }
        return getArguments().getInt("UUID", 0);
    }

    private void addSpeechListeners(){
        ImageButton nameButton = this.getActivity().findViewById(R.id.dictate_name);
        nameButton.setOnClickListener(new VoiceDictationClickListener(RunElement.NAME));

        ImageButton startingPointButton = this.getActivity().findViewById(R.id.dictate_starting_point);
        startingPointButton.setOnClickListener(new VoiceDictationClickListener(RunElement.STARTING_POINT));
    }

    private class VoiceDictationClickListener implements View.OnClickListener {
        RunElement runElement;
        public VoiceDictationClickListener(RunElement runElement){
            this.runElement = runElement;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt(TYPE_KEY, runElement.ordinal());
            colorMicButton(runElement, true);
            setButtonsEnabled(false);
            voiceDictation.doRecognition(bundle);
        }
    }

    @Override
    public void onSpeech(@NonNull String received, @Nullable Bundle options) {
        if(options == null){
            return;
        }
        RunElement element = RunElement.values()[options.getInt(TYPE_KEY)];
        EditText editText = null;
        if(element == RunElement.NAME) {
            editText = this.getActivity().findViewById(R.id.run_name_text);
        }
        if(element == RunElement.STARTING_POINT) {
            editText = this.getActivity().findViewById(R.id.starting_point_text);
        }
        editText.setText(received);
    }

    @Override
    public void onSpeechDone(boolean error, @Nullable Bundle options) {
        if(options == null){
            return;
        }
        colorMicButton(RunElement.values()[options.getInt(TYPE_KEY)], false);
        setButtonsEnabled(true);
    }

    private void colorMicButton(RunElement element, boolean active){
        ImageButton imageButton = this.getActivity().findViewById(element.getButtonId());
        int background = R.color.micBackgroundOff;
        int tint = R.color.micOff;
        if(active){
            background = R.color.micBackgroundActive;
            tint = R.color.micActive;
        }
        imageButton.setBackgroundTintList(getContext().getResources().getColorStateList(background, null));
        imageButton.setColorFilter(getContext().getColor(tint), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setButtonsEnabled(boolean enabled){
        for(RunElement element : RunElement.values()){
            ImageButton button = this.getActivity().findViewById(element.getButtonId());
            button.setEnabled(enabled);
        }
    }
}
