/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.services.downloader.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class DownloadRequest {

    public static enum State {
        NEW,
        QUEUED,
        IN_PROGRESS,
        FAILED,
        DONE,
    }

    private final URI source;
    private URL callback;
    private String id;
    private State state;
    private final AtomicLong downloadedBytes;
    private String savedObjectId;
    private String objectStoreId;
    private String token;

    @JsonProperty("orgUUID")
    private String orgId;

    public DownloadRequest(URI source, String orgId, String token) {
        this.source = source;
        this.state = State.NEW;
        this.downloadedBytes = new AtomicLong(0);
        this.token = Objects.requireNonNull(token);
        this.orgId = Objects.requireNonNull(orgId);
    }

    public void setId(@NotNull String id) {
        Preconditions.checkState(this.id == null, "Trying to replace existing id");
        this.id = id;
    }

    public long getDownloadedBytes() {
        return downloadedBytes.get();
    }

    public void incDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes.addAndGet(downloadedBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DownloadRequest) {
            DownloadRequest other = (DownloadRequest) object;
            return Objects.equals(source, other.source);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("id", id)
                .add("source", source)
                .add("state", state)
                .add("savedObjectId", savedObjectId)
                .add("objectStoreId", objectStoreId)
                .toString();
    }
}
