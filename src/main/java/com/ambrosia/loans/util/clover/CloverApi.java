package com.ambrosia.loans.util.clover;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.util.InstantGsonSerializing;
import com.ambrosia.loans.util.clover.response.PlayerTermsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

public class CloverApi {

    private static final OkHttpClient client = new OkHttpClient();

    public static PlayerTermsResponse request(CloverRequest cloverBody) {
        RequestBody body = RequestBody.create(gson().toJson(cloverBody).getBytes(StandardCharsets.ISO_8859_1));
        Request request = new Builder()
            .url("https://api.wynncloud.com/v1/player/term")
            .post(body)
            .build();
        try (Response response = client.newCall(request).execute()) {
            return parseResponse(response);
        } catch (IOException e) {
            Ambrosia.get().logger().error("Failed request for {}", cloverBody.player(), e);
            return null;
        }
    }

    private static PlayerTermsResponse parseResponse(Response response) throws IOException {
        if (!response.isSuccessful())
            return null;
        ResponseBody body = response.body();
        if (body == null) return null;
        try (Reader reader = new InputStreamReader(body.byteStream(), StandardCharsets.ISO_8859_1)) {
            return gson().fromJson(reader, PlayerTermsResponse.class);
        }
    }

    private static @NotNull Gson gson() {
        return new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantGsonSerializing())
            .create();
    }
}
