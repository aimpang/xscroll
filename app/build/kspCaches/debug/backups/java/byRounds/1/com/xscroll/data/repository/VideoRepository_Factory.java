package com.xscroll.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class VideoRepository_Factory implements Factory<VideoRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseStorage> storageProvider;

  public VideoRepository_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseStorage> storageProvider) {
    this.firestoreProvider = firestoreProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public VideoRepository get() {
    return newInstance(firestoreProvider.get(), storageProvider.get());
  }

  public static VideoRepository_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseStorage> storageProvider) {
    return new VideoRepository_Factory(firestoreProvider, storageProvider);
  }

  public static VideoRepository newInstance(FirebaseFirestore firestore, FirebaseStorage storage) {
    return new VideoRepository(firestore, storage);
  }
}
