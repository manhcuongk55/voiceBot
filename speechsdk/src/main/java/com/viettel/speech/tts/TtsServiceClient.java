//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Speech-TTS
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.viettel.speech.tts;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

class TtsServiceClient {
    private static final String LOG_TAG = "SpeechSDKTTS";
    private static String s_contentType = "application/ssml+xml";
    private final String m_serviceUri;
    private String m_outputFormat;
    private Authentication m_auth;
    private byte[] m_result;

    public TtsServiceClient(String apiKey) {
        m_outputFormat = "raw-16khz-16bit-mono-pcm";
      //  m_serviceUri = "https://speech.platform.bing.com/synthesize";
        m_serviceUri = "http://203.113.152.90/hmm-stream/syn";
        m_auth = new Authentication(apiKey);
    }

    public void Authentication() {

    }

    protected void doWork(String text) {
        int code = -1;
        synchronized (m_auth) {
            String accessToken = m_auth.GetAccessToken();
            try {
                URL url = new URL(m_serviceUri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("data", text)
                        .appendQueryParameter("voices", "doanngocle.htsvoice")
                        .appendQueryParameter("key", "K9W6tNTeUuwrkyYARkAmzJ94D9vUR2Qdo5YwVI7D");
                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                connection.setRequestMethod("POST");
                connection.connect();

                code = connection.getResponseCode();
                if (code == 200) {
                    InputStream in = connection.getInputStream();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int ret = in.read(bytes);
                    while (ret > 0) {
                        bout.write(bytes, 0, ret);
                        ret = in.read(bytes);
                    }
                    m_result = bout.toByteArray();
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception error", e);
            }
        }
    }

    public byte[] SpeakSSML(final String ssml) {
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                doWork(ssml);
            }
        });
        try {
            th.start();
            th.join();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception error", e);
        }

        return m_result;
    }
}
