package user;

public class ClientInfo {
  public String username;
  public String connectionSpeed;
  public String hostName;

  ClientInfo(String un, String cs, String hn) {
    username = un;
    connectionSpeed = cs;
    hostName = hn;
  }
}
