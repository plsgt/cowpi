package net.cowpi.android.perf;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.cowpi.android.R;

import java.util.logging.Logger;

public class PerfActivity extends AppCompatActivity {
    private static final Logger logger = Logger.getLogger(PerfActivity.class.toString());

    private PerfExperiment experiment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perf);

        experiment = DaggerPerfComponent.builder().build().experiment();

        experiment.setup();
    }

    @Override
    protected void onDestroy() {
        experiment.shutdown();
        super.onDestroy();
    }

    public void runTests(View view){
        logger.info("Running tests...");

        new StartExpAsyncTask(experiment).execute();
    }


}
