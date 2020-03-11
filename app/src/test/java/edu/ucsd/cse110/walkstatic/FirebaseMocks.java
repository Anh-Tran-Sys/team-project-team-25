package edu.ucsd.cse110.walkstatic;

import edu.ucsd.cse110.walkstatic.runs.RunUpdateListener;
import edu.ucsd.cse110.walkstatic.store.DefaultStorage;
import edu.ucsd.cse110.walkstatic.store.ResponseWatcher;
import edu.ucsd.cse110.walkstatic.store.RunStore;
import edu.ucsd.cse110.walkstatic.store.StorageWatcher;
import edu.ucsd.cse110.walkstatic.store.TeammateRequestStore;
import edu.ucsd.cse110.walkstatic.teammate.TeammateRequest;
import edu.ucsd.cse110.walkstatic.teammate.TeammateRequestListener;

import static org.mockito.Mockito.mock;

public class FirebaseMocks {
    public static class MockStorageWatcher implements StorageWatcher {
        public RunUpdateListener runUpdateListener;
        public TeammateRequestListener teammateRequestListener;

        @Override
        public void addRunUpdateListener(RunUpdateListener runUpdateListener) {
            this.runUpdateListener = runUpdateListener;
        }

        @Override
        public void addTeammateRequestUpdateListener(TeammateRequestListener teammateRequestsListener) {
            this.teammateRequestListener = teammateRequestsListener;
        }

        @Override
        public void deleteAllListeners() {

        }
    }

    public static class MockTeammateRequestStore implements TeammateRequestStore {
        public TeammateRequest lastAdded;

        @Override
        public void addRequest(TeammateRequest request) {
            this.lastAdded = request;
        }

        @Override
        public void delete(TeammateRequest request) {

        }
    }

    public static void setBasicMocks(){
        StorageWatcher watcher = mock(StorageWatcher.class);
        TeammateRequestStore store = mock(TeammateRequestStore.class);
        RunStore runStore = mock(RunStore.class);
        ResponseWatcher responseWatcher = mock(ResponseWatcher.class);

        DefaultStorage.setDefaultFirebaseInitialization(context -> {});
        DefaultStorage.setDefaultStorageWatcher(ignored -> watcher);
        DefaultStorage.setDefaultRunStore(() -> runStore);
        DefaultStorage.setDefaultTeammateRequestStore(() -> store);
        DefaultStorage.setDefaultResponseWatcher(() -> responseWatcher);
    }

}