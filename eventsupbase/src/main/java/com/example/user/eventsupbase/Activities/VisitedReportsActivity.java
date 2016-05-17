package com.example.user.eventsupbase.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.eventsupbase.HttpClient;
import com.example.user.eventsupbase.JsonParsing;
import com.example.user.eventsupbase.Models.Report;
import com.example.user.eventsupbase.Models.User;
import com.example.user.eventsupbase.R;

import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class VisitedReportsActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    String url_get_all_visited_reports;
    List<Report> reports;
    LinearLayout baseLinearLayout;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_reports);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        baseLinearLayout = (LinearLayout) findViewById(R.id.visited_linearLayout);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.visited_coordLayout);

        url_get_all_visited_reports = String.format("http://diploma.welcomeru.ru/visited/%s", User.md5_login);
        new GetJsonInfo().execute(url_get_all_visited_reports);
    }

    class GetJsonInfo extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(VisitedReportsActivity.this);
            pDialog.setMessage("Загрузка данных");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new HttpClient(params[0]);
            return httpClient.getData();
        }

        @Override
        protected void onPostExecute(String response) {
            pDialog.dismiss();
            switch (response) {
                case "[]":
                    Snackbar.make(coordinatorLayout, "Вы еще не посетили ни одного события", Snackbar.LENGTH_SHORT).show();
                    break;
                case "":
                    Snackbar.make(coordinatorLayout, "Такого юзера в системе нет", Snackbar.LENGTH_SHORT).show();
                    break;
                case "-2":
                case "0":
                    Snackbar.make(coordinatorLayout, "Ошибка или нет соединения с интернетом!", Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    JsonParsing parsing = new JsonParsing();
                    reports = parsing.GetReportsFromJsonString(response);
                    ShowAllReports(reports);
                    break;
            }
        }
    }

    private void ShowAllReports(List<Report> reports) {
        LayoutInflater layoutInflater = getLayoutInflater();

        for (int i = 0; i < reports.size(); i++) {
            LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.item_report, baseLinearLayout, false);
            linearLayout.setBackgroundColor(Color.parseColor("#c9f5ff"));
            linearLayout.setId(i);
            TextView report_title = (TextView) linearLayout.findViewById(R.id.c_report_title);
            TextView report_date = (TextView) linearLayout.findViewById(R.id.report_date);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)report_date.getLayoutParams();
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, 10, 0 , 20);
            report_date.setLayoutParams(params);
            TextView status = (TextView)linearLayout.findViewById(R.id.report_status);
            status.setVisibility(View.GONE);

            TextView report_address = (TextView) linearLayout.findViewById(R.id.report_address);
            report_address.setVisibility(View.GONE);
            TextView report_authors = (TextView) linearLayout.findViewById(R.id.report_authors);
            report_authors.setVisibility(View.GONE);
            TextView report_description = (TextView) linearLayout.findViewById(R.id.report_description);
            report_description.setVisibility(View.GONE);

            report_title.setText(reports.get(i).report_name);
            report_date.setText(reports.get(i).time.substring(0, reports.get(i).time.length() - 3));

            report_title.setTextColor(Color.parseColor("#8592a9"));
            report_date.setTextColor(Color.parseColor("#8592a9"));

            registerForContextMenu(linearLayout);
            baseLinearLayout.addView(linearLayout);
        }
    }

    public void onGridClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        String text = String.format("Отменить посещение %s доклад", v.getId()+1);
        menu.add(0, v.getId(), 0, text);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        String report_id = reports.get(item_id).id_report;
        String url_remove_visited_report = String.format("http://diploma.welcomeru.ru/remove/%s/%s", User.md5_login, report_id);
        new RemovingVisitedReport().execute(url_remove_visited_report);
        return super.onContextItemSelected(item);
    }

    class RemovingVisitedReport extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new HttpClient(params[0]);
            return httpClient.SendDataOrReturnVisitedReports();
        }

        protected void onPostExecute(String response) {
            switch (response) {
                case "[]":
                    Snackbar.make(coordinatorLayout, "Вы еще не посетили ни одного события", Snackbar.LENGTH_SHORT).show();
                    break;
                case "":
                    Snackbar.make(coordinatorLayout, "Такого юзера в системе нет", Snackbar.LENGTH_SHORT).show();
                    break;
                case "-2":
                case "0":
                    Snackbar.make(coordinatorLayout, "Ошибка или нет соединения с интернетом!", Snackbar.LENGTH_LONG).show();
                    break;
                case "1":
                    finish();
                    startActivity(getIntent());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_user:
                String message = String.format("Username: %s", User.login);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_exit:
                User.login = null;
                Intent intent3 = new Intent(this, StartActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_exit_user, menu);
        return true;
    }


}
