package io.rapidpro.surveyor.task;

import android.os.AsyncTask;

import java.io.IOException;

import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.net.TembaException;
import io.rapidpro.surveyor.net.TembaService;

/**
 * Task for sending submissions to the server
 */
public class SubmitSubmissionsTask extends AsyncTask<Submission, Integer, Void> {

    private Listener listener;
    private int numFailed = 0;

    public SubmitSubmissionsTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Submission... submissions) {
        int s = 0;
        for (Submission submission : submissions) {
            try {
                submission.submit();
            } catch (IOException | TembaException e) {
                e.printStackTrace();
                numFailed++;
            }

            s++;
            publishProgress(100 * s / submissions.length);
        }
        return null;
    }

    /**
     * @see AsyncTask#onProgressUpdate(Object[])
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        listener.onProgress(values[0]);
    }

    /**
     * @see AsyncTask#onPostExecute(Object)
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (numFailed > 0) {
            this.listener.onFailure(numFailed);
        } else {
            this.listener.onComplete();
        }
    }

    public interface Listener {
        void onProgress(int percent);

        void onComplete();

        void onFailure(int numFailed);
    }
}