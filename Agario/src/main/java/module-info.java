module com.example.agario {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.agario to javafx.fxml;
    exports com.example.agario;
}