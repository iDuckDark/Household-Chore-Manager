package com.uottawa.linkedpizza.householdchoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TabHost;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.*;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.*;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.*;

public class YoutubeAcitvity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String DEVELOPER_KEY = "AIzaSyAn2hKb-dwBT9jg3_L3autSMXDzmwapFHo";

    private static ArrayList<String> videoID = new ArrayList<>();

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer player = null;
    private WebView webView;
    private Button next;
    private Button prev;
    private boolean formState = true;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        // Retrieve state from intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            videoID = extras.getStringArrayList("videoID");
        }

        // Initialize tabs
        TabHost mainHost = (TabHost) findViewById(R.id.mainTabHost);
        mainHost.setup();

        // Tab 1
        TabHost.TabSpec ytTab = mainHost.newTabSpec("Youtube");
        ytTab.setIndicator("Youtube");
        ytTab.setContent(R.id.youtube);

        // Tab 2
        TabHost.TabSpec wbTab = mainHost.newTabSpec("Web Browser");
        wbTab.setIndicator("Web Browser");
        wbTab.setContent(R.id.web_browser);

        // Add tabs to TabHost
        mainHost.addTab(ytTab);
        mainHost.addTab(wbTab);

        TabHost tabhost = mainHost;
        tabhost.setCurrentTab(0);

        webView = (WebView) findViewById(R.id.webView);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.previous);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(DEVELOPER_KEY, this);

        tabhost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                System.out.println(s);
                if (s == "Web Browser")
                    formState = false;
                else
                    formState = true;

                updateUI();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null){
                    if (++index < videoID.size())
                        player.cueVideo(videoID.get(index));
                    else
                        index = 0;

                    // Add more videos to the list.
                    if (index >= (videoID.size()/2)){
                        
                    }
                }
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null){
                    if (--index > -1)
                        player.cueVideo(videoID.get(index));
                    else
                        index = videoID.size();
                }
            }
        });
    }

    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
    }

    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean restored){
        this.player = player;
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        for (String s : videoID) {
            System.out.println("VIDEO IDs: " + s);
        }

        if (!restored){
            if (videoID != null && index < videoID.size()) {
                player.cueVideo(videoID.get(index));
            }
        }
    }

    private void updateUI(){

        if (formState){
            // Stop google.
            if (webView != null) {
                webView.setVisibility(View.GONE);
            }
            // Show youtube.
            if (youTubePlayerView != null) {
                youTubePlayerView.setVisibility(View.VISIBLE);
                player.cueVideo(videoID.get(index));
            }
        }
        else {
            // Stop youtube.
            if (youTubePlayerView != null) {
                player.pause();
                youTubePlayerView.setVisibility(View.GONE);
            }
            // Show google.
            if (webView != null) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("https://www.google.ca/");
            }
        }
    }


    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
        }

        @Override
        public void onError(ErrorReason errorReason) {
            player.setPlayerStateChangeListener(playerStateChangeListener);
            player.setPlaybackEventListener(playbackEventListener);

            if (++index < videoID.size())
                player.cueVideo((videoID.get(index)));
            else
                index = 0;
        }
    };

    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };
}
