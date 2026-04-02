package com.xscroll.data.repository;

import com.google.firebase.auth.FirebaseAuth;
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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  public UserRepository_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    this.authProvider = authProvider;
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public UserRepository get() {
    return newInstance(authProvider.get(), firestoreProvider.get());
  }

  public static UserRepository_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    return new UserRepository_Factory(authProvider, firestoreProvider);
  }

  public static UserRepository newInstance(FirebaseAuth auth, FirebaseFirestore firestore) {
    return new UserRepository(auth, firestore);
  }
}
