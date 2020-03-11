package edu.ucsd.cse110.walkstatic;

import android.widget.EditText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

import edu.ucsd.cse110.walkstatic.runs.Run;
import edu.ucsd.cse110.walkstatic.runs.RunProposal;
import edu.ucsd.cse110.walkstatic.time.TimeMachine;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)

public class ProposeRunTest {
    @Before
    public void setBasicMocks(){
        MockFirebaseHelpers.mockStorage();
    }

    @Test
    public void testTimeMachine() {
        FragmentScenario<ProposeRunFragment> scenario = FragmentScenario.launchInContainer(ProposeRunFragment.class);
        scenario.onFragment(activity -> {
            LocalDateTime fake = LocalDateTime.of(2020, 1, 1, 10, 10);
            TimeMachine.useFixedClockAt(fake);
            TimeMachine.setNow(fake);
            Assert.assertEquals(fake, TimeMachine.now());
        }).moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void testInvalidDate() {
        FragmentScenario<ProposeRunFragment> scenario = FragmentScenario.launchInContainer(ProposeRunFragment.class);
        scenario.onFragment(activity -> {
            LocalDateTime current = LocalDateTime.of(2020, 1, 2, 10, 10);
            TimeMachine.useFixedClockAt(current);
            TimeMachine.setNow(current);
            LocalDateTime invalid = LocalDateTime.of(2020, 1, 1, 10, 10);
            boolean valid = (invalid.getDayOfMonth() >= current.getDayOfMonth());
            Assert.assertEquals(current, TimeMachine.now());
            Assert.assertEquals(false, valid);
        }).moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void testTimeDate() {
        FragmentScenario<ProposeRunFragment> scenario = FragmentScenario.launchInContainer(ProposeRunFragment.class);
        scenario.onFragment(activity -> {
            Run run = new Run().setName("This is a run").setStartingPoint("This is a starting point").setFavorited(true);

            EditText date = activity.getActivity().findViewById(R.id.editDateText);
            EditText time = activity.getActivity().findViewById(R.id.editTimeText);
            RunProposal rp = new RunProposal(run);
            String fakeTime = "5:05";
            String fakeDate = "10/10/2020";

            rp.setTime(fakeTime);
            rp.setDate(fakeDate);

            date.setText(fakeDate);
            time.setText(fakeTime);

            assertThat(date.getText().toString()).isEqualTo(fakeDate);
            assertThat(time.getText().toString()).isEqualTo(fakeTime);
            assertThat(rp.getDate().equals(fakeDate));
            assertThat(rp.getTime().equals(fakeTime));
        }).moveToState(Lifecycle.State.DESTROYED);
    }

    @After
    public void checkForLeaks(){
        MockFirebaseHelpers.assertNoListenerLeak();
    }

}
