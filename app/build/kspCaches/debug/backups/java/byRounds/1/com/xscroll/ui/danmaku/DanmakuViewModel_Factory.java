package com.xscroll.ui.danmaku;

import com.xscroll.data.repository.DanmakuRepository;
import com.xscroll.data.repository.UserRepository;
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
public final class DanmakuViewModel_Factory implements Factory<DanmakuViewModel> {
  private final Provider<DanmakuRepository> danmakuRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public DanmakuViewModel_Factory(Provider<DanmakuRepository> danmakuRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.danmakuRepositoryProvider = danmakuRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public DanmakuViewModel get() {
    return newInstance(danmakuRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static DanmakuViewModel_Factory create(
      Provider<DanmakuRepository> danmakuRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new DanmakuViewModel_Factory(danmakuRepositoryProvider, userRepositoryProvider);
  }

  public static DanmakuViewModel newInstance(DanmakuRepository danmakuRepository,
      UserRepository userRepository) {
    return new DanmakuViewModel(danmakuRepository, userRepository);
  }
}
