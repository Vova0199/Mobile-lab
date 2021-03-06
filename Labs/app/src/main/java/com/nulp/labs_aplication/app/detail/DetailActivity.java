package com.nulp.labs_aplication.app.detail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nulp.labs_aplication.R;
import com.nulp.labs_aplication.api.model.Genre;
import com.nulp.labs_aplication.api.model.Images;
import com.nulp.labs_aplication.api.model.Movie;
import com.nulp.labs_aplication.api.model.SpokenLanguage;
import com.nulp.labs_aplication.app.App;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DetailActivity extends AppCompatActivity implements DetailContract.View {
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_TITLE = "movie_title";

    @Inject
    DetailPresenter mDetailPresenter;

    @BindView(R.id.container)
    View mContentView;
    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.overviewHeader)
    View mOverviewHeader;
    @BindView(R.id.overviewTextView)
    TextView mOverviewTextView;
    @BindView(R.id.genresTextView)
    TextView mGenresTextView;
    @BindView(R.id.durationTextView)
    TextView mDurationTextView;
    @BindView(R.id.languageTextView)
    TextView mLanguageTextView;
    @BindView(R.id.bookButton)
    Button mBookButton;
    @BindView(R.id.textView)
    View mErrorView;
    @BindView(R.id.progressBar)
    View mLoadingView;

    private int mMovieId = -1;
    private Images mImages;

    public static Intent getStartIntent(Context context, int movieId, String movieTitle) {
        Intent i = new Intent(context, DetailActivity.class);
        i.putExtra(MOVIE_ID, movieId);
        i.putExtra(MOVIE_TITLE, movieTitle);
        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        DaggerDetailComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .detailModule(new DetailModule(this))
                .build()
                .inject(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMovieId = extras.getInt(MOVIE_ID);
            String movieTitle = extras.getString(MOVIE_TITLE);

            setTitle(movieTitle);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDetailPresenter.start(mMovieId);
    }

    @Override
    public void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
        showContent(false);
        mErrorView.setVisibility(View.GONE);
    }

    @Override
    public void showContent(Movie movie) {
        String fullImageUrl = getFullImageUrl(movie);

        if (!fullImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(fullImageUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .transition(withCrossFade())
                    .into(mImageView);
        }

        mOverviewTextView.setText(getOverview(movie.overview));
        mGenresTextView.setText(getGenres(movie));
        mDurationTextView.setText(getDuration(movie));
        mLanguageTextView.setText(getLanguages(movie));

        mLoadingView.setVisibility(View.GONE);
        showContent(true);
        mErrorView.setVisibility(View.GONE);
    }

    private String getDuration(Movie movie) {
        int runtime = movie.runtime;
        return runtime <= 0 ? "-" : getResources().getQuantityString(R.plurals.duration, runtime, runtime);
    }

    private String getOverview(String overview) {
        return TextUtils.isEmpty(overview) ? "-" : overview;
    }

    @NonNull
    private String getFullImageUrl(Movie movie) {
        String imagePath;

        if (movie.posterPath != null && !movie.posterPath.isEmpty()) {
            imagePath = movie.posterPath;
        } else {
            imagePath = movie.backdropPath;
        }

        if (mImages != null && mImages.baseUrl != null && !mImages.baseUrl.isEmpty()) {
            if (mImages.posterSizes != null) {
                if (mImages.posterSizes.size() > 4) {
                    // usually equal to 'w500'
                    return mImages.baseUrl + mImages.posterSizes.get(4) + imagePath;
                } else {
                    // back-off to hard-coded value
                    return mImages.baseUrl + "w500" + imagePath;
                }
            }
        }

        return "";
    }

    private String getGenres(Movie movie) {
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < movie.genres.size(); i++) {
            Genre genre = movie.genres.get(i);
            genres.append(genre.name).append(", ");
        }

        genres = new StringBuilder(removeTrailingComma(genres.toString()));

        return (genres.length() == 0) ? "-" : genres.toString();
    }

    private String getLanguages(Movie movie) {
        StringBuilder languages = new StringBuilder();
        for (int i = 0; i < movie.spokenLanguages.size(); i++) {
            SpokenLanguage language = movie.spokenLanguages.get(i);
            languages.append(language.name).append(", ");
        }

        languages = new StringBuilder(removeTrailingComma(languages.toString()));

        return (languages.length() == 0) ? "-" : languages.toString();
    }

    @NonNull
    private String removeTrailingComma(String text) {
        text = text.trim();
        if (text.endsWith(",")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    @Override
    public void showError() {
        mLoadingView.setVisibility(View.GONE);
        showContent(false);
        mErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationSet(Images images) {
        this.mImages = images;
    }

    private void showContent(boolean show) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;

        mContentView.setVisibility(visibility);
        mOverviewHeader.setVisibility(visibility);
        mOverviewTextView.setVisibility(visibility);
        mBookButton.setVisibility(visibility);
    }

    @OnClick(R.id.bookButton)
    void onBookButtonClick() {
        String url = getString(R.string.web_url) + mMovieId;

        if (Build.VERSION.SDK_INT >= 16) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

}
