package com.example.bhati.routeapplication.Model;

//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.gax.paging.Page;
//import com.google.auth.oauth2.ComputeEngineCredentials;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.storage.Bucket;
//import com.google.cloud.storage.Storage;
//import com.google.cloud.storage.StorageOptions;


public class AuthCredentials {
    private static final String TAG = "AUTH_CREDENTIALS";
//    static void authImplicit()
//    {
//        Storage storage = StorageOptions.getDefaultInstance().getService();
//        Log.d(TAG, "authimplicit: ");
//        Page<Bucket> bukcets = storage.list();
//        for (Bucket bucket : bukcets.iterateAll())
//        {
//            Log.d(TAG, "authimplicit: "+bucket.toString());
//        }
//    }
//    static void authExplicit(String jsonPath) throws IOException
//    {
//        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
//                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//        Log.d(TAG, "authExplicit: ");
//        Page<Bucket> bucketPage = storage.list();
//        for (Bucket bucket : bucketPage.iterateAll())
//        {
//            Log.d(TAG, "authExplicit: "+bucket.toString());
//        }
//
//    }
//    static void authCompute() {
//        // Explicitly request service account credentials from the compute engine instance.
//        GoogleCredentials credentials = ComputeEngineCredentials.create();
//        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//        System.out.println("Buckets:");
//        Page<Bucket> buckets = storage.list();
//        for (Bucket bucket : buckets.iterateAll()) {
//            System.out.println(bucket.toString());
//        }
//    }

}
