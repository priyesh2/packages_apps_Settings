/*
 * Copyright (C) 2017 The Android Open Source Project
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
 *
 */

package com.android.settings.search2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settings.search2.SearchResult.Builder;
import com.android.settings.testutils.FakeFeatureFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class IntentSearchViewHolderTest {

    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Context mContext;
    @Mock
    private SearchFragment mFragment;
    private FakeFeatureFactory mFeatureFactory;
    private IntentSearchViewHolder mHolder;
    private Drawable mIcon;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        FakeFeatureFactory.setupForTest(mContext);
        mFeatureFactory = (FakeFeatureFactory) FakeFeatureFactory.getFactory(mContext);

        final Context context = RuntimeEnvironment.application;
        View view = LayoutInflater.from(context).inflate(R.layout.search_intent_item, null);
        mHolder = new IntentSearchViewHolder(view);

        mIcon = context.getDrawable(R.drawable.ic_search_history);
    }

    @Test
    public void testConstructor_membersNotNull() {
        assertThat(mHolder.titleView).isNotNull();
        assertThat(mHolder.summaryView).isNotNull();
        assertThat(mHolder.iconView).isNotNull();
        assertThat(mHolder.breadcrumbView).isNotNull();
    }

    @Test
    public void testBindViewElements_allUpdated() {
        SearchResult result = getSearchResult(TITLE, SUMMARY, mIcon);
        mHolder.onBind(mFragment, result);
        mHolder.itemView.performClick();

        assertThat(mHolder.titleView.getText()).isEqualTo(TITLE);
        assertThat(mHolder.summaryView.getText()).isEqualTo(SUMMARY);
        assertThat(mHolder.iconView.getDrawable()).isEqualTo(mIcon);
        assertThat(mHolder.summaryView.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mHolder.breadcrumbView.getVisibility()).isEqualTo(View.GONE);

        verify(mFragment).onSearchResultClicked();
        verify(mFragment).startActivity(any(Intent.class));
        verify(mFeatureFactory.metricsFeatureProvider).action(any(Context.class),
                eq(MetricsProto.MetricsEvent.ACTION_CLICK_SETTINGS_SEARCH_RESULT),
                eq(((ResultPayload)result.payload).getIntent().getComponent().flattenToString()),
                any(Pair.class));
    }

    @Test
    public void testBindViewIcon_nullIcon_imageDrawableIsNull() {
        final SearchResult result = getSearchResult(TITLE, SUMMARY, null);
        mHolder.onBind(mFragment, result);

        assertThat(mHolder.iconView.getDrawable()).isNull();
    }

    @Test
    public void testBindViewElements_emptySummary_hideSummaryView() {
        final SearchResult result = new Builder()
                .addTitle(TITLE)
                .addRank(1)
                .addPayload(new ResultPayload(null))
                .addIcon(mIcon)
                .build();

        mHolder.onBind(mFragment, result);
        assertThat(mHolder.summaryView.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void testBindViewElements_withBreadcrumb_shouldFormatBreadcrumb() {
        final List<String> breadcrumbs = new ArrayList<>();
        breadcrumbs.add("a");
        breadcrumbs.add("b");
        breadcrumbs.add("c");
        final SearchResult result = new Builder()
                .addTitle(TITLE)
                .addRank(1)
                .addPayload(new ResultPayload(null))
                .addBreadcrumbs(breadcrumbs)
                .addIcon(mIcon)
                .build();

        mHolder.onBind(mFragment, result);
        assertThat(mHolder.breadcrumbView.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mHolder.breadcrumbView.getText()).isEqualTo("a > b > c");
    }

    @Test
    public void testBindElements_placeholderSummary_visibilityIsGone() {
        String nonBreakingSpace = mContext.getString(R.string.summary_placeholder);
        SearchResult result = new Builder()
                .addTitle(TITLE)
                .addSummary(nonBreakingSpace)
                .addPayload(new ResultPayload(null))
                .build();

        mHolder.onBind(mFragment, result);

        assertThat(mHolder.summaryView.getVisibility()).isEqualTo(View.GONE);
    }

    private SearchResult getSearchResult(String title, String summary, Drawable icon) {
        Builder builder = new Builder();
        builder.addTitle(title)
                .addSummary(summary)
                .addRank(1)
                .addPayload(new ResultPayload(
                        new Intent().setComponent(new ComponentName("pkg", "class"))))
                .addBreadcrumbs(new ArrayList<>())
                .addIcon(icon);

        return builder.build();
    }
}
