package com.mycompany.controlescolar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ControlEscolar {

    public static void main(String[] args) {
        String url = "jdbc:mysql://hopper.proxy.rlwy.net:47112/railway";
        String usuario = "root";
        String contraseña = "OmSZrDuQpEJWcYPLwRipzZkqDfFtgJpB";

        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            System.out.println("Conexión exitosa a la base de datos.");

            String query = "SELECT * FROM maestros";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String nombre = rs.getString("nombre_completo");
                String curp = rs.getString("curp");
                String rfc = rs.getString("rfc");
                String correo = rs.getString("correo");
                String telefono = rs.getString("telefono");
                String direccion = rs.getString("direccion");

                System.out.println("Nombre: " + nombre +
                                   ", CURP: " + curp +
                                   ", RFC: " + rfc +
                                   ", Correo: " + correo +
                                   ", Teléfono: " + telefono +
                                   ", Dirección: " + direccion);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
