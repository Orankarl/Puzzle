package com.example.orankarl.puzzle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.publicsuffix.PublicSuffixDatabase;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

public class Api {
    private interface RequestCallback {
        void onFinish(String jsonResponse);
    }

    private OkHttpClient _client;
    private String _url;
    private int _port;
    private Socket _socket;
    private Handler _handler;
    private String _token;

    // Initialization part
    Api(String url, int port, Handler handler) {
        _client = new OkHttpClient();
        _url = url;
        _port = port;
        _handler = handler;
        try {
            _socket = IO.socket("http://" + url + ":" + Integer.toString(port));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        _socket.connect();
    }

    public boolean connected() {
        return _socket.connected();
    }

    public void setToken(String token) {
        _token = token;
    }

    // Core part
    private HttpUrl.Builder JsonToQueryParams(
            HttpUrl.Builder builder,
            JSONObject json)
    {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                if (json.get(k) instanceof String) {
                    builder.addQueryParameter(k, (String) json.get(k));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder;
    }

    private void execute(
            String action,
            String method,
            JSONObject data,
            RequestCallback cb)
    {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("http")
                .host(_url)
                .port(_port)
                .addPathSegment("api")
                .addPathSegment(action);
        _execute(urlBuilder, method, data, cb);
    }

    private void execute(
            String[] actions,
            String method,
            JSONObject data,
            RequestCallback cb)
    {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("http")
                .host(_url)
                .port(_port)
                .addPathSegment("api");
        for (String action: actions)
            urlBuilder.addPathSegment(action);
        _execute(urlBuilder, method, data, cb);
    }

    private void _execute(
            HttpUrl.Builder urlBuilder,
            String method,
            JSONObject data,
            RequestCallback cb)
    {
        Request.Builder requestBuilder = new Request.Builder();
        if (method.equals("GET")) {
            urlBuilder = JsonToQueryParams(urlBuilder, data);
            requestBuilder = requestBuilder.get();
        } else {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    data.toString()
            );
            requestBuilder = requestBuilder.post(body);
        }
        Request request = requestBuilder.url(urlBuilder.build()).build();
        _client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = " { \"status\": -1 } ";
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null)
                        data = body.string();
                }
                final String _data = data;
                _handler.post(() -> cb.onFinish(_data));
            }
        });
    }

    // Game param
    class GetGameParamResponse {
        int status;
        int split;
        int pattern;
        int[] sequence;
        Bitmap image;
        String _imageBase64;
    }
    interface GetGameParamCallback {
        void onResponse(GetGameParamResponse response);
    }
    public void onGetGameParam(GetGameParamCallback cb) {
        _socket.on("gameParam", (response0) -> {
            JSONObject data = new JSONObject();
            try {
                data.put("token", _token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            execute("gameParam", "GET", data, response -> {
                GetGameParamResponse r = new Gson().fromJson(response, GetGameParamResponse.class);
                byte[] binary = Base64.decode(r._imageBase64, Base64.DEFAULT);
                r.image = BitmapFactory.decodeByteArray(binary, 0, binary.length);
                cb.onResponse(r);
            });
        });
    }

    class GameParamResponse {
        int status;
    }
    interface GameParamCallback {
        void onResponse(GameParamResponse response);
    }
    public void gameParam(
            int split, int pattern, Bitmap image,
            int[] sequence, final GameParamCallback cb)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        JSONObject data = new JSONObject();
        try {
            data.put("token", _token);
            data.put("split", split);
            data.put("pattern", pattern);
            JSONArray seq = new JSONArray(sequence);
            data.put("sequence", seq);
            data.put("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute("gameParam", "POST", data, response -> {
            cb.onResponse(new Gson().fromJson(response, GameParamResponse.class));
        });
    }

    // User aware part
    class LoginResponse {
        int status;
        String token;
    }
    interface LoginCallback {
        void onResponse(LoginResponse response);
    }
    public void login(String username, String password, final LoginCallback cb)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("password", sha256(password));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute("login", "POST", data, response -> {
            LoginResponse r = new Gson().fromJson(response, LoginResponse.class);
            _token = r.token;
            socketAuth(r.token);
            cb.onResponse(r);
        });
    }

    class RegisterResponse {
        int status;
    }
    interface RegisterCallback {
        void onResponse(RegisterResponse response);
    }
    public void register(String username, String nickname, String password, final RegisterCallback cb)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
            data.put("nickname", nickname);
            data.put("password", sha256(password));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute("register", "POST", data, response -> {
            cb.onResponse(new Gson().fromJson(response, RegisterResponse.class));
        });
    }

    class UserInfoResponse {
        int status;
        String _id;
        String username;
        String nickname;
    }
    interface UserInfoCallback {
        void onResponse(UserInfoResponse response);
    }
    public void userInfo(UserInfoCallback cb)
    {
        if (_token == null)
            return;
        JSONObject data = new JSONObject();
        try {
            data.put("token", _token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute("user", "GET", data, response -> {
            cb.onResponse(new Gson().fromJson(response, UserInfoResponse.class));
        });
    }

    class NewResultResponse {
        int status;
    }
    interface NewResultCallback {
        void onResponse(NewResultResponse response);
    }
    public void newResult(int pattern, int split, int time, NewResultCallback cb)
    {
        if (_token == null)
            return;
        Long timestamp = System.currentTimeMillis();
        JSONObject data = new JSONObject();
        try {
            data.put("token", _token);
            data.put("pattern", pattern);
            data.put("time", time);
            data.put("split", split);
            data.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute("result", "POST", data, response -> {
            cb.onResponse(new Gson().fromJson(response, NewResultResponse.class));
        });
    }

    class RankResponse {
        int status;
        RankResponseEntry[] rank;
    }
    class RankResponseEntry {
        int time;
        Date timestamp;
        String username;
        String nickname;
    }
    interface RankCallback {
        void onResponse(RankResponse response);
    }
    public void rank(int pattern, int split, RankCallback cb)
    {
        JSONObject data = new JSONObject();
        execute(
                new String[]{"rank", Integer.toString(pattern), Integer.toString(split)},
                "GET", data, response -> {
            cb.onResponse(new Gson().fromJson(response, RankResponse.class));
        });
    }

    // Socket part
    public void socketAuth(String token) {
        if (_token == null)
            _token = token;
        _socket.emit("auth", token);
    }

    // Room aware part
    public void newRoom(int split, int pattern) {
        JSONObject j = new JSONObject();
        try {
            j.put("split", split);
            j.put("pattern", pattern);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        _socket.emit("newRoom", j);
    }

    public void enterRoom(String master) {
        _socket.emit("enterRoom", master);
    }

    public void leaveRoom() {
        _socket.emit("leaveRoom");
    }

    public void roomList() {
        _socket.emit("roomList");
    }

    public void startGame() {
        _socket.emit("startGame");
    }

    public void deleteRoom() {
        _socket.emit("deleteRoom");
    }

    class NewRoomResponse {
        String username;
        int size;
        int pattern;
        int split;
    }
    interface NewRoomCallback {
        void onResponse(NewRoomResponse response);
    }
    public void onNewRoom(NewRoomCallback cb) {
        _socket.on("newRoom", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), NewRoomResponse.class));
            });
        });
    }

    class EnterRoomResponse {
        String username;
    }
    interface EnterRoomCallback {
        void onResponse(EnterRoomResponse response);
    }
    public void onEnterRoom(EnterRoomCallback cb) {
        _socket.on("enterRoom", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), EnterRoomResponse.class));
            });
        });
    }

    class LeaveRoomResponse {
        String username;
    }
    interface LeaveRoomCallback {
        void onResponse(LeaveRoomResponse response);
    }
    public void onLeaveRoom(LeaveRoomCallback cb) {
        _socket.on("leaveRoom", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), LeaveRoomResponse.class));
            });
        });
    }

    class RoomListEntry {
        String username;
        int size;
        int pattern;
        int split;
    }
    class RoomListResponse {
        RoomListEntry[] rooms;
    }
    interface RoomListCallback {
        void onResponse(RoomListResponse response);
    }
    public void onRoomList(RoomListCallback cb) {
        _socket.on("roomList", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), RoomListResponse.class));
            });
        });
    }

    class RoomMemberResponse {
        String[] members;
    }
    interface RoomMemberCallback {
        void onResponse(RoomMemberResponse response);
    }
    public void onRoomMember(RoomMemberCallback cb) {
        _socket.on("roomMember", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), RoomMemberResponse.class));
            });
        });
    }

    class ChangeRoomResponse {
        String room;
        int size;
    }
    interface ChangeRoomCallback {
        void onResponse(ChangeRoomResponse response);
    }
    public void onChangeRoom(ChangeRoomCallback cb) {
        _socket.on("changeRoom", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), ChangeRoomResponse.class));
            });
        });
    }

    class DeleteRoomResponse {
        String room;
        int size;
    }
    interface DeleteRoomCallback {
        void onResponse(DeleteRoomResponse response);
    }
    public void onDeleteRoom(DeleteRoomCallback cb) {
        _socket.on("deleteRoom", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), DeleteRoomResponse.class));
            });
        });
    }

    interface StartGameCallBack {
        void onResponse();
    }
    public void onStartGame(StartGameCallBack cb) {
        _socket.on("startGame", response -> {
            _handler.post(cb::onResponse);
        });
    }

    interface CancelRoomCallBack {
        void onResponse();
    }
    public void onCancelRoom(CancelRoomCallBack cb) {
        _socket.on("cancelRoom", response -> {
            _handler.post(cb::onResponse);
        });
    }

    // Gaming aware part
    public void pick(int pieceIndex) {
        _socket.emit("pickPiece", pieceIndex);
    }
    
    public void rotate(int pieceIndex) {
        _socket.emit("rotatePiece", pieceIndex);
    }

    public void moveTo(double X, double Y) {
        JSONObject j = new JSONObject();
        try {
            j.put("X", X);
            j.put("Y", Y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        _socket.emit("movePieceTo", j);
    }

    public void release() {
        _socket.emit("releasePiece");
    }

    class PickResponse {
        int pieceIndex;
        String username;
    }
    interface PickCallback {
        void onResponse(PickResponse response);
    }
    public void onPick(PickCallback cb) {
        _socket.on("pickPiece", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), PickResponse.class));
            });
        });
    }

    class MoveToResponse {
        double X, Y;
        String username;
        int pieceIndex;
    }
    interface MoveToCallback {
        void onResponse(MoveToResponse response);
    }
    public void onMoveTo(MoveToCallback cb) {
        _socket.on("movePieceTo", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), MoveToResponse.class));
            });
        });
    }

    class RotateResponse {
        String username;
        int pieceIndex;
    }
    interface RotateCallback {
        void onResponse(RotateResponse response);
    }
    public void onRotate(RotateCallback cb) {
        _socket.on("rotatePiece", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), RotateResponse.class));
            });
        });
    }

    class ReleaseResponse {
        String username;
        int pieceIndex;
    }
    interface ReleaseCallback {
        void onResponse(ReleaseResponse response);
    }
    public void onRelease(ReleaseCallback cb) {
        _socket.on("releasePiece", response -> {
            _handler.post(() -> {
                cb.onResponse(new Gson().fromJson(response[0].toString(), ReleaseResponse.class));
            });
        });
    }

    // Util
    private String sha256(String s)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b: md.digest())
                sb.append(Integer.toHexString((b & 0xff) + 0x100).substring(1));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
