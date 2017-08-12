package com.zxt.dlna.dmr;

import android.content.Context;
import android.util.Log;

import com.zxt.dlna.util.Utils;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * @author offbye
 */
public class AVTransportService extends AbstractAVTransportService {

    private static final String TAG = "GstAVTransportService";

    final private Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players;

    private Context mContext;

    protected AVTransportService(LastChange lastChange, Context context, Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> players) {
        super(lastChange);
        this.mContext = context;
        this.players = players;
    }

    protected Map<UnsignedIntegerFourBytes, ZxtMediaPlayer> getPlayers() {
        return players;
    }

    protected ZxtMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        ZxtMediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    private void play(UnsignedIntegerFourBytes instanceId, URI uri, String metaData) throws AVTransportException {
        String type = MediaType.UNKNOWN;
        String name = null;
        if(metaData != null) {
            try {
                MetaData data = MetaData.parse(metaData);
                if(data.type != null){
                    type = data.type;
                }
                if(data.name != null){
                    name = data.name;
                }
            } catch (SAXException e) {
                Log.e(TAG, "Parse meta data", e);
            }
        }

        if(MediaType.UNKNOWN.equals(type)) {
            switch (uri.getScheme()) {
                case "http":
                case "https":
                    try {
                        type = getTypeOfHttpHead(uri);
                    } catch (Exception e) {
                        Log.e(TAG, "HEAD HTTP(s) " + uri.toString(), e);
                    }
                    if (!MediaType.UNKNOWN.equals(type)) {
                        break;
                    }
                case "file":
                    type = MediaType.fromUriPath(uri);
                    break;
                default:
                    type = MediaType.VIDEO;
                    break;
            }
        }

        if(name == null){
            name = uri.toString();
        }

        Log.i(TAG, type + " : " + name);
        getInstance(instanceId).setURI(uri, type, name, metaData);
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        Log.d(TAG, currentURI + " --- " + currentURIMetaData);
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        play(instanceId, uri, currentURIMetaData);
//        if(uri != null){
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
//            this.mContext.startActivity(intent);
//        }
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentMediaInfo();
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentTransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return getInstance(instanceId).getCurrentPositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).stop();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
        getInstance(instanceId).play();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).pause();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        Log.i(TAG, "### TODO: Not implemented: Record");
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        final ZxtMediaPlayer player = getInstance(instanceId);
        SeekMode seekMode;
        try {
            seekMode = SeekMode.valueOrExceptionOf(unit);

            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }

//            final ClockTime ct = ClockTime.fromSeconds(ModelUtil.fromTimeString(target));
            int pos = (int) (Utils.getRealTime(target) * 1000);
            Log.i(TAG, "### " + unit + " target: " + target + "  pos: " + pos);

//            if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PLAYING)) {
//                getInstance(instanceId).pause();
//                getInstance(instanceId).seek(pos);
//                getInstance(instanceId).play();
//            } else if (getInstance(instanceId).getCurrentTransportInfo().getCurrentTransportState()
//                    .equals(TransportState.PAUSED_PLAYBACK)) {
            getInstance(instanceId).seek(pos);
//            }

        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        Log.i(TAG, "### TODO: Not implemented: Next");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        Log.i(TAG, "### TODO: Not implemented: Previous");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        Log.i(TAG, "### TODO: Not implemented: SetNextAVTransportURI");
        // Not implemented
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        // Not implemented
        Log.i(TAG, "### TODO: Not implemented: SetPlayMode");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        // Not implemented
        Log.i(TAG, "### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }

    private static String getTypeOfHttpHead(URI uri) throws IOException {
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        httpURLConnection.setRequestMethod("HEAD");
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setConnectTimeout(2000);
        httpURLConnection.setDoInput(false);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.connect();
        String contentType = httpURLConnection.getHeaderField("Content-Type");
        return MediaType.fromContentType(contentType);
    }
}
