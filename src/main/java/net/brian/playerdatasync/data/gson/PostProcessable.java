package net.brian.playerdatasync.data.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.brian.playerdatasync.PlayerDataSync;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface PostProcessable {

    void gsonPostSerialize();

    void gsonPostDeserialize();

    class PostProcessingEnabler implements TypeAdapterFactory{

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this,typeToken);

            return new TypeAdapter<T>() {

                @Override
                public void write(JsonWriter jsonWriter,T o) throws IOException {
                    if(o instanceof PostProcessable){
                        try {
                            if(!PlayerDataSync.isDisabling){
                                CompletableFuture.runAsync(((PostProcessable) o)::gsonPostSerialize,PlayerDataSync.getMainExecutor()).get();
                            }
                            else {
                                ((PostProcessable) o).gsonPostSerialize();
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    delegate.write(jsonWriter,o);
                }

                @Override
                public T read(JsonReader jsonReader) throws IOException {
                    T object = delegate.read(jsonReader);
                    if(object instanceof PostProcessable){
                        try {
                            CompletableFuture.runAsync(((PostProcessable) object)::gsonPostDeserialize,PlayerDataSync.getMainExecutor()).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    return object;
                }
            };
        }
    }

}