/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yulay.oneimagefetcher.sample;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yulay.imagefetcher.OneImageFetcher;

import java.util.List;

public class ImageDetailActivity extends FragmentActivity implements OnClickListener {
    public static final String EXTRA_IMAGE = "extra_image";

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;

    @TargetApi(VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);

        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;

        // Set up ViewPager and backing adapter
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_page_margin));
        mPager.setOffscreenPageLimit(2);

        // Set up activity to go full screen
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb()) {
            final ActionBar actionBar = getActionBar();

            if (actionBar != null) {
                // Hide title text and set home as up
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);

                // Hide and show the ActionBar as the visibility changes
                mPager.setOnSystemUiVisibilityChangeListener(
                        new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int vis) {
                                if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                                    actionBar.hide();
                                } else {
                                    actionBar.show();
                                }
                            }
                        });

                // Start low profile mode and hide ActionBar
                mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                actionBar.hide();
            }
        }

        // Set the current item based on the extra passed in to this activity
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        /*if (extraCurrentItem != -1) {
            mPager.setCurrentItem(extraCurrentItem);
        }*/

        ImagesDataSource.get().loadImages(new ImagesDataSource.LoadCallback() {
            @Override
            public void onSuccess(List<String> images) {
                mAdapter.setImages(images);
                if (extraCurrentItem != -1) {
                    mPager.setCurrentItem(extraCurrentItem);
                }
            }

            @Override
            public void onFail() {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        OneImageFetcher.with(this).setExitTasksEarly(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        OneImageFetcher.with(this).setExitTasksEarly(true);
        OneImageFetcher.with(this).flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*OneImageFetcher.with(this).closeCache();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.clear_cache:
                OneImageFetcher.with(this).clearCache();
                Toast.makeText(
                        this, R.string.clear_cache_complete_toast,Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
     * could be a large number of items in the ViewPager and we don't want to retain them all in
     * memory at once but create/destroy them on the fly.
     */
    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private List<String> images;

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setImages(List<String> images) {
            this.images = images;
            notifyDataSetChanged();
        }

        public int getImagesSize() {
            return images != null ? images.size() : 0;
        }

        @Override
        public int getCount() {
            return getImagesSize();
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(getImagesSize() > 0 ? images.get(position) : "");
        }
    }

    /**
     * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode
     * when the ImageView is touched.
     */
    @TargetApi(VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
}
