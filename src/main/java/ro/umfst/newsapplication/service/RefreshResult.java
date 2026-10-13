package ro.umfst.newsapplication.service;

import java.util.List;
/**
 *
 * @author hunorszilvasi
 */
public record RefreshResult(int saved, int skipped, int failed, List<String> failedSources) {

    public boolean allFailed() {
        return saved == 0 && skipped == 0 && failed > 0;
    }
}