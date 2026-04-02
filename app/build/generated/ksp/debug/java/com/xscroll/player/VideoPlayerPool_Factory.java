package com.xscroll.player;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class VideoPlayerPool_Factory implements Factory<VideoPlayerPool> {
  private final Provider<Context> contextProvider;

  public VideoPlayerPool_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public VideoPlayerPool get() {
    return newInstance(contextProvider.get());
  }

  public static VideoPlayerPool_Factory create(Provider<Context> contextProvider) {
    return new VideoPlayerPool_Factory(contextProvider);
  }

  public static VideoPlayerPool newInstance(Context context) {
    return new VideoPlayerPool(context);
  }
}
