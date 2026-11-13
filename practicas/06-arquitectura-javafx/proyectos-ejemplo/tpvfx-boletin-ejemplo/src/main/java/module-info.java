module es.um.informatica.TpvFx {
    requires javafx.controls;
    requires javafx.fxml;
	requires jakarta.xml.bind;
	requires com.google.common;
	requires javafx.base;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
	requires java.sql;
 
    opens tds.tpv to javafx.fxml;
    opens tds.tpv.vista to javafx.fxml;
    opens tds.tpv.negocio.modelo to javafx.base;
    opens tds.tpv.negocio.modelo.impl to javafx.base, com.fasterxml.jackson.databind;
    opens tds.tpv.adapters.repository.impl to com.fasterxml.jackson.databind;

    exports tds.tpv;
}
