package ba.sum.fsre.mealplanner.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    private String email;
    private String password;
    private Data data;

    public RegisterRequest(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.data = new Data(fullName);
    }

    public static class Data {
        @SerializedName("full_name")
        private String fullName;

        public Data(String fullName) {
            this.fullName = fullName;
        }
    }
}
