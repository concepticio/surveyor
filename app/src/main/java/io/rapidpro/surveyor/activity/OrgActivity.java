package io.rapidpro.surveyor.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import io.rapidpro.surveyor.Logger;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorIntent;
import io.rapidpro.surveyor.adapter.FlowListAdapter;
import io.rapidpro.surveyor.data.Flow;
import io.rapidpro.surveyor.data.Org;
import io.rapidpro.surveyor.data.Submission;
import io.rapidpro.surveyor.fragment.FlowListFragment;
import io.rapidpro.surveyor.legacy.Legacy;
import io.rapidpro.surveyor.task.RefreshOrgTask;
import io.rapidpro.surveyor.ui.BlockingProgress;
import io.rapidpro.surveyor.ui.ViewCache;

/**
 * Home screen for an org - shows available flows and pending submissions
 */
public class OrgActivity extends BaseSubmissionsActivity implements FlowListFragment.Container {

    private Org org;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this holds our flow list fragment which shows all available flows
        setContentView(R.layout.activity_org);

        if (savedInstanceState == null) {
            Fragment fragment = new FlowListFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, fragment).commit();
        }

        refresh();

        // if this org doesn't have downloaded assets, ask the user if we can download them now
        if (!org.hasAssets()) {
            confirmRefreshOrg(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    protected void refresh() {
        String orgUUID = getIntent().getStringExtra(SurveyorIntent.EXTRA_ORG_UUID);
        try {
            org = getSurveyor().getOrgService().get(orgUUID);
        } catch (Exception e) {
            Logger.e("Unable to load org", e);
            showBugReportDialog();
            finish();
            return;
        }

        setTitle(org.getName());

        FlowListAdapter adapter = (FlowListAdapter) getViewCache().getListViewAdapter(android.R.id.list);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        int pending = getSurveyor().getSubmissionService().getCompletedCount(getOrg());
        pending += Legacy.getCompletedCount(getOrg());

        ViewCache cache = getViewCache();
        cache.setVisible(R.id.container_pending, pending > 0);
        cache.setButtonText(R.id.button_pending, NumberFormat.getInstance().format(pending));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_org, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onActionRefresh(MenuItem item) {
        confirmRefreshOrg(false);
    }

    public void confirmRefreshOrg(boolean initial) {
        int msgId = initial ? R.string.confirm_org_download : R.string.confirm_org_refresh;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(msgId))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        doRefresh();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void doRefresh() {
        final BlockingProgress progressModal = new BlockingProgress(OrgActivity.this, R.string.one_moment, R.string.refresh_org);
        progressModal.show();

        new RefreshOrgTask(new RefreshOrgTask.Listener() {
            @Override
            public void onProgress(int percent) {
                progressModal.setProgress(percent);
            }

            @Override
            public void onComplete() {
                refresh();

                progressModal.dismiss();
            }

            @Override
            public void onFailure() {
                progressModal.dismiss();

                Toast.makeText(OrgActivity.this, getString(R.string.error_org_refresh), Toast.LENGTH_SHORT).show();
            }
        }).execute(getOrg());
    }

    /**
     * @see BaseSubmissionsActivity#getPendingSubmissions()
     */
    @Override
    protected List<Submission> getPendingSubmissions() {
        return getSurveyor().getSubmissionService().getCompleted(getOrg());
    }

    @Override
    protected List<File> getLegacySubmissions() {
        return Legacy.getCompleted(getOrg());
    }

    @Override
    public Org getOrg() {
        return org;
    }

    /**
     * @see FlowListFragment.Container#getListItems()
     */
    @Override
    public List<Flow> getListItems() {
        return getOrg().getFlows();
    }

    /**
     * @see FlowListFragment.Container#onItemClick(Flow)
     */
    @Override
    public void onItemClick(Flow flow) {
        Intent intent = new Intent(this, FlowActivity.class);
        intent.putExtra(SurveyorIntent.EXTRA_ORG_UUID, getOrg().getUuid());
        intent.putExtra(SurveyorIntent.EXTRA_FLOW_UUID, flow.getUuid());
        startActivity(intent);
    }
}
