package user;

public class FileInfo {
  public String fileName;
  public String description;
  public ClientInfo host;

  FileInfo(String fn, String d, ClientInfo h) {
    fileName = fn;
    description = d;
    host = h;
  }
}
