package com.xscroll.ui.feed;

import com.xscroll.data.repository.UserRepository;
import com.xscroll.data.repository.VideoRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class FeedViewModel_Factory implements Factory<FeedViewModel> {
  private final Provider<VideoRepository> videoRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public FeedViewModel_Factory(Provider<VideoRepository> videoRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.videoRepositoryProvider = videoRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public FeedViewModel get() {
    return newInstance(videoRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static FeedViewModel_Factory create(Provider<VideoRepository> videoRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new FeedViewModel_Factory(videoRepositoryProvider, userRepositoryProvider);
  }

  public static FeedViewModel newInstance(VideoRepository videoRepository,
      UserRepository userRepository) {
    return new FeedViewModel(videoRepository, userRepository);
  }
}
