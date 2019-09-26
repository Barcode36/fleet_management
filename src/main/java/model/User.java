package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private IntegerProperty userId;
    private StringProperty userName;
    private StringProperty firstName;
    private StringProperty lastName;

    private Category category;
    public User() {
        this.userId = new SimpleIntegerProperty(1);
        this.userName = new SimpleStringProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();

    }

    @Override
    public String toString() {
        return "{ Login: " + getUserName() + ", Full name: " + getFirstName() + " " + getLastName() + " }";
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public String getUserName() {
        return userName.get();
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }


    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public StringProperty categoryStringProperty() {
        return new SimpleStringProperty(getCategory().toString());
    }

    public enum Category {
        ADMIN("Admin"), REGULAR("Regular");
        private String category;
        Category(String category) {
            this.category = category;
        }

        @Override
        public String toString() {
            return category;
        }
    }
}
