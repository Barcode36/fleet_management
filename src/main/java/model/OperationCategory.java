package main.java.model;

public enum OperationCategory {
    HIRE_OPERATION("Hire operation"),
    NORMAL_OPERATION("Normal PSV operation")
    ;
    private String category;
     OperationCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return category;
    }
}
