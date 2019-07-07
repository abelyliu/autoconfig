package feign;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

public class CustomClient extends Client.Default {
    /**
     * Null parameters imply platform defaults.
     *
     * @param sslContextFactory
     * @param hostnameVerifier
     */
    public CustomClient(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
        super(sslContextFactory, hostnameVerifier);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Transaction transaction = Cat.newTransaction("internal_call", request.url());
        Response execute;
        try {
            execute = super.execute(request, options);
            transaction.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            Cat.logError(e);
            transaction.setStatus(e);
            throw e;
        } finally {
            transaction.complete();
        }
        return execute;
    }
}
