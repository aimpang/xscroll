package com.xscroll.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
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
public final class DanmakuRepository_Factory implements Factory<DanmakuRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public DanmakuRepository_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public DanmakuRepository get() {
    return newInstance(firestoreProvider.get());
  }

  public static DanmakuRepository_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new DanmakuRepository_Factory(firestoreProvider);
  }

  public static DanmakuRepository newInstance(FirebaseFirestore firestore) {
    return new DanmakuRepository(firestore);
  }
}
