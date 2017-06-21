package com.piterwilson.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.MediaExtractor.SEEK_TO_PREVIOUS_SYNC;

/**
 * Created by liming on 17/3/21.
 */

public class DecodeWaveFrame extends AsyncTask<Void, Void, Void> {
    private MediaExtractor extractor;
    private MediaCodec codec;
    private boolean seekOffsetFlag = false;
    private long seekOffset;
    private int inputBufIndex;
    private ArrayList<Short> dataList;
//    private int maxSize;
    private String mUrlString;
    private DecodeWaveListener listener = null;
    private boolean isDecoder = true;
    private long duration;

    public DecodeWaveFrame(String mUrlString) {
        this.mUrlString = mUrlString;
    }

    @Override
    protected Void doInBackground(Void... params) {
        decodeLoop();
        return null;
    }

    /**
     * 设置监听方法
     *
     * @param listener
     */
    public void setDecodeListener(DecodeWaveListener listener) {
        this.listener = listener;
    }

    /**
     * @throws IOException
     */
    @SuppressLint("NewApi")
    private void decodeLoop() {
        if (this.listener != null) {
            this.listener.decodeStart();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isDecoder = false;
                    listener.decodeDissmisDialog();
                }
            },1500);
        }
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;

        // 这里配置一个路径文件
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(this.mUrlString);
//            File _file = new File(mUrlString);
//            ParcelFileDescriptor _parcelFileDescriptor = ParcelFileDescriptor.open(_file,ParcelFileDescriptor.MODE_READ_ONLY);
//            FileDescriptor _fileDescriptor = _parcelFileDescriptor.getFileDescriptor();
//            extractor.setDataSource(_fileDescriptor,1024*128*2*getmLoadCount(),1024*128*2*(getmLoadCount()+1));
//            _parcelFileDescriptor.close();
        } catch (Exception e) {
            return;
        }

        //获取多媒体文件信息
        MediaFormat format = extractor.getTrackFormat(0);
        //媒体类型
        String mime = format.getString(MediaFormat.KEY_MIME);

        // 检查是否为音频文件
        if (!mime.startsWith("audio/")) {
            Log.e("MP3RadioStreamPlayer", "不是音频文件!");
            return;
        }

        // 声道个数：单声道或双声道
        int channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        // if duration is 0, we are probably playing a live stream

        //时长
        duration = format.getLong(MediaFormat.KEY_DURATION);

        if (listener != null) {
            listener.maxDuration(duration);
        }
        // System.out.println("歌曲总时间秒:"+duration/1000000);

        //时长
        //int bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);

        // the actual decoder
        try {
            // 实例化一个指定类型的解码器,提供数据输出
            codec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
        codec.start();
        // 用来存放目标文件的数据
        codecInputBuffers = codec.getInputBuffers();
        // 解码后的数据
        codecOutputBuffers = codec.getOutputBuffers();

        // get the sample rate to configure AudioTrack
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);


        // 设置声道类型:AudioFormat.CHANNEL_OUT_MONO单声道，AudioFormat.CHANNEL_OUT_STEREO双声道
        int channelConfiguration = channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        //Log.i(TAG, "channelConfiguration=" + channelConfiguration);

        extractor.selectTrack(0);//选择读取音轨

        // start decoding
        final long kTimeOutUs = 10000;//超时
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        // 解码
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int noOutputCounter = 0;
        int noOutputCounterLimit = 50;

        while (!sawOutputEOS && noOutputCounter < noOutputCounterLimit) {
            if (isCancelled()) {
                break;
            }
            if(!isDecoder){
                try {
                    //防止死循环ANR
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            //是否加载
//            if(isSawFile){
            noOutputCounter++;
            if (!sawInputEOS) {
                if (seekOffsetFlag) {
                    seekOffsetFlag = false;
                    extractor.seekTo(seekOffset, SEEK_TO_PREVIOUS_SYNC);
                }

                inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);

                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];

                    int sampleSize =
                            extractor.readSampleData(dstBuf, 0 /* offset */);

                    long presentationTimeUs = 0;

                    if (sampleSize < 0) {
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                    }
                    codec.queueInputBuffer(
                            inputBufIndex,
                            0 /* offset */,
                            sampleSize,
                            presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);


                    if (!sawInputEOS) {
                        extractor.advance();
                    }
                }
            }

            // decode to PCM and push it to the AudioTrack player
            // 解码数据为PCM
            int res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            if (res >= 0) {
                //Log.d(LOG_TAG, "got frame, size " + info.size + "/" + info.presentationTimeUs);
                if (info.size > 0) {
                    noOutputCounter = 0;
                }

                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];

                final byte[] chunk = new byte[info.size];
                buf.get(chunk);
                buf.clear();
                if (chunk.length > 0) {

                    //根据数据的大小为把byte合成short文件
                    //然后计算音频数据的音量用于判断特征
                    short[] music = (!isBigEnd()) ? byteArray2ShortArrayLittle(chunk, chunk.length / 2) : byteArray2ShortArrayBig(chunk, chunk.length / 2);
                    Log.i("info", "Time>>>>>>=====Decode_start|||||||||||||:" + SystemClock.currentThreadTimeMillis());
                    sendData(music, music.length);
                    Log.i("info", "Time>>>>>>=====Decode_start=============:" + SystemClock.currentThreadTimeMillis());
                }
                Log.i("info", "Time>>>>>>=====Decode_start+++++++++:" + SystemClock.currentThreadTimeMillis());
                //释放
                codec.releaseOutputBuffer(outputBufIndex, false /* render */);
                Log.i("info", "Time>>>>>>=====Decode_start---------:" + SystemClock.currentThreadTimeMillis());
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    sawOutputEOS = true;
                }
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = codec.getOutputBuffers();
                Log.i("info", "MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");

            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = codec.getOutputFormat();
                Log.i("info", "MediaCodec.INFO_OUTPUT_FORMAT_CHANGED");

            }
            Log.i("info", "Time=====>Decode_stop:" + SystemClock.currentThreadTimeMillis());
        }
        if (this.listener != null) {
            this.listener.decodeIsOver();
        }
    }

    private void sendData(short[] shorts, int readSize) {
        if (dataList != null) {

            short resultMax = 0, resultMin = 0;
            for (short i = 0/*, k = 0*/; i < readSize; /*i++, */i += 1152/3) {
                resultMax = shorts[i];

                if (resultMax > 10000 || resultMax < -10000) {
                    resultMax = 10000;
                }

                if (dataList.size() > (1080 * 4)) {
                    dataList.remove(0);
                }

                dataList.add(resultMax);
            }
        }
    }

    private boolean isBigEnd() {
        short i = 0x1;
        boolean bRet = ((i >> 8) == 0x1);
        return bRet;
    }

    private short[] byteArray2ShortArrayBig(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2 + 1] & 0xff) | (data[i * 2] & 0xff) << 8);

        return retVal;
    }

    private short[] byteArray2ShortArrayLittle(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);

        return retVal;
    }

    /**
     * 设置数据的获取显示，设置最大的获取数，一般都是控件大小/线的间隔offset
     *
     * @param dataList 数据
     * @param maxSize  最大个数
     */
    public DecodeWaveFrame setDataList(ArrayList<Short> dataList, int maxSize) {
        this.dataList = dataList;
//        this.maxSize = maxSize;
        return this;
    }


    public void setSeekOffsetFlag(boolean seekOffsetFlag) {
        this.seekOffsetFlag = seekOffsetFlag;
    }

    public void setSeekOffset(long seekOffset) {
        this.seekOffset = seekOffset;
    }

    public void setDecoder(boolean decoder) {
        isDecoder = decoder;
    }
}
