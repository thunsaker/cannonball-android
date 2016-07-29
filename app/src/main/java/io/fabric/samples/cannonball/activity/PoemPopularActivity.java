/**
 * Copyright (C) 2014 Twitter Inc and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric.samples.cannonball.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import io.fabric.samples.cannonball.R;

/**
 * PoemPopularActivity that displays a list of tweets, showing only the tweet text.
 */
public class PoemPopularActivity extends ListActivity {

    private static final String TAG = "PoemPopularActivity";
    private static final String SEARCH_QUERY = "#cannonballapp AND pic.twitter.com AND " +
            "(#adventure OR #nature OR #romance OR #mystery)";
    private static final String SEARCH_QUERY_ALT = "#toddlerLife AND pic.twitter.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        setUpViews();
    }

    private void setUpViews() {
        setUpPopularList();
        setUpBack();
    }

    private void setUpPopularList() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder().query(SEARCH_QUERY).build();

        final TweetTimelineListAdapter timelineAdapter = new TweetTimelineListAdapter(this, searchTimeline);
        setListAdapter(timelineAdapter);

        final LinearLayout popularEmpty = (LinearLayout) findViewById(android.R.id.empty);
        final LinearLayout popularNoTweets = (LinearLayout) findViewById(R.id.popular_poems_no_recent);
        final ProgressBar popularLoading = (ProgressBar) findViewById(R.id.loading);

        final ImageView create = (ImageView) findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final Handler popularHandler = new Handler();
        if(popularEmpty.getVisibility() == View.VISIBLE) {
            popularHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    popularNoTweets.setVisibility(View.VISIBLE);
                    popularNoTweets.startAnimation(new AlphaAnimation(0, 1));

                    popularLoading.startAnimation(new AlphaAnimation(1, 0));
                    popularLoading.setVisibility(View.GONE);
                }
            }, 10000);
        }

        final ImageView refresh = (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popularEmpty.setVisibility(View.VISIBLE);
                popularNoTweets.startAnimation(new AlphaAnimation(1, 0));
                popularNoTweets.setVisibility(View.GONE);

                popularHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popularNoTweets.setVisibility(View.VISIBLE);
                        popularNoTweets.startAnimation(new AlphaAnimation(0, 1));

                        popularLoading.startAnimation(new AlphaAnimation(1, 0));
                        popularLoading.setVisibility(View.GONE);
                    }
                }, 10000);

                timelineAdapter.refresh(new Callback<TimelineResult<Tweet>>() {
                    @Override
                    public void success(Result<TimelineResult<Tweet>> result) {
                        popularLoading.startAnimation(new AlphaAnimation(1, 0));
                        popularLoading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.i("PoemPopularActivity", "Failed to refresh.");
                    }
                });
            }
        });

        refresh.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(PoemPopularActivity.this, "Switching tweet list", Toast.LENGTH_SHORT).show();
                SearchTimeline searchTimelineAlt = new SearchTimeline.Builder().query(SEARCH_QUERY_ALT).build();

                final TweetTimelineListAdapter timelineAdapterAlt = new TweetTimelineListAdapter(PoemPopularActivity.this, searchTimelineAlt);
                setListAdapter(timelineAdapterAlt);

                return true;
            }
        });
    }

    private void setUpBack() {
        final ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("PopularTweets: getting back to theme chooser");
                onBackPressed();
            }
        });
    }
}
