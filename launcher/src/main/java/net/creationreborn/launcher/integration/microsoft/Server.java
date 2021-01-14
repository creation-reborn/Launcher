/*
 * Copyright 2021 creationreborn.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.creationreborn.launcher.integration.microsoft;

import fi.iki.elonen.NanoHTTPD;
import net.creationreborn.launcher.util.Toolbox;

import java.util.List;
import java.util.Map;

public class Server extends NanoHTTPD {

    private Result result;

    public Server() {
        super(0);
    }

    public Result await() {
        try {
            synchronized (this) {
                wait();
                return result;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void stop() {
        super.stop();

        synchronized (this) {
            this.result = null;
            notifyAll();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        if (parameters.containsKey("code")) {
            return handleSuccess(Toolbox.first(parameters.get("code")));
        }

        return handleFailure(Toolbox.join(parameters.get("error")), Toolbox.join(parameters.get("error_description")));
    }

    private Response handleFailure(String error, String errorDescription) {
        synchronized (this) {
            this.result = new Result(error, errorDescription);
            notifyAll();
        }

        Response response = newFixedLengthResponse(Response.Status.TEMPORARY_REDIRECT, MIME_HTML, null);
        response.addHeader("Location", "https://launcher.creationreborn.net/callback?error=" + error + "&error_description=" + errorDescription);
        return response;
    }

    private Response handleSuccess(String code) {
        synchronized (this) {
            this.result = new Result(code);
            notifyAll();
        }

        Response response = newFixedLengthResponse(Response.Status.TEMPORARY_REDIRECT, MIME_HTML, null);
        response.addHeader("Location", "https://launcher.creationreborn.net/callback");
        return response;
    }

    public static class Result {

        private final String code;
        private final String error;
        private final String errorDescription;

        public Result(String code) {
            this(code, null, null);
        }

        public Result(String error, String errorDescription) {
            this(null, error, errorDescription);
        }

        private Result(String code, String error, String errorDescription) {
            this.code = code;
            this.error = error;
            this.errorDescription = errorDescription;
        }

        public String getCode() {
            return code;
        }

        public String getError() {
            return error;
        }

        public String getErrorDescription() {
            return errorDescription;
        }
    }
}