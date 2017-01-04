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
package org.trustedanalytics.services.downloader.api.rest;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.services.downloader.core.DownloadRequest;
import org.trustedanalytics.services.downloader.core.DownloadingEngine;
import org.trustedanalytics.services.downloader.store.DownloadRequestsStore;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

@RestController
public class RestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);
    private static final String URI_BASE = "/rest/downloader/requests";

    private DownloadRequestsStore downloadRequestsStore;
    private DownloadingEngine downloadingEngine;
    private AuthTokenRetriever tokenRetriever;

    @Autowired
    public RestService(DownloadRequestsStore downloadRequestsStore, DownloadingEngine downloadingEngine,
            AuthTokenRetriever tokenRetriever) {
        this.downloadRequestsStore = downloadRequestsStore;
        this.downloadingEngine = downloadingEngine;
        this.tokenRetriever = tokenRetriever;
    }

    @ApiOperation(
            value = "Adds download request",
            notes = "Privilege level: Consumer of this endpoint must have valid access token"
    )
    @RequestMapping(value = URI_BASE, method = RequestMethod.POST)
    public DownloadRequest addRequest(@RequestBody Request request)
            throws IOException, LoginException, InterruptedException {

        LOGGER.info("add({})", request);
        DownloadRequest downloadRequest =
                new DownloadRequest(URI.create(request.getSource()), request.getOrgId(), extractToken(), request.getTitle());
        if (request.getCallback() != null) {
            downloadRequest.setCallback(new URL(request.getCallback()));
        }

        downloadRequestsStore.add(downloadRequest);
        downloadingEngine.download(downloadRequest);

        return downloadRequest;
    }

    @ApiOperation(
            value = "Get download request data for given ID. Used mostly to check status",
            notes = "Privilege level: Consumer of this endpoint must have valid access token"
    )
    @RequestMapping(value = URI_BASE + "/{id}", method = RequestMethod.GET)
    public DownloadRequest getRequest(@PathVariable String id) {
        LOGGER.info("get({})", id);
        return downloadRequestsStore.get(id);
    }

    private String extractToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return tokenRetriever.getAuthToken(authentication);
    }
}
