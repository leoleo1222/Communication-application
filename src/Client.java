public class Client {
    final String ID; // the ID of the user
    public String username; // the username of the user
    private String password; // the password of the user
    public boolean status; // the online/offline status of the user

    Client(String ID,String username, String password){
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.status = false; // when the user
    }

    private void change_password(String new_password){
        this.password = new_password;
    }

    public String get_id(){
        return this.ID;
    }

    public String get_username(){
        return this.username;
    }

    public String get_password(){
        return this.password;
    }

    private void change_status(boolean s){
        this.status = s;
    }


}
