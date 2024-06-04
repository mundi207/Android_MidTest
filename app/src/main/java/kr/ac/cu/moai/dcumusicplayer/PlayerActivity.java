package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playBtn;
    private Button gobackBtn;
    private SeekBar seekBar;
    private TextView musicTime;

    int playPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        playBtn = (Button) findViewById(R.id.playBtn);
        gobackBtn = (Button) findViewById(R.id.goback);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        musicTime = (TextView) findViewById(R.id.tvDurationPos);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3"); // 여기서 mp3파일 이름을 받아오는 것 같다.
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file); // mp3 파일에 대해 메타데이터를 수집한다.

            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover); // 음악 표지

            TextView tvTitle = findViewById(R.id.tvTitle); // 음악 제목
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration); // 음악 길이
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist); // 음악 가수
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(mp3file); // 플레이 할 파일 지정하고
                        mediaPlayer.prepare(); // 파일 불러오고
                        mediaPlayer.start(); // 재생 시작
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (mediaPlayer != null && mediaPlayer.isPlaying() == false) { // mediaplayer 객체 존재, 재생중이 아니라면
                    mediaPlayer.seekTo(playPos); // 멈췄던 위치 부터
                    mediaPlayer.start(); // 재생
                } else if (mediaPlayer != null && mediaPlayer.isPlaying() == true) { // mediaplayer 객체 존재, 재생 중이라면
                    mediaPlayer.pause(); // 재생 멈춤
                    playPos = mediaPlayer.getCurrentPosition(); // 현재 재생 위치 저장
                    mediaPlayer.seekTo(playPos); // 재생위치 반영
                }
                seekBar.setMax(mediaPlayer.getDuration()); // 시크바 최대길이 설정
                new Thread(new Runnable() { // 스레드 생성
                    @Override
                    public void run() {
                        while(mediaPlayer.isPlaying()) { // 음악 재생 중이라면
                            try {
                                Thread.sleep(1000); // 1초마다
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            int TimePos = mediaPlayer.getCurrentPosition() / 1000;
                            int TimeMax = mediaPlayer.getDuration() / 1000;
                            seekBar.setProgress(mediaPlayer.getCurrentPosition()); // 시크바를 움직임
                            if(TimePos > 60) { // 음악 시간 표기
                                if((TimePos % 60) < 10) {
                                    musicTime.setText(TimePos / 60 + ":" + 0 + TimePos % 60 + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                                }
                                else
                                    musicTime.setText(TimePos / 60 + ":" + TimePos % 60 + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                            }
                            else if(TimePos < 10)
                                musicTime.setText(0 + ":" + 0 + TimePos + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                            else
                                musicTime.setText(0 + ":" + TimePos + "/" + TimeMax / 60 + ":" + TimeMax % 60);
                        }
                    }
                }).start();
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // 시크바 이벤트 처리
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser && mediaPlayer != null) { // 시크바를 터치하고, mediaplayer 객체가 존재하면,
                            mediaPlayer.seekTo(progress); // 원하는 위치부터 음악 시작
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }
        });
        gobackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying() == true) {
                    mediaPlayer.stop();
                    finish();
                }
            }
        });
    }
}