package com.teamtreehouse.albumcover;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AlbumDetailActivity extends Activity {

    public static final String EXTRA_ALBUM_ART_RESID = "EXTRA_ALBUM_ART_RESID";

    @Bind(R.id.album_art) ImageView albumArtView;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.title_panel) ViewGroup titlePanel;
    @Bind(R.id.track_panel) ViewGroup trackPanel;
    @Bind(R.id.detail_container) ViewGroup detailContainer;

    private TransitionManager mTransitionManager;
    private Scene mExpandedScene;
    private Scene mCollapsedScene;
    private Scene mCurrentScene;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        ButterKnife.bind(this);
        populate();
        setupTransition();
        mCurrentScene = mExpandedScene;

    }

    private void animate() {

        ObjectAnimator oaFacX = ObjectAnimator.ofFloat(fab,"scaleX",0,1);
        ObjectAnimator oaFacY = ObjectAnimator.ofFloat(fab,"scaleY",0,1);
        AnimatorSet fabAnimatorScale = new AnimatorSet();
        fabAnimatorScale.playTogether(oaFacX, oaFacY);


        int titleStartValue=titlePanel.getTop();
        int  titleEndValue= titlePanel.getBottom();
       ObjectAnimator animatorTitle = ObjectAnimator.ofInt(titlePanel,"bottom",titleStartValue, titleEndValue);

        int trackStartValue=trackPanel.getTop();
        int trackEndValue= trackPanel.getBottom();
        ObjectAnimator animatorTrack = ObjectAnimator.ofInt(trackPanel,"bottom", trackStartValue,trackEndValue);

        titlePanel.setBottom(titleStartValue);
        trackPanel.setBottom(titleStartValue);
        fab.setScaleY(0);
        fab.setScaleX(0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(fabAnimatorScale, animatorTitle, animatorTrack);
        animatorSet.start();
    }

    @OnClick(R.id.album_art)
    public void onAlbumArtClick(View view) {
        animate();
    }

    @OnClick(R.id.track_panel)
    public void onTrackPanleClick(View view){

    if(mCurrentScene == mExpandedScene){
        mCurrentScene = mCollapsedScene;
    }else{
        mCurrentScene = mExpandedScene;
    }
    mTransitionManager.transitionTo(mCurrentScene);
    }

    private void setupTransition() {
        mTransitionManager = new TransitionManager();
        ViewGroup transitionRoot = detailContainer;
        //Collapse Scene
        mCollapsedScene = Scene.getSceneForLayout(transitionRoot, R.layout.activity_album_detail, this);
        mCollapsedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene=mCollapsedScene;
            }
        });

        TransitionSet collapseTransitionSet = new TransitionSet();
        collapseTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);



        Fade fadeOutLyrics = new Fade();
        fadeOutLyrics.addTarget(R.id.lyrics);
        fadeOutLyrics.setDuration(150);
        collapseTransitionSet.addTransition(fadeOutLyrics);

        ChangeBounds resetBounds  = new ChangeBounds();
        resetBounds.setDuration(200);

        collapseTransitionSet.addTransition(resetBounds);
        //Expanded Scene
        mExpandedScene = Scene.getSceneForLayout(transitionRoot, R.layout.activity_album_detail_expanded, this);
        mExpandedScene.setEnterAction(new Runnable() {
            @Override
            public void run() {
                ButterKnife.bind(AlbumDetailActivity.this);
                populate();
                mCurrentScene = mExpandedScene;

            }
        });

        TransitionSet expandTransitionSet = new TransitionSet();
        expandTransitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        ChangeBounds changeBounds  = new ChangeBounds();
        changeBounds.setDuration(200);

        expandTransitionSet.addTransition(changeBounds);

        Fade fadeLyrics = new Fade();
        fadeLyrics.addTarget(R.id.lyrics);
        fadeLyrics.setDuration(150);
        expandTransitionSet.addTransition(fadeLyrics);

        mTransitionManager.setTransition(mExpandedScene, mCollapsedScene, collapseTransitionSet);
        mTransitionManager.setTransition(mCollapsedScene, mExpandedScene, expandTransitionSet);
    }

    private void populate() {
        int albumArtResId = getIntent().getIntExtra(EXTRA_ALBUM_ART_RESID, R.drawable.mean_something_kinder_than_wolves);
        albumArtView.setImageResource(albumArtResId);

        Bitmap albumBitmap = getReducedBitmap(albumArtResId);
        colorizeFromImage(albumBitmap);
    }

    private Bitmap getReducedBitmap(int albumArtResId) {
        // reduce image size in memory to avoid memory errors
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 8;
        return BitmapFactory.decodeResource(getResources(), albumArtResId, options);
    }

    private void colorizeFromImage(Bitmap image) {
        Palette palette = Palette.from(image).generate();

        // set panel colors
        int defaultPanelColor = 0xFF808080;
        int defaultFabColor = 0xFFEEEEEE;
        titlePanel.setBackgroundColor(palette.getDarkVibrantColor(defaultPanelColor));
        trackPanel.setBackgroundColor(palette.getLightMutedColor(defaultPanelColor));

        // set fab colors
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed}
        };

        int[] colors = new int[]{
                palette.getVibrantColor(defaultFabColor),
                palette.getLightVibrantColor(defaultFabColor)
        };
        fab.setBackgroundTintList(new ColorStateList(states, colors));
    }
}
