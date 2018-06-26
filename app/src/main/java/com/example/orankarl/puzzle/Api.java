package com.example.orankarl.puzzle;

import android.os.Handler;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.lang.Iterable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

public class Api {
    interface RequestCallback
    {
        void onFinish(String jsonResponse);
    }

    private OkHttpClient _client;
    private String _url;
    private int _port;
    private Socket _socket;
    private Handler _handler;

    Api(String url, int port, Handler handler)
    {
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

    public boolean connected()
    {
        return _socket.connected();
    }

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
                _handler.post(() -> {
                    cb.onFinish(_data);
                });
            }
        });
    }

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
            cb.onResponse(new Gson().fromJson(response, LoginResponse.class));
        });
    }

    class RegisterResponse {
        int status;
    }
    interface RegisterCallback {
        void onResponse(RegisterResponse response);
    }
    public void register(String username, String password, final RegisterCallback cb)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
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
    public void userInfo(String token, UserInfoCallback cb)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
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
    public void newResult(String token, int pattern, int time, NewResultCallback cb)
    {
        Long timestamp = System.currentTimeMillis();
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
            data.put("pattern", pattern);
            data.put("time", time);
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
    public void rank(String token, int pattern, RankCallback cb)
    {
        JSONObject data = new JSONObject();
        try {
            data.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        execute(new String[]{"rank", Integer.toString(pattern)}, "GET", data, response -> {
            cb.onResponse(new Gson().fromJson(response, RankResponse.class));
        });
    }

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
