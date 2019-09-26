package main.java.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class UserPermission {
    private Permission permission;
    private BooleanProperty allowed;

    public UserPermission(Permission permission, boolean allowed) {
        this.permission = permission;
        this.allowed = new SimpleBooleanProperty(allowed);
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public boolean isAllowed() {
        return allowed.get();
    }

    public BooleanProperty allowedProperty() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed.set(allowed);
    }

    @Override
    public String toString() {
        return getPermission().toString();
    }
}
