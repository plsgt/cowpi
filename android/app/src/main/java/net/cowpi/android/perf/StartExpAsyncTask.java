package net.cowpi.android.perf;

import android.os.AsyncTask;

public class StartExpAsyncTask extends AsyncTask<Void, Void, Void> {

    private final PerfExperiment experiment;

    public StartExpAsyncTask(PerfExperiment experiment) {
        this.experiment = experiment;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            experiment.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
