package edu.ucsd.cse110.walkstatic.store;

import edu.ucsd.cse110.walkstatic.teammate.Teammate;

public class DefaultStorage {
    public interface StorageWatcherBlueprint {
        public StorageWatcher getStorageWatcher(Teammate user);
    }

    public interface TeammateRequestStoreBlueprint {
        public TeammateRequestStore getTeammateRequestStore();
    }

    private static StorageWatcherBlueprint defaultStorageWatcher;
    private static TeammateRequestStoreBlueprint defaultTeammateRequestStore;
    public static StorageWatcher getDefaultStorageWatcher(Teammate user){
        if(defaultStorageWatcher == null){
            return new FirebaseStorageWatcher(user);
        }
        return defaultStorageWatcher.getStorageWatcher(user);
    }

    public static TeammateRequestStore getDefaultTeammateRequestStore(){
        if(defaultTeammateRequestStore == null){
            return new FirebaseStore();
        }
        return defaultTeammateRequestStore.getTeammateRequestStore();
    }

    public void setDefaultStorageWatcher(StorageWatcherBlueprint defaultStorageWatcher){
        DefaultStorage.defaultStorageWatcher = defaultStorageWatcher;
    }

    public void setDefaultTeammateRequestStore(TeammateRequestStoreBlueprint defaultTeammateRequestStore){
        DefaultStorage.defaultTeammateRequestStore = defaultTeammateRequestStore;
    }
}
