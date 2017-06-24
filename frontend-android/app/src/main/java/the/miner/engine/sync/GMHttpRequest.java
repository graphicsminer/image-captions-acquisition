package the.miner.engine.sync;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GMHttpRequest {

    /* --------------------- GET-SET ------------------------- */

    /* ---------------------- METHOD ------------------------- */

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("api/image-data")
    Call<ResponseBody> createImage(@Query("token") String token, @Field("data") String jsonData);

    @FormUrlEncoded
    @PUT("api/image-data/{hash}")
    Call<ResponseBody> updateImage(@Path("hash") String hash, @Query("token") String token, @Field("data") String jsonData);

    @Multipart
    @POST("api/image-data/upload")
    Call<ResponseBody> upload(@Query("token") String token, @Part("filename") RequestBody filename, @Part MultipartBody.Part photo);

    @GET("api/resource/config/{filename}")
    Call<ResponseBody> getConfigFile(@Path("filename") String filename, @Query("token") String token);
}
