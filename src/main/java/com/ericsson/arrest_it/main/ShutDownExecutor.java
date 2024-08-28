package com.ericsson.arrest_it.main;

import com.ericsson.arrest_it.io.ResultSerializer;
import com.ericsson.arrest_it.logging.LogbackFileUtils;
import com.ericsson.arrest_it.manager.DBManager;

public class ShutDownExecutor extends Thread {
    protected DBManager db;
    protected HttpEngine httpE;
    protected ResultSerializer resultSerializer;

    public ShutDownExecutor(final DBManager db, final HttpEngine httpE, final ResultSerializer resultSerializer) {
        this.db = db;
        this.httpE = httpE;
        this.resultSerializer = resultSerializer;
    }

    @Override
    public void run() {
        System.out.println("Shutting down");
        db.closeConnectionOnShutDown();
        httpE.stop();
        resultSerializer.stopSerialization();
        LogbackFileUtils.stop();
    }
}
