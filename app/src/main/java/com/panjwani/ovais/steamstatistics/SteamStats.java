package com.panjwani.ovais.steamstatistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SteamStats extends Activity {
    public String steamID = "";
    public ArrayList<Game> gamesList = new ArrayList<Game>();
    private static Context context;
    public ProgressDialog loading;
    public Spinner orderSpinner;
    private ArrayAdapter<String> spinnerArrayAdapter;
    private DataAdapter dataAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SteamStats.context = getApplicationContext();
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if(titleId == 0)
            titleId = R.id.action_bar_title;
        TextView mAppName = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "SF Speedwaystar.ttf");
        mAppName.setTypeface(face);
        //Enabling WebView and getting it to fullscreen
        final WebView webview = new WebView(this);
        boolean redirect;
        setContentView(webview);
        loading = new ProgressDialog(SteamStats.this);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
		/* WebViewClient must be set BEFORE calling loadUrl! */
        //So when the page is loaded it makes sure it's on the right place and then extracts html
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("url", url);
                if (url.equals("https://store.steampowered.com/account")) {
				/* This call inject JavaScript into the page which just finished loading. */
                    webview.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    setContentView(R.layout.activity_steam_stats);
                    orderSpinner = (Spinner) findViewById(R.id.orderSpinner);
                    final String[] spinnerText = {"Title", "Hours Played", "Paid Price", "Store Price", "Savings", "Hours/Dollar"};
                    spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerText);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    orderSpinner.setAdapter(spinnerArrayAdapter);
                    loading.setMessage("Retrieving Info");
                    loading.show();
                }else if(webview.getUrl().equals("https://store.steampowered.com/login/?redir=account&redir_ssl=1")){
                    showAlertDialog("Login Reason", "The login is only to reach your transactions page so that the app can " +
                            "receive the price you paid for your games.");
                }
            }
        });
		/* load a web page */
        webview.loadUrl("https://store.steampowered.com/account");
    }

    public void showAlertDialog(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create()
                .show();
    }

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        public void showHTML(String html)
        {
            Document doc = Jsoup.parse(html);

            //This is when I'm getting the titles for the transactions and putting them in an ArrayList
            Elements transactionsTitles = doc.getElementsByClass("transactionRowTitle");
            ArrayList<String> titles = new ArrayList<String>();
            for(Element transactionTitle : transactionsTitles){
                titles.add(transactionTitle.text());
            }

            //This is when I get the prices of the transactions and set the values accordingly
            Elements transactionsPrices = doc.getElementsByClass("transactionRowPrice");
            ArrayList<Double> prices = new ArrayList<Double>();
            for(Element transactionPrice : transactionsPrices){
                if(!transactionPrice.text().equals("Total")) {
                    if(transactionPrice.text().equals("Free")){
                        prices.add(0.0);
                    } else {
                        prices.add(Double.parseDouble(transactionPrice.text().substring(1)));
                    }
                }
            }

            //This is all to reach the steamID
            Element link = doc.getElementById("wishlist_link");
            String linkHref = link.attr("href");
            String[] linkComponents = linkHref.replace("http://", "").split("/");
            steamID = linkComponents[2];

            //This is when i put the two ArrayLists together in a HashMap and average the prices of the games if
            //there were multiple games in the transaction
            HashMap<String, Double> transactions = new HashMap<String, Double>();
            for(int i = 0; i < titles.size(); i++){
                if(titles.get(i).contains(",") && !titles.get(i).equals("Papers, Please")){
                    String[] games = titles.get(i).split(",");
                    int numGames = games.length;
                    for(int j = 0; j < numGames; j++){
                        transactions.put(games[j], prices.get(i)/numGames);
                    }
                } else{
                    transactions.put(titles.get(i), prices.get(i));
                }
            }

            //This is to add the price of DLCs for the games since most DLCs contain the entire name of the game
            //in the title of the transaction
            Iterator<Map.Entry<String, Double>> iter = transactions.entrySet().iterator();
            HashMap<String, Double> newGameList = new HashMap<String, Double>();
            while(iter.hasNext()) {
                Map.Entry<String,Double> entry = iter.next();
                boolean contains = false;
                for(String title: titles){
                    if(entry.getKey().contains(title) && entry.getKey().length() > title.length() + 3){
                        newGameList.put(title, transactions.get(title) + entry.getValue());
                        contains = true;
                    }
                }
                if(contains){
                    iter.remove();
                }
                //This is to remove special keywords that indicate that it has multiple games or was simply bought from the store
                if(entry.getKey().contains(" Bundle") && !contains){
                    if(!entry.getKey().contains("Humble")) {
                        newGameList.put(entry.getKey().split(" Bundle")[0], entry.getValue());
                    }
                    iter.remove();
                }else if(entry.getKey().contains(" Collection")&& !contains){
                    newGameList.put(entry.getKey().split(" Collection")[0], entry.getValue());
                    iter.remove();
                }else if(entry.getKey().contains(" Retail")&& !contains){
                    newGameList.put(entry.getKey().split(" Retail")[0], entry.getValue());
                    iter.remove();
                }else if(entry.getKey().contains(" Complete")&& !contains){
                    newGameList.put(entry.getKey().split(" Complete")[0], entry.getValue());
                    iter.remove();
                }
            }
            transactions.putAll(newGameList);
            titles = new ArrayList<String>(transactions.keySet());

            try {
                Document docGauge = Jsoup.connect("http://www.mysteamgauge.com/account?username=" + steamID).timeout(12000).get();
                Elements gameTitles = docGauge.getElementsByClass("title_col");
                Elements gameActualPrices = docGauge.getElementsByClass("store_price_default_usd_col");
                Elements gameHoursPlayed = docGauge.getElementsByClass("hours_played_col");
                Elements gameIcons = docGauge.getElementsByClass("icon_col");

                //This creates a table essentially from rows of Game objects
                for(int m = 1; m < gameTitles.size() - 1; m++){
                    Game newGame = new Game();
                    if(!gameTitles.get(m).text().contains("Beta") || !gameTitles.get(m).text().contains("Test")) {
                        newGame.setTitle(gameTitles.get(m).text().replaceAll("[\\.:]", ""));
                        if (gameActualPrices.get(m).text().equals("None")) {
                            newGame.setActualAmount(0.0);
                        } else {
                            newGame.setActualAmount(Double.parseDouble(gameActualPrices.get(m).text()));
                        }
                        newGame.setHoursPlayed(Double.parseDouble(gameHoursPlayed.get(m).text()));
                        newGame.makeIcon(gameIcons.get(m).childNode(0).attr("src"));
                        Log.d(newGame.title, Double.toString(newGame.actualAmount));
                        gamesList.add(newGame);
                    }
                }

                //This sets the paid price of each game in the Game object accordingly
                for(String title: titles){
                    int counter = 0;
                    boolean actualGame = false;
                    for(int i = 0; i < gamesList.size(); i++){
                        if(gamesList.get(i).title.equals(title)){
                            actualGame = true;
                            gamesList.get(i).setPaidAmount(transactions.get(title));
                        //A counter system for games in a collection
                        } else if(gamesList.get(i).title.contains(title) || title.contains(gamesList.get(i).title)){
                            counter++;
                        }
                    }
                    for(int j = 0; j < gamesList.size(); j++){
                        //Making the paid price 0 if the store price was 0 indicating it was a free game
                        if(gamesList.get(j).actualAmount == 0.0){
                            gamesList.get(j).setPaidAmount(0.0);
                        //Averaging the paid price based off the counter system
                        } else if((gamesList.get(j).title.contains(title) && !actualGame) || (title.contains(gamesList.get(j).title) && !actualGame)){
                            gamesList.get(j).setPaidAmount(transactions.get(title)/counter);
                        }
                    }
                }

                Log.d("size", Integer.toString(gamesList.size()));
                for(int i = 0; i < gamesList.size(); i++){
                    Log.d(gamesList.get(i).title, Double.toString(gamesList.get(i).paidAmount));
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        loading.dismiss();
                        orderSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                Game.COMPARE_ON = spinnerArrayAdapter.getItem(i);
                                dataAdapter.updateSort();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                Game.COMPARE_ON = "Title";
                                dataAdapter.updateSort();
                            }
                        });
                        dataAdapter = new DataAdapter(SteamStats.this, gamesList);
                        dataAdapter.updateSort();
                        ListView listView = (ListView) findViewById(R.id.listview);
                        listView.setAdapter(dataAdapter);
                    }
                });
            } catch (IOException e){
                showAlertDialog("Connection Error", "You require better bandwidth to update the data.");
            }
        }
    }
}
