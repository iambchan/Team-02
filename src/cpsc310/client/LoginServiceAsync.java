package cpsc310.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface LoginServiceAsync {
  public void login(String requestUri, AsyncCallback<LoginInfo> async);
  public void storeUser(LoginInfo info, AsyncCallback<Void> async);
  public void getUser(String email, AsyncCallback<LoginInfo> async);
  public void editUser(String email, String nickname, 
			long phoneNumber, String website, String description, AsyncCallback<Void> async);
}