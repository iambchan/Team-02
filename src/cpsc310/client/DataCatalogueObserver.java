package cpsc310.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("observer")
public interface DataCatalogueObserver extends RemoteService {
	String downloadFile() throws IllegalArgumentException;
}